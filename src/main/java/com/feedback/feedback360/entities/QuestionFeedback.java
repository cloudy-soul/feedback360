package com.feedback.feedback360.entities;

import com.feedback.feedback360.enums.QuestionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "question_feedback")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuestionFeedback {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Builder.Default
    private boolean required = true;

    @Column(name = "display_order")
    @Builder.Default
    private int displayOrder = 0;

    @Builder.Default
    private boolean active = true;
}