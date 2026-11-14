package com.apkkasir.payment

import androidx.room.withTransaction
import com.apkkasir.data.*

data class SaleDetail(val receipt: Receipt, val history: List<CorrectionRow>)

class SaleCorrectionService(
    private val database: KasirDatabase,
    private val session: LocalSession,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val sales = database.sales()
    private val corrections = database.corrections()

    suspend fun find(number: String): Sale? {
        val actor = session.requireRole(Role.CASHIER)
        return sales.byNumber(number.trim()).also { session.checkActive(actor) }
    }

    suspend fun cancelled(start: Long, end: Long): List<Sale> {
        val actor = session.requireRole(Role.CASHIER)
        return sales.cancelledQris(start, end).also { session.checkActive(actor) }
    }

    suspend fun detail(id: Long): SaleDetail {
        val actor = session.requireRole(Role.CASHIER)
        return database.withTransaction {
            session.checkActive(actor)
            val sale = sales.get(id) ?: error("Tagihan tidak ditemukan.")
            SaleDetail(Receipt(sale, sales.lines(id)), corrections.history(id)).also { session.checkActive(actor) }
        }
    }

    suspend fun apply(id: Long, kind: CorrectionKind, reason: String, method: PaymentMethod? = null) {
        val actor = session.requireRole(Role.CASHIER)
        require(reason.isNotBlank()) { "Isi alasan koreksi." }
        database.withTransaction {
            session.checkActive(actor)
            val previous = sales.get(id) ?: error("Tagihan tidak ditemukan.")
            val now = clock()
            val updated = when (kind) {
                CorrectionKind.METHOD -> {
                    check(previous.status == SaleStatus.LUNAS) { "Transaksi harus berstatus Lunas." }
                    require(method != null && previous.method != method) { "Pilih metode pembayaran yang berbeda." }
                    previous.copy(method = method, cash = null)
                }
                CorrectionKind.CANCEL -> {
                    check(previous.status == SaleStatus.LUNAS) { "Transaksi harus berstatus Lunas." }
                    previous.copy(status = SaleStatus.DIBATALKAN)
                }
                CorrectionKind.RECOVER -> {
                    if (previous.status == SaleStatus.LUNAS && corrections.latest(id)?.kind == CorrectionKind.RECOVER) {
                        return@withTransaction
                    }
                    check(previous.status == SaleStatus.DIBATALKAN && previous.method == PaymentMethod.QRIS) {
                        "Pemulihan hanya berlaku untuk tagihan QRIS yang dibatalkan."
                    }
                    previous.copy(
                        status = SaleStatus.LUNAS,
                        cash = null,
                        receiptNumber = previous.receiptNumber ?: receiptNumber(id),
                        confirmedAt = previous.confirmedAt ?: now,
                        confirmedBy = previous.confirmedBy ?: actor.id,
                    )
                }
            }
            session.checkActive(actor)
            sales.update(updated)
            corrections.insert(
                SaleCorrection(
                    saleId = id, kind = kind,
                    fromStatus = previous.status, toStatus = updated.status,
                    fromMethod = previous.method, toMethod = updated.method,
                    fromCash = previous.cash, toCash = updated.cash,
                    reason = reason.trim(), adminId = actor.id, at = now,
                )
            )
        }
    }
}
