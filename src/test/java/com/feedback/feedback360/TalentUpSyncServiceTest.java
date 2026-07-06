package com.feedback.feedback360;

import com.feedback.feedback360.enums.FeedbackStatus;
import com.feedback.feedback360.repositories.*;
import com.feedback.feedback360.services.talentup.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class TalentUpSyncServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired TalentUpSyncService syncService;
    @Autowired UserRepository userRepository;
    @Autowired FeedbackRepository feedbackRepository;
    @Autowired ModuleFormationRepository moduleRepository;

    private TalentUpCompletionDto sampleCompletion(String email, Long moduleId) {
        return new TalentUpCompletionDto(
            new TalentUpUserDto(1L, email, "Test User"),
            new TalentUpModuleDto(moduleId, "Test Module", new TalentUpModuleTypeDto(1L, "Apprentissage_TU")),
            new TalentUpParcoursDto(1L, "Talent Up"),
            new TalentUpPopulationDto(1L, "Dev")
        );
    }

    @Test
    void processOne_createsUserModuleFeedback() {
        syncService.processOne(sampleCompletion("newuser@test.com", 999L));

        assertThat(userRepository.findByEmailIgnoreCase("newuser@test.com")).isPresent();
        assertThat(feedbackRepository.findAll())
                .anyMatch(f -> f.getUser().getEmail().equals("newuser@test.com")
                        && f.getStatus() == FeedbackStatus.NOT_SUBMITTED);
    }

    @Test
    void processOne_duplicateIsSkipped() {
        syncService.processOne(sampleCompletion("dup@test.com", 888L));
        syncService.processOne(sampleCompletion("dup@test.com", 888L)); // same again

        long count = moduleRepository.findAll().stream()
                .filter(m -> m.getTalentupModuleId().equals(888L)).count();
        assertThat(count).isEqualTo(1); // dedup working
    }

    @Test
    void processOne_existingUser_notDuplicated() {
        syncService.processOne(sampleCompletion("existing@test.com", 777L));
        syncService.processOne(sampleCompletion("existing@test.com", 700L)); // different module

        long userCount = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals("existing@test.com")).count();
        assertThat(userCount).isEqualTo(1); // same user, not created twice
    }
}