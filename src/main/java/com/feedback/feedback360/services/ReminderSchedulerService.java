package com.feedback.feedback360.services;

import com.feedback.feedback360.entities.IntegrationLog;
import com.feedback.feedback360.entities.Notification;
import com.feedback.feedback360.enums.NotificationStatus;
import com.feedback.feedback360.repositories.IntegrationLogRepository;
import com.feedback.feedback360.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderSchedulerService {

    private final NotificationRepository notificationRepository;
    private final MailService mailService;
    private final SystemSettingService settingService;
    private final IntegrationLogRepository logRepository;

    @Scheduled(cron = "${app.reminders.cron}")
    @Transactional
    public void sendDueReminders() {
        int delayDays = settingService.getInt("reminder.delay.days", 5);
        String template = settingService.get("email.template.reminder",
                "Don't forget to share your feedback on {moduleTitle}!");

        LocalDateTime cutoff = LocalDateTime.now().minusDays(delayDays);

        List<Notification> due = notificationRepository
                .findByStatusAndSentDateBefore(NotificationStatus.SENT, cutoff);

        log.info("Reminder run started: {} notification(s) due (cutoff={})", due.size(), cutoff);

        int sent = 0, failed = 0;

        for (Notification n : due) {
            log.debug("Sending reminder to {} for module {} (reminderCount={})",
                    n.getUser().getEmail(), n.getModule().getId(), n.getReminderCount());
            boolean ok = mailService.sendReminder(n.getUser(), n.getModule(), template);
            if (ok) {
                n.setReminderCount(n.getReminderCount() + 1);
                n.setSentDate(LocalDateTime.now());
                notificationRepository.save(n);
                sent++;
                writeLog("REMINDER_SENT", "SUCCESS",
                        "Reminder sent to " + n.getUser().getEmail() + " for module " + n.getModule().getId(),
                        n.getModule().getId());
            } else {
                failed++;
                log.warn("Failed to send reminder to {} for module {} — see prior SMTP error above",
                        n.getUser().getEmail(), n.getModule().getId());
                writeLog("REMINDER_FAILED", "FAILED",
                        "Reminder failed for " + n.getUser().getEmail() + " / module " + n.getModule().getId(),
                        n.getModule().getId());
            }
        }

        log.info("Reminder run completed: due={}, sent={}, failed={}", due.size(), sent, failed);
    }

    private void writeLog(String type, String status, String message, Long moduleId) {
        logRepository.save(IntegrationLog.builder()
                .type(type).status(status).message(message).relatedModuleId(moduleId).build());
    }
}