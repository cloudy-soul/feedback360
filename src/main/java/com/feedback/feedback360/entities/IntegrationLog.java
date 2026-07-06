package com.feedback.feedback360.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.feedback.feedback360.enums.*;

@Entity
@Table(name = "integration_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IntegrationLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String type;       // POLL_RUN, POLL_ITEM_SAVED, POLL_ITEM_SKIPPED, POLL_ERROR, EMAIL_SENT, EMAIL_FAILED, REMINDER_SENT

    @Column(nullable = false)
    private String status;     // SUCCESS | FAILED

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "related_module_id")
    private Long relatedModuleId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}