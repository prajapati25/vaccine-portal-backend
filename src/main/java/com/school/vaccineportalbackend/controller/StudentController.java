package com.school.vaccineportalbackend.controller;

import com.school.vaccineportalbackend.dto.ImportResult;
import com.school.vaccineportalbackend.dto.StudentDTO;
import com.school.vaccineportalbackend.model.Student;
import com.school.vaccineportalbackend.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
@Validated
public class StudentController {
    @Autowired
    private StudentService studentService;

    @GetMapping
    public ResponseEntity<Page<StudentDTO>> getAllStudents(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(studentService.getAllStudents(pageRequest));
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<StudentDTO> getStudentById(
            @PathVariable @NotBlank String studentId) {
        return studentService.getStudentById(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/grade/{grade}")
    public ResponseEntity<Page<StudentDTO>> getStudentsByGrade(
            @PathVariable @NotBlank @Pattern(regexp = "^[0-9]{1,2}$", message = "Grade must be a number between 1 and 12") 
            String grade,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(studentService.getStudentsByGrade(grade, pageRequest));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StudentDTO>> searchStudents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String grade,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(studentService.searchStudents(name, grade, pageRequest));
    }

    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(@RequestBody @Valid Student student) {
        return ResponseEntity.ok(studentService.createStudent(student));
    }

    @PutMapping("/{studentId}")
    public ResponseEntity<StudentDTO> updateStudent(
            @PathVariable @NotBlank String studentId,
            @RequestBody @Valid Student student) {
        return ResponseEntity.ok(studentService.updateStudent(studentId, student));
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable @NotBlank String studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResult> importStudents(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            ImportResult result = new ImportResult();
            result.setErrors(List.of("Please select a file to upload"));
            return ResponseEntity.badRequest().body(result);
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            ImportResult result = new ImportResult();
            result.setErrors(List.of("Only CSV files are supported"));
            return ResponseEntity.badRequest().body(result);
        }
        
        ImportResult result = studentService.importStudentsFromCSV(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStudents() {
        byte[] csvData = studentService.exportStudentsToCSV();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "students.csv");
        
        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }
} 