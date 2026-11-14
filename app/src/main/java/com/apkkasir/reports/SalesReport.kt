package com.apkkasir.reports

import com.apkkasir.data.KasirDatabase
import com.apkkasir.data.LocalSession
import com.apkkasir.data.Role
import com.apkkasir.data.Sale
import com.apkkasir.data.SaleStatus
import java.math.BigInteger
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.map

private val dateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(Jakarta)

fun formatWib(time: Long): String = dateTime.format(Instant.ofEpochMilli(time)) + " WIB"

data class ReportSnapshot(val rows: List<Sale>) {
    private val paid = rows.filter { it.status == SaleStatus.LUNAS }
    val count = paid.size
    val total: BigInteger = paid.fold(BigInteger.ZERO) { sum, sale ->
        sum.add(BigInteger.valueOf(sale.total))
    }
}

class SalesReport(private val database: KasirDatabase, private val session: LocalSession) {
    fun observe(period: ReportPeriod): kotlinx.coroutines.flow.Flow<ReportSnapshot> {
        val actor = session.requireRole(Role.CASHIER)
        return database.sales().observeWithin(period.startMillis, period.endMillis).map {
            session.checkActive(actor)
            ReportSnapshot(it)
        }
    }

    suspend fun snapshot(period: ReportPeriod): ReportSnapshot {
        val actor = session.requireRole(Role.CASHIER)
        return ReportSnapshot(database.sales().within(period.startMillis, period.endMillis))
            .also { session.checkActive(actor) }
    }
}
