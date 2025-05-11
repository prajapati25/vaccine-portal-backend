package com.school.vaccineportalbackend.repository;

import com.school.vaccineportalbackend.model.VaccinationDrive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VaccinationDriveRepository extends JpaRepository<VaccinationDrive, Long> {
    @Query("SELECT vd FROM VaccinationDrive vd WHERE vd.driveDate BETWEEN :startDate AND :endDate AND vd.isActive = true")
    Page<VaccinationDrive> findByDriveDateBetweenAndIsActiveTrue(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("SELECT vd FROM VaccinationDrive vd WHERE vd.applicableGrades LIKE %:grade% AND vd.isActive = true")
    Page<VaccinationDrive> findByApplicableGradesContainingAndIsActiveTrue(@Param("grade") String grade, Pageable pageable);

    @Query("SELECT vd FROM VaccinationDrive vd WHERE vd.isActive = :isActive")
    List<VaccinationDrive> findByIsActive(@Param("isActive") boolean isActive);

    @Query("SELECT vd FROM VaccinationDrive vd WHERE vd.driveDate > :date")
    List<VaccinationDrive> findByDriveDateAfter(@Param("date") LocalDate date);

    @Query("SELECT vd FROM VaccinationDrive vd WHERE vd.driveDate > :date AND vd.isActive = true")
    List<VaccinationDrive> findByDriveDateAfterAndIsActiveTrue(@Param("date") LocalDate date);

    @Query("SELECT vd FROM VaccinationDrive vd WHERE vd.id = :id AND vd.isActive = true")
    Optional<VaccinationDrive> findByIdAndIsActiveTrue(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(vd) > 0 THEN true ELSE false END FROM VaccinationDrive vd WHERE vd.driveDate = :date AND vd.applicableGrades LIKE %:grade% AND vd.isActive = true")
    boolean existsByDateAndGradeAndIsActiveTrue(@Param("date") LocalDate date, @Param("grade") String grade);

    List<VaccinationDrive> findByDriveDate(LocalDate driveDate);

    List<VaccinationDrive> findByDriveDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT v FROM VaccinationDrive v WHERE v.driveDate >= :today AND v.driveDate <= :thirtyDaysLater ORDER BY v.driveDate ASC")
    List<VaccinationDrive> findUpcomingDrives(@Param("today") LocalDate today, @Param("thirtyDaysLater") LocalDate thirtyDaysLater);
    
    @Query("SELECT COUNT(v) > 0 FROM VaccinationDrive v WHERE v.driveDate = :date AND v.applicableGrades IN :grades")
    boolean existsOverlappingDrive(@Param("date") LocalDate date, @Param("grades") List<String> grades);

    Page<VaccinationDrive> findByDriveDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    Page<VaccinationDrive> findByApplicableGradesContaining(String grade, Pageable pageable);
} 