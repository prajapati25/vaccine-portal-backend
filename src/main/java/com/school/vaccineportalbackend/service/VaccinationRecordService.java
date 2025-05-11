package com.school.vaccineportalbackend.service;

import com.school.vaccineportalbackend.model.Student;
import com.school.vaccineportalbackend.model.VaccinationDrive;
import com.school.vaccineportalbackend.model.VaccinationRecord;
import com.school.vaccineportalbackend.repository.StudentRepository;
import com.school.vaccineportalbackend.repository.VaccinationRecordRepository;
import com.school.vaccineportalbackend.dto.VaccinationRecordDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VaccinationRecordService {
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    
    @Autowired
    private StudentRepository studentRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private VaccinationRecordDTO convertToDTO(VaccinationRecord record) {
        VaccinationRecordDTO dto = new VaccinationRecordDTO();
        dto.setId(record.getId());
        dto.setStudentId(record.getStudent().getStudentId());
        dto.setStudentName(record.getStudent().getName());
        dto.setVaccinationDriveId(record.getVaccinationDrive().getId());
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
        VaccinationDrive drive = vaccinationRecordRepository.findVaccinationDriveById(dto.getVaccinationDriveId())
            .orElseThrow(() -> new IllegalArgumentException("Vaccination drive not found with ID: " + dto.getVaccinationDriveId()));
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
        VaccinationRecord existingRecord = vaccinationRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaccination record not found"));
        
        // Update only the fields that are provided in the DTO
        if (recordDTO.getDoseNumber() != null) {
            existingRecord.setDoseNumber(recordDTO.getDoseNumber());
        }
        if (recordDTO.getVaccinationDate() != null) {
            try {
                existingRecord.setVaccinationDate(LocalDateTime.parse(recordDTO.getVaccinationDate(), DATE_TIME_FORMATTER));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid vaccination date format. Expected format: yyyy-MM-dd'T'HH:mm:ss");
            }
        }
        if (recordDTO.getNextDoseDate() != null) {
            try {
                existingRecord.setNextDoseDate(LocalDateTime.parse(recordDTO.getNextDoseDate(), DATE_TIME_FORMATTER));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid next dose date format. Expected format: yyyy-MM-dd'T'HH:mm:ss");
            }
        }
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
            existingRecord.setStatus(recordDTO.getStatus());
        }
        if (recordDTO.getSideEffects() != null) {
            existingRecord.setSideEffects(recordDTO.getSideEffects());
        }
        if (recordDTO.getNotes() != null) {
            existingRecord.setNotes(recordDTO.getNotes());
        }
        
        VaccinationRecord savedRecord = vaccinationRecordRepository.save(existingRecord);
        return convertToDTO(savedRecord);
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
}