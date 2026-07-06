package com.feedback.feedback360.services;

import com.feedback.feedback360.entities.ModuleFormation;
import com.feedback.feedback360.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Builds email bodies as a dedicated responsibility, separate from the service
 * that actually sends the email (SmtpMailServiceImpl). Per code review:
 * never mix HTML construction with sending/transport logic in the same class.
 */
@Service
public class MailTemplateService {

    @Value("${app.feedback.base-url}")
    private String feedbackBaseUrl;

    public String buildFeedbackInvite(User user, ModuleFormation module) {
        return """
                <html>
                    <body>
                        <p>Hello %s,</p>

                        <p>
                            Please complete your feedback for
                            <b>%s</b>.
                        </p>

                        <p>
                            <a href="%s?moduleId=%d">
                                Complete feedback
                            </a>
                        </p>
                    </body>
                </html>
                """
                .formatted(
                        user.getFirstName(),
                        module.getTitle(),
                        feedbackBaseUrl,
                        module.getId()
                );
    }

    public String buildReminder(String template, ModuleFormation module) {
        return template.replace("{moduleTitle}", module.getTitle());
    }
}
