package com.apkkasir

import com.apkkasir.reports.ReportSnapshot
import com.apkkasir.ui.DailyReportState
import com.apkkasir.ui.dailyReportStates
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyReportStateTest {
    @Test fun stateDropsPreviousDayAtMidnightWib() = runBlocking {
        val clock = TestClock(Instant.parse("2026-11-14T16:59:59.990Z"))
        val snapshot = ReportSnapshot(emptyList())
        val states = withTimeout(2_000) {
            dailyReportStates(clock) { period ->
                if (period.start == LocalDate.parse("2026-11-14")) {
                    clock.now = Instant.parse("2026-11-14T17:00:00Z")
                }
                flowOf(snapshot)
            }.take(4).toList()
        }
        assertEquals(listOf(
            DailyReportState.Loading,
            DailyReportState.Ready(LocalDate.parse("2026-11-14"), snapshot),
            DailyReportState.Loading,
            DailyReportState.Ready(LocalDate.parse("2026-11-15"), snapshot),
        ), states)
    }

    @Test fun sourceFailureShowsErrorAndReopeningRetries() = runBlocking {
        val clock = Clock.fixed(Instant.parse("2026-11-14T08:00:00Z"), ZoneOffset.UTC)
        val failed = dailyReportStates(clock) { error("Uji kegagalan pembacaan") }.take(2).toList()
        assertEquals(listOf(DailyReportState.Loading, DailyReportState.Failed), failed)
        val snapshot = ReportSnapshot(emptyList())
        val reloaded = dailyReportStates(clock) { flowOf(snapshot) }.take(2).toList()
        assertEquals(DailyReportState.Ready(LocalDate.parse("2026-11-14"), snapshot), reloaded.last())
    }

    private class TestClock(var now: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = fixed(now, zone)
        override fun instant(): Instant = now
    }
}
