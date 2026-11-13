package com.apkkasir

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apkkasir.data.PaymentMethod
import com.apkkasir.data.SaleStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckoutRecoveryTest {
    @Test fun draftAndReceiptRemainOnSameBill() = runBlocking {
        KasirFixture().use { f ->
            f.setup()
            val product = f.item()
            f.cashier()
            val sale = f.checkout.create(mapOf(product to 1))
            f.checkout.updateDraft(sale.id, PaymentMethod.TUNAI, 50_000)
            f.cashier()
            assertEquals(50_000L, f.checkout.history().first().single().cash)
            f.checkout.updateDraft(sale.id, PaymentMethod.TUNAI, null)
            assertNull(f.checkout.history().first().single().cash)
            fails { f.checkout.confirm(sale.id, PaymentMethod.TUNAI, 50_000) }
            f.checkout.updateDraft(sale.id, PaymentMethod.TUNAI, 50_000)
            f.checkout.updateDraft(sale.id, PaymentMethod.QRIS, null)
            assertNull(f.checkout.history().first().single().cash)
            f.checkout.updateDraft(sale.id, PaymentMethod.TUNAI, 40_000)
            fails { f.checkout.confirm(sale.id, PaymentMethod.TUNAI, 50_000) }
            val receipt = f.checkout.confirm(sale.id, PaymentMethod.TUNAI, 40_000)
            val repeated = f.checkout.confirm(sale.id, PaymentMethod.TUNAI, 40_000)
            assertEquals(receipt, repeated)
            assertEquals(1, f.checkout.history().first().size)
        }
    }

    @Test fun cancelUnpaidCannotConfirm() = runBlocking {
        KasirFixture().use { f ->
            f.setup()
            val product = f.item()
            f.cashier()
            val sale = f.checkout.create(mapOf(product to 1))
            f.checkout.cancel(sale.id)
            assertEquals(SaleStatus.DIBATALKAN, f.checkout.history().first().single().status)
            fails { f.checkout.confirm(sale.id, PaymentMethod.TUNAI, 33_000) }
        }
    }
}
