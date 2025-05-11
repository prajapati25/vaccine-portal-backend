package com.school.vaccineportalbackend.service;

import com.school.vaccineportalbackend.dto.ImportResult;
import com.school.vaccineportalbackend.model.Student;
import com.school.vaccineportalbackend.repository.StudentRepository;
import com.school.vaccineportalbackend.dto.StudentDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {
    private static final Logger logger = LogManager.getLogger(StudentService.class);

    @Autowired
    private StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public Page<StudentDTO> getAllStudents(Pageable pageable) {
        logger.info("Getting all students with pagination");
        Page<Student> students = studentRepository.findAll(pageable);
        logger.debug("Found {} students", students.getTotalElements());
        return students.map(student -> {
            StudentDTO dto = StudentDTO.fromEntity(student);
            dto.setHasVaccinationRecords(studentRepository.hasVaccinationRecords(student.getStudentId()));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Optional<StudentDTO> getStudentById(String studentId) {
        logger.info("Getting student by ID: {}", studentId);
        return studentRepository.findById(studentId)
                .map(student -> {
                    StudentDTO dto = StudentDTO.fromEntity(student);
                    dto.setHasVaccinationRecords(studentRepository.hasVaccinationRecords(studentId));
                    logger.debug("Found student: {}", student.getName());
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public Page<StudentDTO> getStudentsByGrade(String grade, Pageable pageable) {
        logger.info("Getting students by grade: {}", grade);
        Page<Student> students = studentRepository.findByGrade(grade, pageable);
        logger.debug("Found {} students in grade: {}", students.getTotalElements(), grade);
        return students.map(student -> {
            StudentDTO dto = StudentDTO.fromEntity(student);
            dto.setHasVaccinationRecords(studentRepository.hasVaccinationRecords(student.getStudentId()));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<StudentDTO> searchStudents(String name, String grade, Pageable pageable) {
        logger.info("Searching students with name: {} and grade: {}", name, grade);
        Page<Student> students;
        
        if (name != null && grade != null) {
            students = studentRepository.findByNameContainingAndGrade(name, grade, pageable);
        } else if (name != null) {
            students = studentRepository.findByNameContaining(name, pageable);
        } else if (grade != null) {
            students = studentRepository.findByGrade(grade, pageable);
        } else {
            students = studentRepository.findAll(pageable);
        }
        
        logger.debug("Found {} students matching search criteria", students.getTotalElements());
        return students.map(student -> {
            StudentDTO dto = StudentDTO.fromEntity(student);
            dto.setHasVaccinationRecords(studentRepository.hasVaccinationRecords(student.getStudentId()));
            return dto;
        });
    }

    private String generateRollNumber() {
        int currentYear = Year.now().getValue();
        String yearPrefix = String.valueOf(currentYear);
        
        // Find the last roll number for the current year
        String lastRollNumber = studentRepository.findTopByStudentIdStartingWithOrderByStudentIdDesc("ROLL-" + yearPrefix)
                .map(Student::getStudentId)
                .orElse("ROLL-" + yearPrefix + "-0000");
        
        // Extract the sequence number and increment it
        int sequence = 0;
        if (lastRollNumber.length() >= 4) {
            try {
                sequence = Integer.parseInt(lastRollNumber.substring(lastRollNumber.length() - 4));
            } catch (NumberFormatException e) {
                // If parsing fails, start from 1
                sequence = 0;
            }
        }
        
        // Generate new roll number
        return String.format("ROLL-%s-%04d", yearPrefix, sequence + 1);
    }

    @Transactional
    public StudentDTO createStudent(Student student) {
        logger.info("Creating new student: {}", student.getName());
        
        // Generate a new roll number
        String rollNumber = generateRollNumber();
        student.setStudentId(rollNumber);
        
        Student savedStudent = studentRepository.save(student);
        logger.debug("Created student with ID: {}", savedStudent.getStudentId());
        
        StudentDTO dto = StudentDTO.fromEntity(savedStudent);
        dto.setHasVaccinationRecords(false); // New student won't have records yet
        return dto;
    }

    @Transactional
    public StudentDTO updateStudent(String studentId, Student student) {
        logger.info("Updating student with ID: {}", studentId);
        
        Student existingStudent = studentRepository.findById(studentId)
            .orElseThrow(() -> {
                logger.error("Student not found with ID: {}", studentId);
                return new RuntimeException("Student not found");
            });
        
        // Preserve the created_at field
        student.setCreatedAt(existingStudent.getCreatedAt());
        
        // Set the studentId
        student.setStudentId(studentId);
        
        // Save the updated student
        Student savedStudent = studentRepository.save(student);
        logger.debug("Updated student: {}", savedStudent.getName());
        
        // Create and return the DTO
        StudentDTO dto = StudentDTO.fromEntity(savedStudent);
        dto.setHasVaccinationRecords(studentRepository.hasVaccinationRecords(studentId));
        return dto;
    }

    @Transactional
    public void deleteStudent(String studentId) {
        logger.info("Deleting student with ID: {}", studentId);
        
        if (!studentRepository.existsById(studentId)) {
            logger.error("Student not found with ID: {}", studentId);
            throw new RuntimeException("Student not found");
        }
        
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        student.setActive(false);
        studentRepository.save(student);
        logger.debug("Successfully deleted student with ID: {}", studentId);
    }

    @Transactional
    public ImportResult importStudentsFromCSV(MultipartFile file) {
        logger.info("Importing students from CSV file: {}", file.getOriginalFilename());
        
        ImportResult result = new ImportResult();
        result.setTotal(0);
        result.setImported(0);
        result.setErrors(new ArrayList<>());
        
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.getErrors().add("Empty file");
                return result;
            }
            
            String[] headers = headerLine.split(",");
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim().toLowerCase(), i);
            }
            
            // Validate required headers
            String[] requiredHeaders = {"name", "grade", "dateofbirth", "gender"};
            for (String required : requiredHeaders) {
                if (!headerMap.containsKey(required)) {
                    result.getErrors().add("Missing required header: " + required);
                    return result;
                }
            }
            
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                result.setTotal(result.getTotal() + 1);
                
                try {
                    String[] values = line.split(",");
                    if (values.length < headers.length) {
                        result.getErrors().add("Line " + lineNumber + ": Invalid number of columns");
                        continue;
                    }
                    
                    Student student = new Student();
                    student.setName(values[headerMap.get("name")].trim());
                    student.setGrade(values[headerMap.get("grade")].trim());
                    student.setDateOfBirth(LocalDate.parse(values[headerMap.get("dateofbirth")].trim()));
                    student.setGender(values[headerMap.get("gender")].trim());
                    
                    // Optional fields
                    if (headerMap.containsKey("parentname")) {
                        student.setParentName(values[headerMap.get("parentname")].trim());
                    }
                    if (headerMap.containsKey("parentemail")) {
                        student.setParentEmail(values[headerMap.get("parentemail")].trim());
                    }
                    if (headerMap.containsKey("contactnumber")) {
                        student.setContactNumber(values[headerMap.get("contactnumber")].trim());
                    }
                    if (headerMap.containsKey("address")) {
                        student.setAddress(values[headerMap.get("address")].trim());
                    }
                    
                    // Generate student ID
                    student.setStudentId(generateRollNumber());
                    student.setActive(true);
                    
                    // Save student
                    studentRepository.save(student);
                    result.setImported(result.getImported() + 1);
                    
                } catch (Exception e) {
                    result.getErrors().add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
            
            logger.debug("Successfully imported {} students", result.getImported());
        } catch (Exception e) {
            logger.error("Error importing students from CSV", e);
            result.getErrors().add("Failed to process file: " + e.getMessage());
        }
        
        return result;
    }

    @Transactional(readOnly = true)
    public byte[] exportStudentsToCSV() {
        logger.info("Exporting students to CSV");
        
        try {
            List<Student> students = studentRepository.findAll();
            
            StringBuilder csv = new StringBuilder();
            // Add headers
            csv.append("Student ID,Name,Grade,Date of Birth,Gender,Parent Name,Parent Email,Contact Number,Address\n");
            
            // Add data rows
            for (Student student : students) {
                csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    escapeCsvField(student.getStudentId()),
                    escapeCsvField(student.getName()),
                    escapeCsvField(student.getGrade()),
                    student.getDateOfBirth(),
                    escapeCsvField(student.getGender()),
                    escapeCsvField(student.getParentName()),
                    escapeCsvField(student.getParentEmail()),
                    escapeCsvField(student.getContactNumber()),
                    escapeCsvField(student.getAddress())
                ));
            }
            
            logger.debug("Successfully exported {} students to CSV", studentRepository.count());
            return csv.toString().getBytes();
        } catch (Exception e) {
            logger.error("Error exporting students to CSV", e);
            throw new RuntimeException("Error exporting students to CSV", e);
        }
    }
    
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
} 