package com.school.vaccineportalbackend.controller;

import com.school.vaccineportalbackend.service.ExportService;
import com.school.vaccineportalbackend.exception.ExportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/api/exports")
@CrossOrigin(origins = "*")
public class ExportController {
    private final ExportService exportService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int MAX_EXPORT_SIZE_MB = 10; // Maximum export size in MB

    @Autowired
    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/csv")
    public ResponseEntity<ByteArrayResource> exportToCsv(@RequestParam Map<String, String> filters) {
        try {
            validateFilters(filters);
            byte[] csvData = exportService.exportToCsv(filters);
            
            // Check file size
            if (csvData.length > MAX_EXPORT_SIZE_MB * 1024 * 1024) {
                throw new ExportException("Export data too large. Please apply more filters to reduce the result set.");
            }
            
            String filename = "vaccination_records_" + LocalDateTime.now().format(DATE_FORMATTER) + ".csv";
            ByteArrayResource resource = new ByteArrayResource(csvData);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvData.length)
                .body(resource);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Please use YYYY-MM-DD format.");
        } catch (ExportException e) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate CSV export.");
        }
    }

    @GetMapping("/pdf")
    public ResponseEntity<ByteArrayResource> exportToPdf(@RequestParam Map<String, String> filters) {
        try {
            validateFilters(filters);
            byte[] pdfData = exportService.exportToPdf(filters);
            
            // Check file size
            if (pdfData.length > MAX_EXPORT_SIZE_MB * 1024 * 1024) {
                throw new ExportException("Export data too large. Please apply more filters to reduce the result set.");
            }
            
            String filename = "vaccination_records_" + LocalDateTime.now().format(DATE_FORMATTER) + ".pdf";
            ByteArrayResource resource = new ByteArrayResource(pdfData);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfData.length)
                .body(resource);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Please use YYYY-MM-DD format.");
        } catch (ExportException e) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate PDF export.");
        }
    }

    private void validateFilters(Map<String, String> filters) {
        // Validate date format if provided
        if (filters.containsKey("startDate") && !filters.get("startDate").isEmpty()) {
            try {
                LocalDateTime.parse(filters.get("startDate") + "T00:00:00");
            } catch (DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid start date format. Please use YYYY-MM-DD format.");
            }
        }
        
        if (filters.containsKey("endDate") && !filters.get("endDate").isEmpty()) {
            try {
                LocalDateTime.parse(filters.get("endDate") + "T23:59:59");
            } catch (DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid end date format. Please use YYYY-MM-DD format.");
            }
        }

        // Validate date range if both dates are provided
        if (filters.containsKey("startDate") && !filters.get("startDate").isEmpty() &&
            filters.containsKey("endDate") && !filters.get("endDate").isEmpty()) {
            LocalDateTime startDate = LocalDateTime.parse(filters.get("startDate") + "T00:00:00");
            LocalDateTime endDate = LocalDateTime.parse(filters.get("endDate") + "T23:59:59");
            
            if (startDate.isAfter(endDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date.");
            }
        }

        // Validate status if provided
        if (filters.containsKey("status") && !filters.get("status").isEmpty()) {
            String status = filters.get("status");
            if (!status.matches("COMPLETED|SCHEDULED|CANCELLED")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value. Must be one of: COMPLETED, SCHEDULED, CANCELLED");
            }
        }
    }
} 