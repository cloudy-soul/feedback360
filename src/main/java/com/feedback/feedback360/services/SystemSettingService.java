package com.feedback.feedback360.services;

import com.feedback.feedback360.entities.SystemSetting;
import com.feedback.feedback360.repositories.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository repository;

    public String get(String key, String defaultValue) {
        return repository.findById(key).map(SystemSetting::getValue).orElse(defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return repository.findById(key)
                .map(s -> {
                    try { return Integer.parseInt(s.getValue()); }
                    catch (NumberFormatException e) { return defaultValue; }
                })
                .orElse(defaultValue);
    }

    public void set(String key, String value) {
        SystemSetting setting = repository.findById(key)
                .orElse(SystemSetting.builder().key(key).build());
        setting.setValue(value);
        repository.save(setting);
    }

    public Map<String, String> getAll() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("reminder.delay.days", get("reminder.delay.days", "5"));
        result.put("email.template.reminder", get("email.template.reminder",
                "Don't forget to share your feedback on {moduleTitle}!"));
        return result;
    }
}
