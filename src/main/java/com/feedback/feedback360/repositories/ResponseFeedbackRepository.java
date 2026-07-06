package com.feedback.feedback360.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.feedback.feedback360.entities.ResponseFeedback;

import java.util.List;

public interface ResponseFeedbackRepository extends JpaRepository<ResponseFeedback, Long> {
    List<ResponseFeedback> findByFeedbackId(Long feedbackId);
}
