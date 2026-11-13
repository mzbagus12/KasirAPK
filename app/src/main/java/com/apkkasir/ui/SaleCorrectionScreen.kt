package com.apkkasir.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apkkasir.data.AppContainer
import com.apkkasir.data.CorrectionKind
import com.apkkasir.data.PaymentMethod
import com.apkkasir.data.Sale
import com.apkkasir.data.SaleStatus
import com.apkkasir.payment.SaleDetail
import com.apkkasir.reports.*
import java.time.LocalDate

@Composable
internal fun SaleSearchScreen(app: AppContainer, back: () -> Unit, detail: (Long) -> Unit) {
    var number by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now(Jakarta).toString()) }
    var results by remember { mutableStateOf<List<Sale>?>(null) }
    val action = rememberAction()
    Page("Cari transaksi", back) {
        OutlinedTextField(number, { number = it }, Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("Nomor nota atau tagihan") })
        Button(enabled = number.isNotBlank() && !action.busy, onClick = {
            action.run { results = listOfNotNull(app.corrections.find(number)) }
        }) { Text("Cari nomor") }
        HorizontalDivider()
        Text("Tagihan QRIS yang dibatalkan", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(date, { date = it }, Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("Tanggal tagihan (YYYY-MM-DD)") })
        OutlinedButton(enabled = !action.busy, onClick = {
            action.run {
                val period = ReportPeriod.of(PeriodKind.DAILY, parseDate(date))
                results = app.corrections.cancelled(period.startMillis, period.endMillis)
            }
        }) { Text("Cari tanggal") }
        ErrorText(action.error)
        results?.let { matches ->
            if (matches.isEmpty()) Text("Transaksi tidak ditemukan.")
            matches.forEach { sale -> SaleListItem(sale) { detail(sale.id) } }
        }
    }
}

@Composable
internal fun SaleCorrectionScreen(app: AppContainer, id: Long, back: () -> Unit) {
    val action = rememberAction()
    var detail by remember { mutableStateOf<SaleDetail?>(null) }
    var correction by remember { mutableStateOf<CorrectionKind?>(null) }
    var reason by remember { mutableStateOf("") }
    LaunchedEffect(id) { action.run { detail = app.corrections.detail(id) } }
    Page("Detail transaksi", back) {
        val info = detail
        if (info == null) {
            if (action.busy) CircularProgressIndicator()
            else OutlinedButton(onClick = { action.run { detail = app.corrections.detail(id) } }) { Text("Coba lagi") }
        } else {
            ReceiptBody(info.receipt)
            val sale = info.receipt.sale
            when (sale.status) {
                SaleStatus.LUNAS -> {
                    OutlinedButton(enabled = !action.busy, onClick = {
                        reason = ""; action.error = null; correction = CorrectionKind.METHOD
                    }) { Text("Koreksi metode") }
                    OutlinedButton(enabled = !action.busy, onClick = {
                        reason = ""; action.error = null; correction = CorrectionKind.CANCEL
                    }) { Text("Batalkan status lunas") }
                }
                SaleStatus.DIBATALKAN -> if (sale.method == PaymentMethod.QRIS) {
                    Button(enabled = !action.busy, onClick = {
                        reason = ""; action.error = null; correction = CorrectionKind.RECOVER
                    }) { Text("Pulihkan pembayaran QRIS") }
                }
                SaleStatus.MENUNGGU -> Unit
            }
            if (info.history.isNotEmpty()) {
                HorizontalDivider()
                Text("Riwayat koreksi", style = MaterialTheme.typography.titleMedium)
                info.history.forEach { entry ->
                    val change = entry.correction
                    Text("${change.kind.label} · ${entry.username} · ${formatWib(change.at)}")
                    Text("${change.fromStatus.label} / ${change.fromMethod?.label ?: "—"} → " +
                        "${change.toStatus.label} / ${change.toMethod?.label ?: "—"}")
                    Text(change.reason)
                }
            }
        }
        ErrorText(action.error)
    }
    correction?.let { kind ->
        val method = if (detail?.receipt?.sale?.method == PaymentMethod.TUNAI) PaymentMethod.QRIS else PaymentMethod.TUNAI
        AlertDialog(
            onDismissRequest = { if (!action.busy) correction = null },
            title = { Text(kind.label) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (kind) {
                        CorrectionKind.METHOD -> Text("Ubah metode menjadi ${method.label}.")
                        CorrectionKind.CANCEL -> Text("Transaksi akan dikeluarkan dari jumlah dan total penjualan.")
                        CorrectionKind.RECOVER -> Text("Cocokkan nominal dan waktu dengan notifikasi BRI sebelum melanjutkan.")
                    }
                    OutlinedTextField(reason, { reason = it }, Modifier.fillMaxWidth(), label = { Text("Alasan") })
                    ErrorText(action.error)
                }
            },
            confirmButton = {
                Button(enabled = reason.isNotBlank() && !action.busy, onClick = {
                    action.run {
                        app.corrections.apply(id, kind, reason, if (kind == CorrectionKind.METHOD) method else null)
                        correction = null
                        detail = app.corrections.detail(id)
                    }
                }) { Text("Simpan koreksi") }
            },
            dismissButton = { TextButton(enabled = !action.busy, onClick = { correction = null }) { Text("Kembali") } },
        )
    }
}

internal fun parseDate(text: String): LocalDate =
    runCatching { LocalDate.parse(text.trim()) }.getOrElse { error("Gunakan format tanggal YYYY-MM-DD.") }
