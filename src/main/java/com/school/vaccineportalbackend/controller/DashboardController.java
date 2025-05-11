package com.school.vaccineportalbackend.controller;

import com.school.vaccineportalbackend.dto.DashboardStatsDTO;
import com.school.vaccineportalbackend.service.DashboardService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    private static final Logger logger = LogManager.getLogger(DashboardController.class);
    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/students/count")
    public ResponseEntity<Long> getStudentCount() {
        logger.info("Getting total student count");
        return ResponseEntity.ok(dashboardService.getTotalStudentCount());
    }

    @GetMapping("/vaccines/administered")
    public ResponseEntity<Long> getVaccinesAdministered() {
        logger.info("Getting count of vaccines administered");
        return ResponseEntity.ok(dashboardService.getVaccinesAdministered());
    }

    @GetMapping("/vaccines/due-soon")
    public ResponseEntity<Long> getVaccinesDueSoon() {
        logger.info("Getting count of vaccines due soon");
        return ResponseEntity.ok(dashboardService.getVaccinesDueSoon());
    }

    @GetMapping("/vaccines/overdue")
    public ResponseEntity<Long> getVaccinesOverdue() {
        logger.info("Getting count of overdue vaccines");
        return ResponseEntity.ok(dashboardService.getVaccinesOverdue());
    }

    @GetMapping("/vaccinations/by-grade")
    public ResponseEntity<DashboardStatsDTO.GradeWiseStats> getVaccinationsByGrade() {
        logger.info("Getting vaccination statistics by grade");
        return ResponseEntity.ok(dashboardService.getVaccinationsByGrade());
    }

    @GetMapping("/vaccinations/status-summary")
    public ResponseEntity<DashboardStatsDTO.StatusSummary> getVaccinationStatusSummary() {
        logger.info("Getting vaccination status summary");
        return ResponseEntity.ok(dashboardService.getVaccinationStatusSummary());
    }

    @GetMapping("/upcoming-drives")
    public ResponseEntity<DashboardStatsDTO.UpcomingDrives> getUpcomingDrives() {
        logger.info("Getting list of upcoming vaccination drives");
        return ResponseEntity.ok(dashboardService.getUpcomingDrives());
    }
} 