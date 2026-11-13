package com.apkkasir.payment

import androidx.room.withTransaction
import com.apkkasir.data.KasirDatabase
import com.apkkasir.data.LocalSession
import com.apkkasir.data.PaymentMethod
import com.apkkasir.data.QrisImageStore
import com.apkkasir.data.Receipt
import com.apkkasir.data.Role
import com.apkkasir.data.Sale
import com.apkkasir.data.SaleLine
import com.apkkasir.data.SaleStatus
import java.util.UUID

class CheckoutService(
    private val database: KasirDatabase,
    private val session: LocalSession,
    private val qris: QrisImageStore,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val sales = database.sales()

    fun history() = sales.forCashier(session.requireRole(Role.CASHIER).id)

    suspend fun create(cart: Map<Long, Int>): Sale {
        val actor = session.requireRole(Role.CASHIER)
        require(cart.isNotEmpty() && cart.values.all { it > 0 }) { "Keranjang belum valid." }
        return database.withTransaction {
            session.checkActive(actor)
            val lines = cart.entries.mapIndexed { index, (productId, count) ->
                val product = database.products().get(productId) ?: error("Barang tidak ditemukan.")
                SaleLine(0, index, productId, product.name, product.price, count, subtotal(product.price, count))
            }
            val sale = Sale(
                billNumber = "TAG-${UUID.randomUUID()}",
                cashierId = actor.id,
                createdAt = clock(),
                total = totalRupiah(lines.map(SaleLine::subtotal)),
            )
            val id = sales.insert(sale)
            sales.insertLines(lines.map { it.copy(saleId = id) })
            sale.copy(id = id)
        }
    }

    suspend fun updateDraft(id: Long, method: PaymentMethod, cash: Long?): Sale {
        val actor = session.requireRole(Role.CASHIER)
        return database.withTransaction {
            session.checkActive(actor)
            val sale = owned(id, actor.id)
            check(sale.status == SaleStatus.MENUNGGU) { "Tagihan sudah selesai." }
            val amount = cash?.takeIf { method == PaymentMethod.TUNAI && it >= 0 }
            sale.copy(method = method, cash = amount).also { sales.update(it) }
        }
    }

    suspend fun confirm(id: Long, expectedMethod: PaymentMethod, expectedCash: Long?): Receipt {
        val actor = session.requireRole(Role.CASHIER)
        return database.withTransaction {
            session.checkActive(actor)
            val sale = owned(id, actor.id)
            if (sale.status == SaleStatus.LUNAS) return@withTransaction Receipt(sale, sales.lines(id))
            check(sale.status == SaleStatus.MENUNGGU) { "Tagihan sudah dibatalkan." }
            check(sale.method == expectedMethod && sale.cash == expectedCash) { "Perubahan tagihan belum tersimpan." }
            when (expectedMethod) {
                PaymentMethod.TUNAI -> check(expectedCash != null && expectedCash >= sale.total) { "Nominal tunai belum mencukupi." }
                PaymentMethod.QRIS -> check(qris.current() != null) { "QRIS toko belum tersedia." }
            }
            session.checkActive(actor)
            val paid = sale.copy(
                status = SaleStatus.LUNAS,
                receiptNumber = receiptNumber(sale.id),
                confirmedAt = clock(),
                confirmedBy = actor.id,
            )
            sales.update(paid)
            Receipt(paid, sales.lines(id))
        }
    }

    suspend fun cancel(id: Long) {
        val actor = session.requireRole(Role.CASHIER)
        database.withTransaction {
            session.checkActive(actor)
            val sale = owned(id, actor.id)
            check(sale.status == SaleStatus.MENUNGGU) { "Tagihan sudah selesai." }
            sales.update(sale.copy(status = SaleStatus.DIBATALKAN))
        }
    }

    suspend fun receipt(id: Long): Receipt {
        val actor = session.requireRole(Role.CASHIER)
        return database.withTransaction {
            session.checkActive(actor)
            val sale = owned(id, actor.id)
            check(sale.receiptNumber != null) { "Nota belum tersedia." }
            Receipt(sale, sales.lines(id))
        }
    }

    private suspend fun owned(id: Long, cashierId: Long): Sale =
        sales.get(id)?.takeIf { it.cashierId == cashierId } ?: error("Tagihan tidak ditemukan.")
}

internal fun receiptNumber(id: Long): String = "NOTA-${id.toString().padStart(6, '0')}"
