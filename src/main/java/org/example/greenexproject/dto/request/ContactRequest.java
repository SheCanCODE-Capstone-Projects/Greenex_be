
package org.example.greenexproject.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactRequest {
    @NotBlank
    @Size(max = 200)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 254)
    private String email;

    @Size(max = 50)
    private String phone;

    @Size(max = 100)
    private String serviceInterest;

    @NotBlank
    @Size(max = 2000)
    private String message;
}
