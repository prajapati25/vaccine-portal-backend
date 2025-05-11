package com.school.vaccineportalbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/grades")
@CrossOrigin(origins = "*")
public class GradeController {

    @GetMapping
    public ResponseEntity<List<String>> getAllGrades() {
        // Generate grades from 1 to 12
        List<String> grades = IntStream.rangeClosed(1, 12)
                .mapToObj(grade -> "Grade " + grade)
                .collect(Collectors.toList());
        return ResponseEntity.ok(grades);
    }
} 