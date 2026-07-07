package com.feedback.feedback360.services.talentup;

import com.feedback.feedback360.entities.*;
import com.feedback.feedback360.enums.*;
import com.feedback.feedback360.repositories.*;
import com.feedback.feedback360.services.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TalentUpSyncService {

    private final TalentUpClient client;
    private final UserRepository userRepository;
    private final ModuleFormationRepository moduleRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificationRepository notificationRepository;
    private final IntegrationLogRepository logRepository;
    private final MailService mailService;

    private LocalDateTime lastPollAt = LocalDateTime.now().minusDays(7);

    @Scheduled(cron = "${app.talentup.poll-cron}")
    public void poll() {

        LocalDateTime pollStartedAt = LocalDateTime.now();
        String since = lastPollAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        log.info("Starting TalentUp poll since {}", since);

        try {
            List<TalentUpCompletionDto> completions = client.fetchCompletions(since);

            int processed = 0;
            int failed = 0;

            for (var completion : completions) {
                try {
                    processOne(completion);
                    processed++;
                } catch (Exception ex) {
                    failed++;
                    log.error("Failed processing completion {}", completion, ex);
                    // Log write moved OUTSIDE processOne's transaction (Fix 6) —
                    // this call runs in its own REQUIRES_NEW transaction so it
                    // survives even though processOne's transaction rolled back.
                    writeLogInNewTransaction("POLL_ITEM_ERROR", "FAILED",
                            completion.user().email() + ": " + ex.getMessage(), null);
                }
            }

            writeLog("POLL_RUN", "SUCCESS",
                    String.format("Fetched=%d Processed=%d Failed=%d", completions.size(), processed, failed),
                    null);

            lastPollAt = pollStartedAt;
            log.info("TalentUp poll completed (processed={}, failed={})", processed, failed);

        } catch (Exception ex) {
            log.error("TalentUp poll failed", ex);
            writeLog("POLL_RUN", "FAILED", ex.getMessage(), null);
        }
    }

    @Transactional
    public void processOne(TalentUpCompletionDto dto) {
        // Match by email — TalentUp user.id is NOT our user.id (different DBs)
        User user = userRepository.findByEmailIgnoreCase(dto.user().email())
                .orElseGet(() -> autoCreateUser(dto));

        // Dedup — RG-FB-03
        if (moduleRepository.findByUserIdAndTalentupModuleId(user.getId(), dto.module().id()).isPresent()) {
            writeLog("POLL_ITEM_SKIPPED", "SUCCESS",
                    "Already synced: " + dto.user().email() + " / module " + dto.module().id(), null);
            return;
        }

        // Save ModuleFormation — TalentUp data is never edited after this (RG-FB-08)
        ModuleFormation module = ModuleFormation.builder()
                .user(user)
                .title(dto.module().name())
                .category(dto.module().type() != null ? dto.module().type().label() : null)
                .description(buildDescription(dto))
                .completionDate(LocalDateTime.now())
                .source("TalentUp")
                .talentupModuleId(dto.module().id())
                .talentupParcoursId(dto.parcours() != null ? dto.parcours().id() : null)
                .talentupParcoursName(dto.parcours() != null ? dto.parcours().name() : null)
                .talentupPopulationId(dto.population() != null ? dto.population().id() : null)
                .talentupPopulationName(dto.population() != null ? dto.population().name() : null)
                .build();
        module = moduleRepository.save(module);

        // Create Feedback shell — NOT_SUBMITTED (RG-FB-05)
        feedbackRepository.save(Feedback.builder()
                .user(user).module(module).status(FeedbackStatus.NOT_SUBMITTED).build());

        // Create Notification + send invite email
        Notification notification = notificationRepository.save(Notification.builder()
                .user(user).module(module)
                .status(NotificationStatus.SENT).type(NotificationType.INITIAL).build());

        boolean emailOk = mailService.sendFeedbackInvite(user, module);
        if (!emailOk) {
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
        }

        writeLog("POLL_ITEM_SAVED", "SUCCESS",
                "Synced module " + module.getId() + " for " + user.getEmail(), module.getId());
    }

    private User autoCreateUser(TalentUpCompletionDto dto) {
        String[] parts = dto.user().fullName().trim().split("\\s+", 2);
        return userRepository.save(User.builder()
                .firstName(parts[0])
                .lastName(parts.length > 1 ? parts[1] : "")
                .email(dto.user().email())
                // Placeholder hash — no password will ever match this.
                // Admin must set a real password via the manage-users API (Chapter 1).
                .passwordHash("$2a$10$PLACEHOLDER_NO_LOGIN_POSSIBLE_UNTIL_ADMIN_SETS_PW")
                .role(com.feedback.feedback360.enums.Role.EMPLOYEE)
                .active(true)
                .build());
    }

    private String buildDescription(TalentUpCompletionDto dto) {
        String p = dto.parcours() != null ? dto.parcours().name() : "—";
        String pop = dto.population() != null ? dto.population().name() : "—";
        return "Parcours: " + p + " · Population: " + pop;
    }

    private void writeLog(String type, String status, String message, Long moduleId) {
        logRepository.save(IntegrationLog.builder()
                .type(type).status(status).message(message).relatedModuleId(moduleId).build());
    }

    // New method — runs in its OWN transaction, independent of the caller's.
    // This guarantees the error log survives even when the calling code's
    // transaction (processOne, in this case) rolls back.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeLogInNewTransaction(String type, String status, String message, Long moduleId) {
        writeLog(type, status, message, moduleId);
    }
}