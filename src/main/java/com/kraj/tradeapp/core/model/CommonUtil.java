package com.kraj.tradeapp.core.model;

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
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

    public static String getIsoNowStr() {
        return ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
    }

    public static String decodeBase64(String base64Encoded) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Encoded);
        return new String(decodedBytes);
    }

    public static String getTimeIndex(long milliseconds) {
        // Define date format (yyyyMMddHHmm)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        // Set UTC timezone to ensure consistency across systems
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Convert milliseconds to formatted string
        return dateFormat.format(new Date(milliseconds));
    }

    public static Long getTimeIndexNY(long milliseconds) {
        // Define date format (yyyyMMddHHmm)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");

        // Set New York timezone (Eastern Time, handles DST automatically)
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        // Convert milliseconds to formatted string
        return Long.parseLong(dateFormat.format(new Date(milliseconds)));
    }
}
