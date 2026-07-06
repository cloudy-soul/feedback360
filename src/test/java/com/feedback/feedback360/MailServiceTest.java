package com.feedback.feedback360;

import com.feedback.feedback360.entities.*;
import com.feedback.feedback360.enums.Role;
import com.feedback.feedback360.services.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class MailServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MailService mailService;

    @Test
    void consoleImpl_returnsTrue() {
        User user = User.builder().firstName("Alice").lastName("Test")
                .email("alice@test.com").role(Role.EMPLOYEE).active(true)
                .passwordHash("x").build();
        ModuleFormation module = ModuleFormation.builder()
                .title("Spring Basics").talentupModuleId(1L)
                .completionDate(LocalDateTime.now()).source("TalentUp").user(user).build();
        assertThat(mailService.sendFeedbackInvite(user, module)).isTrue();
    }
}