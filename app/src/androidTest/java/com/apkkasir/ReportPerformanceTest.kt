package com.apkkasir

import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.room.withTransaction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apkkasir.data.AppContainer
import com.apkkasir.data.PaymentMethod
import com.apkkasir.data.Role
import com.apkkasir.data.Sale
import com.apkkasir.data.SaleStatus
import com.apkkasir.reports.Jakarta
import com.apkkasir.reports.PeriodKind
import com.apkkasir.ui.KasirApp
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportPerformanceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun measureDailyScreenAndMonthlyPdfAtOneThousandSales() {
        KasirFixture().use { fixture ->
            val app = AppContainer(fixture.context, fixture.database)
            runBlocking {
                app.accounts.setup("kasir", "kasir1234")
                app.accounts.login("kasir", "kasir1234")
                val cashier = checkNotNull(fixture.database.accounts().byRole(Role.CASHIER))
                val now = System.currentTimeMillis()
                fixture.database.withTransaction {
                    repeat(1000) { index ->
                        fixture.database.sales().insert(
                            Sale(
                                billNumber = "UJI-$index", cashierId = cashier.id,
                                createdAt = now, total = 33_000, status = SaleStatus.LUNAS,
                                method = PaymentMethod.TUNAI, cash = 33_000, receiptNumber = "NOTA-$index",
                                confirmedAt = now, confirmedBy = cashier.id,
                            )
                        )
                    }
                }
            }
            val widgetTotal = hasText("1000 transaksi lunas") and hasAnyAncestor(hasTestTag("sales-widget"))
            val tableTotal = hasText("1000 transaksi lunas") and hasAnyAncestor(hasTestTag("daily-report"))
            val firstRow = hasText("NOTA-999") and hasAnyAncestor(hasTestTag("daily-report"))
            val homeStart = SystemClock.elapsedRealtime()
            compose.setContent { KasirApp(app) }
            compose.waitUntil(10_000) { compose.onAllNodes(widgetTotal).fetchSemanticsNodes().isNotEmpty() }
            val homeMs = SystemClock.elapsedRealtime() - homeStart
            val button = compose.onNodeWithText("Laporan hari ini").performScrollTo()
            val recapReady = compose.onAllNodes(widgetTotal).fetchSemanticsNodes().isNotEmpty()
            val tableStart = SystemClock.elapsedRealtime()
            button.performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodes(tableTotal).fetchSemanticsNodes().isNotEmpty() &&
                    compose.onAllNodes(firstRow).fetchSemanticsNodes().isNotEmpty()
            }
            val tableMs = SystemClock.elapsedRealtime() - tableStart
            val pdfStart = SystemClock.elapsedRealtime()
            runBlocking { app.archives.create(PeriodKind.MONTHLY, LocalDate.now(Jakarta)) }
            val pdfMs = SystemClock.elapsedRealtime() - pdfStart
            Log.i("KasirPerformance",
                "1000 transaksi: beranda ${homeMs} ms, rekap siap saat ketuk=$recapReady, tabel ${tableMs} ms, PDF ${pdfMs} ms")
            assertTrue("Tabel: ${tableMs} ms", tableMs <= 2_000)
            assertTrue("PDF: ${pdfMs} ms", pdfMs <= 10_000)
        }
    }
}
