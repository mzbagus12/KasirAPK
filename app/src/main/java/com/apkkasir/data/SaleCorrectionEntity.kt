package com.apkkasir.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CorrectionKind(val label: String) {
    METHOD("Koreksi metode"), CANCEL("Batalkan lunas"), RECOVER("Pulihkan QRIS")
}

@Entity(
    indices = [Index("saleId"), Index("adminId")],
    foreignKeys = [
        ForeignKey(entity = Sale::class, parentColumns = ["id"], childColumns = ["saleId"]),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["adminId"]),
    ],
)
data class SaleCorrection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val kind: CorrectionKind,
    val fromStatus: SaleStatus,
    val toStatus: SaleStatus,
    val fromMethod: PaymentMethod?,
    val toMethod: PaymentMethod?,
    val fromCash: Long?,
    val toCash: Long?,
    val reason: String,
    val adminId: Long,
    val at: Long,
)
