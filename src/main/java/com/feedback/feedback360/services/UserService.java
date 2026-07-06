package com.feedback.feedback360.services;

import com.feedback.feedback360.dto.UserRequestDTO;
import com.feedback.feedback360.entities.User;
import com.feedback.feedback360.enums.Role;
import com.feedback.feedback360.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> listAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Transactional
    public User create(UserRequestDTO dto) {
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }
        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .department(dto.getDepartment())
                .active(true)
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, UserRequestDTO dto) {
        User user = findById(id);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(dto.getRole());
        user.setDepartment(dto.getDepartment());
        // Email is intentionally NOT editable — it is the TalentUp match key.
        // Changing it would silently break the sync dedup logic in Chapter 3.
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        return userRepository.save(user);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        User user = findById(id);
        user.setActive(active);
        userRepository.save(user);
    }
}