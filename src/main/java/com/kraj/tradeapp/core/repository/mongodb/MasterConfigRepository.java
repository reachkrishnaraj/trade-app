package com.kraj.tradeapp.core.repository.mongodb;

import com.kraj.tradeapp.core.model.persistance.mongodb.MasterConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterConfigRepository extends MongoRepository<MasterConfig, String> {}
