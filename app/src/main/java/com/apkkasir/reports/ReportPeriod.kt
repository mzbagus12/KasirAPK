package com.apkkasir.reports

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

val Jakarta: ZoneId = ZoneId.of("Asia/Jakarta")

enum class PeriodKind(val label: String) {
    DAILY("Harian"), WEEKLY("Mingguan"), MONTHLY("Bulanan")
}

data class ReportPeriod(val start: LocalDate, val endExclusive: LocalDate) {
    val startMillis: Long get() = start.atStartOfDay(Jakarta).toInstant().toEpochMilli()
    val endMillis: Long get() = endExclusive.atStartOfDay(Jakarta).toInstant().toEpochMilli()

    companion object {
        fun of(kind: PeriodKind, date: LocalDate): ReportPeriod = when (kind) {
            PeriodKind.DAILY -> ReportPeriod(date, date.plusDays(1))
            PeriodKind.WEEKLY -> {
                val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                ReportPeriod(monday, monday.plusWeeks(1))
            }
            PeriodKind.MONTHLY -> {
                val first = date.withDayOfMonth(1)
                ReportPeriod(first, first.plusMonths(1))
            }
        }
    }
}
