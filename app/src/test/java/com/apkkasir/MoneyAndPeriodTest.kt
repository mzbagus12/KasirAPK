package com.apkkasir

import com.apkkasir.payment.*
import com.apkkasir.reports.*
import com.apkkasir.data.Sale
import com.apkkasir.data.SaleStatus
import java.time.Instant
import java.time.LocalDate
import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Test

class MoneyAndPeriodTest {
    @Test fun rupiahOnlyAcceptsWholeDigits() {
        assertEquals(50_000L, parseRupiah("50000"))
        listOf("", "50.000", "-1", "7x", "9223372036854775808").forEach {
            assertNull(parseRupiah(it))
        }
    }

    @Test(expected = ArithmeticException::class)
    fun subtotalDetectsOverflow() {
        subtotal(Long.MAX_VALUE, 2)
    }

    @Test(expected = ArithmeticException::class)
    fun totalDetectsOverflow() {
        totalRupiah(listOf(Long.MAX_VALUE, 1L))
    }

    @Test fun reportAggregateCanExceedAnIndividualBillLimit() {
        val paid = Sale(billNumber = "a", cashierId = 1, createdAt = 0, total = Long.MAX_VALUE, status = SaleStatus.LUNAS)
        assertEquals("18446744073709551614", ReportSnapshot(listOf(paid, paid.copy(billNumber = "b"))).total.toString())
    }

    @Test fun localBoundariesAreJakartaRegardlessOfDeviceZone() {
        val previous = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
            val day = LocalDate.of(2026, 11, 30)
            val daily = ReportPeriod.of(PeriodKind.DAILY, day)
            assertEquals(Instant.parse("2026-11-29T17:00:00Z").toEpochMilli(), daily.startMillis)
            assertEquals(Instant.parse("2026-11-30T17:00:00Z").toEpochMilli(), daily.endMillis)
            val week = ReportPeriod.of(PeriodKind.WEEKLY, day.plusDays(2))
            assertEquals(day, week.start)
            assertEquals(LocalDate.of(2026, 12, 7), week.endExclusive)
            val month = ReportPeriod.of(PeriodKind.MONTHLY, day)
            assertEquals(LocalDate.of(2026, 11, 1), month.start)
            assertEquals(LocalDate.of(2026, 12, 1), month.endExclusive)
        } finally {
            TimeZone.setDefault(previous)
        }
    }
}
