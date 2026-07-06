package com.feedback.feedback360.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "module_formation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModuleFormation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(length = 150)
    private String category;

    @Column(name = "completion_date", nullable = false)
    private LocalDateTime completionDate;

    @Builder.Default
    @Column(length = 50)
    private String source = "TalentUp";

    @Column(name = "talentup_module_id", nullable = false)
    private Long talentupModuleId;

    @Column(name = "talentup_parcours_id")
    private Long talentupParcoursId;

    @Column(name = "talentup_parcours_name", length = 150)
    private String talentupParcoursName;

    @Column(name = "talentup_population_id")
    private Long talentupPopulationId;

    @Column(name = "talentup_population_name", length = 150)
    private String talentupPopulationName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
