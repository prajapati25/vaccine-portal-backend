package com.school.vaccineportalbackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class ImportResult {
    private int total;
    private int imported;
    private List<String> errors;
    private boolean success;
    
    public boolean isSuccess() {
        return errors.isEmpty() && imported > 0;
    }
} 