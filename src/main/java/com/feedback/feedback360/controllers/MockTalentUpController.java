package com.feedback.feedback360.controllers;

import com.feedback.feedback360.services.talentup.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulates the external TalentUp API for local dev.
 * Fixture data lives in src/main/resources/mock-data/talentup-completions.json
 * instead of being instantiated in Java — easier to edit/extend without recompiling,
 * and mirrors how a real mock-input contract would be reviewed (see code review).
 */
@RestController
@RequestMapping("/mock-talentup/api")
@Tag(name = "Mock TalentUp", description = "Simulates the external TalentUp API for local dev")
@Slf4j
public class MockTalentUpController {

    private static final String MOCK_FILE = "mock-data/talentup-completions.json";

    private final ObjectMapper objectMapper;
    private List<TalentUpCompletionDto> fixtures = new ArrayList<>();

    public MockTalentUpController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadFixtures() {
        try (InputStream is = new ClassPathResource(MOCK_FILE).getInputStream()) {
            TalentUpCompletionsResponse response = objectMapper.readValue(is, TalentUpCompletionsResponse.class);
            fixtures = new ArrayList<>(response.completions());
            log.info("Loaded {} mock TalentUp completion(s) from {}", fixtures.size(), MOCK_FILE);
        } catch (IOException ex) {
            log.error("Failed to load mock TalentUp data from {}", MOCK_FILE, ex);
            fixtures = new ArrayList<>();
        }
    }

    @GetMapping("/completions")
    @Operation(summary = "Returns all available module completions (mock, loaded from JSON file)")
    public TalentUpCompletionsResponse completions(@RequestParam(required = false) String since) {
        return new TalentUpCompletionsResponse(fixtures);
    }

    @PostMapping("/admin/seed-completion")
    @Operation(summary = "Add a new completion to the mock (simulates a new TalentUp event)")
    public TalentUpCompletionDto seed(@RequestBody TalentUpCompletionDto dto) {
        fixtures.add(dto);
        return dto;
    }
}
