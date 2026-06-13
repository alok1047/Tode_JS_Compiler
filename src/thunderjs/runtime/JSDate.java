package thunderjs.runtime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class JSDate {
    private ZonedDateTime value; // null if invalid date

    private static final ZoneId LOCAL_ZONE = ZoneId.systemDefault();
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    // Formatter for JavaScript local toString representation:
    // e.g. "Sat Jun 13 2026 23:38:55 GMT+0530"
    private static final DateTimeFormatter LOCAL_TO_STRING_FORMAT = 
        DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.US);

    // Formatter for toUTCString representation:
    // e.g. "Sat, 13 Jun 2026 18:08:55 GMT"
    private static final DateTimeFormatter UTC_TO_STRING_FORMAT = 
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

    // Formatter for toDateString: "Sat Jun 13 2026"
    private static final DateTimeFormatter DATE_STRING_FORMAT = 
        DateTimeFormatter.ofPattern("EEE MMM dd yyyy", Locale.US);

    // Formatter for toTimeString: "23:38:55 GMT+0530"
    private static final DateTimeFormatter TIME_STRING_FORMAT = 
        DateTimeFormatter.ofPattern("HH:mm:ss 'GMT'Z", Locale.US);

    // Constructor: new Date()
    public JSDate() {
        this.value = ZonedDateTime.now(LOCAL_ZONE);
    }

    // Constructor: new Date(timestamp)
    public JSDate(double ms) {
        if (Double.isNaN(ms) || Double.isInfinite(ms)) {
            this.value = null;
        } else {
            try {
                Instant instant = Instant.ofEpochMilli((long) ms);
                this.value = ZonedDateTime.ofInstant(instant, LOCAL_ZONE);
            } catch (Exception e) {
                this.value = null;
            }
        }
    }

    // Constructor: new Date("dateString")
    public JSDate(String dateStr) {
        this.value = parseDateString(dateStr);
    }

    // Constructor: new Date(year, month, ...)
    public JSDate(double year, double month, double day, double hours, double minutes, double seconds, double ms) {
        if (Double.isNaN(year) || Double.isInfinite(year) ||
            Double.isNaN(month) || Double.isInfinite(month) ||
            Double.isNaN(day) || Double.isInfinite(day) ||
            Double.isNaN(hours) || Double.isInfinite(hours) ||
            Double.isNaN(minutes) || Double.isInfinite(minutes) ||
            Double.isNaN(seconds) || Double.isInfinite(seconds) ||
            Double.isNaN(ms) || Double.isInfinite(ms)) {
            this.value = null;
            return;
        }

        int y = (int) year;
        if (y >= 0 && y <= 99) {
            y += 1900;
        }

        try {
            // JS months are 0-based, days are 1-based.
            // We start at y-01-01T00:00:00 local time and roll:
            ZonedDateTime base = ZonedDateTime.of(y, 1, 1, 0, 0, 0, 0, LOCAL_ZONE);
            base = base.plusMonths((long) month);
            base = base.plusDays((long) day - 1);
            base = base.plusHours((long) hours);
            base = base.plusMinutes((long) minutes);
            base = base.plusSeconds((long) seconds);
            base = base.plusNanos((long) ms * 1_000_000L);
            this.value = base;
        } catch (Exception e) {
            this.value = null;
        }
    }

    private ZonedDateTime parseDateString(String dateStr) {
        if (dateStr == null) return null;
        dateStr = dateStr.trim();
        if (dateStr.isEmpty()) return null;

        try {
            // 1. Zoned ISO format (e.g. "2025-06-13T10:30:45Z" or "2025-06-13T10:30:45+05:30")
            return ZonedDateTime.parse(dateStr);
        } catch (DateTimeParseException e) { }

        try {
            // 2. Local ISO format (e.g. "2025-06-13T10:30:45")
            LocalDateTime ldt = LocalDateTime.parse(dateStr);
            return ZonedDateTime.of(ldt, LOCAL_ZONE);
        } catch (DateTimeParseException e) { }

        try {
            // 3. Simple date (e.g. "2025-06-13") -> ISO specifies parsing YYYY-MM-DD as UTC!
            java.time.LocalDate ld = java.time.LocalDate.parse(dateStr);
            return ld.atStartOfDay(UTC_ZONE).withZoneSameInstant(LOCAL_ZONE);
        } catch (DateTimeParseException e) { }

        // 4. Try format with slashes (e.g. "2025/06/13" or "2025/06/13 10:30:45")
        try {
            String temp = dateStr.replace('/', '-');
            if (temp.contains(" ")) {
                temp = temp.replace(' ', 'T');
                LocalDateTime ldt = LocalDateTime.parse(temp);
                return ZonedDateTime.of(ldt, LOCAL_ZONE);
            } else {
                java.time.LocalDate ld = java.time.LocalDate.parse(temp);
                return ld.atStartOfDay(LOCAL_ZONE);
            }
        } catch (Exception e) { }

        // 5. RFC 1123 format
        try {
            return ZonedDateTime.parse(dateStr, DateTimeFormatter.RFC_1123_DATE_TIME);
        } catch (Exception e) { }

        return null; // Invalid Date
    }

    public boolean isValid() {
        return value != null;
    }

    // ── Static Methods ──────────────────────────────────────────────────

    public static double now() {
        return (double) System.currentTimeMillis();
    }

    public static double parse(String dateStr) {
        JSDate temp = new JSDate(dateStr);
        return temp.getTime();
    }

    public static double UTC(double year, double month, double day, double hours, double minutes, double seconds, double ms) {
        if (Double.isNaN(year) || Double.isInfinite(year) ||
            Double.isNaN(month) || Double.isInfinite(month) ||
            Double.isNaN(day) || Double.isInfinite(day) ||
            Double.isNaN(hours) || Double.isInfinite(hours) ||
            Double.isNaN(minutes) || Double.isInfinite(minutes) ||
            Double.isNaN(seconds) || Double.isInfinite(seconds) ||
            Double.isNaN(ms) || Double.isInfinite(ms)) {
            return Double.NaN;
        }

        int y = (int) year;
        if (y >= 0 && y <= 99) {
            y += 1900;
        }

        try {
            ZonedDateTime base = ZonedDateTime.of(y, 1, 1, 0, 0, 0, 0, UTC_ZONE);
            base = base.plusMonths((long) month);
            base = base.plusDays((long) day - 1);
            base = base.plusHours((long) hours);
            base = base.plusMinutes((long) minutes);
            base = base.plusSeconds((long) seconds);
            base = base.plusNanos((long) ms * 1_000_000L);
            return base.toInstant().toEpochMilli();
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    // ── Getters ─────────────────────────────────────────────────────────

    public double getTime() {
        if (value == null) return Double.NaN;
        return (double) value.toInstant().toEpochMilli();
    }

    public double getFullYear() {
        if (value == null) return Double.NaN;
        return (double) value.getYear();
    }

    public double getMonth() {
        if (value == null) return Double.NaN;
        return (double) (value.getMonthValue() - 1); // 0-based in JS
    }

    public double getDate() {
        if (value == null) return Double.NaN;
        return (double) value.getDayOfMonth();
    }

    public double getDay() {
        if (value == null) return Double.NaN;
        int day = value.getDayOfWeek().getValue(); // 1 = Mon, 7 = Sun
        return day == 7 ? 0.0 : (double) day;
    }

    public double getHours() {
        if (value == null) return Double.NaN;
        return (double) value.getHour();
    }

    public double getMinutes() {
        if (value == null) return Double.NaN;
        return (double) value.getMinute();
    }

    public double getSeconds() {
        if (value == null) return Double.NaN;
        return (double) value.getSecond();
    }

    public double getMilliseconds() {
        if (value == null) return Double.NaN;
        return (double) (value.getNano() / 1_000_000);
    }

    public double getTimezoneOffset() {
        if (value == null) return Double.NaN;
        // returns offset in minutes (negative for East, positive for West)
        return -value.getOffset().getTotalSeconds() / 60.0;
    }

    // ── Setters ─────────────────────────────────────────────────────────

    public double setTime(double ms) {
        if (Double.isNaN(ms) || Double.isInfinite(ms)) {
            this.value = null;
            return Double.NaN;
        }
        try {
            Instant instant = Instant.ofEpochMilli((long) ms);
            this.value = ZonedDateTime.ofInstant(instant, LOCAL_ZONE);
            return ms;
        } catch (Exception e) {
            this.value = null;
            return Double.NaN;
        }
    }

    public double setFullYear(double year) {
        if (value == null) return Double.NaN;
        if (Double.isNaN(year) || Double.isInfinite(year)) {
            this.value = null;
            return Double.NaN;
        }
        this.value = this.value.withYear((int) year);
        return getTime();
    }

    public double setMonth(double month) {
        if (value == null) return Double.NaN;
        if (Double.isNaN(month) || Double.isInfinite(month)) {
            this.value = null;
            return Double.NaN;
        }
        int currentMonthIndex = value.getMonthValue() - 1;
        long diff = (long) month - currentMonthIndex;
        this.value = this.value.plusMonths(diff);
        return getTime();
    }

    public double setDate(double date) {
        if (value == null) return Double.NaN;
        if (Double.isNaN(date) || Double.isInfinite(date)) {
            this.value = null;
            return Double.NaN;
        }
        int currentDay = value.getDayOfMonth();
        long diff = (long) date - currentDay;
        this.value = this.value.plusDays(diff);
        return getTime();
    }

    public double setHours(double hours) {
        if (value == null) return Double.NaN;
        if (Double.isNaN(hours) || Double.isInfinite(hours)) {
            this.value = null;
            return Double.NaN;
        }
        int current = value.getHour();
        long diff = (long) hours - current;
        this.value = this.value.plusHours(diff);
        return getTime();
    }

    public double setMinutes(double minutes) {
        if (value == null) return Double.NaN;
        if (Double.isNaN(minutes) || Double.isInfinite(minutes)) {
            this.value = null;
            return Double.NaN;
        }
        int current = value.getMinute();
        long diff = (long) minutes - current;
        this.value = this.value.plusMinutes(diff);
        return getTime();
    }

    public double setSeconds(double seconds) {
        if (value == null) return Double.NaN;
        if (Double.isNaN(seconds) || Double.isInfinite(seconds)) {
            this.value = null;
            return Double.NaN;
        }
        int current = value.getSecond();
        long diff = (long) seconds - current;
        this.value = this.value.plusSeconds(diff);
        return getTime();
    }

    public double setMilliseconds(double ms) {
        if (value == null) return Double.NaN;
        if (Double.isNaN(ms) || Double.isInfinite(ms)) {
            this.value = null;
            return Double.NaN;
        }
        int current = value.getNano() / 1_000_000;
        long diff = (long) ms - current;
        this.value = this.value.plusNanos(diff * 1_000_000L);
        return getTime();
    }

    // ── String Methods ──────────────────────────────────────────────────

    @Override
    public String toString() {
        if (value == null) return "Invalid Date";
        try {
            return value.format(LOCAL_TO_STRING_FORMAT);
        } catch (Exception e) {
            return "Invalid Date";
        }
    }

    public String toISOString() {
        if (value == null) {
            // throw RangeError caught by interpreter
            throw new IllegalArgumentException("Invalid time value");
        }
        // Always in UTC
        ZonedDateTime utc = value.withZoneSameInstant(UTC_ZONE);
        // Format: YYYY-MM-DDTHH:mm:ss.sssZ
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        return utc.format(formatter);
    }

    public String toUTCString() {
        if (value == null) return "Invalid Date";
        ZonedDateTime utc = value.withZoneSameInstant(UTC_ZONE);
        return utc.format(UTC_TO_STRING_FORMAT);
    }

    public String toDateString() {
        if (value == null) return "Invalid Date";
        return value.format(DATE_STRING_FORMAT);
    }

    public String toTimeString() {
        if (value == null) return "Invalid Date";
        return value.format(TIME_STRING_FORMAT);
    }

    public Object toJSON() {
        if (value == null) return JSNull.INSTANCE;
        try {
            return toISOString();
        } catch (Exception e) {
            return JSNull.INSTANCE;
        }
    }
}
