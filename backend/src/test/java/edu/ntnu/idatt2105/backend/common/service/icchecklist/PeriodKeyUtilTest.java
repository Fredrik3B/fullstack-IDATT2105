package edu.ntnu.idatt2105.backend.common.service.icchecklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.ntnu.idatt2105.backend.common.model.enums.ChecklistFrequency;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PeriodKeyUtil")
class PeriodKeyUtilTest {

    // ── periodToFrequency ─────────────────────────────────────────────────────

    @Test
    @DisplayName("periodToFrequency - 'daily' returns DAILY")
    void periodToFrequency_daily() {
        assertThat(PeriodKeyUtil.periodToFrequency("daily")).isEqualTo(ChecklistFrequency.DAILY);
    }

    @Test
    @DisplayName("periodToFrequency - 'weekly' returns WEEKLY")
    void periodToFrequency_weekly() {
        assertThat(PeriodKeyUtil.periodToFrequency("weekly")).isEqualTo(ChecklistFrequency.WEEKLY);
    }

    @Test
    @DisplayName("periodToFrequency - 'monthly' returns MONTHLY")
    void periodToFrequency_monthly() {
        assertThat(PeriodKeyUtil.periodToFrequency("monthly")).isEqualTo(ChecklistFrequency.MONTHLY);
    }

    @Test
    @DisplayName("periodToFrequency - 'Day' prefix is accepted (case-insensitive)")
    void periodToFrequency_dayPrefix() {
        assertThat(PeriodKeyUtil.periodToFrequency("Day")).isEqualTo(ChecklistFrequency.DAILY);
    }

    @Test
    @DisplayName("periodToFrequency - 'Week' prefix is accepted (case-insensitive)")
    void periodToFrequency_weekPrefix() {
        assertThat(PeriodKeyUtil.periodToFrequency("Week")).isEqualTo(ChecklistFrequency.WEEKLY);
    }

    @Test
    @DisplayName("periodToFrequency - 'Month' prefix is accepted (case-insensitive)")
    void periodToFrequency_monthPrefix() {
        assertThat(PeriodKeyUtil.periodToFrequency("Month")).isEqualTo(ChecklistFrequency.MONTHLY);
    }

    @Test
    @DisplayName("periodToFrequency - unsupported value throws IllegalArgumentException")
    void periodToFrequency_unknownThrows() {
        assertThatThrownBy(() -> PeriodKeyUtil.periodToFrequency("yearly"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yearly");
    }

    // ── frequencyToPeriod ─────────────────────────────────────────────────────

    @Test
    @DisplayName("frequencyToPeriod - DAILY returns 'daily'")
    void frequencyToPeriod_daily() {
        assertThat(PeriodKeyUtil.frequencyToPeriod(ChecklistFrequency.DAILY)).isEqualTo("daily");
    }

    @Test
    @DisplayName("frequencyToPeriod - WEEKLY returns 'weekly'")
    void frequencyToPeriod_weekly() {
        assertThat(PeriodKeyUtil.frequencyToPeriod(ChecklistFrequency.WEEKLY)).isEqualTo("weekly");
    }

    @Test
    @DisplayName("frequencyToPeriod - MONTHLY returns 'monthly'")
    void frequencyToPeriod_monthly() {
        assertThat(PeriodKeyUtil.frequencyToPeriod(ChecklistFrequency.MONTHLY)).isEqualTo("monthly");
    }

    @Test
    @DisplayName("frequencyToPeriod - null defaults to 'daily'")
    void frequencyToPeriod_null() {
        assertThat(PeriodKeyUtil.frequencyToPeriod(null)).isEqualTo("daily");
    }

    // ── currentPeriodKey (date-based) ─────────────────────────────────────────

    @Test
    @DisplayName("currentPeriodKey - DAILY returns YYYY-MM-DD")
    void currentPeriodKey_daily() {
        LocalDate date = LocalDate.of(2024, 3, 5);
        assertThat(PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, date)).isEqualTo("2024-03-05");
    }

    @Test
    @DisplayName("currentPeriodKey - MONTHLY returns YYYY-MM")
    void currentPeriodKey_monthly() {
        LocalDate date = LocalDate.of(2024, 3, 5);
        assertThat(PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.MONTHLY, date)).isEqualTo("2024-03");
    }

    @Test
    @DisplayName("currentPeriodKey - WEEKLY returns YYYY-Www")
    void currentPeriodKey_weekly() {
        LocalDate date = LocalDate.of(2024, 1, 15); // ISO week 3 of 2024
        String key = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.WEEKLY, date);
        assertThat(key).isEqualTo("2024-W03");
    }

    @Test
    @DisplayName("currentPeriodKey - null frequency defaults to DAILY format")
    void currentPeriodKey_nullFrequencyDefaultsToDaily() {
        LocalDate date = LocalDate.of(2024, 6, 10);
        assertThat(PeriodKeyUtil.currentPeriodKey(null, date)).isEqualTo("2024-06-10");
    }

    @Test
    @DisplayName("currentPeriodKey - null date defaults to today")
    void currentPeriodKey_nullDate() {
        String key = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, (LocalDate) null);
        assertThat(key).matches("\\d{4}-\\d{2}-\\d{2}");
    }

    // ── validatePeriodKey ─────────────────────────────────────────────────────

    @Test
    @DisplayName("validatePeriodKey - valid daily key passes without exception")
    void validatePeriodKey_dailyValid() {
        PeriodKeyUtil.validatePeriodKey("2024-03-05", ChecklistFrequency.DAILY);
    }

    @Test
    @DisplayName("validatePeriodKey - valid weekly key passes without exception")
    void validatePeriodKey_weeklyValid() {
        PeriodKeyUtil.validatePeriodKey("2024-W03", ChecklistFrequency.WEEKLY);
    }

    @Test
    @DisplayName("validatePeriodKey - valid monthly key passes without exception")
    void validatePeriodKey_monthlyValid() {
        PeriodKeyUtil.validatePeriodKey("2024-03", ChecklistFrequency.MONTHLY);
    }

    @Test
    @DisplayName("validatePeriodKey - null periodKey throws IllegalArgumentException")
    void validatePeriodKey_nullThrows() {
        assertThatThrownBy(() -> PeriodKeyUtil.validatePeriodKey(null, ChecklistFrequency.DAILY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("validatePeriodKey - blank periodKey throws IllegalArgumentException")
    void validatePeriodKey_blankThrows() {
        assertThatThrownBy(() -> PeriodKeyUtil.validatePeriodKey("  ", ChecklistFrequency.DAILY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("validatePeriodKey - invalid daily format throws IllegalArgumentException")
    void validatePeriodKey_invalidDailyFormatThrows() {
        assertThatThrownBy(() -> PeriodKeyUtil.validatePeriodKey("20240305", ChecklistFrequency.DAILY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("validatePeriodKey - invalid weekly format (missing W) throws")
    void validatePeriodKey_invalidWeeklyFormatThrows() {
        assertThatThrownBy(() -> PeriodKeyUtil.validatePeriodKey("2024-03", ChecklistFrequency.WEEKLY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("validatePeriodKey - invalid monthly format (has day) throws")
    void validatePeriodKey_invalidMonthlyFormatThrows() {
        assertThatThrownBy(() -> PeriodKeyUtil.validatePeriodKey("2024-03-05", ChecklistFrequency.MONTHLY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── nextPeriodKey ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("nextPeriodKey - daily advances by one day")
    void nextPeriodKey_daily() {
        assertThat(PeriodKeyUtil.nextPeriodKey(ChecklistFrequency.DAILY, "2024-01-31"))
                .isEqualTo("2024-02-01");
    }

    @Test
    @DisplayName("nextPeriodKey - weekly advances by one week")
    void nextPeriodKey_weekly() {
        String next = PeriodKeyUtil.nextPeriodKey(ChecklistFrequency.WEEKLY, "2024-W03");
        assertThat(next).isEqualTo("2024-W04");
    }

    @Test
    @DisplayName("nextPeriodKey - monthly advances by one month")
    void nextPeriodKey_monthly() {
        assertThat(PeriodKeyUtil.nextPeriodKey(ChecklistFrequency.MONTHLY, "2024-01"))
                .isEqualTo("2024-02");
    }

    @Test
    @DisplayName("nextPeriodKey - monthly wraps December to January of next year")
    void nextPeriodKey_monthlyWrap() {
        assertThat(PeriodKeyUtil.nextPeriodKey(ChecklistFrequency.MONTHLY, "2024-12"))
                .isEqualTo("2025-01");
    }

    @Test
    @DisplayName("nextPeriodKey - invalid key throws IllegalArgumentException")
    void nextPeriodKey_invalidKeyThrows() {
        assertThatThrownBy(() -> PeriodKeyUtil.nextPeriodKey(ChecklistFrequency.DAILY, "bad-key"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
