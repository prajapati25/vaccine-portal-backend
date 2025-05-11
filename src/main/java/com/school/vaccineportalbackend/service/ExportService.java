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
import com.school.vaccineportalbackend.repository.VaccinationRecordRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportService {
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public ExportService(VaccinationRecordRepository vaccinationRecordRepository) {
        this.vaccinationRecordRepository = vaccinationRecordRepository;
    }

    @Transactional(readOnly = true)
    public byte[] exportToCsv(Map<String, String> filters) throws IOException {
        List<VaccinationRecord> records = getFilteredRecords(filters);
        
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
        
        return out.toByteArray();
    }

    @Transactional(readOnly = true)
    public byte[] exportToPdf(Map<String, String> filters) throws IOException {
        List<VaccinationRecord> records = getFilteredRecords(filters);
        
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
        return out.toByteArray();
    }

    private List<VaccinationRecord> getFilteredRecords(Map<String, String> filters) {
        // Start with all records
        List<VaccinationRecord> records = vaccinationRecordRepository.findAll();

        // Apply filters
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
} 