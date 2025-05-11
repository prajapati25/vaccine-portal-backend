package com.school.vaccineportalbackend.controller;

import com.school.vaccineportalbackend.dto.VaccinationRecordDTO;
import com.school.vaccineportalbackend.model.VaccinationRecord;
import com.school.vaccineportalbackend.service.VaccinationRecordService;
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

@RestController
@RequestMapping("/api/vaccination-records")
@CrossOrigin(origins = "*")
@Validated
public class VaccinationRecordController {
    @Autowired
    private VaccinationRecordService vaccinationRecordService;

    @GetMapping
    public ResponseEntity<Page<VaccinationRecordDTO>> getAllRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationRecordService.getAllRecords(pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VaccinationRecordDTO> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(vaccinationRecordService.getRecordById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Page<VaccinationRecordDTO>> getRecordsByStudentId(
            @PathVariable @NotBlank String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
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
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(vaccinationRecordService.getRecordsByDateRange(startDate, endDate, pageRequest));
    }

    @PostMapping
    public ResponseEntity<VaccinationRecordDTO> createRecord(@Valid @RequestBody VaccinationRecordDTO recordDTO) {
        return ResponseEntity.ok(vaccinationRecordService.createRecord(recordDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VaccinationRecordDTO> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody VaccinationRecordDTO recordDTO) {
        return ResponseEntity.ok(vaccinationRecordService.updateRecord(id, recordDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        vaccinationRecordService.deleteRecord(id);
        return ResponseEntity.ok().build();
    }
}