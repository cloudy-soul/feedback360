package com.feedback.feedback360.services;

import com.feedback.feedback360.entities.Notification;
import com.feedback.feedback360.enums.NotificationStatus;
import com.feedback.feedback360.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs on the cron defined in app.reminders.cron (application.yaml).
 * Finds every notification still SENT (invite sent, employee never responded)
 * past the configured delay, and sends a reminder email automatically.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderSchedulerService {

    private final NotificationRepository notificationRepository;
    private final MailService mailService;
    private final SystemSettingService settingService;

    @Scheduled(cron = "${app.reminders.cron}")
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
            boolean ok = mailService.sendReminder(n.getUser(), n.getModule(), template);
            if (ok) {
                n.setReminderCount(n.getReminderCount() + 1);
                n.setSentDate(LocalDateTime.now()); // reset the clock for the next reminder cycle
                notificationRepository.save(n);
                sent++;
            } else {
                failed++;
                log.warn("Failed to send reminder to {}", n.getUser().getEmail());
            }
        }

        log.info("Reminder run completed: due={}, sent={}, failed={}", due.size(), sent, failed);
    }
}