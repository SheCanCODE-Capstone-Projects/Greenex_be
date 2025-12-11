package org.example.greenexproject.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.UserStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "citizen_accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CitizenAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_user_id", nullable = false, unique = true)
    private SystemUser citizenUser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false, unique = true)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
