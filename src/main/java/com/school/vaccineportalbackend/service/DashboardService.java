package com.school.vaccineportalbackend.service;

import com.school.vaccineportalbackend.dto.DashboardStatsDTO;
import com.school.vaccineportalbackend.dto.VaccinationDriveDTO;
import com.school.vaccineportalbackend.model.Student;
import com.school.vaccineportalbackend.model.VaccinationDrive;
import com.school.vaccineportalbackend.model.VaccinationRecord;
import com.school.vaccineportalbackend.repository.StudentRepository;
import com.school.vaccineportalbackend.repository.VaccinationDriveRepository;
import com.school.vaccineportalbackend.repository.VaccinationRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final StudentRepository studentRepository;
    private final VaccinationDriveRepository vaccinationDriveRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final VaccinationDriveService vaccinationDriveService;

    @Autowired
    public DashboardService(
            StudentRepository studentRepository,
            VaccinationDriveRepository vaccinationDriveRepository,
            VaccinationRecordRepository vaccinationRecordRepository,
            VaccinationDriveService vaccinationDriveService) {
        this.studentRepository = studentRepository;
        this.vaccinationDriveRepository = vaccinationDriveRepository;
        this.vaccinationRecordRepository = vaccinationRecordRepository;
        this.vaccinationDriveService = vaccinationDriveService;
    }

    @Transactional(readOnly = true)
    public long getTotalStudentCount() {
        return studentRepository.count();
    }

    @Transactional(readOnly = true)
    public long getVaccinesAdministered() {
        return vaccinationRecordRepository.countByStatus("COMPLETED");
    }

    @Transactional(readOnly = true)
    public long getVaccinesDueSoon() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        return vaccinationRecordRepository.countByNextDoseDateBetween(
                today.atStartOfDay(),
                thirtyDaysFromNow.atTime(23, 59, 59)
        );
    }

    @Transactional(readOnly = true)
    public long getVaccinesOverdue() {
        return vaccinationRecordRepository.countByNextDoseDateBeforeAndStatusNot(
                LocalDateTime.now(),
                "COMPLETED"
        );
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO.GradeWiseStats getVaccinationsByGrade() {
        List<Student> students = studentRepository.findAll();
        Map<String, DashboardStatsDTO.GradeStats> gradeStats = new HashMap<>();

        // Initialize stats for each grade
        students.forEach(student -> {
            String grade = student.getGrade();
            if (!gradeStats.containsKey(grade)) {
                gradeStats.put(grade, new DashboardStatsDTO.GradeStats(0, 0));
            }
            DashboardStatsDTO.GradeStats stats = gradeStats.get(grade);
            stats.setTotalStudents(stats.getTotalStudents() + 1);
        });

        // Count vaccinated students per grade
        List<VaccinationRecord> completedRecords = vaccinationRecordRepository.findByStatus("COMPLETED");
        completedRecords.forEach(record -> {
            String grade = record.getStudent().getGrade();
            if (gradeStats.containsKey(grade)) {
                DashboardStatsDTO.GradeStats stats = gradeStats.get(grade);
                stats.setVaccinatedStudents(stats.getVaccinatedStudents() + 1);
            }
        });

        return new DashboardStatsDTO.GradeWiseStats(gradeStats);
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO.StatusSummary getVaccinationStatusSummary() {
        long total = vaccinationRecordRepository.count();
        long completed = vaccinationRecordRepository.countByStatus("COMPLETED");
        long overdue = getVaccinesOverdue();
        long pending = total - completed - overdue;

        return new DashboardStatsDTO.StatusSummary(total, completed, pending, overdue);
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO.UpcomingDrives getUpcomingDrives() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        
        List<VaccinationDrive> upcomingDrives = vaccinationDriveRepository
                .findByDriveDateBetweenAndIsActiveTrue(today, thirtyDaysFromNow, null)
                .getContent();

        List<VaccinationDriveDTO> driveDTOs = upcomingDrives.stream()
                .map(vaccinationDriveService::convertToDTO)
                .collect(Collectors.toList());

        return new DashboardStatsDTO.UpcomingDrives(driveDTOs);
    }
} 