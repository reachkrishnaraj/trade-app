package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.persistance.AggregatedSqlOHLC;
import com.kraj.tradeapp.core.model.persistance.AggregatedSqlOHLCPK;
import com.kraj.tradeapp.core.model.persistance.AggregatedSqlOHLCProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AggregatedSqlOHLCRepository extends JpaRepository<AggregatedSqlOHLC, AggregatedSqlOHLCPK> {
    @Modifying
    @Transactional
    @Query(
        value = """
        WITH ohlc_raw AS (
            SELECT\s
                symbol,
                FLOOR(id / ?) * ? AS aggregated_id,
                ? AS timeframe,
                open AS raw_open,
                high AS raw_high,
                low AS raw_low,
                close AS raw_close,
                id,
                to_char(to_timestamp(id / 1000.0) AT TIME ZONE 'America/New_York', 'YYYYMMDDHH24MI')::BIGINT AS ny_date_time_id  -- ✅ Fix: Compute NY DateTime ID
            FROM ohlc_data
            WHERE id >= (
                EXTRACT(EPOCH FROM (NOW() - ? * INTERVAL '1 millisecond')) * 1000
            )
        ),
        ohlc_aggregated AS (
            SELECT
                symbol,
                aggregated_id,
                timeframe,
                FIRST_VALUE(raw_open) OVER (PARTITION BY symbol, aggregated_id ORDER BY id ASC) AS open,
                MAX(raw_high) OVER (PARTITION BY symbol, aggregated_id) AS high,
                MIN(raw_low) OVER (PARTITION BY symbol, aggregated_id) AS low,
                LAST_VALUE(raw_close) OVER (PARTITION BY symbol, aggregated_id ORDER BY id ASC ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS close,
                FIRST_VALUE(ny_date_time_id) OVER (PARTITION BY symbol, aggregated_id ORDER BY id ASC) AS ny_date_time_id  -- ✅ Fix: Use NY DateTime ID
            FROM ohlc_raw
        )
        INSERT INTO ohlc_aggregated (id, symbol, timestamp, timeframe, ny_date_time_id, open, high, low, close)  -- ✅ Fix: Added `ny_date_time_id`
        SELECT aggregated_id, symbol, to_timestamp(aggregated_id / 1000.0), timeframe, ny_date_time_id, open, high, low, close FROM ohlc_aggregated
        ON CONFLICT (id, timeframe) DO UPDATE
        SET ny_date_time_id = EXCLUDED.ny_date_time_id,
            open = EXCLUDED.open,
            high = EXCLUDED.high,
            low = EXCLUDED.low,
            close = EXCLUDED.close;
        """,
        nativeQuery = true
    )
    void aggregateOHLC(long intervalMillis, long intervalMillis2, String timeframe, long lookbackMillis);

    @Transactional
    @Query(
        value = """
            WITH ohlc_raw AS (
                SELECT
                    symbol,
                    FLOOR(id / :intervalMillis) * :intervalMillis AS aggregated_id,
                    :timeframe AS timeframe,
                    open AS raw_open,
                    high AS raw_high,
                    low AS raw_low,
                    close AS raw_close,
                    id,
                    to_char(to_timestamp(id / 1000.0) AT TIME ZONE 'America/New_York', 'YYYYMMDDHH24MI')::BIGINT AS ny_date_time_id
                FROM ohlc_data
                WHERE id >= (
                    EXTRACT(EPOCH FROM (NOW() - (:lookbackMillis) * INTERVAL '1 millisecond')) * 1000
                )
            )
            SELECT
                aggregated_id AS id,
                symbol,
                to_timestamp(aggregated_id / 1000.0) AS timestamp,
                timeframe,
                MIN(ny_date_time_id) AS ny_date_time_id,
                (array_agg(raw_open ORDER BY id ASC))[1] AS open,
                MAX(raw_high) AS high,
                MIN(raw_low) AS low,
                (array_agg(raw_close ORDER BY id DESC))[1] AS close
            FROM ohlc_raw
            GROUP BY symbol, aggregated_id, timeframe;
        """,
        nativeQuery = true
    )
    List<Object[]> fetchAggregatedOHLC(
        @Param("intervalMillis") long intervalMillis,
        @Param("timeframe") String timeframe,
        @Param("lookbackMillis") long lookbackMillis
    );

    //    @Query(value = """
    //    WITH ohlc_raw AS (
    //        SELECT
    //            symbol,
    //            FLOOR(
    //                EXTRACT(EPOCH FROM timezone('America/New_York', timestamp)) * 1000 / :intervalMillis
    //            ) * :intervalMillis AS aggregated_epoch,
    //            :timeframe AS timeframe,
    //            to_char(timezone('America/New_York', timestamp), 'YYYYMMDDHH24MI')::BIGINT AS ny_date_time_id,
    //            open AS raw_open,
    //            high AS raw_high,
    //            low AS raw_low,
    //            close AS raw_close,
    //            id
    //        FROM ohlc_data
    //        WHERE id >= (
    //            EXTRACT(EPOCH FROM (NOW() - (:lookbackMillis) * INTERVAL '1 millisecond')) * 1000
    //        )
    //    ),
    //    ohlc_grouped AS (
    //        SELECT
    //            symbol,
    //            aggregated_epoch,
    //            timeframe,
    //            MIN(ny_date_time_id) AS ny_date_time_id,
    //            MIN(id) AS min_id,
    //            MAX(id) AS max_id,
    //            MAX(raw_high) AS high,
    //            MIN(raw_low) AS low
    //        FROM ohlc_raw
    //        GROUP BY symbol, aggregated_epoch, timeframe
    //    ),
    //    ohlc_aggregated AS (
    //        SELECT
    //            og.symbol,
    //            og.aggregated_epoch,
    //            og.timeframe,
    //            og.ny_date_time_id,
    //            (SELECT raw_open FROM ohlc_raw WHERE id = og.min_id) AS open,
    //            og.high,
    //            og.low,
    //            (SELECT raw_close FROM ohlc_raw WHERE id = og.max_id) AS close
    //        FROM ohlc_grouped og
    //    )
    //    SELECT
    //        to_timestamp(aggregated_epoch / 1000) AS timestamp,
    //        symbol,
    //        timeframe,
    //        ny_date_time_id,
    //        open,
    //        high,
    //        low,
    //        close
    //    FROM ohlc_aggregated
    //    ORDER BY timestamp;
    //""", nativeQuery = true)
    //    List<AggregatedSqlOHLCProjection> fetchAggregatedOHLC_futures(@Param("intervalMillis") long intervalMillis,
    //                                                                  @Param("timeframe") String timeframe,
    //                                                                  @Param("lookbackMillis") long lookbackMillis);

    @Query(
        value = """
            WITH ohlc_raw AS (
                SELECT
                    symbol,
                    FLOOR(
                        EXTRACT(EPOCH FROM timezone('America/New_York', timestamp)) * 1000 / :intervalMillis
                    ) * :intervalMillis AS aggregated_epoch,
                    :timeframe AS timeframe,
                    to_char(timezone('America/New_York', timestamp), 'YYYYMMDDHH24MI')::BIGINT AS ny_date_time_id,
                    timestamp AS raw_timestamp,
                    open AS raw_open,
                    high AS raw_high,
                    low AS raw_low,
                    close AS raw_close
                FROM ohlc_data
                WHERE timestamp >= (NOW() - (:lookbackMillis) * INTERVAL '1 millisecond')
            ),
            ohlc_grouped AS (
                SELECT
                    symbol,
                    aggregated_epoch,
                    timeframe,
                    MIN(ny_date_time_id) AS ny_date_time_id,
                    MIN(raw_timestamp) AS min_timestamp,
                    MAX(raw_timestamp) AS max_timestamp,
                    MAX(raw_high) AS high,
                    MIN(raw_low) AS low
                FROM ohlc_raw
                GROUP BY symbol, aggregated_epoch, timeframe
            ),
            ohlc_aggregated AS (
                SELECT
                    og.symbol,
                    og.aggregated_epoch,
                    og.timeframe,
                    og.ny_date_time_id,
                    (SELECT raw_open FROM ohlc_raw WHERE raw_timestamp = og.min_timestamp AND ohlc_raw.symbol = og.symbol) AS open,
                    og.high,
                    og.low,
                    (SELECT raw_close FROM ohlc_raw WHERE raw_timestamp = og.max_timestamp AND ohlc_raw.symbol = og.symbol) AS close
                FROM ohlc_grouped og
            )
            SELECT
                to_timestamp(aggregated_epoch / 1000) AS timestamp,
                symbol,
                timeframe,
                ny_date_time_id,
                open,
                high,
                low,
                close
            FROM ohlc_aggregated
            ORDER BY timestamp;
        """,
        nativeQuery = true
    )
    List<AggregatedSqlOHLCProjection> fetchAggregatedOHLC_futures(
        @Param("intervalMillis") long intervalMillis,
        @Param("timeframe") String timeframe,
        @Param("lookbackMillis") long lookbackMillis
    );

    @Query(
        value = """
            WITH ohlc_raw AS (
                                             SELECT
                                                 symbol,
                                                 -- ✅ Align timestamps correctly by anchoring to futures session start at 6PM EST
                                                 FLOOR(
                                                     (EXTRACT(EPOCH FROM timestamp AT TIME ZONE 'America/New_York') - MOD(EXTRACT(EPOCH FROM timestamp AT TIME ZONE 'America/New_York'), :intervalSeconds))
                                                 ) AS aggregated_epoch,
                                                 :timeframe AS timeframe,
                                                 to_char(timestamp AT TIME ZONE 'America/New_York', 'YYYYMMDDHH24MI')::BIGINT AS ny_date_time_id,
                                                 open AS raw_open,
                                                 high AS raw_high,
                                                 low AS raw_low,
                                                 close AS raw_close,
                                                 id
                                             FROM ohlc_data
                                             WHERE timestamp >= (NOW() - (:lookbackSeconds * INTERVAL '1 second'))
                                         ),
                                         ohlc_grouped AS (
                                             SELECT
                                                 symbol,
                                                 aggregated_epoch,
                                                 timeframe,
                                                 MIN(ny_date_time_id) AS ny_date_time_id,
                                                 MIN(id) AS min_id,
                                                 MAX(id) AS max_id,
                                                 MAX(raw_high) AS high,
                                                 MIN(raw_low) AS low
                                             FROM ohlc_raw
                                             GROUP BY symbol, aggregated_epoch, timeframe
                                         ),
                                         ohlc_aggregated AS (
                                             SELECT
                                                 og.symbol,
                                                 og.aggregated_epoch,
                                                 og.timeframe,
                                                 og.ny_date_time_id,
                                                 (SELECT raw_open FROM ohlc_raw WHERE id = og.min_id) AS open,
                                                 og.high,
                                                 og.low,
                                                 (SELECT raw_close FROM ohlc_raw WHERE id = og.max_id) AS close
                                             FROM ohlc_grouped og
                                         )
                                         SELECT
                                             to_timestamp(aggregated_epoch) AS timestamp,  -- ✅ Convert epoch back to timestamp
                                             symbol,
                                             timeframe,
                                             ny_date_time_id,
                                             open,
                                             high,
                                             low,
                                             close
                                         FROM ohlc_aggregated
                                         ORDER BY timestamp;
        """,
        nativeQuery = true
    )
    List<AggregatedSqlOHLCProjection> fetchAggregatedOHLC_futuresv2(
        @Param("intervalSeconds") long intervalSeconds,
        @Param("timeframe") String timeframe,
        @Param("lookbackSeconds") long lookbackSeconds
    );

    @Query(
        value = """
            WITH ohlc_raw AS (
                SELECT
                    symbol,
                    FLOOR(
                        (EXTRACT(EPOCH FROM timezone('America/New_York', timestamp)) - 18 * 3600) / :intervalSeconds
                    ) * :intervalSeconds + 18 * 3600 AS aggregated_epoch,
                    :timeframe AS timeframe,
                    to_char(timezone('America/New_York', timestamp), 'YYYYMMDDHH24MI')::BIGINT AS ny_date_time_id,
                    timestamp AS raw_timestamp,
                    open AS raw_open,
                    high AS raw_high,
                    low AS raw_low,
                    close AS raw_close
                FROM ohlc_data
                WHERE timestamp >= (NOW() - (:lookbackSeconds * INTERVAL '1 second'))
            ),
            ohlc_grouped AS (
                SELECT
                    symbol,
                    aggregated_epoch,
                    timeframe,
                    MIN(ny_date_time_id) AS ny_date_time_id,
                    MIN(raw_timestamp) AS min_timestamp,
                    MAX(raw_timestamp) AS max_timestamp,
                    MAX(raw_high) AS high,
                    MIN(raw_low) AS low
                FROM ohlc_raw
                GROUP BY symbol, aggregated_epoch, timeframe
            ),
            ohlc_aggregated AS (
                SELECT
                    og.symbol,
                    og.aggregated_epoch,
                    og.timeframe,
                    og.ny_date_time_id,
                    (SELECT raw_open FROM ohlc_raw WHERE raw_timestamp = og.min_timestamp AND ohlc_raw.symbol = og.symbol) AS open,
                    og.high,
                    og.low,
                    (SELECT raw_close FROM ohlc_raw WHERE raw_timestamp = og.max_timestamp AND ohlc_raw.symbol = og.symbol) AS close
                FROM ohlc_grouped og
            )
            SELECT
                to_timestamp(aggregated_epoch) AS timestamp,
                symbol,
                timeframe,
                ny_date_time_id,
                open,
                high,
                low,
                close
            FROM ohlc_aggregated
            ORDER BY timestamp;
        """,
        nativeQuery = true
    )
    List<AggregatedSqlOHLCProjection> fetchAggregatedOHLC_futuresV3(
        @Param("intervalSeconds") long intervalSeconds,
        @Param("timeframe") String timeframe,
        @Param("lookbackSeconds") long lookbackSeconds
    );

    @Query(
        value = """
            WITH ohlc_raw AS (
                SELECT
                    symbol,
                -- ✅ Align to Futures session start (6 PM EST)
            FLOOR(
                    (EXTRACT(EPOCH FROM timezone('America/New_York', timestamp)) - 21600) / (:intervalSeconds)
                ) * :intervalSeconds + 21600 AS aggregated_epoch,
                :timeframe AS timeframe,
            to_char(timezone('America/New_York', timestamp), 'YYYYMMDDHH24MI')::BIGINT AS ny_date_time_id,
            timestamp AS raw_timestamp,
            open AS raw_open,
            high AS raw_high,
            low AS raw_low,
            close AS raw_close
            FROM ohlc_data
            WHERE timestamp >= NOW() - (:lookbackSeconds * INTERVAL '1 second')
                ),
            ohlc_grouped AS (
                SELECT
                    symbol,
                aggregated_epoch,
                timeframe,
                MIN(ny_date_time_id) AS ny_date_time_id,
            MIN(raw_timestamp) AS min_timestamp,
            MAX(raw_timestamp) AS max_timestamp,
            MAX(raw_high) AS high,
            MIN(raw_low) AS low
            FROM ohlc_raw
            GROUP BY symbol, aggregated_epoch, timeframe
        ),
            ohlc_aggregated AS (
                SELECT
                    og.symbol,
                og.aggregated_epoch,
                og.timeframe,
                og.ny_date_time_id,
                (SELECT raw_open FROM ohlc_raw WHERE raw_timestamp = og.min_timestamp AND ohlc_raw.symbol = og.symbol) AS open,
            og.high,
            og.low,
                (SELECT raw_close FROM ohlc_raw WHERE raw_timestamp = og.max_timestamp AND ohlc_raw.symbol = og.symbol) AS close
            FROM ohlc_grouped og
        )
            SELECT
            to_timestamp(aggregated_epoch) AT TIME ZONE 'America/New_York' AS timestamp,  -- ✅ Ensure NY Local Time
            symbol,
            timeframe,
            ny_date_time_id,
            open,
            high,
            low,
            close
            FROM ohlc_aggregated
            ORDER BY timestamp;
            """,
        nativeQuery = true
    )
    List<AggregatedSqlOHLCProjection> fetchAggregatedOHLC_futuresV4(
        @Param("intervalSeconds") long intervalSeconds,
        @Param("timeframe") String timeframe,
        @Param("lookbackSeconds") long lookbackSeconds
    );

    @Query(
        value = """
            WITH ohlc_raw AS (
                SELECT
                    symbol,
                -- ✅ Align 4H candles to Futures session (6 PM NY time)
            FLOOR(
                    (EXTRACT(EPOCH FROM timezone('America/New_York', timestamp)) - 18 * 3600) / (:intervalSeconds)
                ) * :intervalSeconds + 18 * 3600 AS aggregated_epoch,
                :timeframe AS timeframe,
            to_char(timezone('America/New_York', timestamp), 'YYYYMMDDHH24MI')::BIGINT AS ny_date_time_id,
            timestamp AS raw_timestamp,
            open AS raw_open,
            high AS raw_high,
            low AS raw_low,
            close AS raw_close
            FROM ohlc_data
            WHERE timestamp >= NOW() - (:lookbackSeconds * INTERVAL '1 second')
                ),
            ohlc_grouped AS (
                SELECT
                    symbol,
                aggregated_epoch,
                timeframe,
                MIN(ny_date_time_id) AS ny_date_time_id,
            MIN(raw_timestamp) AS min_timestamp,
            MAX(raw_timestamp) AS max_timestamp,
            MAX(raw_high) AS high,
            MIN(raw_low) AS low
            FROM ohlc_raw
            GROUP BY symbol, aggregated_epoch, timeframe
        ),
            ohlc_aggregated AS (
                SELECT
                    og.symbol,
                og.aggregated_epoch,
                og.timeframe,
                og.ny_date_time_id,
                (SELECT raw_open FROM ohlc_raw WHERE raw_timestamp = og.min_timestamp AND ohlc_raw.symbol = og.symbol) AS open,
            og.high,
            og.low,
                (SELECT raw_close FROM ohlc_raw WHERE raw_timestamp = og.max_timestamp AND ohlc_raw.symbol = og.symbol) AS close
            FROM ohlc_grouped og
        )
            SELECT
            to_timestamp(aggregated_epoch) AT TIME ZONE 'America/New_York' AS timestamp,  -- ✅ Convert to NY Local Time
                symbol,
                timeframe,
                ny_date_time_id,
                open,
                high,
                low,
                close
            FROM ohlc_aggregated
            WHERE EXTRACT(HOUR FROM to_timestamp(aggregated_epoch) AT TIME ZONE 'America/New_York') IN (18, 22, 2, 6, 10, 14)  -- ✅ Force correct 4H times
            ORDER BY timestamp;
        """,
        nativeQuery = true
    )
    List<AggregatedSqlOHLCProjection> fetchAggregatedOHLC_futuresV5(
        @Param("intervalSeconds") long intervalSeconds,
        @Param("timeframe") String timeframe,
        @Param("lookbackSeconds") long lookbackSeconds
    );

    @Query(
        value = """
            WITH ohlc_raw AS (
                SELECT
                    symbol,
                    -- Align to futures trading session starting at 6 PM NY time
                    FLOOR(
                        (EXTRACT(EPOCH FROM timezone('America/New_York', timestamp)) - 18 * 3600) / :intervalSeconds
                    ) * :intervalSeconds + 18 * 3600 AS aggregated_epoch,
                    :timeframe AS timeframe,
                    to_char(timezone('America/New_York', timestamp), 'YYYYMMDDHH24MI')::BIGINT AS ny_date_time_id,
                    timestamp AS raw_timestamp,
                    open AS raw_open,
                    high AS raw_high,
                    low AS raw_low,
                    close AS raw_close
                FROM ohlc_data
                WHERE timestamp >= NOW() - (:lookbackSeconds * INTERVAL '1 second')
            ),
            ohlc_grouped AS (
                SELECT
                    symbol,
                    aggregated_epoch,
                    timeframe,
                    MIN(ny_date_time_id) AS ny_date_time_id,
                    MIN(raw_timestamp) AS min_timestamp,
                    MAX(raw_timestamp) AS max_timestamp,
                    MAX(raw_high) AS high,
                    MIN(raw_low) AS low
                FROM ohlc_raw
                GROUP BY symbol, aggregated_epoch, timeframe
            ),
            ohlc_aggregated AS (
                SELECT
                    og.symbol,
                    og.aggregated_epoch,
                    og.timeframe,
                    og.ny_date_time_id,
                    (SELECT raw_open FROM ohlc_raw WHERE raw_timestamp = og.min_timestamp AND ohlc_raw.symbol = og.symbol) AS open,
                    og.high,
                    og.low,
                    (SELECT raw_close FROM ohlc_raw WHERE raw_timestamp = og.max_timestamp AND ohlc_raw.symbol = og.symbol) AS close
                FROM ohlc_grouped og
            )
            SELECT
                to_timestamp(aggregated_epoch) AS timestamp,
                symbol,
                timeframe,
                ny_date_time_id,
                open,
                high,
                low,
                close
            FROM ohlc_aggregated
            -- Remove the hour filtering since we want all candles aligned to session start
            ORDER BY timestamp;
        """,
        nativeQuery = true
    )
    List<AggregatedSqlOHLCProjection> fetchAggregatedOHLC_futuresV6(
        @Param("intervalSeconds") long intervalSeconds,
        @Param("timeframe") String timeframe,
        @Param("lookbackSeconds") long lookbackSeconds
    );

    @Query(
        value = """
            WITH ohlc_raw AS (
                SELECT
                    symbol,
                    -- Adjust offset to align to exactly 6 PM NY time (use 17 * 3600 to get 18:00)
                    FLOOR(
                        (EXTRACT(EPOCH FROM timezone('America/New_York', timestamp)) - 17 * 3600) / :intervalSeconds
                    ) * :intervalSeconds + 17 * 3600 AS aggregated_epoch,
                    :timeframe AS timeframe,
                    to_char(timezone('America/New_York', timestamp), 'YYYYMMDDHH24MI')::BIGINT AS ny_date_time_id,
                    timestamp AS raw_timestamp,
                    open AS raw_open,
                    high AS raw_high,
                    low AS raw_low,
                    close AS raw_close
                FROM ohlc_data
                WHERE timestamp >= NOW() - (:lookbackSeconds * INTERVAL '1 second')
            ),
            ohlc_grouped AS (
                SELECT
                    symbol,
                    aggregated_epoch,
                    timeframe,
                    MIN(ny_date_time_id) AS ny_date_time_id,
                    MIN(raw_timestamp) AS min_timestamp,
                    MAX(raw_timestamp) AS max_timestamp,
                    MAX(raw_high) AS high,
                    MIN(raw_low) AS low
                FROM ohlc_raw
                GROUP BY symbol, aggregated_epoch, timeframe
            ),
            ohlc_aggregated AS (
                SELECT
                    og.symbol,
                    og.aggregated_epoch,
                    og.timeframe,
                    og.ny_date_time_id,
                    (SELECT raw_open FROM ohlc_raw WHERE raw_timestamp = og.min_timestamp AND ohlc_raw.symbol = og.symbol) AS open,
                    og.high,
                    og.low,
                    (SELECT raw_close FROM ohlc_raw WHERE raw_timestamp = og.max_timestamp AND ohlc_raw.symbol = og.symbol) AS close
                FROM ohlc_grouped og
            )
            SELECT
                to_timestamp(aggregated_epoch) AS timestamp,
                symbol,
                timeframe,
                ny_date_time_id,
                open,
                high,
                low,
                close
            FROM ohlc_aggregated
            ORDER BY timestamp;
        """,
        nativeQuery = true
    )
    List<AggregatedSqlOHLCProjection> fetchAggregatedOHLC_futuresV7(
        @Param("intervalSeconds") long intervalSeconds,
        @Param("timeframe") String timeframe,
        @Param("lookbackSeconds") long lookbackSeconds
    );
}
