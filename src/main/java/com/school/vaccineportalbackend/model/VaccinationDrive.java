package com.school.vaccineportalbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "vaccination_drives")
public class VaccinationDrive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    @Column
    private String vaccineBatch;

    @Column(nullable = false)
    private LocalDate driveDate;

    @Column(nullable = false)
    private Integer availableDoses;

    @Column(nullable = false)
    private String applicableGrades;

    @Column
    private Integer minimumAge;

    @Column
    private Integer maximumAge;

    @Column(nullable = false)
    private String status = "SCHEDULED";

    @Column(nullable = false)
    private boolean isActive = true;

    @JsonIgnore
    @OneToMany(mappedBy = "vaccinationDrive", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<VaccinationRecord> vaccinationRecords = new HashSet<>();

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