package com.apkkasir.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SaleStatus(val label: String) {
    MENUNGGU("Menunggu"), LUNAS("Lunas"), DIBATALKAN("Dibatalkan")
}

enum class PaymentMethod(val label: String) { TUNAI("Tunai"), QRIS("QRIS") }

@Entity(
    indices = [
        Index(value = ["billNumber"], unique = true), Index(value = ["receiptNumber"], unique = true),
        Index("confirmedAt"), Index("cashierId"), Index("confirmedBy"),
    ],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["cashierId"]),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["confirmedBy"]),
    ],
)
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billNumber: String,
    val cashierId: Long,
    val createdAt: Long,
    val total: Long,
    val status: SaleStatus = SaleStatus.MENUNGGU,
    val method: PaymentMethod? = null,
    val cash: Long? = null,
    val receiptNumber: String? = null,
    val confirmedAt: Long? = null,
    val confirmedBy: Long? = null,
)

@Entity(
    primaryKeys = ["saleId", "position"],
    foreignKeys = [
        ForeignKey(entity = Sale::class, parentColumns = ["id"], childColumns = ["saleId"]),
        ForeignKey(entity = Product::class, parentColumns = ["id"], childColumns = ["productId"]),
    ],
    indices = [Index("productId")],
)
data class SaleLine(
    val saleId: Long,
    val position: Int,
    val productId: Long,
    val name: String,
    val unitPrice: Long,
    val quantity: Int,
    val subtotal: Long,
)

data class Receipt(val sale: Sale, val lines: List<SaleLine>)
