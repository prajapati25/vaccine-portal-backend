package com.school.vaccineportalbackend.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.school.vaccineportalbackend.model.VaccinationRecord;
import com.school.vaccineportalbackend.model.VaccinationDrive;
import com.school.vaccineportalbackend.repository.VaccinationRecordRepository;
import com.school.vaccineportalbackend.repository.VaccinationDriveRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportService {
    private static final Logger logger = LogManager.getLogger(ExportService.class);
    
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final VaccinationDriveRepository vaccinationDriveRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public ExportService(VaccinationRecordRepository vaccinationRecordRepository, VaccinationDriveRepository vaccinationDriveRepository) {
        this.vaccinationRecordRepository = vaccinationRecordRepository;
        this.vaccinationDriveRepository = vaccinationDriveRepository;
    }

    @Transactional(readOnly = true)
    public byte[] exportToCsv(Map<String, String> filters) throws IOException {
        logger.info("Exporting vaccination records to CSV with filters: {}", filters);
        
        List<VaccinationRecord> records = getFilteredRecords(filters);
        logger.debug("Found {} records to export", records.size());
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out);
        
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Student ID", "Student Name", "Vaccine", "Date", "Grade", "Status", "Dose Number", "Batch Number", "Administered By"))) {
            
            for (VaccinationRecord record : records) {
                csvPrinter.printRecord(
                    record.getStudent().getStudentId(),
                    record.getStudent().getName(),
                    record.getVaccinationDrive().getVaccine().getName(),
                    record.getVaccinationDate().format(DATE_FORMATTER),
                    record.getStudent().getGrade(),
                    record.getStatus(),
                    record.getDoseNumber(),
                    record.getBatchNumber(),
                    record.getAdministeredBy()
                );
            }
        }
        
        logger.debug("Generated CSV export with {} bytes", out.toByteArray().length);
        return out.toByteArray();
    }

    @Transactional(readOnly = true)
    public byte[] exportToPdf(Map<String, String> filters) throws IOException {
        logger.info("Exporting vaccination records to PDF with filters: {}", filters);
        
        List<VaccinationRecord> records = getFilteredRecords(filters);
        logger.debug("Found {} records to export", records.size());
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Add title
        Paragraph title = new Paragraph("Vaccination Records Report")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(20)
            .setBold();
        document.add(title);

        // Add filters info
        if (!filters.isEmpty()) {
            Paragraph filtersInfo = new Paragraph("Filters Applied:")
                .setFontSize(12)
                .setMarginTop(10);
            document.add(filtersInfo);
            
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    document.add(new Paragraph(entry.getKey() + ": " + entry.getValue())
                        .setFontSize(10)
                        .setMarginLeft(20));
                }
            }
        }

        // Create table
        Table table = new Table(UnitValue.createPercentArray(new float[]{15, 15, 15, 10, 10, 10, 15, 10}))
            .useAllAvailableWidth()
            .setMarginTop(20);

        // Add headers
        String[] headers = {"Student ID", "Student Name", "Vaccine", "Date", "Grade", "Status", "Dose", "Batch"};
        for (String header : headers) {
            table.addHeaderCell(new Cell()
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(header)));
        }

        // Add data rows
        for (VaccinationRecord record : records) {
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(record.getStudent().getStudentId())));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(record.getStudent().getName())));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(record.getVaccinationDrive().getVaccine().getName())));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(record.getVaccinationDate().format(DATE_FORMATTER))));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(record.getStudent().getGrade())));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(record.getStatus())));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(String.valueOf(record.getDoseNumber()))));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(record.getBatchNumber())));
        }

        document.add(table);

        // Add footer with date
        Paragraph footer = new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_FORMATTER))
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10)
            .setMarginTop(20);
        document.add(footer);

        document.close();
        logger.debug("Generated PDF export with {} bytes", out.toByteArray().length);
        return out.toByteArray();
    }

    private List<VaccinationRecord> getFilteredRecords(Map<String, String> filters) {
        logger.debug("Filtering vaccination records with criteria - filters: {}", filters);
        
        List<VaccinationRecord> records = vaccinationRecordRepository.findAll();
        
        return records.stream()
            .filter(record -> {
                boolean matches = true;

                // Filter by date range
                if (filters.containsKey("startDate") && !filters.get("startDate").isEmpty()) {
                    LocalDateTime startDate = LocalDateTime.parse(filters.get("startDate") + "T00:00:00");
                    matches &= !record.getVaccinationDate().isBefore(startDate);
                }
                if (filters.containsKey("endDate") && !filters.get("endDate").isEmpty()) {
                    LocalDateTime endDate = LocalDateTime.parse(filters.get("endDate") + "T23:59:59");
                    matches &= !record.getVaccinationDate().isAfter(endDate);
                }

                // Filter by vaccine
                if (filters.containsKey("vaccineName") && !filters.get("vaccineName").isEmpty()) {
                    matches &= record.getVaccinationDrive().getVaccine().getName()
                        .equalsIgnoreCase(filters.get("vaccineName"));
                }

                // Filter by grade
                if (filters.containsKey("grade") && !filters.get("grade").isEmpty()) {
                    matches &= record.getStudent().getGrade().equals(filters.get("grade"));
                }

                // Filter by status
                if (filters.containsKey("status") && !filters.get("status").isEmpty()) {
                    matches &= record.getStatus().equals(filters.get("status"));
                }

                return matches;
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] exportDrivesToCsv(Map<String, String> filters) throws IOException {
        logger.info("Exporting vaccination drives to CSV with filters: {}", filters);
        
        List<VaccinationDrive> drives = getFilteredDrives(filters);
        logger.debug("Found {} drives to export", drives.size());
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out);
        
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Drive ID", "Vaccine", "Drive Date", "Available Doses", "Applicable Grades", 
                           "Minimum Age", "Maximum Age", "Status", "Batch Number", "Notes"))) {
            
            for (VaccinationDrive drive : drives) {
                csvPrinter.printRecord(
                    drive.getId(),
                    drive.getVaccine().getName(),
                    drive.getDriveDate().format(DATE_FORMATTER),
                    drive.getAvailableDoses(),
                    drive.getApplicableGrades(),
                    drive.getMinimumAge(),
                    drive.getMaximumAge(),
                    drive.getStatus(),
                    drive.getVaccineBatch(),
                    drive.getNotes()
                );
            }
        }
        
        logger.debug("Generated CSV export with {} bytes", out.toByteArray().length);
        return out.toByteArray();
    }

    @Transactional(readOnly = true)
    public byte[] exportDrivesToPdf(Map<String, String> filters) throws IOException {
        logger.info("Exporting vaccination drives to PDF with filters: {}", filters);
        
        List<VaccinationDrive> drives = getFilteredDrives(filters);
        logger.debug("Found {} drives to export", drives.size());
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Add title
        Paragraph title = new Paragraph("Vaccination Drives Report")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(20)
            .setBold();
        document.add(title);

        // Add filters info
        if (!filters.isEmpty()) {
            Paragraph filtersInfo = new Paragraph("Filters Applied:")
                .setFontSize(12)
                .setMarginTop(10);
            document.add(filtersInfo);
            
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    document.add(new Paragraph(entry.getKey() + ": " + entry.getValue())
                        .setFontSize(10)
                        .setMarginLeft(20));
                }
            }
        }

        // Create table
        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 15, 15, 10, 15, 10, 10, 15}))
            .useAllAvailableWidth()
            .setMarginTop(20);

        // Add headers
        String[] headers = {"Drive ID", "Vaccine", "Date", "Doses", "Grades", "Min Age", "Max Age", "Status"};
        for (String header : headers) {
            table.addHeaderCell(new Cell()
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(header)));
        }

        // Add data rows
        for (VaccinationDrive drive : drives) {
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(String.valueOf(drive.getId()))));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(drive.getVaccine().getName())));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(drive.getDriveDate().format(DATE_FORMATTER))));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(String.valueOf(drive.getAvailableDoses()))));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(drive.getApplicableGrades())));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(String.valueOf(drive.getMinimumAge()))));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(String.valueOf(drive.getMaximumAge()))));
            table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(drive.getStatus())));
        }

        document.add(table);

        // Add footer with date
        Paragraph footer = new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_FORMATTER))
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10)
            .setMarginTop(20);
        document.add(footer);

        document.close();
        logger.debug("Generated PDF export with {} bytes", out.toByteArray().length);
        return out.toByteArray();
    }

    private List<VaccinationDrive> getFilteredDrives(Map<String, String> filters) {
        logger.debug("Filtering vaccination drives with criteria - filters: {}", filters);
        
        List<VaccinationDrive> drives = vaccinationDriveRepository.findAll();
        
        return drives.stream()
            .filter(drive -> {
                boolean matches = true;

                // Filter by date range
                if (filters.containsKey("startDate") && !filters.get("startDate").isEmpty()) {
                    LocalDateTime startDate = LocalDateTime.parse(filters.get("startDate") + "T00:00:00");
                    LocalDate startDateLocal = startDate.toLocalDate();
                    matches &= !drive.getDriveDate().isBefore(startDateLocal);
                }
                if (filters.containsKey("endDate") && !filters.get("endDate").isEmpty()) {
                    LocalDateTime endDate = LocalDateTime.parse(filters.get("endDate") + "T23:59:59");
                    LocalDate endDateLocal = endDate.toLocalDate();
                    matches &= !drive.getDriveDate().isAfter(endDateLocal);
                }

                // Filter by status
                if (filters.containsKey("status") && !filters.get("status").isEmpty()) {
                    matches &= drive.getStatus().equals(filters.get("status"));
                }

                // Filter by grade
                if (filters.containsKey("grade") && !filters.get("grade").isEmpty()) {
                    matches &= drive.getApplicableGrades().contains(filters.get("grade"));
                }

                return matches;
            })
            .collect(Collectors.toList());
    }

    public ResponseEntity<byte[]> exportVaccinationRecords(
            String vaccineName, String status, String grade,
            LocalDate startDate, LocalDate endDate, String format) {
        logger.info("Exporting vaccination records with filters - vaccineName: {}, status: {}, grade: {}, startDate: {}, endDate: {}, format: {}", 
            vaccineName, status, grade, startDate, endDate, format);
        
        try {
            List<VaccinationRecord> records = getFilteredRecords(vaccineName, status, grade, startDate, endDate);
            logger.debug("Found {} records to export", records.size());
            
            byte[] content;
            String filename;
            String contentType;
            
            if ("pdf".equalsIgnoreCase(format)) {
                content = exportToPdf(Map.of("vaccineName", vaccineName, "status", status, "grade", grade, "startDate", startDate.toString(), "endDate", endDate.toString()));
                filename = "vaccination_records.pdf";
                contentType = "application/pdf";
                logger.debug("Generated PDF export with {} bytes", content.length);
            } else {
                content = exportToCsv(Map.of("vaccineName", vaccineName, "status", status, "grade", grade, "startDate", startDate.toString(), "endDate", endDate.toString()));
                filename = "vaccination_records.csv";
                contentType = "text/csv";
                logger.debug("Generated CSV export with {} bytes", content.length);
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content);
        } catch (Exception e) {
            logger.error("Error exporting vaccination records", e);
            throw new RuntimeException("Error exporting vaccination records", e);
        }
    }

    public ResponseEntity<byte[]> exportVaccinationDrives(
            String status, String grade, LocalDate startDate, LocalDate endDate, String format) {
        logger.info("Exporting vaccination drives with filters - status: {}, grade: {}, startDate: {}, endDate: {}, format: {}", 
            status, grade, startDate, endDate, format);
        
        try {
            List<VaccinationDrive> drives = getFilteredDrives(Map.of("status", status, "grade", grade, "startDate", startDate.toString(), "endDate", endDate.toString()));
            logger.debug("Found {} drives to export", drives.size());
            
            byte[] content;
            String filename;
            String contentType;
            
            if ("pdf".equalsIgnoreCase(format)) {
                content = exportDrivesToPdf(Map.of("status", status, "grade", grade, "startDate", startDate.toString(), "endDate", endDate.toString()));
                filename = "vaccination_drives.pdf";
                contentType = "application/pdf";
                logger.debug("Generated PDF export with {} bytes", content.length);
            } else {
                content = exportDrivesToCsv(Map.of("status", status, "grade", grade, "startDate", startDate.toString(), "endDate", endDate.toString()));
                filename = "vaccination_drives.csv";
                contentType = "text/csv";
                logger.debug("Generated CSV export with {} bytes", content.length);
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content);
        } catch (Exception e) {
            logger.error("Error exporting vaccination drives", e);
            throw new RuntimeException("Error exporting vaccination drives", e);
        }
    }

    private List<VaccinationRecord> getFilteredRecords(
            String vaccineName, String status, String grade,
            LocalDate startDate, LocalDate endDate) {
        logger.debug("Filtering vaccination records with criteria - vaccineName: {}, status: {}, grade: {}, startDate: {}, endDate: {}", 
            vaccineName, status, grade, startDate, endDate);
        
        List<VaccinationRecord> records = vaccinationRecordRepository.findAll();
        
        return records.stream()
                .filter(record -> vaccineName == null || 
                        record.getVaccinationDrive().getVaccine().getName().equalsIgnoreCase(vaccineName))
                .filter(record -> status == null || 
                        record.getStatus().equalsIgnoreCase(status))
                .filter(record -> grade == null || 
                        record.getStudent().getGrade().equals(grade))
                .filter(record -> startDate == null || 
                        record.getVaccinationDate().toLocalDate().isAfter(startDate.minusDays(1)))
                .filter(record -> endDate == null || 
                        record.getVaccinationDate().toLocalDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());
    }

    private List<VaccinationDrive> getFilteredDrives(
            String status, String grade, LocalDate startDate, LocalDate endDate) {
        logger.debug("Filtering vaccination drives with criteria - status: {}, grade: {}, startDate: {}, endDate: {}", 
            status, grade, startDate, endDate);
        
        List<VaccinationDrive> drives = vaccinationDriveRepository.findAll();
        
        return drives.stream()
                .filter(drive -> status == null || 
                        drive.getStatus().equalsIgnoreCase(status))
                .filter(drive -> grade == null || 
                        drive.getApplicableGrades().contains(grade))
                .filter(drive -> startDate == null || 
                        drive.getDriveDate().isAfter(startDate.minusDays(1)))
                .filter(drive -> endDate == null || 
                        drive.getDriveDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());
    }

    private byte[] generatePdf(List<?> data) throws IOException {
        logger.debug("Generating PDF export for {} items", data.size());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Add title
        Paragraph title = new Paragraph("Vaccination Records Report")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(20)
            .setBold();
        document.add(title);

        // Add filters info
        if (!data.isEmpty()) {
            Paragraph filtersInfo = new Paragraph("Filters Applied:")
                .setFontSize(12)
                .setMarginTop(10);
            document.add(filtersInfo);
            
            for (Object item : data) {
                if (item instanceof Map) {
                    Map<?, ?> itemMap = (Map<?, ?>) item;
                    for (Map.Entry<?, ?> entry : itemMap.entrySet()) {
                        if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
                            document.add(new Paragraph(entry.getKey().toString() + ": " + entry.getValue())
                                .setFontSize(10)
                                .setMarginLeft(20));
                        }
                    }
                }
            }
        }

        // Create table
        Table table = new Table(UnitValue.createPercentArray(new float[]{15, 15, 15, 10, 10, 10, 15, 10}))
            .useAllAvailableWidth()
            .setMarginTop(20);

        // Add headers
        String[] headers = {"Student ID", "Student Name", "Vaccine", "Date", "Grade", "Status", "Dose", "Batch"};
        for (String header : headers) {
            table.addHeaderCell(new Cell()
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(header)));
        }

        // Add data rows
        for (Object item : data) {
            if (item instanceof VaccinationRecord) {
                VaccinationRecord record = (VaccinationRecord) item;
                table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(record.getStudent().getStudentId())));
                table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(record.getStudent().getName())));
                table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(record.getVaccinationDrive().getVaccine().getName())));
                table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(record.getVaccinationDate().format(DATE_FORMATTER))));
                table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(record.getStudent().getGrade())));
                table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(record.getStatus())));
                table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(String.valueOf(record.getDoseNumber()))));
                table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(record.getBatchNumber())));
            }
        }

        document.add(table);

        // Add footer with date
        Paragraph footer = new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_FORMATTER))
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10)
            .setMarginTop(20);
        document.add(footer);

        document.close();
        logger.debug("Generated PDF export with {} bytes", out.toByteArray().length);
        return out.toByteArray();
    }

    private byte[] generateCsv(List<?> data) throws IOException {
        logger.debug("Generating CSV export for {} items", data.size());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out);
        
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Student ID", "Student Name", "Vaccine", "Date", "Grade", "Status", "Dose Number", "Batch Number", "Administered By"))) {
            
            for (Object item : data) {
                if (item instanceof VaccinationRecord) {
                    VaccinationRecord record = (VaccinationRecord) item;
                    csvPrinter.printRecord(
                        record.getStudent().getStudentId(),
                        record.getStudent().getName(),
                        record.getVaccinationDrive().getVaccine().getName(),
                        record.getVaccinationDate().format(DATE_FORMATTER),
                        record.getStudent().getGrade(),
                        record.getStatus(),
                        record.getDoseNumber(),
                        record.getBatchNumber(),
                        record.getAdministeredBy()
                    );
                }
            }
        }
        
        logger.debug("Generated CSV export with {} bytes", out.toByteArray().length);
        return out.toByteArray();
    }
} 