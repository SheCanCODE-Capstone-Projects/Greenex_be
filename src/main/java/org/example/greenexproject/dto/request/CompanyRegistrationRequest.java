package org.example.greenexproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyRegistrationRequest {

    @NotBlank
    private String companyName;

    @NotBlank
    private String registrationNumber;

    @NotBlank
    private String email;

    @NotBlank
    private String phone;

    @NotBlank
    private String address;

    @NotBlank
    private String managerName;

    @NotBlank
    private String managerEmail;

    @NotBlank
    private String managerPhone;

    @NotBlank
    private String password;
}