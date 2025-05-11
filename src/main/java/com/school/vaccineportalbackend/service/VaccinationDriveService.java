package com.school.vaccineportalbackend.service;

import com.school.vaccineportalbackend.dto.VaccinationDriveDTO;
import com.school.vaccineportalbackend.dto.VaccinationRecordDTO;
import com.school.vaccineportalbackend.model.VaccinationDrive;
import com.school.vaccineportalbackend.model.VaccinationRecord;
import com.school.vaccineportalbackend.model.Vaccine;
import com.school.vaccineportalbackend.repository.VaccinationDriveRepository;
import com.school.vaccineportalbackend.repository.VaccineRepository;
import com.school.vaccineportalbackend.repository.VaccinationRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;

@Service
public class VaccinationDriveService {
    private final VaccinationDriveRepository vaccinationDriveRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final int MIN_DAYS_BEFORE_DRIVE = 15;

    @Autowired
    public VaccinationDriveService(VaccinationDriveRepository vaccinationDriveRepository,
                                 VaccinationRecordRepository vaccinationRecordRepository) {
        this.vaccinationDriveRepository = vaccinationDriveRepository;
        this.vaccinationRecordRepository = vaccinationRecordRepository;
    }

    @Transactional(readOnly = true)
    public Page<VaccinationDriveDTO> getAllDrives(Pageable pageable) {
        return vaccinationDriveRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public VaccinationDriveDTO getDriveById(Long id) {
        return vaccinationDriveRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Vaccination drive not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<VaccinationDriveDTO> getUpcomingDrives(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return vaccinationDriveRepository.findByDriveDateBetweenAndIsActiveTrue(startDate, endDate, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<VaccinationDriveDTO> getDrivesByGrade(String grade, Pageable pageable) {
        return vaccinationDriveRepository.findByApplicableGradesContainingAndIsActiveTrue(grade, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<VaccinationRecordDTO> getVaccinationRecordsForDrive(Long driveId, Pageable pageable) {
        VaccinationDrive drive = vaccinationDriveRepository.findById(driveId)
                .orElseThrow(() -> new RuntimeException("Vaccination drive not found with id: " + driveId));
        return vaccinationRecordRepository.findByVaccinationDrive(drive, pageable)
                .map(this::convertToRecordDTO);
    }

    @Transactional
    public VaccinationDrive createDrive(VaccinationDrive drive) {
        validateDriveSchedule(drive);
        return vaccinationDriveRepository.save(drive);
    }

    @Transactional
    public VaccinationDrive updateDrive(Long id, VaccinationDrive updatedDrive) {
        VaccinationDrive existingDrive = vaccinationDriveRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Drive not found"));

        // Check if drive is in the past
        if (existingDrive.getDriveDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot modify past drives");
        }

        validateDriveSchedule(updatedDrive);
        
        // Update fields
        if (updatedDrive.getVaccine() != null) {
            existingDrive.setVaccine(updatedDrive.getVaccine());
        }
        existingDrive.setDriveDate(updatedDrive.getDriveDate());
        existingDrive.setAvailableDoses(updatedDrive.getAvailableDoses());
        existingDrive.setApplicableGrades(updatedDrive.getApplicableGrades());
        existingDrive.setMinimumAge(updatedDrive.getMinimumAge());
        existingDrive.setMaximumAge(updatedDrive.getMaximumAge());
        existingDrive.setNotes(updatedDrive.getNotes());
        existingDrive.setStatus(updatedDrive.getStatus());
        existingDrive.setActive(updatedDrive.isActive());
        
        return vaccinationDriveRepository.save(existingDrive);
    }

    @Transactional
    public void deleteDrive(Long id) {
        VaccinationDrive drive = vaccinationDriveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaccination drive not found with id: " + id));
        drive.setActive(false);
        vaccinationDriveRepository.save(drive);
    }

    public VaccinationDriveDTO convertToDTO(VaccinationDrive drive) {
        if (drive == null) {
            return null;
        }
        
        VaccinationDriveDTO dto = new VaccinationDriveDTO();
        dto.setId(drive.getId());
        
        // Handle vaccine data safely
        if (drive.getVaccine() != null) {
            VaccinationDriveDTO.VaccineDTO vaccineDTO = new VaccinationDriveDTO.VaccineDTO();
            vaccineDTO.setId(drive.getVaccine().getId());
            vaccineDTO.setName(drive.getVaccine().getName());
            dto.setVaccine(vaccineDTO);
        }
        
        dto.setDriveDate(drive.getDriveDate());
        dto.setAvailableDoses(drive.getAvailableDoses());
        dto.setApplicableGrades(drive.getApplicableGrades());
        dto.setMinimumAge(drive.getMinimumAge());
        dto.setMaximumAge(drive.getMaximumAge());
        dto.setStatus(drive.getStatus());
        dto.setActive(drive.isActive());
        dto.setNotes(drive.getNotes());
        
        if (drive.getCreatedAt() != null) {
            dto.setCreatedAt(drive.getCreatedAt().format(DATE_TIME_FORMATTER));
        }
        if (drive.getUpdatedAt() != null) {
            dto.setUpdatedAt(drive.getUpdatedAt().format(DATE_TIME_FORMATTER));
        }
        
        return dto;
    }

    private VaccinationRecordDTO convertToRecordDTO(VaccinationRecord record) {
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

    private void validateDriveSchedule(VaccinationDrive drive) {
        LocalDate today = LocalDate.now();
        LocalDate driveDate = drive.getDriveDate();

        // Check if drive is scheduled at least 15 days in advance
        if (driveDate.isBefore(today.plusDays(MIN_DAYS_BEFORE_DRIVE))) {
            throw new RuntimeException("Vaccination drive must be scheduled at least " + MIN_DAYS_BEFORE_DRIVE + " days in advance");
        }

        // Check for scheduling conflicts
        List<VaccinationDrive> existingDrives = vaccinationDriveRepository.findByDriveDateBetween(
            driveDate.minusDays(1),
            driveDate.plusDays(1)
        );

        // Split grades string into list
        List<String> driveGrades = List.of(drive.getApplicableGrades().split(","));

        for (VaccinationDrive existingDrive : existingDrives) {
            if (!existingDrive.getId().equals(drive.getId())) {
                List<String> existingGrades = List.of(existingDrive.getApplicableGrades().split(","));
                if (hasGradeOverlap(existingGrades, driveGrades)) {
                    throw new RuntimeException("Cannot schedule drive: Overlapping with existing drive on " + existingDrive.getDriveDate());
                }
            }
        }
    }

    private boolean hasGradeOverlap(List<String> grades1, List<String> grades2) {
        return grades1.stream().anyMatch(grades2::contains);
    }

    @Transactional(readOnly = true)
    public List<VaccinationDrive> getUpcomingDrives() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        return vaccinationDriveRepository.findByDriveDateBetween(today, thirtyDaysLater);
    }
} 