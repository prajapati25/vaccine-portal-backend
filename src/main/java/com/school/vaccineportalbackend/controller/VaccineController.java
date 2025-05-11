package com.school.vaccineportalbackend.controller;

import com.school.vaccineportalbackend.model.Vaccine;
import com.school.vaccineportalbackend.service.VaccineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vaccines")
@CrossOrigin(origins = "*")
public class VaccineController {
    private final VaccineService vaccineService;

    @Autowired
    public VaccineController(VaccineService vaccineService) {
        this.vaccineService = vaccineService;
    }

    @GetMapping
    public ResponseEntity<List<Vaccine>> getAllVaccines() {
        return ResponseEntity.ok(vaccineService.getAllVaccines());
    }
} 