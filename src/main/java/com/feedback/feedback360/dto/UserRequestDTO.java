package com.feedback.feedback360.dto;
import com.feedback.feedback360.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder

public class UserRequestDTO {
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String password;

    @NotNull (message = "Role is required")
    private Role role;

    private String department;
    
}
