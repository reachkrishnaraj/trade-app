package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.*;
import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import com.kraj.tradeapp.core.model.persistance.mongodb.TradeSignalScoreSnapshot;
import com.kraj.tradeapp.core.model.persistance.mongodb.TradeSignalScoreSnapshotLatest;
import com.kraj.tradeapp.core.repository.NotificationEventRepository;
import com.kraj.tradeapp.core.repository.mongodb.TradeSignalScoreSnapshotLatestRepository;
import com.kraj.tradeapp.core.repository.mongodb.TradeSignalScoreSnapshotRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeSignalSnapshotProcessor {

    private final ReentrantLock EVENT_PROCESSOR_JOB_LOCK = new ReentrantLock();

    private final LinkedList<Boolean> eventProcessCueQueue = new LinkedList<>();

    private final NotificationEventRepository notificationEventRepository;

    private final TradeSignalScoreSnapshotLatestRepository tradeSignalScoreSnapshotLatestRepository;

    private final TradeSignalScoreSnapshotRepository tradeSignalScoreSnapshotRepository;

    public Optional<TradeSignalScoreSnapshot> getLatestSnapshot(String symbol) {
        Optional<TradeSignalScoreSnapshotLatest> mayBeLatest = tradeSignalScoreSnapshotLatestRepository.findById(symbol);
        if (mayBeLatest.isEmpty()) {
            return Optional.empty();
        }
        return tradeSignalScoreSnapshotRepository.findById(mayBeLatest.get().getLatestRecordId());
    }

    public void notifyEventForProcessing() {
        eventProcessCueQueue.add(true);
    }

    @Scheduled(fixedRate = 5000)
    public void processEventJob() {
        try {
            if (!EVENT_PROCESSOR_JOB_LOCK.tryLock()) {
                return;
            }
            //            if (eventProcessCueQueue.isEmpty()) {
            //                EVENT_PROCESSOR_JOB_LOCK.unlock();
            //                return;
            //            }

            List<NotificationEvent> notificationEvents = notificationEventRepository.findEventsPendingTradeSignalProcessing(
                LocalDateTime.now().minusHours(4),
                LocalDateTime.now()
            );
            if (notificationEvents.isEmpty()) {
                EVENT_PROCESSOR_JOB_LOCK.unlock();
                return;
            }
            processEventForTradeSignalSnapshot(notificationEvents);
            eventProcessCueQueue.clear();
        } catch (Exception e) {
            log.error("Error in processing event job", e);
        } finally {
            if (EVENT_PROCESSOR_JOB_LOCK.isLocked()) {
                EVENT_PROCESSOR_JOB_LOCK.unlock();
            }
        }
    }

    public void processEventForTradeSignalSnapshot(List<NotificationEvent> notificationEvents) {
        for (NotificationEvent event : notificationEvents) {
            try {
                processEvent(event);
                event.setTradeSignalProcessStatus(ProcessingStatus.PROCESSED.name());
                notificationEventRepository.save(event);
            } catch (Exception e) {
                log.error("Error in processing event id: {}, proceed to next", event.getId(), e);
            }
        }
    }

    private void processEvent(NotificationEvent event) {
        if (ProcessingStatus.fromString(event.getTradeSignalProcessStatus()) != ProcessingStatus.PENDING) {
            log.info("Skipping processing for event id {}, status: {}", event.getId(), event.getTradeSignalProcessStatus());
            return;
        }
        // process event
        Optional<TradeSignalScoreSnapshotLatest> mayBeLatest = tradeSignalScoreSnapshotLatestRepository.findById(event.getSymbol());
        TradeSignalScoreSnapshot snapshot = null;
        if (mayBeLatest.isEmpty()) {
            log.info("No latest snapshot found for symbol {}, starting snapshot from now", event.getSymbol());
            snapshot = TradeSignalScoreSnapshot.builder()
                .id(UUID.randomUUID().toString())
                .symbol(event.getSymbol())
                .dateTime(CommonUtil.getNYLocalDateTimeNow())
                .candleIntervalGroupedRecords(new ArrayList<>())
                .minScore(event.getMinScore())
                .maxScore(event.getMaxScore())
                .score(event.getScore())
                .direction(event.getDirection())
                .build();
        } else {
            log.info(
                "Latest snapshot found for symbol {}, id {}, will update the snapshot",
                event.getSymbol(),
                mayBeLatest.get().getLatestRecordId()
            );
            snapshot = tradeSignalScoreSnapshotRepository
                .findById(mayBeLatest.get().getLatestRecordId())
                .orElseThrow(
                    () ->
                        new RuntimeException(
                            "Latest snapshot, symbol:%s not found, id:%s ".formatted(
                                    event.getSymbol(),
                                    mayBeLatest.get().getLatestRecordId()
                                )
                        )
                );
        }

        //get the matching candle type interval record for the event
        CandleIntervalGroupedRecord matchingCandleTypeIntervalGroupedRecord = getCandleTypeIntervalGroupedRecord(snapshot, event).orElseGet(
            () -> initializeCandleTypeIntervalGroupedRecord(event)
        );

        //get the matching indicator record for the event
        IndicatorScoreRecord matchingIndicatorScoreRecord = getMatchingIndicatorScoreRecord(
            matchingCandleTypeIntervalGroupedRecord,
            event
        ).orElseGet(() -> initIndicatorScoreRecord(event));

        //get the matching sub category record
        IndicatorSubCategoryScoreRecord matchingIndicatorSubCategoryScoreRecord = getMatchingSubCategoryScoreRecord(
            matchingIndicatorScoreRecord,
            event
        ).orElseGet(() -> initializeSubCategoryScoreRecord(event));

        //update the sub category record
        matchingIndicatorSubCategoryScoreRecord.setMaxScore(event.getMaxScore());
        matchingIndicatorSubCategoryScoreRecord.setMinScore(event.getMinScore());
        matchingIndicatorSubCategoryScoreRecord.setScore(event.getScore());

        BigDecimal subcategoryScorePercent = ScoringService.calculateBipolarPercentage(
            matchingIndicatorSubCategoryScoreRecord.getMinScore(),
            matchingIndicatorSubCategoryScoreRecord.getMaxScore(),
            matchingIndicatorSubCategoryScoreRecord.getScore()
        );
        matchingIndicatorSubCategoryScoreRecord.setScorePercentage(subcategoryScorePercent);
        matchingIndicatorSubCategoryScoreRecord.setDirection(ScoringService.categorizeScore(subcategoryScorePercent).name());
        matchingIndicatorSubCategoryScoreRecord.setLastMsg(event.getRawAlertMsg());
        matchingIndicatorSubCategoryScoreRecord.setLastMsgDateTime(CommonUtil.getNYLocalDateTimeNow().toString());
        matchingIndicatorSubCategoryScoreRecord.setStrategy(event.isStrategy());
        matchingIndicatorSubCategoryScoreRecord.setStrategyName(event.getStrategyName());

        //replace the sub category record in the indicator score record
        List<IndicatorSubCategoryScoreRecord> subCategoryScoreRecords = matchingIndicatorScoreRecord.getSubCategoryScores();
        subCategoryScoreRecords.removeIf(record -> StringUtils.equals(record.getKey(), matchingIndicatorSubCategoryScoreRecord.getKey()));
        subCategoryScoreRecords.add(matchingIndicatorSubCategoryScoreRecord);
        matchingIndicatorScoreRecord.setSubCategoryScores(subCategoryScoreRecords);

        //aggregate the min, max & score for each sub indicator record
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal minScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        for (IndicatorSubCategoryScoreRecord subCategoryScoreRecord : matchingIndicatorScoreRecord.getSubCategoryScores()) {
            totalScore = totalScore.add(subCategoryScoreRecord.getScore());
            minScore = minScore.add(subCategoryScoreRecord.getMinScore());
            maxScore = maxScore.add(subCategoryScoreRecord.getMaxScore());
        }
        matchingIndicatorScoreRecord.setScore(totalScore);
        matchingIndicatorScoreRecord.setMinScore(minScore);
        matchingIndicatorScoreRecord.setMaxScore(maxScore);
        //derive the direction based on the total score
        BigDecimal indicatorScorePercent = ScoringService.calculateBipolarPercentage(
            matchingIndicatorScoreRecord.getMinScore(),
            matchingIndicatorScoreRecord.getMaxScore(),
            matchingIndicatorScoreRecord.getScore()
        );
        matchingIndicatorScoreRecord.setScorePercentage(indicatorScorePercent);
        matchingIndicatorScoreRecord.setDirection(ScoringService.categorizeScore(indicatorScorePercent).name());

        //replace the indicator record in the candle type interval grouped record
        List<IndicatorScoreRecord> indicatorScoreRecords = matchingCandleTypeIntervalGroupedRecord.getIndicatorScoreRecords();
        indicatorScoreRecords.removeIf(record -> StringUtils.equals(record.getKey(), matchingIndicatorScoreRecord.getKey()));
        indicatorScoreRecords.add(matchingIndicatorScoreRecord);
        matchingCandleTypeIntervalGroupedRecord.setIndicatorScoreRecords(indicatorScoreRecords);

        //aggregate the min, max & score for each indicator record
        BigDecimal totalCandleTypeIntervalScore = BigDecimal.ZERO;
        BigDecimal minCandleTypeIntervalScore = BigDecimal.ZERO;
        BigDecimal maxCandleTypeIntervalScore = BigDecimal.ZERO;
        for (IndicatorScoreRecord indicatorScoreRecord : matchingCandleTypeIntervalGroupedRecord.getIndicatorScoreRecords()) {
            totalCandleTypeIntervalScore = totalCandleTypeIntervalScore.add(indicatorScoreRecord.getScore());
            minCandleTypeIntervalScore = minCandleTypeIntervalScore.add(indicatorScoreRecord.getMinScore());
            maxCandleTypeIntervalScore = maxCandleTypeIntervalScore.add(indicatorScoreRecord.getMaxScore());
        }
        matchingCandleTypeIntervalGroupedRecord.setScore(totalCandleTypeIntervalScore);
        matchingCandleTypeIntervalGroupedRecord.setMinScore(minCandleTypeIntervalScore);
        matchingCandleTypeIntervalGroupedRecord.setMaxScore(maxCandleTypeIntervalScore);
        BigDecimal candleTypeIntervalScorePercentage = ScoringService.calculateBipolarPercentage(
            matchingCandleTypeIntervalGroupedRecord.getMinScore(),
            matchingCandleTypeIntervalGroupedRecord.getMaxScore(),
            matchingCandleTypeIntervalGroupedRecord.getScore()
        );
        matchingCandleTypeIntervalGroupedRecord.setScorePercentage(candleTypeIntervalScorePercentage);
        matchingCandleTypeIntervalGroupedRecord.setDirection(ScoringService.categorizeScore(candleTypeIntervalScorePercentage).name());

        //replace the candle type interval grouped record in the snapshot
        List<CandleIntervalGroupedRecord> candleIntervalGroupedRecords = snapshot.getCandleIntervalGroupedRecords();
        candleIntervalGroupedRecords.removeIf(
            record -> StringUtils.equals(record.getKey(), matchingCandleTypeIntervalGroupedRecord.getKey())
        );
        candleIntervalGroupedRecords.add(matchingCandleTypeIntervalGroupedRecord);
        snapshot.setCandleIntervalGroupedRecords(candleIntervalGroupedRecords);

        //aggregate the min, max & score for every candle type interval grouped record - OVERALL SCORE for the symbol(with different candle types & intervals)
        BigDecimal totalSnapshotScore = BigDecimal.ZERO;
        BigDecimal minSnapshotScore = BigDecimal.ZERO;
        BigDecimal maxSnapshotScore = BigDecimal.ZERO;
        for (CandleIntervalGroupedRecord candleIntervalGroupedRecord : snapshot.getCandleIntervalGroupedRecords()) {
            totalSnapshotScore = totalSnapshotScore.add(candleIntervalGroupedRecord.getScore());
            minSnapshotScore = minSnapshotScore.add(candleIntervalGroupedRecord.getMinScore());
            maxSnapshotScore = maxSnapshotScore.add(candleIntervalGroupedRecord.getMaxScore());
        }

        snapshot.setScore(totalSnapshotScore);
        snapshot.setMinScore(minSnapshotScore);
        snapshot.setMaxScore(maxSnapshotScore);
        BigDecimal scorePercentage = ScoringService.calculateBipolarPercentage(
            snapshot.getMinScore(),
            snapshot.getMaxScore(),
            snapshot.getScore()
        );
        snapshot.setScorePercentage(scorePercentage);
        snapshot.setDirection(ScoringService.categorizeScore(scorePercentage).name());

        //update the id with new snapshot id
        snapshot.setId(UUID.randomUUID().toString());
        tradeSignalScoreSnapshotRepository.save(snapshot);
        TradeSignalScoreSnapshotLatest tradeSignalScoreSnapshotLatest = TradeSignalScoreSnapshotLatest.builder()
            .latestRecordId(snapshot.getId())
            .symbol(snapshot.getSymbol())
            .lastUpdated(CommonUtil.getNYLocalDateTimeNow())
            .build();
        tradeSignalScoreSnapshotLatestRepository.save(tradeSignalScoreSnapshotLatest);
    }

    private IndicatorScoreRecord initIndicatorScoreRecord(NotificationEvent event) {
        return IndicatorScoreRecord.builder()
            .symbol(event.getSymbol())
            .key(IndicatorScoreRecord.getKeyFor(event.getSymbol(), event.getCandleType(), event.getInterval(), event.getIndicator()))
            .candleType(event.getCandleType())
            .interval(event.getInterval())
            .name(event.getIndicator())
            .displayName(event.getIndicatorDisplayName())
            .dateTime(CommonUtil.getNYLocalDateTimeNow())
            .minScore(new BigDecimal(-1))
            .maxScore(new BigDecimal(1))
            .score(event.getScore())
            .direction(event.getDirection())
            .lastMsg(event.getRawAlertMsg())
            .subCategoryScores(new ArrayList<>())
            .build();
    }

    private Optional<CandleIntervalGroupedRecord> getCandleTypeIntervalGroupedRecord(
        TradeSignalScoreSnapshot scoreSnapshot,
        NotificationEvent event
    ) {
        for (CandleIntervalGroupedRecord record : scoreSnapshot.getCandleIntervalGroupedRecords()) {
            String key = CandleIntervalGroupedRecord.getKeyFor(event.getSymbol(), event.getCandleType(), event.getInterval());
            if (StringUtils.equals(record.getKey(), key)) {
                return Optional.of(record);
            }
        }
        return Optional.empty();
    }

    private CandleIntervalGroupedRecord initializeCandleTypeIntervalGroupedRecord(NotificationEvent event) {
        String key = CandleIntervalGroupedRecord.getKeyFor(event.getSymbol(), event.getCandleType(), event.getInterval());
        return CandleIntervalGroupedRecord.builder()
            .symbol(event.getSymbol())
            .key(key)
            .candleType(event.getCandleType())
            .interval(event.getInterval())
            .direction(event.getDirection())
            .lastMsg(event.getRawAlertMsg())
            .lastMsgDateTime(event.getDatetime().toString())
            .minScore(event.getMinScore())
            .maxScore(event.getMaxScore())
            .score(event.getScore())
            .dateTime(CommonUtil.getNYLocalDateTimeNow())
            .indicatorScoreRecords(new ArrayList<>())
            .build();
    }

    private Optional<IndicatorScoreRecord> getMatchingIndicatorScoreRecord(
        CandleIntervalGroupedRecord candleIntervalGroupedRecord,
        NotificationEvent event
    ) {
        for (IndicatorScoreRecord record : candleIntervalGroupedRecord.getIndicatorScoreRecords()) {
            String key = IndicatorScoreRecord.getKeyFor(
                event.getSymbol(),
                event.getCandleType(),
                event.getInterval(),
                event.getIndicator()
            );
            if (StringUtils.equals(record.getKey(), key)) {
                return Optional.of(record);
            }
        }
        return Optional.empty();
    }

    private Optional<IndicatorSubCategoryScoreRecord> getMatchingSubCategoryScoreRecord(
        IndicatorScoreRecord record,
        NotificationEvent event
    ) {
        for (IndicatorSubCategoryScoreRecord subCategoryScoreRecord : record.getSubCategoryScores()) {
            String key = IndicatorSubCategoryScoreRecord.getKeyFor(
                event.getSymbol(),
                event.getCandleType(),
                event.getInterval(),
                event.getIndicator(),
                event.getIndicatorSubCategory()
            );
            if (StringUtils.equals(subCategoryScoreRecord.getKey(), key)) {
                return Optional.of(subCategoryScoreRecord);
            }
        }
        return Optional.empty();
    }

    private IndicatorSubCategoryScoreRecord initializeSubCategoryScoreRecord(NotificationEvent event) {
        String key = IndicatorSubCategoryScoreRecord.getKeyFor(
            event.getSymbol(),
            event.getCandleType(),
            event.getInterval(),
            event.getIndicator(),
            event.getIndicatorSubCategory()
        );
        return IndicatorSubCategoryScoreRecord.builder()
            .key(key)
            .symbol(event.getSymbol())
            .candleType(event.getCandleType())
            .interval(event.getInterval())
            .indicatorName(event.getIndicator())
            .indicatorDisplayName(event.getIndicatorDisplayName())
            .name(event.getIndicatorSubCategory())
            .displayName(event.getIndicatorSubCategoryDisplayName())
            .minScore(event.getMinScore())
            .maxScore(event.getMaxScore())
            .score(event.getScore())
            .direction(event.getDirection())
            .lastMsg(event.getRawAlertMsg())
            .lastMsgDateTime(ZonedDateTime.now().toString())
            .isStrategy(event.isStrategy())
            .strategyName(event.getStrategyName())
            .build();
    }
}
