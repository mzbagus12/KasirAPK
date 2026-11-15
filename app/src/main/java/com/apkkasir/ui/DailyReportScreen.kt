package com.apkkasir.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.apkkasir.data.Sale
import com.apkkasir.payment.formatRupiah
import com.apkkasir.reports.formatWib

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DailyReportScreen(state: DailyReportState, documents: () -> Unit, detail: (Long) -> Unit) {
    val data = (state as? DailyReportState.Ready)?.report
    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = { KasirToolbar("Laporan hari ini", null) }) { insets ->
        Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                Modifier.widthIn(max = 640.dp).fillMaxSize().testTag("daily-report"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { OutlinedButton(onClick = documents) { Text("Dokumen PDF") } }
                item {
                    when (state) {
                        DailyReportState.Loading -> Text("Memuat laporan…")
                        DailyReportState.Failed -> ErrorText("Laporan gagal dimuat. Buka kembali Laporan untuk mencoba lagi.")
                        is DailyReportState.Ready -> {
                            SummaryCard("Penjualan · ${state.date} WIB", formatRupiah(state.report.total),
                                "${state.report.count} transaksi lunas")
                            if (state.report.rows.isEmpty()) {
                                Text("Belum ada penjualan hari ini.", Modifier.padding(top = 12.dp))
                            }
                        }
                    }
                }
                items(data?.rows.orEmpty(), key = { it.id }) { sale -> SaleListItem(sale) { detail(sale.id) } }
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
