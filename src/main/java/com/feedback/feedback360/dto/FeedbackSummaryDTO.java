package com.feedback.feedback360.dto;

import com.feedback.feedback360.entities.Feedback;
import com.feedback.feedback360.enums.FeedbackStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackSummaryDTO {
    private Long feedbackId;
    private Long moduleId;
    private String moduleTitle;
    private String moduleCategory;
    private LocalDateTime completionDate;
    private FeedbackStatus status;
    private LocalDateTime submittedAt;

    public static FeedbackSummaryDTO from(Feedback f) {
        return FeedbackSummaryDTO.builder()
                .feedbackId(f.getId())
                .moduleId(f.getModule().getId())
                .moduleTitle(f.getModule().getTitle())
                .moduleCategory(f.getModule().getCategory())
                .completionDate(f.getModule().getCompletionDate())
                .status(f.getStatus())
                .submittedAt(f.getSubmittedAt())
                .build();
    }
}
