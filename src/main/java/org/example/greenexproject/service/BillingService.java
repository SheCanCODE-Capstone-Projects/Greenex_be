package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.GenerateBillingRequest;
import org.example.greenexproject.dto.response.BillingGenerationResult;
import org.example.greenexproject.dto.response.PaymentResponse;
import org.example.greenexproject.exception.BadRequestException;
import org.example.greenexproject.exception.ResourceNotFoundException;
import org.example.greenexproject.model.entity.*;
import org.example.greenexproject.model.enums.NotificationType;
import org.example.greenexproject.model.enums.PaymentStatus;
import org.example.greenexproject.model.enums.UserStatus;
import org.example.greenexproject.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {
    private final PaymentRepository paymentRepository;
    private final HouseholdRepository householdRepository;
    private final TariffRuleRepository tariffRuleRepository;
    private final WasteCompanyRepository wasteCompanyRepository;
    private final CitizenAccountRepository citizenAccountRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public BillingGenerationResult generateMonthlyBills(UUID companyId, GenerateBillingRequest request) {
        WasteCompany company = wasteCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        // Validate period format
        String periodMonth = request.getPeriodMonth();
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(periodMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        } catch (Exception e) {
            throw new BadRequestException("Invalid period format. Use YYYY-MM");
        }

        // Check if bills already exist for this period
        if (paymentRepository.existsByWasteCompany_IdAndPeriodMonth(companyId, periodMonth)) {
            throw new BadRequestException("Bills for " + periodMonth + " have already been generated");
        }

        // Get all active households for this company
        List<Household> households = householdRepository.findByWasteCompany_IdAndStatus(
                companyId, UserStatus.ACTIVE);

        int totalHouseholds = households.size();
        int billsGenerated = 0;
        int billsSkipped = 0;

        LocalDate dueDate = yearMonth.atEndOfMonth().plusDays(7); // Due 7 days after month end

        for (Household household : households) {
            try {
                // Find applicable tariff rule
                Optional<TariffRule> tariffRule = tariffRuleRepository.findActiveRuleForHousehold(
                        companyId, household.getZone().getId(), household.getHouseType());

                if (tariffRule.isEmpty()) {
                    billsSkipped++;
                    continue;
                }

                BigDecimal amount = tariffRule.get().getAmount();

                // Create payment
                Payment payment = new Payment();
                payment.setPeriodMonth(periodMonth);
                payment.setAmount(amount);
                payment.setDueDate(dueDate);
                payment.setStatus(PaymentStatus.PENDING);
                payment.setHousehold(household);
                payment.setWasteCompany(company);

                paymentRepository.save(payment);
                billsGenerated++;

                // Notify citizen if household is linked
                Optional<CitizenAccount> citizenAccount = citizenAccountRepository
                        .findByHousehold_Id(household.getId());

                if (citizenAccount.isPresent()) {
                    Notification notification = new Notification();
                    notification.setType(NotificationType.BILLING);
                    notification.setTitle("New Bill Generated");
                    notification.setMessage(String.format(
                            "Your bill for %s has been generated. Amount: RWF %.2f. Due date: %s",
                            periodMonth, amount, dueDate));
                    notification.setRelatedEntityId(payment.getId());
                    notification.setRecipientUser(citizenAccount.get().getCitizenUser());
                    notificationRepository.save(notification);
                }

            } catch (Exception e) {
                billsSkipped++;
            }
        }

        return BillingGenerationResult.builder()
                .totalHouseholds(totalHouseholds)
                .billsGenerated(billsGenerated)
                .billsSkipped(billsSkipped)
                .periodMonth(periodMonth)
                .message(String.format("Generated %d bills out of %d households. %d skipped.",
                        billsGenerated, totalHouseholds, billsSkipped))
                .build();
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByCompany(UUID companyId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByWasteCompany_Id(companyId, pageable);
        return payments.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByPeriod(UUID companyId, String periodMonth, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByWasteCompany_IdAndPeriodMonth(
                companyId, periodMonth, pageable);
        return payments.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUnpaidPayments(UUID companyId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByWasteCompany_IdAndStatus(
                companyId, PaymentStatus.PENDING, pageable);
        return payments.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getOverduePayments(UUID companyId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByWasteCompany_IdAndStatus(
                companyId, PaymentStatus.OVERDUE, pageable);
        return payments.map(this::mapToResponse);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse.PaymentResponseBuilder builder = PaymentResponse.builder()
                .id(payment.getId())
                .periodMonth(payment.getPeriodMonth())
                .amount(payment.getAmount())
                .dueDate(payment.getDueDate())
                .status(payment.getStatus())
                .transactionReference(payment.getTransactionRef())
                .paidAt(payment.getPaidAt())
                .householdId(payment.getHousehold().getId())
                .householdCode(payment.getHousehold().getCode())
                .householdAddress(payment.getHousehold().getAddress())
                .companyId(payment.getWasteCompany().getId())
                .companyName(payment.getWasteCompany().getName())
                .createdAt(payment.getCreatedAt());

        // Add citizen info if available
        Optional<CitizenAccount> citizenAccount = citizenAccountRepository
                .findByHousehold_Id(payment.getHousehold().getId());

        citizenAccount.ifPresent(account -> {
            builder.citizenUserId(account.getCitizenUser().getId())
                    .citizenName(account.getCitizenUser().getFullName())
                    .citizenPhone(account.getCitizenUser().getPhone());
        });

        return builder.build();
    }
}
