package com.apkkasir.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.apkkasir.data.AppContainer
import com.apkkasir.data.Sale
import com.apkkasir.payment.formatRupiah
import com.apkkasir.reports.*
import java.time.LocalDate
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DailyReportScreen(app: AppContainer, back: () -> Unit, detail: (Long) -> Unit) {
    val date by produceState(LocalDate.now(Jakarta)) {
        while (true) {
            value = LocalDate.now(Jakarta)
            val end = ReportPeriod.of(PeriodKind.DAILY, value).endMillis
            delay((end - System.currentTimeMillis()).coerceIn(1L, 30_000L))
        }
    }
    val source = remember(app, date) { app.reports.observe(ReportPeriod.of(PeriodKind.DAILY, date)) }
    val snapshot = key(date) { source.collectAsState(null).value }
    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = { KasirToolbar("Laporan hari ini", back) }) { insets ->
        Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                Modifier.widthIn(max = 640.dp).fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    val data = snapshot
                    if (data == null) CircularProgressIndicator()
                    else {
                        SummaryCard("Penjualan · $date WIB", formatRupiah(data.total), "${data.count} transaksi lunas")
                        if (data.rows.isEmpty()) Text("Belum ada penjualan hari ini.", Modifier.padding(top = 12.dp))
                    }
                }
                items(snapshot?.rows.orEmpty(), key = { it.id }) { sale -> SaleListItem(sale) { detail(sale.id) } }
            }
        }
    }
}

@Composable
internal fun SaleListItem(sale: Sale, open: () -> Unit) {
    OutlinedCard(onClick = open, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(sale.receiptNumber ?: sale.billNumber, style = MaterialTheme.typography.titleSmall)
            Text(formatRupiah(sale.total), style = MaterialTheme.typography.titleMedium)
            Text("${sale.method?.label ?: "Belum memilih metode"} · ${sale.status.label}",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(sale.confirmedAt?.let(::formatWib) ?: formatWib(sale.createdAt),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
