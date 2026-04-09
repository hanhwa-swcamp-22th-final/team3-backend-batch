package com.ohgiraffers.team3backendbatch.domain.order.scoring;

import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model.OrderDifficultySource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderDifficultyCalculator {

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal TEN = BigDecimal.TEN;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal DEFAULT_WEIGHT = BigDecimal.valueOf(0.25);
    private static final BigDecimal PROCESS_BASE = BigDecimal.valueOf(3.60);
    private static final BigDecimal QUALITY_BASE = BigDecimal.valueOf(3.70);
    private static final BigDecimal COMPETENCY_BASE = BigDecimal.valueOf(3.60);

    private static final List<KeywordRule> PROCESS_KEYWORDS = List.of(
        KeywordRule.of("1.20", "TI-6AL-4V", "TURBINE", "AEROSPACE", "항공"),
        KeywordRule.of("0.90", "SEMICONDUCTOR", "반도체", "PCB", "MODULE"),
        KeywordRule.of("0.80", "GEARBOX", "ROBOT", "금형", "MOLD", "JIG"),
        KeywordRule.of("0.60", "ASSEMBLY", "어셈블리", "HOUSING", "PANEL")
    );

    private static final List<KeywordRule> QUALITY_KEYWORDS = List.of(
        KeywordRule.of("1.30", "정밀", "PRECISION", "SENSOR", "CMM", "측정"),
        KeywordRule.of("1.00", "SEMICONDUCTOR", "반도체", "PCB", "MODULE", "CHIP"),
        KeywordRule.of("0.80", "SHAFT", "GEAR", "TURBINE", "COUPLING", "플랜지"),
        KeywordRule.of("0.60", "검사", "INSPECT", "CALIBRATION", "TOLERANCE")
    );

    private static final List<KeywordRule> CAPACITY_KEYWORDS = List.of(
        KeywordRule.of("1.00", "PACK", "PANEL", "MODULE", "어셈블리"),
        KeywordRule.of("0.80", "SHAFT", "FLANGE", "플랜지", "FRAME"),
        KeywordRule.of("0.60", "BRACKET", "볼트", "PLATE", "COVER")
    );

    private static final List<KeywordRule> COMPETENCY_KEYWORDS = List.of(
        KeywordRule.of("1.20", "TI-6AL-4V", "TURBINE", "AEROSPACE", "PRECISION", "CMM", "SENSOR"),
        KeywordRule.of("0.90", "SEMICONDUCTOR", "PCB", "MODULE", "ROBOT", "MOLD", "JIG", "GEARBOX"),
        KeywordRule.of("0.70", "SHAFT", "FLANGE", "COUPLING", "CALIBRATION", "TOLERANCE"),
        KeywordRule.of("0.50", "ASSEMBLY", "HOUSING", "PANEL", "FRAME")
    );

    private static final List<KeywordRule> PROCESS_KEYWORDS_V2 = List.of(
        KeywordRule.of("1.20", "TURBINE", "AEROSPACE", "FORGING", "CASTING"),
        KeywordRule.of("0.90", "GEARBOX", "MOLD", "JIG", "MACHINING"),
        KeywordRule.of("0.60", "ASSEMBLY", "HOUSING", "PANEL", "FRAME")
    );

    private static final List<KeywordRule> QUALITY_KEYWORDS_V2 = List.of(
        KeywordRule.of("1.30", "PRECISION", "SENSOR", "CMM", "TOLERANCE"),
        KeywordRule.of("1.00", "CALIBRATION", "INSPECT", "METROLOGY", "MEASUREMENT"),
        KeywordRule.of("0.80", "SEMICONDUCTOR", "PCB", "CHIP", "CLEANROOM")
    );

    private static final List<KeywordRule> COMPETENCY_KEYWORDS_V2 = List.of(
        KeywordRule.of("1.20", "TI-6AL-4V", "TITANIUM", "ROBOT", "AUTOMATION"),
        KeywordRule.of("0.90", "MODULE", "COUPLING", "SHAFT", "FLANGE"),
        KeywordRule.of("0.70", "PROTOTYPE", "CUSTOM", "TOOLING", "SETUP")
    );

    public BigDecimal calculateProcessComplexity(OrderDifficultySource source) {
        BigDecimal score = normalizeLinear(safeProcessStepCount(source.getProcessStepCount()), 1, 50);
        int quantity = safeQuantity(source.getOrderQuantity());

        if (quantity <= 10) {
            score = score.add(BigDecimal.valueOf(0.60));
        } else if (quantity <= 50) {
            score = score.add(BigDecimal.valueOf(0.30));
        } else if (quantity >= 500) {
            score = score.subtract(BigDecimal.valueOf(0.20));
        }
        return normalizeComponent(score);
    }

    public BigDecimal calculateQualityPrecision(OrderDifficultySource source) {
        BigDecimal score = mapToleranceToScore(source.getToleranceMm());

        if (safeQuantity(source.getOrderQuantity()) <= 20) {
            score = score.add(BigDecimal.valueOf(0.30));
        }
        return normalizeComponent(score);
    }

    public BigDecimal calculateCompetencyRequirements(OrderDifficultySource source) {
        BigDecimal score = BigDecimal.valueOf(safeSkillLevel(source.getSkillLevel())).multiply(BigDecimal.valueOf(2));
        int quantity = safeQuantity(source.getOrderQuantity());

        if (quantity <= 20) {
            score = score.add(BigDecimal.valueOf(0.50));
        } else if (quantity >= 500) {
            score = score.subtract(BigDecimal.valueOf(0.20));
        }
        return normalizeComponent(score);
    }

    /**
     * Legacy method name kept for compatibility.
     * V3 is interpreted as competency requirements, not equipment capacity.
     */
    public BigDecimal calculateCapacityRequirements(OrderDifficultySource source) {
        return calculateCompetencyRequirements(source);
    }

    public BigDecimal calculateSpaceTimeUrgency(OrderDifficultySource source) {
        int daysUntilDue = daysUntilDue(source.getReferenceDate(), source.getDueDate());
        int quantity = safeQuantity(source.getOrderQuantity());
        BigDecimal score;

        if (daysUntilDue <= 1) {
            score = BigDecimal.valueOf(9.50);
        } else if (daysUntilDue <= 3) {
            score = BigDecimal.valueOf(8.70);
        } else if (daysUntilDue <= 5) {
            score = BigDecimal.valueOf(7.80);
        } else if (daysUntilDue <= 7) {
            score = BigDecimal.valueOf(6.80);
        } else if (daysUntilDue <= 10) {
            score = BigDecimal.valueOf(5.80);
        } else if (daysUntilDue <= 14) {
            score = BigDecimal.valueOf(4.80);
        } else {
            score = BigDecimal.valueOf(3.50);
        }

        BigDecimal unitsPerDay = BigDecimal.valueOf(quantity)
            .divide(BigDecimal.valueOf(Math.max(daysUntilDue, 1)), 4, RoundingMode.HALF_UP);

        if (quantity >= 300) {
            score = score.add(BigDecimal.valueOf(0.50));
        }
        if (unitsPerDay.compareTo(BigDecimal.valueOf(100)) >= 0) {
            score = score.add(BigDecimal.valueOf(0.80));
        } else if (unitsPerDay.compareTo(BigDecimal.valueOf(40)) >= 0) {
            score = score.add(BigDecimal.valueOf(0.40));
        }
        return normalizeComponent(score);
    }

    public BigDecimal calculateAlphaNovelty(OrderDifficultySource source) {
        return Boolean.TRUE.equals(source.getFirstOrder())
            ? BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateDifficultyScore(
        OrderDifficultySource source,
        BigDecimal v1,
        BigDecimal v2,
        BigDecimal v3,
        BigDecimal v4,
        BigDecimal alphaNovelty
    ) {
        BigDecimal weightV1 = resolveWeight(source.getWeightV1());
        BigDecimal weightV2 = resolveWeight(source.getWeightV2());
        BigDecimal weightV3 = resolveWeight(source.getWeightV3());
        BigDecimal weightV4 = resolveWeight(source.getWeightV4());
        BigDecimal weightSum = weightV1.add(weightV2).add(weightV3).add(weightV4);

        if (weightSum.compareTo(BigDecimal.ZERO) <= 0) {
            weightV1 = DEFAULT_WEIGHT;
            weightV2 = DEFAULT_WEIGHT;
            weightV3 = DEFAULT_WEIGHT;
            weightV4 = DEFAULT_WEIGHT;
            weightSum = BigDecimal.ONE;
        }

        BigDecimal weightedScore = safe(v1).multiply(weightV1)
            .add(safe(v2).multiply(weightV2))
            .add(safe(v3).multiply(weightV3))
            .add(safe(v4).multiply(weightV4))
            .divide(weightSum, 4, RoundingMode.HALF_UP)
            .multiply(TEN);

        BigDecimal alphaContribution = safe(alphaNovelty).multiply(safe(source.getAlphaWeight()));
        BigDecimal industryCorrection = resolveIndustryCorrection(source.getIndustryPreset());
        BigDecimal total = weightedScore.add(alphaContribution).add(industryCorrection);
        return total.min(HUNDRED).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public String classifyDifficultyGrade(BigDecimal difficultyScore) {
        if (difficultyScore == null) {
            return "D1";
        }
        if (difficultyScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return "D5";
        }
        if (difficultyScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "D4";
        }
        if (difficultyScore.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "D3";
        }
        if (difficultyScore.compareTo(BigDecimal.valueOf(30)) >= 0) {
            return "D2";
        }
        return "D1";
    }

    private BigDecimal baseScoreByIndustry(
        String industryPreset,
        String custom,
        String vehicle,
        String battery,
        String display,
        String semiconductor
    ) {
        if (industryPreset == null || industryPreset.isBlank()) {
            return new BigDecimal(custom);
        }

        return switch (industryPreset.trim().toUpperCase()) {
            case "SEMICONDUCTOR" -> new BigDecimal(semiconductor);
            case "DISPLAY" -> new BigDecimal(display);
            case "BATTERY" -> new BigDecimal(battery);
            case "VEHICLE" -> new BigDecimal(vehicle);
            default -> new BigDecimal(custom);
        };
    }

    private BigDecimal sumKeywordBoost(String normalizedText, List<KeywordRule> rules) {
        BigDecimal score = BigDecimal.ZERO;
        for (KeywordRule rule : rules) {
            if (rule.matches(normalizedText)) {
                score = score.add(rule.boost());
            }
        }
        return score;
    }

    private String normalizeText(OrderDifficultySource source) {
        String productName = source.getProductName() == null ? "" : source.getProductName().toUpperCase();
        String productCode = source.getProductCode() == null ? "" : source.getProductCode().toUpperCase();
        return (productName + " " + productCode).trim();
    }

    private boolean containsAny(String normalizedText, String... keywords) {
        for (String keyword : keywords) {
            if (normalizedText.contains(keyword.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private int daysUntilDue(LocalDate referenceDate, LocalDate dueDate) {
        if (dueDate == null) {
            return 14;
        }
        LocalDate baseDate = referenceDate == null ? LocalDate.now() : referenceDate;
        return (int) Math.max(0, ChronoUnit.DAYS.between(baseDate, dueDate));
    }

    private int safeQuantity(Integer quantity) {
        return quantity == null ? 0 : Math.max(quantity, 0);
    }

    private int safeProcessStepCount(Integer processStepCount) {
        if (processStepCount == null) {
            return 1;
        }
        return Math.min(Math.max(processStepCount, 1), 50);
    }

    private int safeSkillLevel(Integer skillLevel) {
        if (skillLevel == null) {
            return 1;
        }
        return Math.min(Math.max(skillLevel, 1), 5);
    }

    private BigDecimal resolveWeight(BigDecimal weight) {
        return weight == null ? BigDecimal.ZERO : weight.max(BigDecimal.ZERO);
    }

    private BigDecimal resolveIndustryCorrection(String industryPreset) {
        if (industryPreset == null || industryPreset.isBlank()) {
            return BigDecimal.ZERO;
        }

        return switch (industryPreset.trim().toUpperCase()) {
            case "SEMICONDUCTOR" -> BigDecimal.valueOf(2.00);
            case "DISPLAY" -> BigDecimal.valueOf(1.50);
            case "BATTERY" -> BigDecimal.valueOf(1.00);
            case "VEHICLE" -> BigDecimal.valueOf(0.50);
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal normalizeComponent(BigDecimal score) {
        BigDecimal normalized = safe(score).max(ONE).min(TEN);
        return normalized.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal normalizeLinear(int value, int min, int max) {
        if (max <= min) {
            return ONE.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal bounded = BigDecimal.valueOf(Math.min(Math.max(value, min), max) - min);
        BigDecimal range = BigDecimal.valueOf(max - min);
        BigDecimal scaled = bounded.multiply(BigDecimal.valueOf(9))
            .divide(range, 4, RoundingMode.HALF_UP);
        return ONE.add(scaled).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal mapToleranceToScore(BigDecimal toleranceMm) {
        BigDecimal tolerance = toleranceMm == null ? BigDecimal.ONE : toleranceMm.abs();

        if (tolerance.compareTo(new BigDecimal("0.0050")) <= 0) {
            return BigDecimal.valueOf(10);
        }
        if (tolerance.compareTo(new BigDecimal("0.0100")) <= 0) {
            return BigDecimal.valueOf(9);
        }
        if (tolerance.compareTo(new BigDecimal("0.0200")) <= 0) {
            return BigDecimal.valueOf(8);
        }
        if (tolerance.compareTo(new BigDecimal("0.0300")) <= 0) {
            return BigDecimal.valueOf(7);
        }
        if (tolerance.compareTo(new BigDecimal("0.0500")) <= 0) {
            return BigDecimal.valueOf(6);
        }
        if (tolerance.compareTo(new BigDecimal("0.1000")) <= 0) {
            return BigDecimal.valueOf(5);
        }
        if (tolerance.compareTo(new BigDecimal("0.2000")) <= 0) {
            return BigDecimal.valueOf(4);
        }
        if (tolerance.compareTo(new BigDecimal("0.3000")) <= 0) {
            return BigDecimal.valueOf(3);
        }
        if (tolerance.compareTo(new BigDecimal("0.5000")) <= 0) {
            return BigDecimal.valueOf(2);
        }
        return ONE;
    }

    private record KeywordRule(BigDecimal boost, String[] keywords) {
        private static KeywordRule of(String boost, String... keywords) {
            return new KeywordRule(new BigDecimal(boost), keywords);
        }

        private boolean matches(String normalizedText) {
            for (String keyword : keywords) {
                if (normalizedText.contains(keyword.toUpperCase())) {
                    return true;
                }
            }
            return false;
        }
    }
}
