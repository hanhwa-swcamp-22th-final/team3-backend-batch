package com.ohgiraffers.team3backendbatch.batch.scheduler;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.common.launcher.BatchJobLauncherFacade;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 정기 배치 실행 스케줄러다.
 *
 * 현재 방향:
 * - 주기/분기/반기/년 단위로 다른 cron 또는 외부 스케줄러 연동 가능
 * - 스케줄러는 periodType만 전달하고 실제 evaluationPeriodId는 잡 내부에서 결정할 수 있음
 * - 또는 외부 스케줄러가 periodType + evaluationPeriodId를 명시적으로 전달하도록 확장 가능
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "batch.schedule", name = "enabled", havingValue = "true")
public class BatchScheduleService {

    private final BatchJobLauncherFacade batchJobLauncherFacade;

    @Scheduled(cron = "${batch.schedule.quantitative-cron}", zone = "${batch.schedule.zone}")
    public void runQuantitativeEvaluationJob() {
        batchJobLauncherFacade.launchScheduled(BatchJobNames.QUANTITATIVE_EVALUATION_JOB, "scheduler");
    }

    @Scheduled(cron = "${batch.schedule.qualitative-normalization-cron}", zone = "${batch.schedule.zone}")
    public void runMonthlyQualitativeNormalizationJob() {
        batchJobLauncherFacade.launchScheduled(
            BatchJobNames.QUALITATIVE_NORMALIZATION_JOB,
            BatchPeriodType.MONTH,
            null,
            "scheduler",
            "Automatic month-end qualitative normalization"
        );
    }

    @Scheduled(cron = "${batch.schedule.score-cron}", zone = "${batch.schedule.zone}")
    public void runScoreAggregationJob() {
        batchJobLauncherFacade.launchScheduled(BatchJobNames.SCORE_AGGREGATION_JOB, "scheduler");
    }

    @Scheduled(cron = "${batch.schedule.promotion-cron}", zone = "${batch.schedule.zone}")
    public void runPromotionCandidateJob() {
        batchJobLauncherFacade.launchScheduled(BatchJobNames.PROMOTION_CANDIDATE_JOB, "scheduler");
    }
}