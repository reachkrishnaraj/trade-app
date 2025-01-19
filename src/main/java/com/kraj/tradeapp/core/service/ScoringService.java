package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.AlertMessageMatchType;
import com.kraj.tradeapp.core.model.Direction;
import com.kraj.tradeapp.core.model.IndicatorMsgRule;
import com.kraj.tradeapp.core.model.IndicatorSubCategoryRange;
import com.opencsv.CSVReader;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import javax.swing.text.html.Option;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class ScoringService {

    private final ResourceLoader resourceLoader;

    private static final String INDICATOR_RULE_FILEPATH = "classpath:config/indicator_rules/notification_indicator_mapping_rules.csv";

    private LinkedList<IndicatorMsgRule> indicatorMsgRules;
    //    private List<IndicatorSubCategoryRange> indicatorSubCategoryRangeList;
    private final Map<String, IndicatorSubCategoryRange> subCategoryRangeMap = new HashMap<>();

    @PostConstruct
    public void init() {
        loadIndicatorScores();
        calculateCategoryRanges();
    }

    private void loadIndicatorScores() {
        indicatorMsgRules = new LinkedList<>();
        Resource resource = resourceLoader.getResource(INDICATOR_RULE_FILEPATH);
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            CSVReader csvReader = new CSVReader(reader);
            // Skip header
            csvReader.readNext();

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                //skip header row
                if (line[0].contains("param_indicator_name")) {
                    continue;
                }
                //skip line starts with #
                if (line[0].startsWith("#")) {
                    continue;
                }
                @Nullable
                String indicatorName = StringUtils.isBlank(StringUtils.trim(line[0])) ? null : StringUtils.trim(line[0]);
                @Nullable
                String indicatorDisplayName = StringUtils.isBlank(StringUtils.trim(line[1])) ? null : StringUtils.trim(line[1]);
                @Nullable
                String description = StringUtils.isBlank(StringUtils.trim(line[2])) ? null : StringUtils.trim(line[2]);
                @Nullable
                String matchType = StringUtils.isBlank(StringUtils.trim(line[3])) ? null : StringUtils.trim(line[3]);
                @Nullable
                String alertMessage = StringUtils.isBlank(StringUtils.trim(line[4])) ? null : StringUtils.trim(line[4]);
                @Nullable
                String interval = StringUtils.isBlank(StringUtils.trim(line[5])) ? null : StringUtils.trim(line[5]);
                @Nullable
                String subCategory = StringUtils.isBlank(StringUtils.trim(line[6])) ? null : StringUtils.trim(line[6]);
                @Nullable
                String subCategoryDisplayName = StringUtils.isBlank(StringUtils.trim(line[7])) ? null : StringUtils.trim(line[7]);
                @Nullable
                String isSkipScoring = StringUtils.isBlank(StringUtils.trim(line[8])) ? null : StringUtils.trim(line[8]);
                @Nullable
                BigDecimal score = StringUtils.isBlank(StringUtils.trim(line[9])) ? null : new BigDecimal(StringUtils.trim(line[9]));
                @Nullable
                String isAlertable = StringUtils.isBlank(StringUtils.trim(line[10])) ? null : StringUtils.trim(line[10]);

                if (indicatorName == null || alertMessage == null) {
                    log.error("Indicator name or alert message is missing in scoring file. Skipping this line");
                    continue;
                }

                //@Nullable String scoreRangeMinStr = scoreRange == null ? null : StringUtils.splitByWholeSeparator(scoreRange, "to")[0];
                //@Nullable String scoreRangeMaxStr = scoreRange == null ? null : StringUtils.splitByWholeSeparator(scoreRange, "to")[1];

                IndicatorMsgRule indicatorMsgRule = IndicatorMsgRule.builder()
                    .indicatorName(indicatorName)
                    .indicatorDisplayName(indicatorDisplayName)
                    .matchType(matchType)
                    //.scoreRangeMin(scoreRangeMinStr == null ? null : new BigDecimal(scoreRangeMinStr))
                    //.scoreRangeMax(scoreRangeMaxStr == null ? null : new BigDecimal(scoreRangeMaxStr))
                    //.scoreRange(scoreRange)
                    .description(description)
                    .interval(interval)
                    .isSkipScoring(isSkipScoring)
                    .score(score)
                    .subCategory(subCategory)
                    .indicatorSubCategoryDisplayName(subCategoryDisplayName)
                    .alertMessage(alertMessage)
                    .isAlertable(StringUtils.isNotBlank(isAlertable) && isAlertable.equalsIgnoreCase("true"))
                    .build();
                indicatorMsgRules.addLast(indicatorMsgRule);
            }
        } catch (Exception e) {
            log.error("Error loading indicator scores", e);
            throw new RuntimeException("Failed to load indicator scores", e);
        }
    }

    private void calculateCategoryRanges() {
        for (IndicatorMsgRule rule : indicatorMsgRules) {
            boolean isSkipScoring = rule.getIsSkipScoring() != null && rule.getIsSkipScoring().equalsIgnoreCase("true");

            if (isSkipScoring) {
                continue;
            }

            if (rule.getScore() == null) {
                throw new RuntimeException("Score is missing for indicator sub category: " + rule.getSubCategory());
            }
            String key = getIndicatorRangeMapKey(rule.getIndicatorName(), rule.getSubCategory());
            IndicatorSubCategoryRange subCategoryRange = subCategoryRangeMap.getOrDefault(key, new IndicatorSubCategoryRange());
            BigDecimal min = subCategoryRange.getMinScore() == null
                ? rule.getScore()
                : BigDecimal.valueOf(Math.min(subCategoryRange.getMinScore().floatValue(), rule.getScore().floatValue()));

            BigDecimal max = subCategoryRange.getMaxScore() == null
                ? rule.getScore()
                : BigDecimal.valueOf(Math.max(subCategoryRange.getMaxScore().floatValue(), rule.getScore().floatValue()));

            subCategoryRange.setSubCategory(rule.getSubCategory());
            subCategoryRange.setMinScore(min);
            subCategoryRange.setMaxScore(max);
            subCategoryRangeMap.put(key, subCategoryRange);
        }
        //        indicatorSubCategoryRangeList = new ArrayList<>(subCategoryRangeMap.values());
    }

    /**
     * Finds matching indicator score based on indicator name and message
     * @param indicatorName name of the indicator
     * @param message alert message to match
     * @return matching IndicatorScore or null if no match found
     */
    public Optional<IndicatorMsgRule> findMatchingIndicatorEventRule(String indicatorName, String message) {
        for (IndicatorMsgRule rule : indicatorMsgRules) {
            if (!StringUtils.equalsIgnoreCase(rule.getIndicatorName(), indicatorName)) {
                continue;
            }
            AlertMessageMatchType matchType = AlertMessageMatchType.fromString(rule.getMatchType());
            if (matchType == AlertMessageMatchType.UNKNOWN) {
                throw new RuntimeException("Invalid match type: " + rule.getMatchType());
            }
            boolean isSkipScoring = rule.getIsSkipScoring() != null && rule.getIsSkipScoring().equalsIgnoreCase("true");
            switch (matchType) {
                case STARTS_WITH:
                    if (StringUtils.startsWithIgnoreCase(message.trim(), rule.getAlertMessage().trim())) {
                        rule.setScoreRangeMax(getMatchingMaxScore(rule).orElse(new BigDecimal("0")));
                        rule.setScoreRangeMin(getMatchingMinScore(rule).orElse(new BigDecimal("0")));

                        return Optional.of(rule);
                    }
                    break;
                case FULL_MATCH:
                    if (StringUtils.equalsIgnoreCase(rule.getAlertMessage(), message.trim())) {
                        rule.setScoreRangeMax(getMatchingMaxScore(rule).orElse(new BigDecimal("0")));
                        rule.setScoreRangeMin(getMatchingMinScore(rule).orElse(new BigDecimal("0")));
                        return Optional.of(rule);
                    }
                    break;
                case JAVA_REGEX:
                    if (message.matches(rule.getAlertMessage())) {
                        rule.setScoreRangeMax(getMatchingMaxScore(rule).orElse(new BigDecimal("0")));
                        rule.setScoreRangeMin(getMatchingMinScore(rule).orElse(new BigDecimal("0")));
                        return Optional.of(rule);
                    }
                    break;
                case CONTAINS:
                    if (StringUtils.containsIgnoreCase(message, rule.getAlertMessage())) {
                        rule.setScoreRangeMax(getMatchingMaxScore(rule).orElse(new BigDecimal("0")));
                        rule.setScoreRangeMin(getMatchingMinScore(rule).orElse(new BigDecimal("0")));
                        return Optional.of(rule);
                    }
                    break;
                default:
                    throw new RuntimeException("Invalid match type: " + rule.getMatchType());
            }
        }
        throw new RuntimeException("No matching rule found for indicator: " + indicatorName + " and message: " + message);
    }

    private Optional<BigDecimal> getMatchingMinScore(IndicatorMsgRule rule) {
        String key = getIndicatorRangeMapKey(rule.getIndicatorName(), rule.getSubCategory());
        return Optional.ofNullable(subCategoryRangeMap.get(key)).map(IndicatorSubCategoryRange::getMinScore);
        //    return indicatorSubCategoryRangeList.stream().filter(subCategoryRange -> StringUtils.equalsIgnoreCase(subCategoryRange.getSubCategory(), rule.getSubCategory()))
        //        .findFirst()
        //        .map(IndicatorSubCategoryRange::getMinScore)
        //        .orElseThrow(() -> new RuntimeException("No matching sub category found for indicator: " + rule.getIndicatorName() + " and sub category: " + rule.getSubCategory()));
    }

    private Optional<BigDecimal> getMatchingMaxScore(IndicatorMsgRule rule) {
        String key = getIndicatorRangeMapKey(rule.getIndicatorName(), rule.getSubCategory());
        return Optional.ofNullable(subCategoryRangeMap.get(key)).map(IndicatorSubCategoryRange::getMaxScore);
        //        return indicatorSubCategoryRangeList.stream().filter(subCategoryRange -> StringUtils.equalsIgnoreCase(subCategoryRange.getSubCategory(), rule.getSubCategory()))
        //            .findFirst()
        //            .map(IndicatorSubCategoryRange::getMaxScore)
        //            .orElseThrow(() -> new RuntimeException("No matching sub category found for indicator: " + rule.getIndicatorName() + " and sub category: " + rule.getSubCategory()));
    }

    //    public static BigDecimal calculatePercentage(BigDecimal min, BigDecimal max, BigDecimal actual) {
    //        BigDecimal range = max.subtract(min); // max - min
    //        BigDecimal offsetScore = actual.subtract(min); // actual - min
    //        BigDecimal percentage = offsetScore.multiply(new BigDecimal("100")).divide(range, 2, RoundingMode.HALF_UP);
    //        return percentage;
    //    }

    //    public static Direction categorizeScore(BigDecimal percentage) {
    //        if (percentage.compareTo(new BigDecimal("20")) <= 0) {
    //            return Direction.STRONG_BEAR;
    //        } else if (percentage.compareTo(new BigDecimal("40")) <= 0) {
    //            return Direction.BEAR;
    //        } else if (percentage.compareTo(new BigDecimal("60")) <= 0) {
    //            return Direction.NEUTRAL;
    //        } else if (percentage.compareTo(new BigDecimal("85")) <= 0) {
    //            return Direction.BULL;
    //        } else {
    //            return Direction.STRONG_BULL;
    //        }
    //    }

    public static BigDecimal calculateBipolarPercentage(BigDecimal min, BigDecimal max, BigDecimal actual) {
        if (actual.compareTo(BigDecimal.ZERO) >= 0) {
            // For positive values (0 to max)
            return actual.multiply(new BigDecimal("100")).divide(max, 2, RoundingMode.HALF_UP);
        } else {
            // For negative values (min to 0)
            return actual.multiply(new BigDecimal("100")).divide(min.abs(), 2, RoundingMode.HALF_UP);
        }
    }

    public static Direction categorizeScore(BigDecimal percentage) {
        // Absolute value of percentage for easier comparison
        BigDecimal absPercentage = percentage.abs();

        if (absPercentage.compareTo(new BigDecimal("85")) >= 0) {
            // 85-100% range
            return percentage.compareTo(BigDecimal.ZERO) >= 0 ? Direction.STRONG_BULL : Direction.STRONG_BEAR;
        } else if (absPercentage.compareTo(new BigDecimal("70")) >= 0) {
            // 70-84% range
            return percentage.compareTo(BigDecimal.ZERO) >= 0 ? Direction.BULL : Direction.BEAR;
        } else {
            // 0-69% range
            return Direction.NEUTRAL;
        }
    }

    public static void main(String[] args) {
        BigDecimal min = new BigDecimal("-3");
        BigDecimal max = new BigDecimal("3");
        BigDecimal actual = new BigDecimal("2");
        BigDecimal percentage = calculateBipolarPercentage(min, max, actual);
        System.out.println("Percentage: " + percentage);
        Direction direction = categorizeScore(percentage);
        System.out.println("Direction: " + direction);

        BigDecimal min2 = new BigDecimal("-10");
        BigDecimal max2 = new BigDecimal("10");
        BigDecimal actual2 = new BigDecimal("9");

        BigDecimal percentage2 = calculateBipolarPercentage(min2, max2, actual2);
        Direction direction2 = categorizeScore(percentage2);
        System.out.println("Percentage: " + percentage2);
        System.out.println("Direction: " + direction2);
    }

    private String getIndicatorRangeMapKey(String indicatorName, String subCategory) {
        return StringUtils.upperCase(StringUtils.joinWith("_", indicatorName, subCategory));
    }
}
