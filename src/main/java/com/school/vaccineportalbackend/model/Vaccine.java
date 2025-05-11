package com.school.vaccineportalbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vaccines")
public class Vaccine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String manufacturer;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer dosesRequired;

    @Column(name = "dose_interval_days")
    private Integer doseInterval;

    @Column(name = "minimum_age_years")
    private Integer minimumAge;

    @Column(name = "maximum_age_years")
    private Integer maximumAge;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Integer daysBetweenDoses;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Integer availableDoses;

    @Column(nullable = false)
    private Double price;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
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