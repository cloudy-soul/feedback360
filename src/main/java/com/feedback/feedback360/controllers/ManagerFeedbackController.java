package com.feedback.feedback360.controllers;

import com.feedback.feedback360.dto.FeedbackBrowseDTO;
import com.feedback.feedback360.entities.Feedback;
import com.feedback.feedback360.repositories.FeedbackRepository;
import com.feedback.feedback360.repository.spec.FeedbackSpecifications;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/manager/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Manager - Feedbacks", description = "Browse, filter and export submitted feedback")
public class ManagerFeedbackController {

    private final FeedbackRepository feedbackRepository;

    @GetMapping
    @Operation(summary = "Browse/filter feedback with pagination")
    @Transactional(readOnly = true)
    public Page<FeedbackBrowseDTO> browse(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Short minRating,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Specification<Feedback> spec = Specification.<Feedback>where(null)
                .and(FeedbackSpecifications.moduleTitleContains(module))
                .and(FeedbackSpecifications.departmentEquals(department))
                .and(FeedbackSpecifications.submittedAfter(dateFrom != null ? dateFrom.atStartOfDay() : null))
                .and(FeedbackSpecifications.submittedBefore(dateTo != null ? dateTo.atTime(23, 59, 59) : null))
                .and(FeedbackSpecifications.minRating(minRating))
                .and(FeedbackSpecifications.statusEquals(status));

        return feedbackRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt")))
                .map(FeedbackBrowseDTO::from);
    }

    @GetMapping("/export")
    @Operation(summary = "Export the filtered feedback list as CSV")
    @Transactional(readOnly = true)
    public void export(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Short minRating,
            @RequestParam(required = false) String status,
            HttpServletResponse response) throws Exception {

        Specification<Feedback> spec = Specification.<Feedback>where(null)
                .and(FeedbackSpecifications.moduleTitleContains(module))
                .and(FeedbackSpecifications.departmentEquals(department))
                .and(FeedbackSpecifications.submittedAfter(dateFrom != null ? dateFrom.atStartOfDay() : null))
                .and(FeedbackSpecifications.submittedBefore(dateTo != null ? dateTo.atTime(23, 59, 59) : null))
                .and(FeedbackSpecifications.minRating(minRating))
                .and(FeedbackSpecifications.statusEquals(status));

        List<Feedback> results = feedbackRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "submittedAt"));

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=feedback_export.csv");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        try (PrintWriter writer = response.getWriter()) {
            writer.println("Module,Employee,Email,Department,Rating,Status,Submitted At");
            for (Feedback f : results) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                        escape(f.getModule().getTitle()),
                        escape(com.feedback.feedback360.util.NameFormatter.display(f.getUser().getFirstName(), f.getUser().getLastName())),
                        escape(f.getUser().getEmail()),
                        escape(f.getUser().getDepartment()),
                        f.getRating() != null ? f.getRating() : "",
                        f.getStatus(),
                        f.getSubmittedAt() != null ? f.getSubmittedAt().format(fmt) : "");
            }
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.contains(",") ? "\"" + value + "\"" : value;
    }
}