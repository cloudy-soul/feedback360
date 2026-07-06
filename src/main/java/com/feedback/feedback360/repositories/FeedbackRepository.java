package com.feedback.feedback360.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.feedback.feedback360.entities.Feedback;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long>, JpaSpecificationExecutor<Feedback> {
    Optional<Feedback> findByUserIdAndModuleId(Long userId, Long moduleId);
    List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId);
}
