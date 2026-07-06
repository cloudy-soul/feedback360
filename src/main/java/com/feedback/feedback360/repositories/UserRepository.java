package com.feedback.feedback360.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.feedback.feedback360.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}