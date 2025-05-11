package com.school.vaccineportalbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VaccinationRecordDTO {
    private Long id;
    
    private String studentId;
    private String studentName;
    private Long driveId;
    private Integer doseNumber;
    private String vaccinationDate;
    private String batchNumber;
    private String administeredBy;
    private String vaccinationSite;
    private String status;
    private String sideEffects;
    private String nextDoseDate;
    private String notes;
    private String createdAt;
    private String updatedAt;
} 