package com.school.vaccineportalbackend.repository;

import com.school.vaccineportalbackend.model.Student;
import com.school.vaccineportalbackend.model.VaccinationDrive;
import com.school.vaccineportalbackend.model.VaccinationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, Long>, JpaSpecificationExecutor<VaccinationRecord> {
    List<VaccinationRecord> findByStudent(Student student);
    List<VaccinationRecord> findByVaccinationDrive(VaccinationDrive drive);
    List<VaccinationRecord> findByStudentAndStatus(Student student, String status);
    List<VaccinationRecord> findByVaccinationDriveAndStatus(VaccinationDrive drive, String status);
    List<VaccinationRecord> findByVaccinationDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<VaccinationRecord> findByDoseNumber(Integer doseNumber);
    Optional<VaccinationRecord> findFirstByStudentAndVaccinationDriveOrderByDoseNumberDesc(Student student, VaccinationDrive drive);
    boolean existsByStudentAndVaccinationDriveAndDoseNumber(Student student, VaccinationDrive drive, Integer doseNumber);
    Page<VaccinationRecord> findByStudent(Student student, Pageable pageable);
    Page<VaccinationRecord> findByVaccinationDrive(VaccinationDrive drive, Pageable pageable);
    Page<VaccinationRecord> findByStudentAndStatus(Student student, String status, Pageable pageable);
    Page<VaccinationRecord> findByVaccinationDriveAndStatus(VaccinationDrive drive, String status, Pageable pageable);
    Page<VaccinationRecord> findByVaccinationDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT vd FROM VaccinationDrive vd WHERE vd.id = :driveId")
    Optional<VaccinationDrive> findVaccinationDriveById(Long driveId);
    
    // New methods for dashboard statistics
    long countByStatus(String status);
    
    @Query("SELECT COUNT(vr) FROM VaccinationRecord vr WHERE vr.nextDoseDate BETWEEN :startDate AND :endDate")
    long countByNextDoseDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(vr) FROM VaccinationRecord vr WHERE vr.nextDoseDate < :date AND vr.status != :status")
    long countByNextDoseDateBeforeAndStatusNot(@Param("date") LocalDateTime date, @Param("status") String status);
    
    List<VaccinationRecord> findByStatus(String status);

    @Query("SELECT vr FROM VaccinationRecord vr WHERE vr.vaccinationDrive.vaccine.id = :vaccineId")
    Page<VaccinationRecord> findByVaccineId(@Param("vaccineId") Long vaccineId, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM VaccinationRecord v WHERE v.vaccinationDrive.vaccine.id = :vaccineId")
    boolean existsByVaccinationDriveVaccineId(@Param("vaccineId") Long vaccineId);
}
