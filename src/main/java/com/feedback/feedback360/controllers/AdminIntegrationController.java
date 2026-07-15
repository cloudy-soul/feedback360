package com.feedback.feedback360.controllers;

import com.feedback.feedback360.services.talentup.TalentUpSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/integration")
@RequiredArgsConstructor
@Tag(name = "Admin - Integration", description = "Manual TalentUp sync trigger and log access")
public class AdminIntegrationController {

    private final TalentUpSyncService syncService;

    @PostMapping("/sync-now")
    @Operation(summary = "Trigger a TalentUp poll immediately (don't wait for the cron)")
    public ResponseEntity<Map<String, String>> syncNow() {
        syncService.poll();
        return ResponseEntity.ok(Map.of("result", "Sync triggered — check integration_log for details"));
    }
}