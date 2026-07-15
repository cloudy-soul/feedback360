package com.feedback.feedback360.services.impl;

import com.feedback.feedback360.entities.ModuleFormation;
import com.feedback.feedback360.entities.User;
import com.feedback.feedback360.services.MailService;
import com.feedback.feedback360.services.MailTemplateService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Plain SMTP via Spring's JavaMailSender. Provider-agnostic — whichever SMTP relay
 * is configured under spring.mail.* (Mailtrap Sending, SendGrid, Gmail, etc.) works
 * without any code change, since it's just SMTP AUTH against a host/port/credentials.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.mail.impl",
        havingValue = "smtp"
)
public class SmtpMailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final MailTemplateService mailTemplateService;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${spring.mail.port:0}")
    private int smtpPort;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${app.mail.from:noreply@feedback360.local}")
    private String fromAddress;

    @Value("${app.mail.from-name:Feedback360}")
    private String fromName;

    @PostConstruct
    public void init() {
        log.info("SmtpMailServiceImpl init — smtp.host={}, smtp.port={}, smtp.username.present={}, from='{}' <{}>",
                smtpHost, smtpPort, smtpUsername != null && !smtpUsername.isBlank(), fromName, fromAddress);

        if (smtpHost == null || smtpHost.isBlank()) {
            log.warn("spring.mail.host is empty; SMTP sends will fail until SMTP_HOST/SMTP_USERNAME/SMTP_PASSWORD are set.");
        }
    }

    @Override
    public boolean sendFeedbackInvite(User user, ModuleFormation module) {
        String subject = "Feedback requested: " + module.getTitle();
        String html = mailTemplateService.buildFeedbackInvite(user, module);
        return send(user.getEmail(), subject, html);
    }

    @Override
    public boolean sendReminder(User user,
                                ModuleFormation module,
                                String template) {
        String html = mailTemplateService.buildReminder(template, module);
        return send(user.getEmail(), "Reminder: feedback pending", html);
    }

    private boolean send(String to, String subject, String html) {
        log.debug("Preparing SMTP send — to={}, from='{}' <{}>, host={}, port={}, subject='{}', htmlLength={}",
                to, fromName, fromAddress, smtpHost, smtpPort, subject, html == null ? 0 : html.length());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("SMTP send to {} succeeded via {}:{}", to, smtpHost, smtpPort);
            return true;

        } catch (MailAuthenticationException ex) {
            // Bad username/password/API key against the configured SMTP relay.
            log.error("SMTP authentication FAILED against {}:{} (user={}) — check SMTP_USERNAME/SMTP_PASSWORD", smtpHost, smtpPort, smtpUsername, ex);
            return false;
        } catch (MailSendException ex) {
            // Per-recipient delivery failure reported by the SMTP server itself (e.g. sender not verified,
            // relay access denied, recipient rejected) — ex.getFailedMessages() has the exact per-message cause.
            log.error("SMTP relay {}:{} REJECTED the send to {} — {}", smtpHost, smtpPort, to, ex.getMessage(), ex);
            return false;
        } catch (MailPreparationException | MailParseException ex) {
            log.error("Failed to build the email message for {} — check from/to addresses and content", to, ex);
            return false;
        } catch (MailException ex) {
            log.error("Unexpected mail error sending to {} via {}:{} — likely a network/connectivity failure reaching the SMTP relay", to, smtpHost, smtpPort, ex);
            return false;
        } catch (Exception ex) {
            log.error("Unexpected error sending email to {} via SMTP", to, ex);
            return false;
        }
    }
}
