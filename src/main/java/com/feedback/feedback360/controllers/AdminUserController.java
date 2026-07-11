package com.feedback.feedback360.controllers;

import com.feedback.feedback360.dto.UserRequestDTO;
import com.feedback.feedback360.dto.UserResponseDTO;
import com.feedback.feedback360.enums.Role;
import com.feedback.feedback360.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "Create, update, and deactivate user accounts")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List users, filterable by name/email and role, paginated with page size of 10, 20 or 50")
    public Page<UserResponseDTO> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        int safePageSize = (pageSize == 10 || pageSize == 20 || pageSize == 50) ? pageSize : 10;

        return userService.search(search, role, active,
                        PageRequest.of(page, safePageSize, Sort.by(Sort.Direction.ASC, "lastName")))
                .map(UserResponseDTO::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single user by ID")
    public UserResponseDTO get(@PathVariable Long id) {
        return UserResponseDTO.from(userService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponseDTO.from(userService.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user (email cannot be changed)")
    public UserResponseDTO update(@PathVariable Long id, @Valid @RequestBody UserRequestDTO dto) {
        return UserResponseDTO.from(userService.update(id, dto));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Soft-deactivate a user (preserves all their feedback history)")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.setActive(id, false);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Reactivate a previously deactivated user")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        userService.setActive(id, true);
        return ResponseEntity.noContent().build();
    }
}