package com.feedback.feedback360.dto;

import com.feedback.feedback360.entities.Feedback;
import com.feedback.feedback360.enums.FeedbackStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackBrowseDTO {
    private Long feedbackId;
    private String moduleTitle;
    private Long moduleId;
    private String employeeName;
    private String employeeEmail;
    private Long employeeId;
    private String department;
    private Short rating;
    private FeedbackStatus status;
    private LocalDateTime submittedAt;

    public static FeedbackBrowseDTO from(Feedback f) {
        return FeedbackBrowseDTO.builder()
                .feedbackId(f.getId())
                .moduleTitle(f.getModule().getTitle())
                .moduleId(f.getModule().getId())
                .employeeName(f.getUser().getFirstName() + " " + f.getUser().getLastName())
                .employeeEmail(f.getUser().getEmail())
                .employeeId(f.getUser().getId())
                .department(f.getUser().getDepartment())
                .rating(f.getRating())
                .status(f.getStatus())
                .submittedAt(f.getSubmittedAt())
                .build();
    }
}
