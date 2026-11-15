package com.apkkasir

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apkkasir.data.AppContainer
import com.apkkasir.data.CorrectionKind
import com.apkkasir.data.PaymentMethod
import com.apkkasir.ui.KasirApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeAndDailyReportTest {
    @get:Rule val compose = createComposeRule()

    @Test fun widgetMatchesReportAfterPaymentAndIsRemovedOnLogout() {
        KasirFixture().use { fixture ->
            val app = AppContainer(fixture.context, fixture.database)
            runBlocking {
                app.accounts.setup("kasir", "kasir1234")
                app.accounts.login("kasir", "kasir1234")
            }
            compose.setContent { KasirApp(app) }
            await("sales-widget", "0 transaksi lunas")
            runBlocking {
                app.products.add("Kopi", 2_000)
                val item = app.products.products.first().single()
                val bill = app.checkout.create(mapOf(item.id to 2))
                app.checkout.updateDraft(bill.id, PaymentMethod.TUNAI, 5_000)
                app.checkout.confirm(bill.id, PaymentMethod.TUNAI, 5_000)
            }
            await("sales-widget", "Rp4.000")
            compose.onNode(hasText("Laporan hari ini") and hasClickAction()).performScrollTo().performClick()
            await("daily-report", "1 transaksi lunas")
            runBlocking {
                val sale = app.checkout.history().first().single()
                app.corrections.apply(sale.id, CorrectionKind.METHOD, "Metode keliru", PaymentMethod.QRIS)
                app.corrections.apply(sale.id, CorrectionKind.CANCEL, "Uji pembatalan")
            }
            await("daily-report", "0 transaksi lunas")
            compose.onNode(hasText("Beranda") and hasClickAction()).performClick()
            await("sales-widget", "Belum ada transaksi lunas hari ini.")
            runBlocking {
                val sale = app.checkout.history().first().single()
                app.corrections.apply(sale.id, CorrectionKind.RECOVER, "Pembayaran diterima")
            }
            await("sales-widget", "1 transaksi lunas")
            compose.onNode(hasText("Pengaturan") and hasClickAction()).performClick()
            compose.onNode(hasText("Keluar") and hasClickAction()).performScrollTo().performClick()
            compose.onNodeWithTag("sales-widget").assertDoesNotExist()
            compose.onNodeWithTag("daily-report").assertDoesNotExist()
        }
    }

    private fun await(area: String, value: String) {
        val matcher = hasText(value) and hasAnyAncestor(hasTestTag(area))
        compose.waitUntil(10_000) { compose.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    }
}
