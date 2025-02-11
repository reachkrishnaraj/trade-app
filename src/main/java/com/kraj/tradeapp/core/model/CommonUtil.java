package com.kraj.tradeapp.core.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class CommonUtil {

    public static LocalDateTime getNYLocalDateTimeNow() {
        return ZonedDateTime.now(ZoneId.of("UTC")) // Use UTC as base
            .withZoneSameInstant(ZoneId.of("America/New_York"))
            .toLocalDateTime();
    }

    public static LocalDateTime getUTCNow() {
        return ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime();
    }
}
