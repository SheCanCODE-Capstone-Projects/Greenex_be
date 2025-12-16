
package org.example.greenexproject.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String serviceInterest;
    private String message;
    private LocalDateTime createdAt;
    private boolean processed;
}
