package org.example.greenexproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private String periodMonth;
    private BigDecimal amount;
    private LocalDate dueDate;
    private PaymentStatus status;
    private String transactionReference;
    private LocalDateTime paidAt;

    // Household info
    private UUID householdId;
    private String householdCode;
    private String householdAddress;

    // Citizen info
    private UUID citizenUserId;
    private String citizenName;
    private String citizenPhone;

    // Company info
    private UUID companyId;
    private String companyName;

    private LocalDateTime createdAt;
}
