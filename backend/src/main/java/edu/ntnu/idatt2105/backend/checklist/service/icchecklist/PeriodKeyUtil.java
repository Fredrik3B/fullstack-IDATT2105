package edu.ntnu.idatt2105.backend.checklist.service.icchecklist;

import edu.ntnu.idatt2105.backend.checklist.model.enums.ChecklistFrequency;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Utility for converting between period strings, {@link ChecklistFrequency} values, and period keys.
 *
 * <p>Period keys identify a specific checklist period:
 * <ul>
 *   <li>Daily: {@code "YYYY-MM-DD"} (ISO local date)</li>
 *   <li>Weekly: {@code "YYYY-Www"} (ISO week, e.g. {@code "2025-W03"})</li>
 *   <li>Monthly: {@code "YYYY-MM"} (e.g. {@code "2025-03"})</li>
 * </ul>
 */
public final class PeriodKeyUtil {

	private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

	private PeriodKeyUtil() {
	}

	/**
	 * Converts a user-facing period string ({@code "daily"}, {@code "weekly"}, {@code "monthly"})
	 * to the corresponding {@link ChecklistFrequency}.
	 *
	 * @param period the period string (case-insensitive, prefix-matched)
	 * @return the matching frequency
	 * @throws IllegalArgumentException if the period is not recognised
	 */
	public static ChecklistFrequency periodToFrequency(String period) {
		String normalized = normalizePeriod(period);
		return switch (normalized) {
			case "daily" -> ChecklistFrequency.DAILY;
			case "weekly" -> ChecklistFrequency.WEEKLY;
			case "monthly" -> ChecklistFrequency.MONTHLY;
			default -> throw new IllegalArgumentException("Unsupported period: " + period);
		};
	}

	/**
	 * Converts a {@link ChecklistFrequency} to its user-facing period string.
	 *
	 * @param frequency the frequency, or {@code null} (defaults to {@code "daily"})
	 * @return {@code "daily"}, {@code "weekly"}, or {@code "monthly"}
	 */
	public static String frequencyToPeriod(ChecklistFrequency frequency) {
		if (frequency == null) return "daily";
		return switch (frequency) {
			case WEEKLY -> "weekly";
			case MONTHLY -> "monthly";
			default -> "daily";
		};
	}

	/**
	 * Returns the period key for the current date in the given time zone.
	 *
	 * @param frequency the checklist frequency
	 * @param zoneId    the time zone to use, or {@code null} for the system default
	 * @return the current period key
	 */
	public static String currentPeriodKey(ChecklistFrequency frequency, ZoneId zoneId) {
		ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
		LocalDate date = LocalDate.now(zone);
		return currentPeriodKey(frequency, date);
	}

	/**
	 * Returns the period key for a given date and frequency.
	 *
	 * @param frequency the checklist frequency, or {@code null} (defaults to DAILY)
	 * @param date      the reference date, or {@code null} (defaults to today)
	 * @return the period key string
	 */
	public static String currentPeriodKey(ChecklistFrequency frequency, LocalDate date) {
		ChecklistFrequency freq = frequency != null ? frequency : ChecklistFrequency.DAILY;
		LocalDate safeDate = date != null ? date : LocalDate.now();

		if (freq == ChecklistFrequency.WEEKLY) {
			int isoYear = safeDate.get(WeekFields.ISO.weekBasedYear());
			int week = safeDate.get(WeekFields.ISO.weekOfWeekBasedYear());
			return "%d-W%02d".formatted(isoYear, week);
		}

		if (freq == ChecklistFrequency.MONTHLY) {
			return YearMonth.from(safeDate).format(MONTH_FORMATTER);
		}

		return safeDate.toString(); // YYYY-MM-DD
	}

	/**
	 * Validates that a period key matches the expected format for the given frequency.
	 *
	 * @param periodKey the period key to validate
	 * @param frequency the expected frequency
	 * @throws IllegalArgumentException if the key is blank or does not match the format
	 */
	public static void validatePeriodKey(String periodKey, ChecklistFrequency frequency) {
		if (periodKey == null || periodKey.isBlank()) {
			throw new IllegalArgumentException("periodKey is required.");
		}

		ChecklistFrequency freq = frequency != null ? frequency : ChecklistFrequency.DAILY;
		String key = periodKey.trim();

		if (freq == ChecklistFrequency.MONTHLY) {
			if (!key.matches("\\d{4}-\\d{2}")) throw new IllegalArgumentException("Invalid monthly periodKey: " + key);
			YearMonth.parse(key, MONTH_FORMATTER);
			return;
		}

		if (freq == ChecklistFrequency.WEEKLY) {
			if (!key.matches("\\d{4}-W\\d{2}")) throw new IllegalArgumentException("Invalid weekly periodKey: " + key);
			int isoYear = Integer.parseInt(key.substring(0, 4));
			int week = Integer.parseInt(key.substring(6, 8));
			if (week < 1) throw new IllegalArgumentException("Invalid weekly periodKey: " + key);
			int maxWeek = LocalDate.of(isoYear, 12, 28).get(WeekFields.ISO.weekOfWeekBasedYear());
			if (week > maxWeek) throw new IllegalArgumentException("Invalid weekly periodKey: " + key);
			return;
		}

		// DAILY
		if (!key.matches("\\d{4}-\\d{2}-\\d{2}")) throw new IllegalArgumentException("Invalid daily periodKey: " + key);
		LocalDate.parse(key, DateTimeFormatter.ISO_LOCAL_DATE);
	}

	/**
	 * Returns the period key immediately following the given one.
	 *
	 * @param frequency the checklist frequency
	 * @param periodKey the current period key
	 * @return the next period key
	 * @throws IllegalArgumentException if the period key is invalid
	 */
	public static String nextPeriodKey(ChecklistFrequency frequency, String periodKey) {
		validatePeriodKey(periodKey, frequency);
		ChecklistFrequency freq = frequency != null ? frequency : ChecklistFrequency.DAILY;
		String key = periodKey.trim();

		if (freq == ChecklistFrequency.MONTHLY) {
			return YearMonth.parse(key, MONTH_FORMATTER).plusMonths(1).format(MONTH_FORMATTER);
		}

		if (freq == ChecklistFrequency.WEEKLY) {
			int isoYear = Integer.parseInt(key.substring(0, 4));
			int week = Integer.parseInt(key.substring(6, 8));
			LocalDate monday = LocalDate.of(isoYear, 1, 4)
				.with(IsoFields.WEEK_BASED_YEAR, isoYear)
				.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
				.with(WeekFields.ISO.dayOfWeek(), 1);
			return currentPeriodKey(freq, monday.plusWeeks(1));
		}

		return LocalDate.parse(key, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1).toString();
	}

	/**
	 * Returns the calendar start date of the period identified by the given key.
	 *
	 * @param frequency the checklist frequency
	 * @param periodKey the period key
	 * @return the first day of the period
	 * @throws IllegalArgumentException if the period key is invalid
	 */
	public static LocalDate periodStartDate(ChecklistFrequency frequency, String periodKey) {
		validatePeriodKey(periodKey, frequency);
		ChecklistFrequency freq = frequency != null ? frequency : ChecklistFrequency.DAILY;
		String key = periodKey.trim();

		if (freq == ChecklistFrequency.MONTHLY) {
			return YearMonth.parse(key, MONTH_FORMATTER).atDay(1);
		}

		if (freq == ChecklistFrequency.WEEKLY) {
			int isoYear = Integer.parseInt(key.substring(0, 4));
			int week = Integer.parseInt(key.substring(6, 8));
			return LocalDate.of(isoYear, 1, 4)
				.with(IsoFields.WEEK_BASED_YEAR, isoYear)
				.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
				.with(WeekFields.ISO.dayOfWeek(), 1);
		}

		return LocalDate.parse(key, DateTimeFormatter.ISO_LOCAL_DATE);
	}

	private static String normalizePeriod(String period) {
		String raw = String.valueOf(period == null ? "" : period).trim();
		if (raw.isEmpty()) return "daily";
		String lower = raw.toLowerCase(Locale.ROOT);
		if (lower.startsWith("day")) return "daily";
		if (lower.startsWith("week")) return "weekly";
		if (lower.startsWith("month")) return "monthly";
		if (lower.equals("daily") || lower.equals("weekly") || lower.equals("monthly")) return lower;
		throw new IllegalArgumentException("Unsupported period: " + period);
	}
}
