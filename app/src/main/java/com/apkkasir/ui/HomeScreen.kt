package com.apkkasir.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apkkasir.R
import com.apkkasir.payment.formatRupiah

@Composable
internal fun HomeScreen(state: DailyReportState, open: (String) -> Unit) {
    Page("Kasir Toko") {
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeAction("Buat tagihan", R.drawable.ic_kasir_register, Modifier.weight(1f), featured = true) {
                open("checkout")
            }
            HomeAction("Tagihan menunggu", R.drawable.ic_kasir_document, Modifier.weight(1f)) {
                open("history/pending")
            }
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeAction("Nota", R.drawable.ic_kasir_receipt, Modifier.weight(1f)) { open("history/receipts") }
            HomeAction("Laporan hari ini", R.drawable.ic_kasir_report, Modifier.weight(1f)) { open("daily") }
        }
        SalesWidget(state)
    }
}

@Composable
private fun HomeAction(label: String, icon: Int, modifier: Modifier, featured: Boolean = false, open: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = open,
        modifier = modifier.fillMaxHeight().heightIn(min = 104.dp),
        shape = MaterialTheme.shapes.large,
        color = if (featured) colors.primary else colors.surface,
        contentColor = if (featured) colors.onPrimary else colors.onSurface,
        border = if (featured) null else BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(shape = MaterialTheme.shapes.medium, color = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer) {
                Icon(painterResource(icon), null, Modifier.padding(9.dp).size(24.dp))
            }
            Text(label, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SalesWidget(state: DailyReportState) {
    val colors = MaterialTheme.colorScheme
    Surface(
        Modifier.fillMaxWidth().testTag("sales-widget"),
        shape = MaterialTheme.shapes.large,
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_kasir_register), null, Modifier.size(24.dp))
                Text("Penjualan hari ini", Modifier.semantics { heading() }, style = MaterialTheme.typography.titleSmall)
            }
            when (state) {
                DailyReportState.Loading -> WidgetStatus("Memuat penjualan hari ini…")
                DailyReportState.Failed -> WidgetStatus("Ringkasan gagal dimuat. Buka kembali Beranda untuk mencoba lagi.")
                is DailyReportState.Ready -> {
                    Text(formatRupiah(state.report.total), style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text("${state.report.count} transaksi lunas")
                    if (state.report.count == 0) Text("Belum ada transaksi lunas hari ini.")
                }
            }
        }
    }
}

@Composable
private fun WidgetStatus(message: String) {
    Text(message, Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}
