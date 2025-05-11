package com.school.vaccineportalbackend.controller;

import com.school.vaccineportalbackend.model.VaccinationRecord;
import com.school.vaccineportalbackend.model.Vaccine;
import com.school.vaccineportalbackend.service.VaccinationRecordService;
import com.school.vaccineportalbackend.service.VaccineService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vaccines")
@CrossOrigin(origins = "*")
public class VaccineController {
    private static final Logger logger = LogManager.getLogger(VaccineController.class);
    
    private final VaccineService vaccineService;
    private final VaccinationRecordService vaccinationRecordService;

    @Autowired
    public VaccineController(VaccineService vaccineService, VaccinationRecordService vaccinationRecordService) {
        this.vaccineService = vaccineService;
        this.vaccinationRecordService = vaccinationRecordService;
    }

    @GetMapping
    public ResponseEntity<List<Vaccine>> getAllVaccines() {
        logger.info("Getting all vaccines");
        
        List<Vaccine> vaccines = vaccineService.getAllVaccines();
        logger.debug("Found {} vaccines", vaccines.size());
        
        return ResponseEntity.ok(vaccines);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vaccine> getVaccineById(@PathVariable Long id) {
        logger.info("Getting vaccine by ID: {}", id);
        
        Vaccine vaccine = vaccineService.getVaccineById(id);
        logger.debug("Found vaccine: {}", vaccine.getName());
        
        return ResponseEntity.ok(vaccine);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Vaccine> getVaccineByName(@PathVariable String name) {
        logger.info("Getting vaccine by name: {}", name);
        
        Vaccine vaccine = vaccineService.getVaccineByName(name);
        logger.debug("Found vaccine with ID: {}", vaccine.getId());
        
        return ResponseEntity.ok(vaccine);
    }

    @PostMapping
    public ResponseEntity<Vaccine> createVaccine(@RequestBody Vaccine vaccine) {
        logger.info("Creating new vaccine: {}", vaccine.getName());
        
        Vaccine createdVaccine = vaccineService.createVaccine(vaccine);
        logger.debug("Created vaccine with ID: {}", createdVaccine.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVaccine);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vaccine> updateVaccine(
            @PathVariable Long id,
            @RequestBody Vaccine vaccine) {
        logger.info("Updating vaccine with ID: {}", id);
        
        Vaccine updatedVaccine = vaccineService.updateVaccine(id, vaccine);
        logger.debug("Updated vaccine: {}", updatedVaccine.getName());
        
        return ResponseEntity.ok(updatedVaccine);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVaccine(@PathVariable Long id) {
        logger.info("Deleting vaccine with ID: {}", id);
        
        vaccineService.deleteVaccine(id);
        logger.debug("Successfully deleted vaccine with ID: {}", id);
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{vaccineId}/records")
    public ResponseEntity<Page<VaccinationRecord>> getVaccineRecords(
            @PathVariable Long vaccineId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Getting vaccination records for vaccine ID: {} with pagination - page: {}, size: {}", 
            vaccineId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VaccinationRecord> records = vaccineService.getRecordsByVaccine(vaccineId, pageable);
        logger.debug("Found {} vaccination records for vaccine ID: {}", records.getTotalElements(), vaccineId);
        
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{vaccineId}/statistics")
    public ResponseEntity<Map<String, Object>> getVaccineStatistics(@PathVariable Long vaccineId) {
        logger.info("Getting statistics for vaccine ID: {}", vaccineId);
        
        Map<String, Object> statistics = vaccineService.getVaccineStatistics(vaccineId);
        logger.debug("Retrieved statistics for vaccine ID: {} - statistics: {}", vaccineId, statistics);
        
        return ResponseEntity.ok(statistics);
    }
} 