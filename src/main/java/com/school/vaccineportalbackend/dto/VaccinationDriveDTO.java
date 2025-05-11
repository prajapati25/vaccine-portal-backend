package com.school.vaccineportalbackend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class VaccinationDriveDTO {
    private Long id;
    private VaccineDTO vaccine;
    private String vaccineBatch;
    private LocalDate driveDate;
    private Integer availableDoses;
    private String applicableGrades;
    private Integer minimumAge;
    private Integer maximumAge;
    private String status;
    private boolean isActive;
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String updatedAt;
    
    private List<VaccinationRecordDTO> vaccinationRecords;

    @Data
    public static class VaccineDTO {
        private Long id;
        private String name;
    }
} 