package com.feedback.feedback360;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Dumps the mail-related properties actually consulted at runtime, since the
 * app.mail.impl bean choice (console vs smtp) and the SMTP relay credentials are a
 * frequent source of silent misconfiguration.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MailPropertyDebug {

    private final Environment env;

    @PostConstruct
    public void debug() {
        String mailImpl = env.getProperty("app.mail.impl");
        String smtpUsername = env.getProperty("spring.mail.username");
        String smtpPassword = env.getProperty("spring.mail.password");
        String feedbackBaseUrl = env.getProperty("app.feedback.base-url");

        log.info("===== MAIL CONFIGURATION =====");
        log.info("app.mail.impl={} (console => ConsoleMailServiceImpl / smtp => SmtpMailServiceImpl)", mailImpl);
        log.info("app.mail.from={} <{}>", env.getProperty("app.mail.from-name"), env.getProperty("app.mail.from"));
        log.info("spring.mail.host={}, spring.mail.port={}", env.getProperty("spring.mail.host"), env.getProperty("spring.mail.port"));
        log.info("spring.mail.username={}", smtpUsername);
        log.info("spring.mail.password.present={}, spring.mail.password.length={}",
                smtpPassword != null && !smtpPassword.isBlank(),
                smtpPassword == null ? 0 : smtpPassword.length());
        log.info("app.feedback.base-url={}", feedbackBaseUrl);
        log.info("app.reminders.cron={}", env.getProperty("app.reminders.cron"));
        log.info("app.talentup.poll-cron={}", env.getProperty("app.talentup.poll-cron"));
        log.info("===============================");
    }
}
