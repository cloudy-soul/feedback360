package com.feedback.feedback360;

import com.feedback.feedback360.entities.QuestionFeedback;
import com.feedback.feedback360.enums.QuestionType;
import com.feedback.feedback360.services.QuestionFeedbackService;
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
class QuestionFeedbackServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired QuestionFeedbackService service;

    private QuestionFeedback makeQuestion(String label) {
        return QuestionFeedback.builder()
                .label(label).type(QuestionType.RATING).required(true).build();
    }

    @Test
    void create_appendsToEnd() {
        var q1 = service.save(makeQuestion("Q1"));
        var q2 = service.save(makeQuestion("Q2"));
        assertThat(q2.getDisplayOrder()).isGreaterThan(q1.getDisplayOrder());
    }

    @Test
    void moveUp_swapsOrderCorrectly() {
        var q1 = service.save(makeQuestion("First"));
        var q2 = service.save(makeQuestion("Second"));
        service.moveUp(q2.getId());
        var ordered = service.listAll();
        assertThat(ordered.get(0).getLabel()).isEqualTo("Second");
    }

    @Test
    void deactivate_excludedFromActiveList() {
        var q = service.save(makeQuestion("ToDeactivate"));
        service.setActive(q.getId(), false);
        assertThat(service.listActiveOrdered())
                .noneMatch(item -> item.getId().equals(q.getId()));
    }
}