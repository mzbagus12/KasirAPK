package com.apkkasir.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.apkkasir.reports.Jakarta
import com.apkkasir.reports.PeriodKind
import com.apkkasir.reports.ReportPeriod
import com.apkkasir.reports.ReportSnapshot
import com.apkkasir.reports.SalesReport
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

internal sealed interface DailyReportState {
    data object Loading : DailyReportState
    data object Failed : DailyReportState
    data class Ready(val date: LocalDate, val report: ReportSnapshot) : DailyReportState
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun dailyReportStates(
    clock: Clock,
    observe: (ReportPeriod) -> Flow<ReportSnapshot>,
): Flow<DailyReportState> = flow {
    while (true) {
        val date = LocalDate.now(clock.withZone(Jakarta))
        val period = ReportPeriod.of(PeriodKind.DAILY, date)
        emit(period)
        delay((period.endMillis - clock.millis()).coerceIn(1L, 30_000L))
    }
}.distinctUntilChanged().flatMapLatest { period ->
    flow {
        emit(DailyReportState.Loading)
        observe(period).collect { emit(DailyReportState.Ready(period.start, it)) }
    }.catch { emit(DailyReportState.Failed) }
}

@Composable
internal fun currentDailyReport(report: SalesReport, destination: String?): DailyReportState {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val active = destination == "home" || destination == "daily"
    val state = produceState<DailyReportState>(DailyReportState.Loading, report, active, lifecycle) {
        value = DailyReportState.Loading
        if (active) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                value = DailyReportState.Loading
                dailyReportStates(Clock.systemUTC(), report::observe).collect { value = it }
            }
        }
    }
    return if (active) state.value else DailyReportState.Loading
}
