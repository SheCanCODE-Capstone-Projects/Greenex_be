package org.example.greenexproject.model.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRegistrationRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Company registration number is required")
    private String registrationNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    private String city;
    private String country;

    @NotBlank(message = "Manager full name is required")
    private String managerFullName;

    @NotBlank(message = "Manager email is required")
    @Email(message = "Invalid manager email format")
    private String managerEmail;

    @NotBlank(message = "Manager phone is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Invalid manager phone number")
    private String managerPhone;

    private String companyType; // e.g., "WASTE_COLLECTION", "RECYCLING"
    private String description;

    @NotBlank(message = "Password is required")
    private String password;

    private String confirmPassword;
}