package com.feedback.feedback360.services.impl;

import com.feedback.feedback360.entities.*;
import com.feedback.feedback360.services.MailService;
import com.feedback.feedback360.services.MailTemplateService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Pure transport responsibility: build the MimeMessage and send it.
 * HTML body construction is delegated to MailTemplateService (single responsibility,
 * per code review — never put HTML in the same class that sends the email).
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.impl", havingValue = "smtp")
public class SmtpMailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final MailTemplateService mailTemplateService;

    @Override
    public boolean sendFeedbackInvite(User user, ModuleFormation module) {

        String subject = "Feedback requested: " + module.getTitle();

        String body = mailTemplateService.buildFeedbackInvite(user, module);

        return send(user.getEmail(), subject, body);
    }

    @Override
    public boolean sendReminder(User user,
                                ModuleFormation module,
                                String template) {

        String body =
                mailTemplateService.buildReminder(template, module);

        return send(
                user.getEmail(),
                "Reminder: feedback pending",
                body
        );
    }

    private boolean send(String to, String subject, String html) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

            return true;

        } catch (MessagingException ex) {
            log.error("Failed to send email to {}", to, ex);
            return false;
        }
    }
}
