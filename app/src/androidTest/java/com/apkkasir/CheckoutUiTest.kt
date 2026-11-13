package com.apkkasir

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apkkasir.data.AppContainer
import com.apkkasir.ui.KasirApp
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckoutUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun cashierCannotConfirmUntilLatestCashEditIsSaved() {
        KasirFixture().use { fixture ->
            val app = AppContainer(fixture.context, fixture.database)
            runBlocking {
                app.accounts.setup("pemilik", "pemilik123", "kasir", "kasir1234")
                app.accounts.login("pemilik", "pemilik123")
                app.products.add("Kopi", 33_000)
                app.session.exit()
                app.accounts.login("kasir", "kasir1234")
            }
            compose.setContent { KasirApp(app) }
            click("Buat tagihan")
            click("Tambah")
            click("Buat tagihan")
            click("Tunai")
            compose.onNodeWithText("Uang diterima").performTextInput("50000")
            compose.waitUntil(10_000) {
                compose.onAllNodes(hasText("Konfirmasi") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
            }
            fixture.database.openHelper.writableDatabase.execSQL(
                "CREATE TRIGGER reject_edit BEFORE UPDATE ON Sale WHEN NEW.cash = 40000 " +
                    "BEGIN SELECT RAISE(ABORT, 'uji gagal simpan'); END"
            )
            compose.onNodeWithText("Uang diterima").performTextReplacement("40000")
            awaitText("Coba simpan lagi")
            compose.onNodeWithText("Konfirmasi").assertIsNotEnabled()
            fixture.database.openHelper.writableDatabase.execSQL("DROP TRIGGER reject_edit")
            click("Coba simpan lagi")
            compose.waitUntil(10_000) {
                compose.onAllNodes(hasText("Konfirmasi") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
            }
            click("Konfirmasi")
            awaitText("Kembalian Rp7.000")
            awaitText("Nota")
            assertEquals(1, runBlocking {
                fixture.database.sales().within(Long.MIN_VALUE, Long.MAX_VALUE).size
            })
        }
    }

    private fun click(text: String) {
        val button = hasText(text) and hasClickAction()
        compose.waitUntil(10_000) { compose.onAllNodes(button).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(button).performScrollTo().performClick()
    }

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }
}
