package com.school.vaccineportalbackend.repository;

import com.school.vaccineportalbackend.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    List<Student> findByGrade(String grade);
    List<Student> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT s FROM Student s WHERE s.studentId LIKE ?1% ORDER BY s.studentId DESC")
    Optional<Student> findTopByStudentIdStartingWithOrderByStudentIdDesc(String prefix);
    
    Page<Student> findByGrade(String grade, Pageable pageable);
    
    @Query("SELECT s FROM Student s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND (:grade IS NULL OR s.grade = :grade)")
    Page<Student> searchStudents(String name, String grade, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM VaccinationRecord v WHERE v.student.studentId = ?1")
    boolean hasVaccinationRecords(String studentId);

    Page<Student> findByNameContaining(String name, Pageable pageable);
    
    Page<Student> findByNameContainingAndGrade(String name, String grade, Pageable pageable);
} 