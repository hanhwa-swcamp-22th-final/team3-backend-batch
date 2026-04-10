package com.ohgiraffers.team3backendbatch.infrastructure.client;

/**
 * MES 원천 데이터 접근 추상화 스켈레톤이다.
 *
 * 예상 기능:
 * - 파일 기반 입력이면 로컬 파일/공유 스토리지 접근 추상화
 * - API 기반 입력이면 MES REST 또는 MQ 소비 추상화
 * - 운영 환경과 샘플 데이터 환경의 차이를 숨김
 */
public interface MesBatchSourceGateway {
    // TODO 생산/품질/설비 상태 원천 접근 인터페이스 정의
}