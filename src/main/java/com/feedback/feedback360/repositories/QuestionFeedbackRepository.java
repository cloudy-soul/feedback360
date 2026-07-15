package com.feedback.feedback360.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.feedback.feedback360.entities.QuestionFeedback;

import java.util.List;

public interface QuestionFeedbackRepository extends JpaRepository<QuestionFeedback, Long> {
    List<QuestionFeedback> findByActiveTrueOrderByDisplayOrderAsc();
}
