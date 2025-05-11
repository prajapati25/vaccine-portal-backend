package com.school.vaccineportalbackend.dto;

import com.school.vaccineportalbackend.model.Student;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentDTO {
    private String studentId;
    private String name;
    private String grade;
    private LocalDate dateOfBirth;
    private String gender;
    private String parentName;
    private String parentEmail;
    private String contactNumber;
    private String address;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Add a field to indicate if the student has vaccination records
    private boolean hasVaccinationRecords;
    
    // Constructor to convert from Student entity
    public static StudentDTO fromEntity(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setStudentId(student.getStudentId());
        dto.setName(student.getName());
        dto.setGrade(student.getGrade());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setGender(student.getGender());
        dto.setParentName(student.getParentName());
        dto.setParentEmail(student.getParentEmail());
        dto.setContactNumber(student.getContactNumber());
        dto.setAddress(student.getAddress());
        dto.setActive(student.isActive());
        dto.setCreatedAt(student.getCreatedAt());
        dto.setUpdatedAt(student.getUpdatedAt());
        
        // Don't access the collection directly to avoid lazy loading
        // Instead, set a default value or use a repository to check if records exist
        dto.setHasVaccinationRecords(false);
        
        return dto;
    }
} 