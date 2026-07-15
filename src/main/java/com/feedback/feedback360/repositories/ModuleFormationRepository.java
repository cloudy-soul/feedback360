package com.feedback.feedback360.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.feedback.feedback360.entities.ModuleFormation;

import java.util.Optional;

public interface ModuleFormationRepository extends JpaRepository<ModuleFormation, Long> {
    Optional<ModuleFormation> findByUserIdAndTalentupModuleId(Long userId, Long talentupModuleId);
}
