package org.example.greenexproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.HouseType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateHouseholdRequest {

    @NotBlank(message = "Sector is required")
    private String sector;

    @NotBlank(message = "Cell is required")
    private String cell;

    @NotBlank(message = "Village is required")
    private String village;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "House type is required")
    private HouseType houseType;

    private String notes;
}
