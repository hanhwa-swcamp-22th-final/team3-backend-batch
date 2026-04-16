package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.AntiGamingFlagEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AntiGamingFlagRepository extends JpaRepository<AntiGamingFlagEntity, Long> {

    List<AntiGamingFlagEntity> findAllByTargetYearAndTargetPeriod(Integer targetYear, String targetPeriod);
}
