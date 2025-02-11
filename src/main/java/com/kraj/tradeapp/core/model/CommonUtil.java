package com.kraj.tradeapp.core.model;

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.apache.commons.lang3.StringUtils;

public class CommonUtil {

    public static LocalDateTime getNYLocalDateTimeNow() {
        return ZonedDateTime.now(ZoneId.of("UTC")) // Use UTC as base
            .withZoneSameInstant(ZoneId.of("America/New_York"))
            .toLocalDateTime();
    }

    public static LocalDateTime getUTCNow() {
        return ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime();
    }

    public static boolean isNumeric(@Nullable String str) {
        if (StringUtils.isBlank(str) || str.isEmpty()) {
            return false;
        }
        try {
            new BigDecimal(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
