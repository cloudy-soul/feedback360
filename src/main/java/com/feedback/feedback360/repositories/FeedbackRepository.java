package com.feedback.feedback360.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedback.feedback360.entities.Feedback;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long>, JpaSpecificationExecutor<Feedback> {
    Optional<Feedback> findByUserIdAndModuleId(Long userId, Long moduleId);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.module WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<Feedback> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}