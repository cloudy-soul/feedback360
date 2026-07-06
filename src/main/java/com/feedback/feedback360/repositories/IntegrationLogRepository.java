package com.feedback.feedback360.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.feedback.feedback360.entities.IntegrationLog;

public interface IntegrationLogRepository extends JpaRepository<IntegrationLog, Long>, JpaSpecificationExecutor<IntegrationLog> {
}
