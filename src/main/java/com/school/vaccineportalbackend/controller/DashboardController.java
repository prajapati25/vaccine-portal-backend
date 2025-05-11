package com.school.vaccineportalbackend.controller;

import com.school.vaccineportalbackend.dto.DashboardStatsDTO;
import com.school.vaccineportalbackend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/students/count")
    public ResponseEntity<Long> getStudentCount() {
        return ResponseEntity.ok(dashboardService.getTotalStudentCount());
    }

    @GetMapping("/vaccines/administered")
    public ResponseEntity<Long> getVaccinesAdministered() {
        return ResponseEntity.ok(dashboardService.getVaccinesAdministered());
    }

    @GetMapping("/vaccines/due-soon")
    public ResponseEntity<Long> getVaccinesDueSoon() {
        return ResponseEntity.ok(dashboardService.getVaccinesDueSoon());
    }

    @GetMapping("/vaccines/overdue")
    public ResponseEntity<Long> getVaccinesOverdue() {
        return ResponseEntity.ok(dashboardService.getVaccinesOverdue());
    }

    @GetMapping("/vaccinations/by-grade")
    public ResponseEntity<DashboardStatsDTO.GradeWiseStats> getVaccinationsByGrade() {
        return ResponseEntity.ok(dashboardService.getVaccinationsByGrade());
    }

    @GetMapping("/vaccinations/status-summary")
    public ResponseEntity<DashboardStatsDTO.StatusSummary> getVaccinationStatusSummary() {
        return ResponseEntity.ok(dashboardService.getVaccinationStatusSummary());
    }

    @GetMapping("/upcoming-drives")
    public ResponseEntity<DashboardStatsDTO.UpcomingDrives> getUpcomingDrives() {
        return ResponseEntity.ok(dashboardService.getUpcomingDrives());
    }
} 