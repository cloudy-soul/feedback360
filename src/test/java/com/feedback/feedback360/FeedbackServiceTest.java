package com.feedback.feedback360;

import com.feedback.feedback360.entities.Feedback;
import com.feedback.feedback360.entities.ModuleFormation;
import com.feedback.feedback360.entities.User;
import com.feedback.feedback360.enums.FeedbackStatus;
import com.feedback.feedback360.repositories.FeedbackRepository;
import com.feedback.feedback360.repositories.ModuleFormationRepository;
import com.feedback.feedback360.repositories.NotificationRepository;
import com.feedback.feedback360.repositories.UserRepository;
import com.feedback.feedback360.services.FeedbackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModuleFormationRepository moduleFormationRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    void submit_createsFeedbackShellWhenMissingAndSubmitsIt() {
        User user = User.builder().id(1L).build();
        ModuleFormation module = ModuleFormation.builder().id(2L).build();

        when(feedbackRepository.findByUserIdAndModuleId(1L, 2L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(moduleFormationRepository.findById(2L)).thenReturn(Optional.of(module));
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationRepository.findAll()).thenReturn(List.of());

        Feedback saved = feedbackService.submit(1L, 2L, (short) 8, "Great module");

        assertThat(saved.getStatus()).isEqualTo(FeedbackStatus.SUBMITTED);
        assertThat(saved.getRating()).isEqualTo((short) 8);
        assertThat(saved.getComment()).isEqualTo("Great module");
        verify(feedbackRepository, atLeastOnce()).save(any(Feedback.class));
    }
}
