package com.feedback.feedback360.services;

import com.feedback.feedback360.entities.Feedback;
import com.feedback.feedback360.enums.FeedbackStatus;
import com.feedback.feedback360.enums.NotificationStatus;
import com.feedback.feedback360.repositories.FeedbackRepository;
import com.feedback.feedback360.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Feedback is composed of exactly two fields — rating (0-10) and comment — both
 * mandatory. No dynamic question bank; confirmed requirement, admin Questions tab removed.
 */
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final NotificationRepository notificationRepository;

    public static class AlreadySubmittedException extends RuntimeException {
        public AlreadySubmittedException(String msg) { super(msg); }
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String msg) { super(msg); }
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String msg) { super(msg); }
    }

    public Feedback findById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Feedback not found: " + id));
    }

    @Transactional
    public Feedback submit(Long userId, Long moduleId, Short rating, String comment) {
        Feedback feedback = feedbackRepository.findByUserIdAndModuleId(userId, moduleId)
                .orElseThrow(() -> new NotFoundException("No feedback shell found for this module"));

        if (feedback.getStatus() == FeedbackStatus.SUBMITTED) {
            throw new AlreadySubmittedException("Feedback already submitted for this module");
        }

        // Both fields mandatory — enforced here even though @Valid already checks the DTO,
        // since the service must never trust the controller layer alone.
        if (rating == null || rating < 0 || rating > 10) {
            throw new ValidationException("Rating must be between 0 and 10");
        }
        if (comment == null || comment.isBlank()) {
            throw new ValidationException("Comment is required");
        }

        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setStatus(FeedbackStatus.SUBMITTED);
        feedback.setSubmittedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);

        notificationRepository.findAll().stream()
                .filter(n -> n.getUser().getId().equals(userId)
                          && n.getModule().getId().equals(moduleId))
                .forEach(n -> {
                    n.setStatus(NotificationStatus.RESPONDED);
                    notificationRepository.save(n);
                });

        return feedback;
    }
}
