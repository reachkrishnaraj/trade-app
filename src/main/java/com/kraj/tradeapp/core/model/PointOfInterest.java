package com.kraj.tradeapp.core.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointOfInterest {

    private String symbol;

    private String name;

    private String type;

    private BigDecimal upperPrice;

    private BigDecimal lowerPrice;

    private String alertMessage;

    private EventInterval eventInterval;

    private List<PointOfInterestEvent> pointOfInterestEventList;
}
