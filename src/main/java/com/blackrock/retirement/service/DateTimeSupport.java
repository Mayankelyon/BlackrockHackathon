package com.blackrock.retirement.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Centralized date parsing and comparison for "YYYY-MM-DD HH:mm:ss" format.
 */
public final class DateTimeSupport {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter LENIENT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static LocalDateTime parse(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return null;
        String normalized = dateTimeStr.trim();
        if (normalized.length() == 16) {
            // "2023-10-12 20:15" -> add :00 for seconds
            normalized = normalized + ":00";
        }
        try {
            return LocalDateTime.parse(normalized, FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTimeStr.trim(), LENIENT);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    public static String format(LocalDateTime dt) {
        return dt == null ? null : dt.format(FORMATTER);
    }

    /** true if t >= start and t <= end (inclusive). */
    public static boolean inRangeInclusive(LocalDateTime t, String start, String end) {
        LocalDateTime s = parse(start);
        LocalDateTime e = parse(end);
        if (s == null || e == null || t == null) return false;
        return !t.isBefore(s) && !t.isAfter(e);
    }

    /** True if start1 is after start2 (start1 "starts latest"). */
    public static boolean startsLater(String start1, String start2) {
        LocalDateTime s1 = parse(start1);
        LocalDateTime s2 = parse(start2);
        if (s1 == null || s2 == null) return false;
        return s1.isAfter(s2);
    }
}
