package com.apkkasir

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apkkasir.data.CorrectionKind
import com.apkkasir.data.PaymentMethod
import com.apkkasir.data.SaleStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaleCorrectionTest {
    @Test fun recoveryPreservesReceiptAndRecordsActor() = runBlocking {
        KasirFixture().use { f ->
            f.setup()
            val product = f.item()
            f.cashier()
            val sale = f.checkout.create(mapOf(product to 1))
            f.checkout.updateDraft(sale.id, PaymentMethod.TUNAI, 33_000)
            val receipt = f.checkout.confirm(sale.id, PaymentMethod.TUNAI, 33_000)
            fails { f.corrections.apply(sale.id, CorrectionKind.CANCEL, "Akses kasir") }
            f.admin()
            f.corrections.apply(sale.id, CorrectionKind.METHOD, "Metode salah", PaymentMethod.QRIS)
            f.corrections.apply(sale.id, CorrectionKind.CANCEL, "Salah konfirmasi")
            f.now += 60_000
            f.corrections.apply(sale.id, CorrectionKind.RECOVER, "BRI berhasil")
            f.corrections.apply(sale.id, CorrectionKind.RECOVER, "BRI berhasil")
            val after = f.corrections.detail(sale.id)
            assertEquals(3, after.history.size)
            assertEquals("pemilik", after.history.first().username)
            assertEquals(receipt.sale.receiptNumber, after.receipt.sale.receiptNumber)
            assertEquals(receipt.sale.confirmedAt, after.receipt.sale.confirmedAt)
        }
    }

    @Test fun latePaymentCreatesFirstReceiptOnOriginalBill() = runBlocking {
        KasirFixture().use { f ->
            f.setup()
            val product = f.item()
            f.cashier()
            val sale = f.checkout.create(mapOf(product to 1))
            f.checkout.updateDraft(sale.id, PaymentMethod.QRIS, null)
            f.checkout.cancel(sale.id)
            f.admin()
            assertNull(f.corrections.detail(sale.id).receipt.sale.receiptNumber)
            f.now += 60_000
            f.corrections.apply(sale.id, CorrectionKind.RECOVER, "Notifikasi terlambat")
            val result = f.corrections.detail(sale.id).receipt.sale
            assertEquals(SaleStatus.LUNAS, result.status)
            assertNotNull(result.receiptNumber)
            assertEquals(f.now, result.confirmedAt)
        }
    }
}
