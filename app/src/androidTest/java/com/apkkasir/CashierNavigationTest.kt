package com.apkkasir

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apkkasir.data.AppContainer
import com.apkkasir.data.PaymentMethod
import com.apkkasir.ui.KasirApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CashierNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun receiptAndCheckoutReturnToTheirSourceAndHistoryTabClearsShortcutFilter() {
        KasirFixture().use { fixture ->
            val app = AppContainer(fixture.context, fixture.database)
            val (billNumber, receiptNumber) = runBlocking {
                app.accounts.setup("kasir", "kasir1234")
                app.accounts.login("kasir", "kasir1234")
                app.products.add("Kopi", 2_000)
                val product = app.products.products.first().single()
                val pending = app.checkout.create(mapOf(product.id to 1))
                val paid = app.checkout.create(mapOf(product.id to 1))
                app.checkout.updateDraft(paid.id, PaymentMethod.TUNAI, 2_000)
                val receipt = app.checkout.confirm(paid.id, PaymentMethod.TUNAI, 2_000)
                pending.billNumber to checkNotNull(receipt.sale.receiptNumber)
            }
            compose.setContent { KasirApp(app) }
            click("Nota")
            await(receiptNumber)
            compose.onNodeWithText(billNumber).assertDoesNotExist()
            click("Lihat nota")
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("receipt-content").fetchSemanticsNodes().isNotEmpty() }
            click("Selesai")
            await("Daftar nota")
            click("Riwayat")
            await(billNumber)
            click("Riwayat")
            compose.onAllNodesWithText("Riwayat transaksi").assertCountEquals(1)
            click("Beranda")
            click("Tagihan menunggu")
            await(billNumber)
            compose.onNodeWithText(receiptNumber).assertDoesNotExist()
            click("Lanjutkan")
            click("Tunai")
            compose.onNodeWithText("Uang diterima").performTextInput("2000")
            compose.waitUntil(10_000) {
                compose.onAllNodes(hasText("Konfirmasi") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
            }
            click("Konfirmasi")
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("receipt-content").fetchSemanticsNodes().isNotEmpty() }
            click("Selesai")
            await("Belum ada tagihan menunggu.")
        }
    }

    private fun click(label: String) {
        val matcher = hasText(label) and hasClickAction()
        compose.waitUntil(10_000) { compose.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(matcher).performClick()
    }

    private fun await(label: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty() }
    }
}
