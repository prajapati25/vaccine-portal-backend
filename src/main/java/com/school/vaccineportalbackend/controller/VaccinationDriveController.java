package com.school.vaccineportalbackend.controller;

import com.school.vaccineportalbackend.dto.VaccinationDriveDTO;
import com.school.vaccineportalbackend.dto.VaccinationRecordDTO;
import com.school.vaccineportalbackend.model.VaccinationDrive;
import com.school.vaccineportalbackend.model.Vaccine;
import com.school.vaccineportalbackend.service.VaccinationDriveService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vaccination-drives")
@CrossOrigin(origins = "*")
@Validated
public class VaccinationDriveController {
    private final VaccinationDriveService vaccinationDriveService;

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
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationDriveService.getAllDrives(pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VaccinationDriveDTO> getDriveById(@PathVariable Long id) {
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
        
        // Set default dates if not provided
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(1); // Default to 1 month from start date
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
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationDriveService.getVaccinationRecordsForDrive(driveId, pageRequest));
    }

    @PostMapping
    public ResponseEntity<VaccinationDriveDTO> createDrive(@Valid @RequestBody VaccinationDriveDTO driveDTO) {
        VaccinationDrive drive = new VaccinationDrive();
        
        // Set vaccine
        if (driveDTO.getVaccine() == null || driveDTO.getVaccine().getId() == null) {
            throw new IllegalArgumentException("Vaccine ID is required");
        }
        Vaccine vaccine = new Vaccine();
        vaccine.setId(driveDTO.getVaccine().getId());
        drive.setVaccine(vaccine);
        
        // Set other fields
        drive.setDriveDate(driveDTO.getDriveDate());
        drive.setAvailableDoses(driveDTO.getAvailableDoses());
        drive.setApplicableGrades(String.join(",", driveDTO.getApplicableGrades()));
        drive.setMinimumAge(driveDTO.getMinimumAge());
        drive.setMaximumAge(driveDTO.getMaximumAge());
        drive.setNotes(driveDTO.getNotes());
        drive.setVaccineBatch(driveDTO.getVaccineBatch());
        
        // Set default values
        drive.setStatus(driveDTO.getStatus() != null ? driveDTO.getStatus() : "SCHEDULED");
        drive.setActive(driveDTO.isActive());
        
        VaccinationDrive createdDrive = vaccinationDriveService.createDrive(drive);
        return ResponseEntity.ok(vaccinationDriveService.convertToDTO(createdDrive));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VaccinationDriveDTO> updateDrive(
            @PathVariable Long id,
            @Valid @RequestBody VaccinationDriveDTO driveDTO) {
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
        vaccinationDriveService.deleteDrive(id);
        return ResponseEntity.ok().build();
    }
} 