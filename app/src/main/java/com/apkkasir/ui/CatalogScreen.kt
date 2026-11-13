package com.apkkasir.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apkkasir.data.AppContainer
import com.apkkasir.data.Product
import com.apkkasir.payment.formatRupiah
import com.apkkasir.payment.parseRupiah

@Composable
internal fun CatalogScreen(app: AppContainer, back: () -> Unit) {
    val products by app.products.products.collectAsState(emptyList())
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Product?>(null) }
    var newPrice by remember { mutableStateOf("") }
    val action = rememberAction()
    Page("Barang", back) {
        Text("Tambah barang", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Nama barang") }, singleLine = true)
        MoneyField("Harga satuan", price) { price = it }
        Button(enabled = !action.busy, onClick = {
            action.run {
                val amount = parseRupiah(price) ?: error("Masukkan harga dalam rupiah bulat.")
                app.products.add(name, amount)
                name = ""
                price = ""
            }
        }) { Text("Tambah barang") }
        ErrorText(action.error)
        HorizontalDivider()
        if (products.isEmpty()) Text("Belum ada barang.")
        products.forEach { product ->
            ListItem(
                headlineContent = { Text(product.name) },
                supportingContent = { Text(formatRupiah(product.price)) },
                trailingContent = {
                    TextButton(onClick = {
                        action.error = null
                        newPrice = product.price.toString()
                        editing = product
                    }) { Text("Ubah harga") }
                },
            )
        }
    }
    editing?.let { product ->
        AlertDialog(
            onDismissRequest = { if (!action.busy) editing = null },
            title = { Text(product.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoneyField("Harga baru", newPrice) { newPrice = it }
                    ErrorText(action.error)
                }
            },
            confirmButton = {
                Button(enabled = !action.busy, onClick = {
                    action.run {
                        val amount = parseRupiah(newPrice) ?: error("Masukkan harga dalam rupiah bulat.")
                        app.products.changePrice(product.id, amount)
                        editing = null
                    }
                }) { Text("Simpan harga") }
            },
            dismissButton = { TextButton(enabled = !action.busy, onClick = { editing = null }) { Text("Batal") } },
        )
    }
}

@Composable
internal fun MoneyField(label: String, value: String, change: (String) -> Unit) {
    OutlinedTextField(
        value, change, modifier = Modifier.fillMaxWidth(), label = { Text(label) },
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}
