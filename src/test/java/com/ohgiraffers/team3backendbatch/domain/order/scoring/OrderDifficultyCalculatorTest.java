package com.ohgiraffers.team3backendbatch.domain.order.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model.OrderDifficultySource;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderDifficultyCalculatorTest {

    private final OrderDifficultyCalculator orderDifficultyCalculator = new OrderDifficultyCalculator();

    @Test
    @DisplayName("Urgent first-order semiconductor product is classified as high difficulty")
    void calculateDifficultyScore_HighDifficultyCase() {
        OrderDifficultySource source = new OrderDifficultySource();
        source.setIndustryPreset("SEMICONDUCTOR");
        source.setProductName("반도체 정밀 모듈");
        source.setProductCode("PCB-TI-900");
        source.setOrderQuantity(320);
        source.setReferenceDate(LocalDate.now());
        source.setDueDate(LocalDate.now().plusDays(2));
        source.setProcessStepCount(45);
        source.setToleranceMm(new BigDecimal("0.0050"));
        source.setSkillLevel(5);
        source.setFirstOrder(true);
        source.setWeightV1(new BigDecimal("0.30"));
        source.setWeightV2(new BigDecimal("0.40"));
        source.setWeightV3(new BigDecimal("0.15"));
        source.setWeightV4(new BigDecimal("0.15"));
        source.setAlphaWeight(new BigDecimal("0.50"));
        source.setProductName("TURBINE PRECISION SENSOR MODULE");
        source.setProductCode("TI-6AL-4V PCB CMM");

        BigDecimal score = orderDifficultyCalculator.calculateDifficultyScore(
            source,
            orderDifficultyCalculator.calculateProcessComplexity(source),
            orderDifficultyCalculator.calculateQualityPrecision(source),
            orderDifficultyCalculator.calculateCompetencyRequirements(source),
            orderDifficultyCalculator.calculateSpaceTimeUrgency(source),
            orderDifficultyCalculator.calculateAlphaNovelty(source)
        );

        assertThat(score).isGreaterThanOrEqualTo(new BigDecimal("95.00"));
        assertThat(orderDifficultyCalculator.classifyDifficultyGrade(score)).isEqualTo("D5");
    }

    @Test
    @DisplayName("Simple custom order with relaxed deadline is classified as low difficulty")
    void calculateDifficultyScore_LowDifficultyCase() {
        OrderDifficultySource source = new OrderDifficultySource();
        source.setIndustryPreset("CUSTOM");
        source.setProductName("표준 브라켓");
        source.setProductCode("BRACKET-01");
        source.setOrderQuantity(20);
        source.setReferenceDate(LocalDate.now());
        source.setDueDate(LocalDate.now().plusDays(18));
        source.setProcessStepCount(3);
        source.setToleranceMm(new BigDecimal("0.3000"));
        source.setSkillLevel(1);
        source.setFirstOrder(false);
        source.setWeightV1(new BigDecimal("0.25"));
        source.setWeightV2(new BigDecimal("0.25"));
        source.setWeightV3(new BigDecimal("0.25"));
        source.setWeightV4(new BigDecimal("0.25"));
        source.setAlphaWeight(new BigDecimal("0.10"));

        BigDecimal score = orderDifficultyCalculator.calculateDifficultyScore(
            source,
            orderDifficultyCalculator.calculateProcessComplexity(source),
            orderDifficultyCalculator.calculateQualityPrecision(source),
            orderDifficultyCalculator.calculateCompetencyRequirements(source),
            orderDifficultyCalculator.calculateSpaceTimeUrgency(source),
            orderDifficultyCalculator.calculateAlphaNovelty(source)
        );

        assertThat(score).isLessThan(new BigDecimal("30.00"));
        assertThat(orderDifficultyCalculator.classifyDifficultyGrade(score)).isEqualTo("D1");
    }

    @Test
    @DisplayName("V3 follows the order skill level input")
    void calculateCompetencyRequirements_FavorsSkillIntensityOverVolume() {
        OrderDifficultySource specialized = new OrderDifficultySource();
        specialized.setIndustryPreset("SEMICONDUCTOR");
        specialized.setProductName("PRECISION SENSOR MODULE");
        specialized.setProductCode("TI-6AL-4V-CMM");
        specialized.setOrderQuantity(8);
        specialized.setSkillLevel(5);
        specialized.setFirstOrder(true);

        OrderDifficultySource routine = new OrderDifficultySource();
        routine.setIndustryPreset("CUSTOM");
        routine.setProductName("BRACKET PANEL COVER");
        routine.setProductCode("BRACKET-01");
        routine.setOrderQuantity(1200);
        routine.setSkillLevel(1);
        routine.setFirstOrder(false);

        BigDecimal specializedScore = orderDifficultyCalculator.calculateCompetencyRequirements(specialized);
        BigDecimal routineScore = orderDifficultyCalculator.calculateCompetencyRequirements(routine);

        assertThat(specializedScore).isGreaterThan(routineScore);
    }

    @Test
    @DisplayName("First-order flag only changes alpha novelty")
    void firstOrderOnlyAffectsAlphaNovelty() {
        OrderDifficultySource base = createSource(
            "DISPLAY",
            "HOUSING FRAME",
            "ITEM-01",
            80,
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 8),
            false
        );
        OrderDifficultySource firstOrder = createSource(
            "DISPLAY",
            "HOUSING FRAME",
            "ITEM-01",
            80,
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 8),
            true
        );

        assertThat(orderDifficultyCalculator.calculateProcessComplexity(firstOrder))
            .isEqualByComparingTo(orderDifficultyCalculator.calculateProcessComplexity(base));
        assertThat(orderDifficultyCalculator.calculateQualityPrecision(firstOrder))
            .isEqualByComparingTo(orderDifficultyCalculator.calculateQualityPrecision(base));
        assertThat(orderDifficultyCalculator.calculateCompetencyRequirements(firstOrder))
            .isEqualByComparingTo(orderDifficultyCalculator.calculateCompetencyRequirements(base));
        assertThat(orderDifficultyCalculator.calculateSpaceTimeUrgency(firstOrder))
            .isEqualByComparingTo(orderDifficultyCalculator.calculateSpaceTimeUrgency(base));
        assertThat(orderDifficultyCalculator.calculateAlphaNovelty(firstOrder))
            .isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(orderDifficultyCalculator.calculateAlphaNovelty(base))
            .isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("V4 uses the reference date from the event instead of the execution date")
    void calculateSpaceTimeUrgency_UsesReferenceDate() {
        OrderDifficultySource earlyReference = createSource(
            "CUSTOM",
            "BRACKET",
            "ITEM-01",
            10,
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 4),
            false
        );
        OrderDifficultySource lateReference = createSource(
            "CUSTOM",
            "BRACKET",
            "ITEM-01",
            10,
            LocalDate.of(2026, 4, 3),
            LocalDate.of(2026, 4, 4),
            false
        );

        assertThat(orderDifficultyCalculator.calculateSpaceTimeUrgency(earlyReference))
            .isEqualByComparingTo(new BigDecimal("8.70"));
        assertThat(orderDifficultyCalculator.calculateSpaceTimeUrgency(lateReference))
            .isEqualByComparingTo(new BigDecimal("9.50"));
    }

    @Test
    @DisplayName("V1 V2 V3 are driven by process steps, tolerance, and skill level respectively")
    void vInputs_AreSeparatedAcrossV1V2V3() {
        OrderDifficultySource processHeavy = createSource(
            "CUSTOM",
            "ITEM",
            "ITEM-01",
            100,
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 12),
            false
        );
        processHeavy.setProcessStepCount(45);
        processHeavy.setToleranceMm(new BigDecimal("0.3000"));
        processHeavy.setSkillLevel(1);

        OrderDifficultySource precisionHeavy = createSource(
            "CUSTOM",
            "ITEM",
            "ITEM-01",
            100,
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 12),
            false
        );
        precisionHeavy.setProcessStepCount(5);
        precisionHeavy.setToleranceMm(new BigDecimal("0.0050"));
        precisionHeavy.setSkillLevel(1);

        OrderDifficultySource skillHeavy = createSource(
            "CUSTOM",
            "ITEM",
            "ITEM-01",
            100,
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 12),
            false
        );
        skillHeavy.setProcessStepCount(5);
        skillHeavy.setToleranceMm(new BigDecimal("0.3000"));
        skillHeavy.setSkillLevel(5);

        assertThat(orderDifficultyCalculator.calculateProcessComplexity(processHeavy))
            .isGreaterThan(orderDifficultyCalculator.calculateProcessComplexity(precisionHeavy));
        assertThat(orderDifficultyCalculator.calculateQualityPrecision(precisionHeavy))
            .isGreaterThan(orderDifficultyCalculator.calculateQualityPrecision(processHeavy));
        assertThat(orderDifficultyCalculator.calculateCompetencyRequirements(skillHeavy))
            .isGreaterThan(orderDifficultyCalculator.calculateCompetencyRequirements(processHeavy));
    }

    @Test
    @DisplayName("Industry preset changes weights, not V1 V2 V3 component scores")
    void vScoresDependOnProductAndOrderNotIndustry() {
        OrderDifficultySource semiconductor = createSource(
            "SEMICONDUCTOR",
            "TURBINE PRECISION SENSOR MODULE",
            "TI-6AL-4V PCB CMM",
            40,
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 8),
            false
        );
        semiconductor.setWeightV1(new BigDecimal("0.20"));
        semiconductor.setWeightV2(new BigDecimal("0.45"));
        semiconductor.setWeightV3(new BigDecimal("0.15"));
        semiconductor.setWeightV4(new BigDecimal("0.20"));

        OrderDifficultySource battery = createSource(
            "BATTERY",
            "TURBINE PRECISION SENSOR MODULE",
            "TI-6AL-4V PCB CMM",
            40,
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 8),
            false
        );
        battery.setWeightV1(new BigDecimal("0.40"));
        battery.setWeightV2(new BigDecimal("0.20"));
        battery.setWeightV3(new BigDecimal("0.25"));
        battery.setWeightV4(new BigDecimal("0.15"));

        BigDecimal semiconductorV1 = orderDifficultyCalculator.calculateProcessComplexity(semiconductor);
        BigDecimal semiconductorV2 = orderDifficultyCalculator.calculateQualityPrecision(semiconductor);
        BigDecimal semiconductorV3 = orderDifficultyCalculator.calculateCompetencyRequirements(semiconductor);
        BigDecimal batteryV1 = orderDifficultyCalculator.calculateProcessComplexity(battery);
        BigDecimal batteryV2 = orderDifficultyCalculator.calculateQualityPrecision(battery);
        BigDecimal batteryV3 = orderDifficultyCalculator.calculateCompetencyRequirements(battery);

        assertThat(semiconductorV1).isEqualByComparingTo(batteryV1);
        assertThat(semiconductorV2).isEqualByComparingTo(batteryV2);
        assertThat(semiconductorV3).isEqualByComparingTo(batteryV3);

        BigDecimal semiconductorScore = orderDifficultyCalculator.calculateDifficultyScore(
            semiconductor,
            semiconductorV1,
            semiconductorV2,
            semiconductorV3,
            orderDifficultyCalculator.calculateSpaceTimeUrgency(semiconductor),
            orderDifficultyCalculator.calculateAlphaNovelty(semiconductor)
        );
        BigDecimal batteryScore = orderDifficultyCalculator.calculateDifficultyScore(
            battery,
            batteryV1,
            batteryV2,
            batteryV3,
            orderDifficultyCalculator.calculateSpaceTimeUrgency(battery),
            orderDifficultyCalculator.calculateAlphaNovelty(battery)
        );

        assertThat(semiconductorScore).isNotEqualByComparingTo(batteryScore);
    }

    private OrderDifficultySource createSource(
        String industryPreset,
        String productName,
        String productCode,
        int quantity,
        LocalDate referenceDate,
        LocalDate dueDate,
        boolean firstOrder
    ) {
        OrderDifficultySource source = new OrderDifficultySource();
        source.setIndustryPreset(industryPreset);
        source.setProductName(productName);
        source.setProductCode(productCode);
        source.setOrderQuantity(quantity);
        source.setProcessStepCount(10);
        source.setToleranceMm(new BigDecimal("0.1000"));
        source.setSkillLevel(2);
        source.setReferenceDate(referenceDate);
        source.setDueDate(dueDate);
        source.setFirstOrder(firstOrder);
        source.setWeightV1(new BigDecimal("0.25"));
        source.setWeightV2(new BigDecimal("0.25"));
        source.setWeightV3(new BigDecimal("0.25"));
        source.setWeightV4(new BigDecimal("0.25"));
        source.setAlphaWeight(new BigDecimal("0.10"));
        return source;
    }
}
