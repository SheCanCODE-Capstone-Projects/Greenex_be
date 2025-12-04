package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.TariffRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TariffRuleRepository extends JpaRepository<TariffRule, UUID> {
}
