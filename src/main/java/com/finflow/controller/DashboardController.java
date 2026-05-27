package com.finflow.controller;

import com.finflow.dto.DashboardResponse;
import com.finflow.entity.User;
import com.finflow.repository.UserRepository;
import com.finflow.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get dashboard summary for a month/year")
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(defaultValue = "0")  int month,
            @RequestParam(defaultValue = "0")  int year) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Default to current month/year if not provided
        if (month == 0) month = LocalDate.now().getMonthValue();
        if (year  == 0) year  = LocalDate.now().getYear();

        return ResponseEntity.ok(
                dashboardService.getDashboard(user.getId(), month, year));
    }
}