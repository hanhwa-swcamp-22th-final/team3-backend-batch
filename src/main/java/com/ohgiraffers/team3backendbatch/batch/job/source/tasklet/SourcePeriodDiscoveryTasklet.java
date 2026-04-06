package com.ohgiraffers.team3backendbatch.batch.job.source.tasklet;

/**
 * 이번 배치에서 사용할 평가 기간과 원천 조회 범위를 결정하는 tasklet 스켈레톤이다.
 *
 * 구현 예정 메서드/내용:
 * - extractExecutionParameters(...)
 *   요청 파라미터에서 periodType, evaluationPeriodId, mode, employeeId, force 를 추출한다.
 * - resolvePeriodMetadata(...)
 *   evaluation_period 테이블에서 기간 메타데이터를 조회한다.
 * - resolveDateWindow(...)
 *   WEEK/MONTH/QUARTER/HALF_YEAR/YEAR 별 실제 시작일, 종료일을 계산한다.
 * - resolveMonthlySettlementWindow(...)
 *   상위 기간 summary 일 때 필요한 월간 settlement 범위를 계산한다.
 * - putResolvedWindowToJobContext(...)
 *   이후 Reader/Processor 가 공통으로 쓰도록 job execution context 에 기간 정보를 저장한다.
 *
 * 주의:
 * - WEEK/MONTH 는 raw `mes_*` 조회 범위를 계산한다.
 * - QUARTER/HALF_YEAR/YEAR 는 raw 원천이 아니라 월간 settlement 집계 범위를 계산한다.
 */
public class SourcePeriodDiscoveryTasklet {
    // TODO 평가 기간 메타데이터 조회 및 job context 기록
}
