package com.apkkasir.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.apkkasir.data.AppContainer
import com.apkkasir.data.PaymentMethod
import com.apkkasir.data.Sale
import com.apkkasir.data.SaleStatus
import com.apkkasir.payment.formatRupiah
import com.apkkasir.payment.parseRupiah
import com.apkkasir.payment.subtotal
import com.apkkasir.payment.totalRupiah
import com.apkkasir.reports.formatWib
import java.io.File

@Composable
internal fun CheckoutScreen(app: AppContainer, id: Long?, back: () -> Unit, onReceipt: (Long) -> Unit) {
    val history = remember(app) { app.checkout.history() }
    val stored by history.collectAsState(null)
    val initial = stored?.firstOrNull { it.id == id }
    if (id != null && initial == null) {
        Page("Tagihan", back) {
            if (stored == null) CircularProgressIndicator() else Text("Tagihan tidak ditemukan.")
        }
        return
    }
    val model: CheckoutViewModel = viewModel(factory = viewModelFactory {
        initializer { CheckoutViewModel(app.checkout, initial) }
    })
    val bill = model.sale
    if (bill == null) {
        CartScreen(app, model, back)
        return
    }
    var qris by remember { mutableStateOf<File?>(null) }
    var cancel by remember { mutableStateOf(false) }
    LaunchedEffect(app) { qris = app.qris.current() }
    Page("Pembayaran", back) {
        SummaryCard("Total tagihan", formatRupiah(bill.total), bill.billNumber)
        if (bill.status != SaleStatus.MENUNGGU) {
            Text(bill.status.label)
            if (bill.receiptNumber != null) Button(onClick = { onReceipt(bill.id) }) { Text("Lihat nota") }
        } else {
            SectionTitle("Metode pembayaran")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(model.method == PaymentMethod.TUNAI, { model.select(PaymentMethod.TUNAI) },
                    { Text("Tunai") }, modifier = Modifier.weight(1f).heightIn(min = 48.dp), enabled = !model.busy)
                FilterChip(model.method == PaymentMethod.QRIS, { model.select(PaymentMethod.QRIS) },
                    { Text("QRIS") }, modifier = Modifier.weight(1f).heightIn(min = 48.dp), enabled = !model.busy && qris != null)
            }
            if (qris == null) Text("QRIS belum tersedia. Minta admin memasang gambar QRIS toko.")
            when (model.method) {
                PaymentMethod.TUNAI -> {
                    MoneyField("Uang diterima", model.cashText, model::cash)
                    val amount = parseRupiah(model.cashText)
                    if (amount != null) {
                        val label = if (amount < bill.total) "Kekurangan" else "Kembalian"
                        val difference = if (amount < bill.total) bill.total - amount else amount - bill.total
                        SummaryCard(label, formatRupiah(difference))
                    } else if (model.cashText.isNotEmpty()) {
                        ErrorText("Masukkan nominal rupiah bulat yang valid.")
                    }
                    Text("Konfirmasi setelah uang diterima dan kembalian diserahkan.")
                }
                PaymentMethod.QRIS -> {
                    QrisFileImage(qris)
                    Text("Konfirmasi setelah notifikasi BRI menunjukkan pembayaran yang sesuai.")
                }
                null -> Unit
            }
            if (model.method != null) {
                Button(
                    enabled = model.canConfirm(qris != null),
                    onClick = { model.confirm(qris != null, onReceipt) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text("Konfirmasi") }
            }
            if (model.saving) Text("Menyimpan perubahan…")
            if (model.saveFailed) OutlinedButton(onClick = model::retry) { Text("Coba simpan lagi") }
            TextButton(enabled = !model.busy && !model.saving, onClick = { cancel = true }) {
                Text("Batalkan tagihan")
            }
        }
        ErrorText(model.error)
    }
    if (cancel) {
        AlertDialog(
            onDismissRequest = { cancel = false },
            title = { Text("Batalkan tagihan?") },
            text = { Text("Batalkan hanya jika pembayaran belum diterima.") },
            confirmButton = {
                TextButton(onClick = { cancel = false; model.cancel(back) }) { Text("Batalkan tagihan") }
            },
            dismissButton = { TextButton(onClick = { cancel = false }) { Text("Kembali") } },
        )
    }
}

@Composable
private fun CartScreen(app: AppContainer, model: CheckoutViewModel, back: () -> Unit) {
    val products by app.products.products.collectAsState(emptyList())
    val cart = remember { mutableStateMapOf<Long, Int>() }
    val total = runCatching {
        totalRupiah(products.mapNotNull { item -> cart[item.id]?.let { subtotal(item.price, it) } })
    }.getOrNull()
    Page("Buat tagihan", back) {
        SummaryCard("Total keranjang", total?.let(::formatRupiah) ?: "Nominal melebihi batas")
        if (products.isEmpty()) Text("Belum ada barang. Minta admin menambahkan barang.")
        products.forEach { product ->
            val count = cart[product.id] ?: 0
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium)
                    Text(formatRupiah(product.price), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(enabled = count > 0 && !model.busy, onClick = {
                            if (count == 1) cart.remove(product.id) else cart[product.id] = count - 1
                        }) { Text("Kurang") }
                        Text(count.toString(), style = MaterialTheme.typography.titleMedium)
                        TextButton(enabled = count < Int.MAX_VALUE && !model.busy, onClick = {
                            cart[product.id] = count + 1
                        }) { Text("Tambah") }
                    }
                    if (count > 0) runCatching { subtotal(product.price, count) }.getOrNull()?.let {
                        Text("Subtotal ${formatRupiah(it)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Button(enabled = cart.isNotEmpty() && total != null && !model.busy, onClick = {
            model.create(cart.toMap())
        }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Buat tagihan") }
        ErrorText(model.error)
    }
}

@Composable
internal fun CashierHistoryScreen(app: AppContainer, back: () -> Unit, onResume: (Long) -> Unit, onReceipt: (Long) -> Unit) {
    val history = remember(app) { app.checkout.history() }
    val sales by history.collectAsState(emptyList())
    Page("Tagihan dan nota", back) {
        if (sales.isEmpty()) Text("Belum ada tagihan.")
        sales.forEach { sale ->
            ListItem(
                headlineContent = { Text(sale.receiptNumber ?: sale.billNumber) },
                supportingContent = { Text("${sale.status.label} · ${formatRupiah(sale.total)} · ${formatWib(sale.createdAt)}") },
                trailingContent = {
                    if (sale.status == SaleStatus.MENUNGGU) {
                        TextButton(onClick = { onResume(sale.id) }) { Text("Lanjutkan") }
                    } else if (sale.receiptNumber != null) {
                        TextButton(onClick = { onReceipt(sale.id) }) { Text("Lihat nota") }
                    }
                },
            )
        }
    }
}
