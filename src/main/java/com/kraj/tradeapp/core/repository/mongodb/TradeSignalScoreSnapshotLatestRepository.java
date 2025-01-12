package com.kraj.tradeapp.core.repository.mongodb;

import com.kraj.tradeapp.core.model.persistance.mongodb.TradeSignalScoreSnapshotLatest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeSignalScoreSnapshotLatestRepository extends MongoRepository<TradeSignalScoreSnapshotLatest, String> {}
