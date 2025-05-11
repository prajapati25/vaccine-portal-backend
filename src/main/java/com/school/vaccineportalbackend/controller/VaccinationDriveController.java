package com.school.vaccineportalbackend.controller;

import com.school.vaccineportalbackend.dto.VaccinationDriveDTO;
import com.school.vaccineportalbackend.dto.VaccinationRecordDTO;
import com.school.vaccineportalbackend.model.VaccinationDrive;
import com.school.vaccineportalbackend.model.Vaccine;
import com.school.vaccineportalbackend.service.VaccinationDriveService;
import com.school.vaccineportalbackend.service.ExportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/vaccination-drives")
@CrossOrigin(origins = "*")
@Validated
public class VaccinationDriveController {
    private static final Logger logger = LogManager.getLogger(VaccinationDriveController.class);
    
    private final VaccinationDriveService vaccinationDriveService;
    @Autowired
    private ExportService exportService;

    @Autowired
    public VaccinationDriveController(VaccinationDriveService vaccinationDriveService) {
        this.vaccinationDriveService = vaccinationDriveService;
    }

    @GetMapping
    public ResponseEntity<Page<VaccinationDriveDTO>> getAllDrives(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        logger.info("Getting all vaccination drives with pagination - page: {}, size: {}, sortBy: {}, direction: {}", 
            page, size, sortBy, direction);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationDriveService.getAllDrives(pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VaccinationDriveDTO> getDriveById(@PathVariable Long id) {
        logger.info("Getting vaccination drive by ID: {}", id);
        return ResponseEntity.ok(vaccinationDriveService.getDriveById(id));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<VaccinationDriveDTO>> getUpcomingDrives(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        logger.info("Getting upcoming vaccination drives - startDate: {}, endDate: {}, page: {}, size: {}", 
            startDate, endDate, page, size);
        
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(1);
        }
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationDriveService.getUpcomingDrives(startDate, endDate, pageRequest));
    }

    @GetMapping("/grade/{grade}")
    public ResponseEntity<Page<VaccinationDriveDTO>> getDrivesByGrade(
            @PathVariable @NotBlank @Pattern(regexp = "^[0-9]{1,2}$", message = "Grade must be a number between 1 and 12") String grade,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        logger.info("Getting vaccination drives for grade: {} with pagination - page: {}, size: {}", 
            grade, page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationDriveService.getDrivesByGrade(grade, pageRequest));
    }

    @GetMapping("/{driveId}/records")
    public ResponseEntity<Page<VaccinationRecordDTO>> getVaccinationRecordsForDrive(
            @PathVariable @NotNull Long driveId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        logger.info("Getting vaccination records for drive: {} with pagination - page: {}, size: {}", 
            driveId, page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationDriveService.getVaccinationRecordsForDrive(driveId, pageRequest));
    }

    @PostMapping
    public ResponseEntity<VaccinationDriveDTO> createDrive(@Valid @RequestBody VaccinationDriveDTO driveDTO) {
        logger.info("Creating new vaccination drive for vaccine: {} on date: {}", 
            driveDTO.getVaccine().getId(), driveDTO.getDriveDate());
        VaccinationDrive drive = new VaccinationDrive();
        
        if (driveDTO.getVaccine() == null || driveDTO.getVaccine().getId() == null) {
            throw new IllegalArgumentException("Vaccine ID is required");
        }
        Vaccine vaccine = new Vaccine();
        vaccine.setId(driveDTO.getVaccine().getId());
        drive.setVaccine(vaccine);
        
        drive.setDriveDate(driveDTO.getDriveDate());
        drive.setAvailableDoses(driveDTO.getAvailableDoses());
        drive.setApplicableGrades(String.join(",", driveDTO.getApplicableGrades()));
        drive.setMinimumAge(driveDTO.getMinimumAge());
        drive.setMaximumAge(driveDTO.getMaximumAge());
        drive.setNotes(driveDTO.getNotes());
        drive.setVaccineBatch(driveDTO.getVaccineBatch());
        
        drive.setStatus(driveDTO.getStatus() != null ? driveDTO.getStatus() : "SCHEDULED");
        drive.setActive(true);
        
        VaccinationDrive createdDrive = vaccinationDriveService.createDrive(drive);
        return ResponseEntity.ok(vaccinationDriveService.convertToDTO(createdDrive));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VaccinationDriveDTO> updateDrive(
            @PathVariable Long id,
            @Valid @RequestBody VaccinationDriveDTO driveDTO) {
        logger.info("Updating vaccination drive with ID: {}", id);
        VaccinationDrive drive = new VaccinationDrive();
        drive.setDriveDate(driveDTO.getDriveDate());
        drive.setAvailableDoses(driveDTO.getAvailableDoses());
        drive.setApplicableGrades(String.join(",", driveDTO.getApplicableGrades()));
        drive.setMinimumAge(driveDTO.getMinimumAge());
        drive.setMaximumAge(driveDTO.getMaximumAge());
        drive.setNotes(driveDTO.getNotes());
        drive.setStatus(driveDTO.getStatus());
        drive.setActive(driveDTO.isActive());
        drive.setVaccineBatch(driveDTO.getVaccineBatch());
        
        VaccinationDrive updatedDrive = vaccinationDriveService.updateDrive(id, drive);
        return ResponseEntity.ok(vaccinationDriveService.convertToDTO(updatedDrive));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrive(@PathVariable Long id) {
        logger.info("Deleting vaccination drive with ID: {}", id);
        vaccinationDriveService.deleteDrive(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportDrives(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        logger.info("Exporting vaccination drives with filters - status: {}, grade: {}, startDate: {}, endDate: {}, format: {}", 
            status, grade, startDate, endDate, format);
        try {
            Map<String, String> filters = new HashMap<>();
            if (status != null) filters.put("status", status);
            if (grade != null) filters.put("grade", grade);
            if (startDate != null) filters.put("startDate", startDate.toString());
            if (endDate != null) filters.put("endDate", endDate.toString());

            byte[] exportData;
            String filename;
            MediaType mediaType;

            if ("pdf".equalsIgnoreCase(format)) {
                exportData = exportService.exportDrivesToPdf(filters);
                filename = "vaccination_drives_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
                mediaType = MediaType.APPLICATION_PDF;
            } else {
                exportData = exportService.exportDrivesToCsv(filters);
                filename = "vaccination_drives_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
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