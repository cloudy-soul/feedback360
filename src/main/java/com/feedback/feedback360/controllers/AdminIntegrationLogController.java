package com.feedback.feedback360.controllers;

import com.feedback.feedback360.dto.IntegrationLogDTO;
import com.feedback.feedback360.entities.IntegrationLog;
import com.feedback.feedback360.repositories.IntegrationLogRepository;
import com.feedback.feedback360.repository.spec.IntegrationLogSpecifications;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * Confirmed requirement: page size selectable between 10, 20 or 50 results per the
 * user's preference. Any other value is ignored and defaults to 20.
 */
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@Tag(name = "Admin - Logs", description = "Integration log browsing with configurable page size")
public class AdminIntegrationLogController {

    private final IntegrationLogRepository logRepository;

    @GetMapping
    @Operation(summary = "List integration logs, filterable, with page size of 10, 20 or 50")
    public Page<IntegrationLogDTO> logs(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        int safePageSize = (pageSize == 10 || pageSize == 20 || pageSize == 50) ? pageSize : 20;

        Specification<IntegrationLog> spec = Specification
                .where(IntegrationLogSpecifications.type(type))
                .and(IntegrationLogSpecifications.status(status))
                .and(IntegrationLogSpecifications.createdAfter(since != null ? since.atStartOfDay() : null));

        return logRepository.findAll(spec,
                PageRequest.of(page, safePageSize, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toDto);
    }

    private IntegrationLogDTO toDto(IntegrationLog l) {
        return new IntegrationLogDTO(
                l.getId(), l.getType(), l.getStatus(), l.getMessage(),
                l.getCreatedAt().toString());
    }
}
