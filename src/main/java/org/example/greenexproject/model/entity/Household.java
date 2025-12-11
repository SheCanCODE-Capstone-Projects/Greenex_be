package org.example.greenexproject.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.HouseType;
import org.example.greenexproject.model.enums.UserStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "households")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Household {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waste_company_id", nullable = false)
    private WasteCompany wasteCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private HouseType houseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(length = 500)
    private String notes;

    @OneToOne(mappedBy = "household", fetch = FetchType.LAZY)
    private CitizenAccount citizenAccount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
