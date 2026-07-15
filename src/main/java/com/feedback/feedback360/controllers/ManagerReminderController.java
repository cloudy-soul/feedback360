package com.feedback.feedback360.controllers;

import com.feedback.feedback360.services.ReminderSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/reminders")
@RequiredArgsConstructor
@Tag(name = "Manager - Reminders", description = "Manual feedback reminder trigger")
public class ManagerReminderController {

    private final ReminderSchedulerService reminderSchedulerService;

    @PostMapping("/send-now")
    @Operation(summary = "Trigger the feedback reminder send immediately (don't wait for the daily cron)")
    public ResponseEntity<Map<String, String>> sendNow() {
        reminderSchedulerService.sendDueReminders();
        return ResponseEntity.ok(Map.of("result", "Reminders triggered — check integration_log for details"));
    }
}
