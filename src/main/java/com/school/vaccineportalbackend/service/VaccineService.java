package com.school.vaccineportalbackend.service;

import com.school.vaccineportalbackend.model.Vaccine;
import com.school.vaccineportalbackend.model.VaccinationRecord;
import com.school.vaccineportalbackend.repository.VaccineRepository;
import com.school.vaccineportalbackend.repository.VaccinationRecordRepository;
import com.school.vaccineportalbackend.exception.ResourceNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class VaccineService {
    private static final Logger logger = LogManager.getLogger(VaccineService.class);
    
    private final VaccineRepository vaccineRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;

    @Autowired
    public VaccineService(VaccineRepository vaccineRepository, 
                         VaccinationRecordRepository vaccinationRecordRepository) {
        this.vaccineRepository = vaccineRepository;
        this.vaccinationRecordRepository = vaccinationRecordRepository;
    }

    @Transactional(readOnly = true)
    public List<Vaccine> getAllVaccines() {
        logger.info("Getting all vaccines");
        List<Vaccine> vaccines = vaccineRepository.findAll();
        logger.debug("Found {} vaccines", vaccines.size());
        return vaccines;
    }

    public Vaccine getVaccineById(Long id) {
        logger.info("Getting vaccine by ID: {}", id);
        Vaccine vaccine = vaccineRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Vaccine not found with ID: {}", id);
                return new ResourceNotFoundException("Vaccine not found");
            });
        logger.debug("Found vaccine: {}", vaccine.getName());
        return vaccine;
    }

    public Vaccine getVaccineByName(String name) {
        logger.info("Getting vaccine by name: {}", name);
        Vaccine vaccine = vaccineRepository.findByName(name)
            .orElseThrow(() -> {
                logger.error("Vaccine not found with name: {}", name);
                return new ResourceNotFoundException("Vaccine not found");
            });
        logger.debug("Found vaccine with ID: {}", vaccine.getId());
        return vaccine;
    }

    public Vaccine createVaccine(Vaccine vaccine) {
        logger.info("Creating new vaccine: {}", vaccine.getName());
        
        if (vaccineRepository.existsByName(vaccine.getName())) {
            logger.error("Vaccine already exists with name: {}", vaccine.getName());
            throw new IllegalArgumentException("Vaccine with this name already exists");
        }
        
        Vaccine savedVaccine = vaccineRepository.save(vaccine);
        logger.debug("Created vaccine with ID: {}", savedVaccine.getId());
        
        return savedVaccine;
    }

    public Vaccine updateVaccine(Long id, Vaccine vaccine) {
        logger.info("Updating vaccine with ID: {}", id);
        
        Vaccine existingVaccine = vaccineRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Vaccine not found with ID: {}", id);
                return new ResourceNotFoundException("Vaccine not found");
            });
        
        // Check if name is being changed and if it already exists
        if (!existingVaccine.getName().equals(vaccine.getName()) && 
            vaccineRepository.existsByName(vaccine.getName())) {
            logger.error("Vaccine already exists with name: {}", vaccine.getName());
            throw new IllegalArgumentException("Vaccine with this name already exists");
        }
        
        existingVaccine.setName(vaccine.getName());
        existingVaccine.setDescription(vaccine.getDescription());
        existingVaccine.setDosesRequired(vaccine.getDosesRequired());
        existingVaccine.setDaysBetweenDoses(vaccine.getDaysBetweenDoses());
        existingVaccine.setManufacturer(vaccine.getManufacturer());
        existingVaccine.setExpiryDate(vaccine.getExpiryDate());
        existingVaccine.setAvailableDoses(vaccine.getAvailableDoses());
        existingVaccine.setPrice(vaccine.getPrice());
        
        Vaccine updatedVaccine = vaccineRepository.save(existingVaccine);
        logger.debug("Updated vaccine: {}", updatedVaccine.getName());
        
        return updatedVaccine;
    }

    public void deleteVaccine(Long id) {
        logger.info("Deleting vaccine with ID: {}", id);
        
        if (!vaccineRepository.existsById(id)) {
            logger.error("Vaccine not found with ID: {}", id);
            throw new ResourceNotFoundException("Vaccine not found");
        }
        
        // Check if vaccine is being used in any vaccination records
        if (vaccinationRecordRepository.existsByVaccinationDriveVaccineId(id)) {
            logger.error("Cannot delete vaccine with ID: {} as it is being used in vaccination records", id);
            throw new IllegalStateException("Cannot delete vaccine that is being used in vaccination records");
        }
        
        vaccineRepository.deleteById(id);
        logger.debug("Successfully deleted vaccine with ID: {}", id);
    }

    public Page<VaccinationRecord> getRecordsByVaccine(Long vaccineId, Pageable pageable) {
        logger.info("Getting vaccination records for vaccine ID: {} with pagination", vaccineId);
        Page<VaccinationRecord> records = vaccinationRecordRepository.findByVaccineId(vaccineId, pageable);
        logger.debug("Found {} vaccination records for vaccine ID: {}", records.getTotalElements(), vaccineId);
        return records;
    }

    public Map<String, Object> getVaccineStatistics(Long vaccineId) {
        logger.info("Getting statistics for vaccine ID: {}", vaccineId);
        
        Map<String, Object> statistics = new HashMap<>();
        // ... populate statistics map ...
        
        logger.debug("Retrieved statistics for vaccine ID: {} - statistics: {}", vaccineId, statistics);
        return statistics;
    }
} 