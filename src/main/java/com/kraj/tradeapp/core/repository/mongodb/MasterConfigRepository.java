package com.kraj.tradeapp.core.repository.mongodb;

import com.kraj.tradeapp.core.model.persistance.mongodb.MasterConfig;
import com.kraj.tradeapp.core.model.persistance.mongodb.TradeAccountConfig;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterConfigRepository extends MongoRepository<MasterConfig, String> {}
