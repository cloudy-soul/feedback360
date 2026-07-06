package com.feedback.feedback360.controllers;

import com.feedback.feedback360.entities.User;
import com.feedback.feedback360.repositories.UserRepository;
import com.feedback.feedback360.security.JwtCookieFilter;
import com.feedback.feedback360.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Login and logout")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Getter @Setter
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @PostMapping("/login")
    @Operation(summary = "Login — sets HttpOnly JWT cookie and returns role for Angular routing")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest body,
                                                      HttpServletResponse response) {
        var userOpt = userRepository.findByEmailIgnoreCase(body.getEmail());

        // Generic message — never reveal whether email or password was wrong
        if (userOpt.isEmpty() || !userOpt.get().isActive()
                || !passwordEncoder.matches(body.getPassword(), userOpt.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        Cookie cookie = new Cookie(JwtCookieFilter.COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(8 * 60 * 60); // 8 hours
        // cookie.setSecure(true); // enable once served over HTTPS
        response.addCookie(cookie);

        // Angular reads role and userId to decide where to navigate after login
        return ResponseEntity.ok(Map.of(
                "role", user.getRole().name(),
                "userId", user.getId().toString()
        ));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout — clears the JWT cookie")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(JwtCookieFilter.COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }
}
