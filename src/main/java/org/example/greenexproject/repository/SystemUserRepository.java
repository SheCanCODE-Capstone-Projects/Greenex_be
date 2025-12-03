package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.SystemUser;
import org.example.greenexproject.model.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, UUID> {
    Optional<SystemUser> findByEmail(String email);

    Optional<SystemUser> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<SystemUser> findByEmailAndUserType(String email, UserType userType);
}
