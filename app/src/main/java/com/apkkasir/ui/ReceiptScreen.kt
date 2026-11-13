package com.apkkasir.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.apkkasir.data.AppContainer
import com.apkkasir.data.Receipt
import com.apkkasir.payment.formatRupiah
import com.apkkasir.reports.formatWib

@Composable
internal fun ReceiptScreen(app: AppContainer, id: Long, back: () -> Unit, home: () -> Unit) {
    var receipt by remember { mutableStateOf<Receipt?>(null) }
    val action = rememberAction()
    LaunchedEffect(id) { action.run { receipt = app.checkout.receipt(id) } }
    Page("Nota", back) {
        receipt?.let { current -> ContentCard { ReceiptBody(current) } }
        if (action.busy) CircularProgressIndicator()
        ErrorText(action.error)
        if (action.error != null) TextButton(onClick = { action.run { receipt = app.checkout.receipt(id) } }) {
            Text("Coba lagi")
        }
        Button(onClick = home) { Text("Selesai") }
    }
}

@Composable
internal fun ReceiptBody(receipt: Receipt) {
    val sale = receipt.sale
    Text(sale.receiptNumber ?: sale.billNumber, style = MaterialTheme.typography.titleLarge)
    Text(sale.confirmedAt?.let(::formatWib) ?: formatWib(sale.createdAt))
    Text("${sale.status.label} · ${sale.method?.label ?: "Metode belum dipilih"}")
    HorizontalDivider()
    receipt.lines.forEach { item ->
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleSmall)
            Text("${item.quantity} × ${formatRupiah(item.unitPrice)} = ${formatRupiah(item.subtotal)}")
        }
    }
    HorizontalDivider()
    Text("Total ${formatRupiah(sale.total)}", style = MaterialTheme.typography.headlineSmall)
    sale.cash?.let {
        Text("Uang diterima ${formatRupiah(it)}")
        if (it >= sale.total) Text("Kembalian ${formatRupiah(it - sale.total)}")
    }
}
