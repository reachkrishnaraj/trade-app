package com.kraj.tradeapp.core.service;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import com.kraj.tradeapp.core.model.persistance.mongodb.OHLCData;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class OHLCAggregationService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public static void main(String[] args) {
        System.out.println(Instant.now().toString());
        System.out.println(Instant.now().minus(3 * 60, ChronoUnit.MINUTES));
    }

    public List<OHLCData> aggregateOHLCWithLookback(String symbol, int intervalMinutes) {
        int lookbackMultiplier = 100; // Look back 3 intervals to catch late data
        Instant lookbackTime = Instant.now().minus(lookbackMultiplier * intervalMinutes, ChronoUnit.MINUTES);

        long intervalMillis = intervalMinutes * 60 * 1000;

        System.out.println("🔍 Lookback Time in Java: " + Date.from(lookbackTime));

        List<OHLCData> debugResults = mongoTemplate.find(
            Query.query(Criteria.where("symbol").is(symbol).and("timestamp").gte(Date.from(lookbackTime))),
            OHLCData.class,
            "ohlc_data"
        );

        System.out.println("✅ Matching Documents Count: " + debugResults.size());

        Aggregation debugAggregation = newAggregation(project("symbol", "open", "high", "low", "close", "timestamp"));
        AggregationResults<OHLCData> debugResults1 = mongoTemplate.aggregate(debugAggregation, "ohlc_data", OHLCData.class);
        System.out.println("✅ Documents after Projection: " + debugResults1.getMappedResults().size());

        Aggregation aggregation1 = newAggregation(
            match(Criteria.where("symbol").is(symbol).and("timestamp").gte(Date.from(lookbackTime))),
            project("symbol", "open", "high", "low", "close", "timestamp")
        );

        AggregationResults<OHLCData> results1 = mongoTemplate.aggregate(aggregation1, "ohlc_data", OHLCData.class);
        System.out.println("✅ Aggregated Documents: " + results1.getMappedResults().size());

        Aggregation aggregation = newAggregation(
            match(Criteria.where("symbol").is(symbol).and("timestamp").gte(Date.from(lookbackTime))), // Lookback window
            project("symbol", "open", "high", "low", "close", "timestamp")
                .and(
                    context ->
                        new Document(
                            "$subtract",
                            List.of(
                                new Document(
                                    "$toLong",
                                    new Document("$dateToString", new Document("format", "%Y%m%d%H%M%S").append("date", "$timestamp"))
                                ),
                                new Document(
                                    "$mod",
                                    List.of(
                                        new Document(
                                            "$toLong",
                                            new Document(
                                                "$dateToString",
                                                new Document("format", "%Y%m%d%H%M%S").append("date", "$timestamp")
                                            )
                                        ),
                                        intervalMillis
                                    )
                                )
                            )
                        )
                )
                .as("aggregatedTimestamp"),
            group("symbol", "aggregatedTimestamp")
                .first("open")
                .as("open")
                .max("high")
                .as("high")
                .min("low")
                .as("low")
                .last("close")
                .as("close"),
            project()
                .and("symbol")
                .as("symbol")
                .and("open")
                .as("open")
                .and("high")
                .as("high")
                .and("low")
                .as("low")
                .and("close")
                .as("close")
                .and(context -> new Document("$dateFromString", new Document("dateString", "$aggregatedTimestamp")))
                .as("timestamp")
        );

        // Run aggregation on "ohlc_data"
        AggregationResults<OHLCData> results = mongoTemplate.aggregate(aggregation, "ohlc_data", OHLCData.class);

        // Overwrite the "ohlc_aggregated" collection with new results
        //        mongoTemplate.dropCollection("ohlc_aggregated"); // Clear old data
        //        mongoTemplate.insert(results.getMappedResults(), "ohlc_aggregated"); // Insert new data

        return results.getMappedResults();
    }

    public List<OHLCData> aggregateOHLCWithLookbackV2(String symbol, int intervalMinutes) {
        int lookbackMultiplier = 100; // Look back 3 intervals to catch late data
        long lookbackMillis = Instant.now().minus(lookbackMultiplier * intervalMinutes, ChronoUnit.MINUTES).toEpochMilli();

        long intervalMillis = intervalMinutes * 60 * 1000;

        Aggregation aggregation = newAggregation(
            match(Criteria.where("symbol").is(symbol).and("_id").gte(lookbackMillis)), // ✅ Use `_id` instead of `timestamp`
            project("symbol", "open", "high", "low", "close", "_id")
                .and(context -> new Document("$subtract", List.of("$_id", new Document("$mod", List.of("$_id", intervalMillis)))))
                .as("aggregatedId"), // ✅ Bucket `_id` into intervals
            group("symbol", "aggregatedId").first("open").as("open").max("high").as("high").min("low").as("low").last("close").as("close"),
            project()
                .and("symbol")
                .as("symbol")
                .and("open")
                .as("open")
                .and("high")
                .as("high")
                .and("low")
                .as("low")
                .and("close")
                .as("close")
                .and(context -> new Document("$toDate", "$aggregatedId"))
                .as("timestamp") // ✅ Convert `_id` back to BSON Date
        );

        // Run aggregation on "ohlc_data"
        AggregationResults<OHLCData> results = mongoTemplate.aggregate(aggregation, "ohlc_data", OHLCData.class);

        // Overwrite the "ohlc_aggregated" collection with new results
        mongoTemplate.dropCollection("ohlc_aggregated"); // Clear old data
        mongoTemplate.insert(results.getMappedResults(), "ohlc_aggregated"); // Insert new data

        return results.getMappedResults();
    }
}
