package com.feedback.feedback360.controllers;

import com.feedback.feedback360.services.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@Tag(name = "Admin - Settings", description = "System settings: reminder delay and email template")
public class AdminSettingsController {

    private final SystemSettingService settingService;

    @Getter @Setter
    public static class SettingUpdateRequest {
        private String key;
        private String value;
    }

    @GetMapping
    @Operation(summary = "Get all system settings")
    public Map<String, String> getSettings() {
        return settingService.getAll();
    }

    @PutMapping
    @Operation(summary = "Update a single system setting by key")
    public ResponseEntity<Void> saveSetting(@RequestBody SettingUpdateRequest body) {
        settingService.set(body.getKey(), body.getValue());
        return ResponseEntity.ok().build();
    }
}
