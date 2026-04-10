package com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.TierConfigProjectionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TierConfigProjectionRepository extends JpaRepository<TierConfigProjectionEntity, Long> {

    Optional<TierConfigProjectionEntity> findByTierConfigTier(String tierConfigTier);
}
