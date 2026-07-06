package com.feedback.feedback360.entities;

import com.feedback.feedback360.enums.*;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "response_feedback")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResponseFeedback {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "question_id", nullable = false)
    private QuestionFeedback question;

    @Column(name = "response_value", columnDefinition = "TEXT", nullable = false)
    private String value;
}