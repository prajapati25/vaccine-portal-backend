package com.school.vaccineportalbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Schema(description = "Login credentials for authentication")
public class AuthRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username for login", example = "admin")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password for login", example = "admin123")
    private String password;
}
