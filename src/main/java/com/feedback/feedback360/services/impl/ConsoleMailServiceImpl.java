package com.feedback.feedback360.services.impl;

import com.feedback.feedback360.entities.*;
import com.feedback.feedback360.services.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(
        name = "app.mail.impl",
        havingValue = "console",
        matchIfMissing = true
)
public class ConsoleMailServiceImpl implements MailService {

    @Override
    public boolean sendFeedbackInvite(User user, ModuleFormation module) {

        log.info("""
            ===== [MOCK EMAIL — INVITE] =====
            To:      {}
            Subject: Feedback requested for '{}'
            Body:    Hi {}, please complete your feedback at /api/feedback/submit?moduleId={}
            =================================
            """,
            user.getEmail(),
            module.getTitle(),
            user.getFirstName(),
            module.getId());

        return true;
    }

    @Override
    public boolean sendReminder(User user,
                                ModuleFormation module,
                                String template) {

        log.info("""
            ===== [MOCK EMAIL — REMINDER] =====
            To:   {}
            Body: {}
            ===================================
            """,
            user.getEmail(),
            template.replace("{moduleTitle}", module.getTitle()));

        return true;
    }
}