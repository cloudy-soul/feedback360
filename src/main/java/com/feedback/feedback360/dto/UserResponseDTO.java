package com.feedback.feedback360.dto;

import com.feedback.feedback360.enums.Role;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String department;
    private boolean active;
    private LocalDateTime createdAt;

    public static UserResponseDTO from(com.feedback.feedback360.entities.User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
