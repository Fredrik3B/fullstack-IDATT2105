package edu.ntnu.idatt2105.backend.common.service.icchecklist;

import edu.ntnu.idatt2105.backend.common.model.enums.ChecklistFrequency;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.Locale;

public final class PeriodKeyUtil {

	private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

	private PeriodKeyUtil() {
	}

	public static ChecklistFrequency periodToFrequency(String period) {
		String normalized = normalizePeriod(period);
		return switch (normalized) {
			case "daily" -> ChecklistFrequency.DAILY;
			case "weekly" -> ChecklistFrequency.WEEKLY;
			case "monthly" -> ChecklistFrequency.MONTHLY;
			default -> throw new IllegalArgumentException("Unsupported period: " + period);
		};
	}

	public static String frequencyToPeriod(ChecklistFrequency frequency) {
		if (frequency == null) return "daily";
		return switch (frequency) {
			case WEEKLY -> "weekly";
			case MONTHLY -> "monthly";
			default -> "daily";
		};
	}

	public static String currentPeriodKey(ChecklistFrequency frequency, ZoneId zoneId) {
		ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
		LocalDate date = LocalDate.now(zone);
		return currentPeriodKey(frequency, date);
	}

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
