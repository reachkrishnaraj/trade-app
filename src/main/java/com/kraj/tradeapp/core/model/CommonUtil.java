package com.kraj.tradeapp.core.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class CommonUtil {

    public static LocalDateTime getNYLocalDateTimeNow() {
        return ZonedDateTime.now().withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime();
    }
}
