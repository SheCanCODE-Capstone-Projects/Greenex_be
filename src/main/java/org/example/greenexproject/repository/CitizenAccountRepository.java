package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.CitizenAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CitizenAccountRepository extends JpaRepository<CitizenAccount, UUID> {
    Optional<CitizenAccount> findByCitizenUser_Id(UUID citizenUserId);
}
