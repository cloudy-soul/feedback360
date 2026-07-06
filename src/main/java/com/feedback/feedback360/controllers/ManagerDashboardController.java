package com.feedback.feedback360.controllers;

import com.feedback.feedback360.dto.DashboardDTO;
import com.feedback.feedback360.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/dashboard")
@RequiredArgsConstructor
@Tag(name = "Manager - Dashboard", description = "Analytics KPIs and chart data for managers/HR")
public class ManagerDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get all KPIs and chart data (response rate, avg rating, by department, trend, top/bottom modules)")
    public DashboardDTO dashboard() {
        return dashboardService.build();
    }
}
