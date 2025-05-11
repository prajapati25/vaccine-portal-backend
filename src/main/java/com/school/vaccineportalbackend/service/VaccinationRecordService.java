package com.school.vaccineportalbackend.service;

import com.school.vaccineportalbackend.model.Student;
import com.school.vaccineportalbackend.model.VaccinationDrive;
import com.school.vaccineportalbackend.model.VaccinationRecord;
import com.school.vaccineportalbackend.repository.StudentRepository;
import com.school.vaccineportalbackend.repository.VaccinationRecordRepository;
import com.school.vaccineportalbackend.dto.VaccinationRecordDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
public class VaccinationRecordService {
    private static final Logger logger = LogManager.getLogger(VaccinationRecordService.class);
    
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    
    @Autowired
    private StudentRepository studentRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private VaccinationRecordDTO convertToDTO(VaccinationRecord record) {
        logger.debug("Converting vaccination record to DTO - ID: {}", record.getId());
        VaccinationRecordDTO dto = new VaccinationRecordDTO();
        dto.setId(record.getId());
        dto.setStudentId(record.getStudent().getStudentId());
        dto.setStudentName(record.getStudent().getName());
        dto.setDriveId(record.getVaccinationDrive().getId());
        dto.setDoseNumber(record.getDoseNumber());
        dto.setVaccinationDate(record.getVaccinationDate().format(DATE_TIME_FORMATTER));
        dto.setBatchNumber(record.getBatchNumber());
        dto.setAdministeredBy(record.getAdministeredBy());
        dto.setVaccinationSite(record.getVaccinationSite());
        dto.setStatus(record.getStatus());
        dto.setSideEffects(record.getSideEffects());
        if (record.getNextDoseDate() != null) {
            dto.setNextDoseDate(record.getNextDoseDate().format(DATE_TIME_FORMATTER));
        }
        dto.setNotes(record.getNotes());
        dto.setCreatedAt(record.getCreatedAt().format(DATE_TIME_FORMATTER));
        dto.setUpdatedAt(record.getUpdatedAt().format(DATE_TIME_FORMATTER));
        return dto;
    }

    private VaccinationRecord convertToEntity(VaccinationRecordDTO dto) {
        VaccinationRecord record = new VaccinationRecord();
        
        // Set student
        Student student = studentRepository.findById(dto.getStudentId())
            .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + dto.getStudentId()));
        record.setStudent(student);
        
        // Set vaccination drive
        VaccinationDrive drive = vaccinationRecordRepository.findVaccinationDriveById(dto.getDriveId())
            .orElseThrow(() -> new IllegalArgumentException("Vaccination drive not found with ID: " + dto.getDriveId()));
        record.setVaccinationDrive(drive);
        
        // Set dates
        try {
            record.setVaccinationDate(LocalDateTime.parse(dto.getVaccinationDate(), DATE_TIME_FORMATTER));
            if (dto.getNextDoseDate() != null) {
                record.setNextDoseDate(LocalDateTime.parse(dto.getNextDoseDate(), DATE_TIME_FORMATTER));
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd'T'HH:mm:ss");
        }
        
        // Set other fields
        record.setDoseNumber(dto.getDoseNumber());
        record.setBatchNumber(dto.getBatchNumber());
        record.setAdministeredBy(dto.getAdministeredBy());
        record.setVaccinationSite(dto.getVaccinationSite());
        record.setStatus(dto.getStatus() != null ? dto.getStatus() : "SCHEDULED");
        record.setSideEffects(dto.getSideEffects());
        record.setNotes(dto.getNotes());
        
        return record;
    }

    @Transactional(readOnly = true)
    public Page<VaccinationRecordDTO> getAllRecords(Pageable pageable) {
        return vaccinationRecordRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public VaccinationRecordDTO getRecordById(Long id) {
        return vaccinationRecordRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Vaccination record not found"));
    }

    @Transactional(readOnly = true)
    public Page<VaccinationRecordDTO> getRecordsByStudentId(String studentId, Pageable pageable) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return vaccinationRecordRepository.findByStudent(student, pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<VaccinationRecordDTO> getRecordsByDrive(Long driveId, Pageable pageable) {
        VaccinationDrive drive = vaccinationRecordRepository.findVaccinationDriveById(driveId)
                .orElseThrow(() -> new RuntimeException("Vaccination drive not found"));
        return vaccinationRecordRepository.findByVaccinationDrive(drive, pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<VaccinationRecordDTO> getRecordsByStudentAndStatus(String studentId, String status, Pageable pageable) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return vaccinationRecordRepository.findByStudentAndStatus(student, status, pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<VaccinationRecordDTO> getRecordsByDriveAndStatus(Long driveId, String status, Pageable pageable) {
        VaccinationDrive drive = vaccinationRecordRepository.findVaccinationDriveById(driveId)
                .orElseThrow(() -> new RuntimeException("Vaccination drive not found"));
        return vaccinationRecordRepository.findByVaccinationDriveAndStatus(drive, status, pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<VaccinationRecordDTO> getRecordsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return vaccinationRecordRepository.findByVaccinationDateBetween(startDate, endDate, pageable).map(this::convertToDTO);
    }

    @Transactional
    public VaccinationRecordDTO createRecord(VaccinationRecordDTO recordDTO) {
        VaccinationRecord record = convertToEntity(recordDTO);
        VaccinationRecord savedRecord = vaccinationRecordRepository.save(record);
        return convertToDTO(savedRecord);
    }

    @Transactional
    public VaccinationRecordDTO updateRecord(Long id, VaccinationRecordDTO recordDTO) {
        logger.info("Updating vaccination record with ID: {}", id);
        
        VaccinationRecord existingRecord = vaccinationRecordRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Vaccination record not found with ID: {}", id);
                    return new RuntimeException("Vaccination record not found");
                });
        
        // Validate student if provided
        if (recordDTO.getStudentId() != null) {
            Student student = studentRepository.findById(recordDTO.getStudentId())
                .orElseThrow(() -> {
                    logger.error("Student not found with ID: {}", recordDTO.getStudentId());
                    return new RuntimeException("Student not found");
                });
            existingRecord.setStudent(student);
        }
        
        // Validate drive if provided
        if (recordDTO.getDriveId() != null) {
            VaccinationDrive drive = vaccinationRecordRepository.findVaccinationDriveById(recordDTO.getDriveId())
                .orElseThrow(() -> {
                    logger.error("Vaccination drive not found with ID: {}", recordDTO.getDriveId());
                    return new RuntimeException("Vaccination drive not found");
                });
            existingRecord.setVaccinationDrive(drive);
        }
        
        // Update dose number if provided
        if (recordDTO.getDoseNumber() != null) {
            // Validate dose number sequence
            Optional<VaccinationRecord> lastDose = vaccinationRecordRepository
                .findFirstByStudentAndVaccinationDriveOrderByDoseNumberDesc(
                    existingRecord.getStudent(), existingRecord.getVaccinationDrive());
            
            if (lastDose.isPresent() && recordDTO.getDoseNumber() <= lastDose.get().getDoseNumber()) {
                logger.error("Invalid dose number sequence for student: {} and drive: {}", 
                    existingRecord.getStudent().getStudentId(), existingRecord.getVaccinationDrive().getId());
                throw new RuntimeException("Invalid dose number sequence");
            }
            existingRecord.setDoseNumber(recordDTO.getDoseNumber());
        }
        
        // Update vaccination date if provided
        if (recordDTO.getVaccinationDate() != null) {
            try {
                LocalDateTime vaccinationDate = LocalDateTime.parse(recordDTO.getVaccinationDate(), DATE_TIME_FORMATTER);
                if (vaccinationDate.isAfter(LocalDateTime.now())) {
                    logger.error("Vaccination date cannot be in the future");
                    throw new RuntimeException("Vaccination date cannot be in the future");
                }
                existingRecord.setVaccinationDate(vaccinationDate);
            } catch (DateTimeParseException e) {
                logger.error("Invalid vaccination date format: {}", recordDTO.getVaccinationDate());
                throw new RuntimeException("Invalid vaccination date format. Expected format: yyyy-MM-dd'T'HH:mm:ss");
            }
        }
        
        // Update next dose date if provided
        if (recordDTO.getNextDoseDate() != null) {
            try {
                existingRecord.setNextDoseDate(LocalDateTime.parse(recordDTO.getNextDoseDate(), DATE_TIME_FORMATTER));
            } catch (DateTimeParseException e) {
                logger.error("Invalid next dose date format: {}", recordDTO.getNextDoseDate());
                throw new RuntimeException("Invalid next dose date format. Expected format: yyyy-MM-dd'T'HH:mm:ss");
            }
        }
        
        // Update other fields if provided
        if (recordDTO.getBatchNumber() != null) {
            existingRecord.setBatchNumber(recordDTO.getBatchNumber());
        }
        if (recordDTO.getAdministeredBy() != null) {
            existingRecord.setAdministeredBy(recordDTO.getAdministeredBy());
        }
        if (recordDTO.getVaccinationSite() != null) {
            existingRecord.setVaccinationSite(recordDTO.getVaccinationSite());
        }
        if (recordDTO.getStatus() != null) {
            // Validate status transition
            String currentStatus = existingRecord.getStatus();
            String newStatus = recordDTO.getStatus();
            if (!isValidStatusTransition(currentStatus, newStatus)) {
                logger.error("Invalid status transition from {} to {}", currentStatus, newStatus);
                throw new RuntimeException("Invalid status transition");
            }
            existingRecord.setStatus(newStatus);
        }
        if (recordDTO.getSideEffects() != null) {
            existingRecord.setSideEffects(recordDTO.getSideEffects());
        }
        if (recordDTO.getNotes() != null) {
            existingRecord.setNotes(recordDTO.getNotes());
        }
        
        VaccinationRecord savedRecord = vaccinationRecordRepository.save(existingRecord);
        logger.debug("Successfully updated vaccination record with ID: {}", savedRecord.getId());
        return convertToDTO(savedRecord);
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // Define valid status transitions
        if (currentStatus.equals("SCHEDULED")) {
            return newStatus.equals("COMPLETED") || newStatus.equals("CANCELLED");
        } else if (currentStatus.equals("COMPLETED")) {
            return newStatus.equals("CANCELLED"); // Can only cancel a completed record
        } else if (currentStatus.equals("CANCELLED")) {
            return false; // Cannot change status of cancelled record
        }
        return false;
    }

    @Transactional
    public void deleteRecord(Long id) {
        VaccinationRecord record = vaccinationRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaccination record not found"));
        vaccinationRecordRepository.delete(record);
    }

    private void validateRecord(VaccinationRecord record) {
        if (record.getVaccinationDate().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Vaccination date cannot be in the future");
        }

        // Check if this dose number already exists for this student and drive
        if (vaccinationRecordRepository.existsByStudentAndVaccinationDriveAndDoseNumber(
                record.getStudent(), record.getVaccinationDrive(), record.getDoseNumber())) {
            throw new RuntimeException("This dose number already exists for this student and drive");
        }

        // Validate dose number sequence
        Optional<VaccinationRecord> lastDose = vaccinationRecordRepository
                .findFirstByStudentAndVaccinationDriveOrderByDoseNumberDesc(
                        record.getStudent(), record.getVaccinationDrive());

        if (lastDose.isPresent() && record.getDoseNumber() <= lastDose.get().getDoseNumber()) {
            throw new RuntimeException("Invalid dose number sequence");
        }
    }

    @Transactional(readOnly = true)
    public Page<VaccinationRecordDTO> generateVaccinationReport(
            String vaccineName,
            String status,
            String grade,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        
        Specification<VaccinationRecord> spec = Specification.where(null);
        
        if (vaccineName != null && !vaccineName.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("vaccinationDrive").get("vaccine").get("name"), vaccineName));
        }
        
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("status"), status));
        }
        
        if (grade != null && !grade.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("student").get("grade"), grade));
        }
        
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> 
                cb.greaterThanOrEqualTo(root.get("vaccinationDate"), startDate.atStartOfDay()));
        }
        
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> 
                cb.lessThanOrEqualTo(root.get("vaccinationDate"), endDate.atTime(23, 59, 59)));
        }
        
        return vaccinationRecordRepository.findAll(spec, pageable).map(this::convertToDTO);
    }
}