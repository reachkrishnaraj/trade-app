package com.kraj.tradeapp.core.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointOfInterestEvent {

    private String eventCode;

    private EventInterval eventInterval;
}
