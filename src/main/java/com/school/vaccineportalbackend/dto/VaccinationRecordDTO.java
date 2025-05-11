package com.school.vaccineportalbackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class VaccinationRecordDTO {
    private Long id;
    private String studentId;
    private String studentName;
    private Long vaccinationDriveId;
    private Integer doseNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String vaccinationDate;
    
    private String batchNumber;
    private String administeredBy;
    private String vaccinationSite;
    private String status;
    private String sideEffects;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String nextDoseDate;
    
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String updatedAt;
} 