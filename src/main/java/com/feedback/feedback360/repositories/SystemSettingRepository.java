package com.feedback.feedback360.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.feedback.feedback360.entities.SystemSetting;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
