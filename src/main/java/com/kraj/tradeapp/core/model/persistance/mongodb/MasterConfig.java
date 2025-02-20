package com.kraj.tradeapp.core.model.persistance.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document(collection = "master_config")
@NoArgsConstructor
@AllArgsConstructor
public class MasterConfig {

    private boolean allAutomationEnabled;

    private boolean vivekAutomationEnabled;

    private boolean krajAutomationEnabled;
}
