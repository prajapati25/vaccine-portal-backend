package com.school.vaccineportalbackend.controller;

import com.school.vaccineportalbackend.dto.VaccinationRecordDTO;
import com.school.vaccineportalbackend.model.VaccinationRecord;
import com.school.vaccineportalbackend.service.VaccinationRecordService;
import com.school.vaccineportalbackend.service.ExportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.io.ByteArrayResource;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/api/vaccination-records")
@CrossOrigin(origins = "*")
@Validated
public class VaccinationRecordController {
    private static final Logger logger = LogManager.getLogger(VaccinationRecordController.class);

    @Autowired
    private VaccinationRecordService vaccinationRecordService;

    @Autowired
    private ExportService exportService;

    @GetMapping
    public ResponseEntity<Page<VaccinationRecordDTO>> getAllRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        logger.info("Getting all vaccination records with pagination - page: {}, size: {}, sortBy: {}, direction: {}", 
            page, size, sortBy, direction);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationRecordService.getAllRecords(pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VaccinationRecordDTO> getRecordById(@PathVariable Long id) {
        logger.info("Getting vaccination record by ID: {}", id);
        return ResponseEntity.ok(vaccinationRecordService.getRecordById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Page<VaccinationRecordDTO>> getRecordsByStudentId(
            @PathVariable @NotBlank String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        logger.info("Getting vaccination records for student: {} with pagination - page: {}, size: {}", 
            studentId, page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationRecordService.getRecordsByStudentId(studentId, pageRequest));
    }

    @GetMapping("/drive/{driveId}")
    public ResponseEntity<Page<VaccinationRecordDTO>> getRecordsByDrive(
            @PathVariable @NotNull Long driveId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        logger.info("Getting vaccination records for drive: {} with pagination - page: {}, size: {}", 
            driveId, page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationRecordService.getRecordsByDrive(driveId, pageRequest));
    }

    @GetMapping("/student/{studentId}/status/{status}")
    public ResponseEntity<Page<VaccinationRecordDTO>> getRecordsByStudentAndStatus(
            @PathVariable @NotBlank String studentId,
            @PathVariable @NotBlank String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationRecordService.getRecordsByStudentAndStatus(studentId, status, pageRequest));
    }

    @GetMapping("/drive/{driveId}/status/{status}")
    public ResponseEntity<Page<VaccinationRecordDTO>> getRecordsByDriveAndStatus(
            @PathVariable @NotNull Long driveId,
            @PathVariable @NotBlank String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationRecordService.getRecordsByDriveAndStatus(driveId, status, pageRequest));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<VaccinationRecordDTO>> getRecordsByDateRange(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        logger.info("Getting vaccination records by date range - startDate: {}, endDate: {}, page: {}, size: {}", 
            startDate, endDate, page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationRecordService.getRecordsByDateRange(startDate, endDate, pageRequest));
    }

    @PostMapping
    public ResponseEntity<VaccinationRecordDTO> createRecord(@Valid @RequestBody VaccinationRecordDTO recordDTO) {
        logger.info("Creating new vaccination record for student: {} and drive: {}", 
            recordDTO.getStudentId(), recordDTO.getDriveId());
        return ResponseEntity.ok(vaccinationRecordService.createRecord(recordDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody VaccinationRecordDTO recordDTO) {
        try {
            logger.info("Updating vaccination record with ID: {}", id);
            VaccinationRecordDTO updatedRecord = vaccinationRecordService.updateRecord(id, recordDTO);
            return ResponseEntity.ok(updatedRecord);
        } catch (RuntimeException e) {
            logger.error("Error updating vaccination record: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        logger.info("Deleting vaccination record with ID: {}", id);
        vaccinationRecordService.deleteRecord(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/report")
    public ResponseEntity<Page<VaccinationRecordDTO>> generateVaccinationReport(
            @RequestParam(required = false) String vaccineName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        logger.info("Generating vaccination report with filters - vaccineName: {}, status: {}, grade: {}, startDate: {}, endDate: {}", 
            vaccineName, status, grade, startDate, endDate);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationRecordService.generateVaccinationReport(
            vaccineName, status, grade, startDate, endDate, pageRequest));
    }

    @GetMapping("/report/export")
    public ResponseEntity<ByteArrayResource> exportVaccinationReport(
            @RequestParam(required = false) String vaccineName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        logger.info("Exporting vaccination report with filters - vaccineName: {}, status: {}, grade: {}, startDate: {}, endDate: {}, format: {}", 
            vaccineName, status, grade, startDate, endDate, format);
        try {
            Map<String, String> filters = new HashMap<>();
            if (vaccineName != null) filters.put("vaccineName", vaccineName);
            if (status != null) filters.put("status", status);
            if (grade != null) filters.put("grade", grade);
            if (startDate != null) filters.put("startDate", startDate.toString());
            if (endDate != null) filters.put("endDate", endDate.toString());

            byte[] exportData;
            String filename;
            MediaType mediaType;

            if ("pdf".equalsIgnoreCase(format)) {
                exportData = exportService.exportToPdf(filters);
                filename = "vaccination_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
                mediaType = MediaType.APPLICATION_PDF;
            } else {
                exportData = exportService.exportToCsv(filters);
                filename = "vaccination_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
                mediaType = MediaType.parseMediaType("text/csv");
            }

            ByteArrayResource resource = new ByteArrayResource(exportData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(mediaType)
                    .contentLength(exportData.length)
                    .body(resource);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate report: " + e.getMessage());
        }
    }
}