package org.example.greenexproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateZoneRequest {
    @NotBlank(message = "Sector is required")
    @Size(max = 100, message = "Sector must not exceed 100 characters")
    private String sector;

    @NotBlank(message = "Cell is required")
    @Size(max = 100, message = "Cell must not exceed 100 characters")
    private String cell;

    @NotBlank(message = "Village is required")
    @Size(max = 100, message = "Village must not exceed 100 characters")
    private String village;

    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

}
