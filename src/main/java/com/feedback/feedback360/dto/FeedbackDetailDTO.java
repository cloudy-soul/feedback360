package com.feedback.feedback360.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackDetailDTO {
    private Long feedbackId;
    private String moduleTitle;
    private String submittedBy;
    private LocalDateTime submittedAt;
    private Short rating;
    private String comment;
}
