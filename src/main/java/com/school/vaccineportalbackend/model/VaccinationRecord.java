package com.school.vaccineportalbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "vaccination_records")
public class VaccinationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "studentId", nullable = false)
    private Student student;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "vaccination_drive_id", nullable = false)
    private VaccinationDrive vaccinationDrive;

    @Column(nullable = false)
    private Integer doseNumber;

    @Column(nullable = false)
    private LocalDateTime vaccinationDate;

    @Column
    private String batchNumber;

    @Column
    private String administeredBy;

    @Column
    private String vaccinationSite;

    @Column(nullable = false)
    private String status;

    @Column
    private String sideEffects;

    @Column
    private LocalDateTime nextDoseDate;

    @Column
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}