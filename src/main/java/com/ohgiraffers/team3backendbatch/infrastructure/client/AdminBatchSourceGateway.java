package com.ohgiraffers.team3backendbatch.infrastructure.client;

/**
 * Admin 서비스 또는 공용 기준정보 접근 추상화 스켈레톤이다.
 *
 * 예상 기능:
 * - employee / equipment / tier_config / algorithm_version 조회
 * - score 보정 규칙 또는 수동 override 조회
 * - 추후 REST client, shared DB query, 캐시 방식 중 하나로 구현
 */
public interface AdminBatchSourceGateway {
    // TODO 관리자 기준정보 조회 메서드 정의
}