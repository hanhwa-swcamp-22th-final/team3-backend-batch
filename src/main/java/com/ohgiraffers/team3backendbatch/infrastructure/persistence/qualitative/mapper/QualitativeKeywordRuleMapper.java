package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.dto.DomainKeywordRuleRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QualitativeKeywordRuleMapper {

    List<DomainKeywordRuleRow> findActiveDomainKeywordRules();
}