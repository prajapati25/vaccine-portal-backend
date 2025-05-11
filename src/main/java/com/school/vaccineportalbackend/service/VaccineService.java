package com.school.vaccineportalbackend.service;

import com.school.vaccineportalbackend.model.Vaccine;
import com.school.vaccineportalbackend.repository.VaccineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VaccineService {
    private final VaccineRepository vaccineRepository;

    @Autowired
    public VaccineService(VaccineRepository vaccineRepository) {
        this.vaccineRepository = vaccineRepository;
    }

    @Transactional(readOnly = true)
    public List<Vaccine> getAllVaccines() {
        return vaccineRepository.findAll();
    }
} 