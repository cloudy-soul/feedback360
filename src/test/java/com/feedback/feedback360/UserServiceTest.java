package com.feedback.feedback360;

import com.feedback.feedback360.dto.UserRequestDTO;
import com.feedback.feedback360.entities.User;
import com.feedback.feedback360.enums.Role;
import com.feedback.feedback360.repositories.UserRepository;
import com.feedback.feedback360.services.UserService;
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
class UserServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired UserService userService;
    @Autowired UserRepository userRepository;

    private UserRequestDTO sampleDto(String email) {
        return UserRequestDTO.builder()
                .firstName("Test").lastName("User")
                .email(email).password("Test123!")
                .role(Role.EMPLOYEE).department("IT")
                .build();
    }

    @Test
    void createUser_savesAndReturns() {
        var user = userService.create(sampleDto("test@example.com"));
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPasswordHash()).doesNotContain("Test123!"); // must be hashed
    }

    @Test
    void createUser_duplicateEmail_throws() {
        userService.create(sampleDto("dup@example.com"));
        assertThatThrownBy(() -> userService.create(sampleDto("dup@example.com")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void deactivate_preventsLogin() {
        var user = userService.create(sampleDto("deact@example.com"));
        userService.setActive(user.getId(), false);
        assertThat(userRepository.findById(user.getId()).get().isActive()).isFalse();
    }

    @Test
    void userCreation_preservesFullName() {
        User user = userService.create(sampleDto("name@example.com"));
        assertThat(user.getFirstName()).isEqualTo("Test");
        assertThat(user.getLastName()).isEqualTo("User");
    }
}