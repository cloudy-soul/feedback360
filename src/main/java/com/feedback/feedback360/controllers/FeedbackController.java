package com.feedback.feedback360.controllers;

import com.feedback.feedback360.dto.*;
import com.feedback.feedback360.entities.Feedback;
import com.feedback.feedback360.repositories.FeedbackRepository;
import com.feedback.feedback360.services.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "Employee feedback submission and history")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;

    @GetMapping("/my-modules")
    @Operation(summary = "List all completed modules and their feedback status for the logged-in user")
    public List<FeedbackSummaryDTO> myModules(@AuthenticationPrincipal Long userId) {
        return feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(FeedbackSummaryDTO::from)
                .toList();
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit feedback (rating + comment, both mandatory) for a module")
    public ResponseEntity<?> submit(
            @RequestParam Long moduleId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody FeedbackSubmitRequest body) {
        try {
            Feedback saved = feedbackService.submit(userId, moduleId, body.rating(), body.comment());
            return ResponseEntity.ok(Map.of(
                    "feedbackId", saved.getId(),
                    "status", saved.getStatus()
            ));
        } catch (FeedbackService.AlreadySubmittedException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        } catch (FeedbackService.ValidationException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (FeedbackService.NotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feedback detail — owner sees their own, manager/admin can see any")
    @Transactional(readOnly = true)
    public ResponseEntity<FeedbackDetailDTO> detail(@PathVariable Long id,
                                                    @AuthenticationPrincipal Long userId) {
        Feedback f = feedbackService.findById(id);

        boolean isOwner = f.getUser().getId().equals(userId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isManagerOrAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isManagerOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(FeedbackDetailDTO.builder()
                .feedbackId(f.getId())
                .moduleTitle(f.getModule().getTitle())
                .submittedBy(com.feedback.feedback360.util.NameFormatter.display(f.getUser().getFirstName(), f.getUser().getLastName()))
                .submittedAt(f.getSubmittedAt())
                .rating(f.getRating())
                .comment(f.getComment())
                .build());
    }
}