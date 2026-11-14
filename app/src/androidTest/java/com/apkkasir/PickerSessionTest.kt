package com.apkkasir

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apkkasir.data.AppContainer
import com.apkkasir.data.Role
import com.apkkasir.reports.PeriodKind
import com.apkkasir.ui.QrisSettingsScreen
import com.apkkasir.ui.ReportDocumentsScreen
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PickerSessionTest {
    @get:Rule val compose = createComposeRule()

    @Test fun screensDiscardOldResultsAndAcceptNewResults() {
        KasirFixture().use { f ->
            val app = AppContainer(f.context, f.database)
            runBlocking {
                app.accounts.setup("kasir", "kasir1234")
                app.accounts.login("kasir", "kasir1234")
                app.archives.create(PeriodKind.DAILY, LocalDate.of(2026, 11, 13))
            }
            val image = File(f.files, "picker.png")
            val bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
            image.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            val stalePdf = File(f.files, "stale.pdf")
            val newPdf = File(f.files, "new.pdf")
            val reports = mutableStateOf(false)
            val picker = TestPicker()
            compose.setContent {
                MaterialTheme {
                    CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                        if (reports.value) ReportDocumentsScreen(app) {} else QrisSettingsScreen(app) {}
                    }
                }
            }

            click("Pilih gambar QRIS")
            app.session.exit()
            compose.runOnIdle { picker.deliver(Uri.fromFile(image)) }
            compose.onNodeWithText("Pratinjau gambar baru").assertDoesNotExist()
            runBlocking { app.accounts.login("kasir", "kasir1234") }
            click("Pilih gambar QRIS")
            app.session.exit()
            runBlocking { app.accounts.login("kasir", "kasir1234") }
            compose.runOnIdle { picker.deliver(Uri.fromFile(image)) }
            compose.onNodeWithText("Pratinjau gambar baru").assertDoesNotExist()
            click("Pilih gambar QRIS")
            compose.runOnIdle { picker.deliver(Uri.fromFile(image)) }
            click("Simpan QRIS")
            awaitText("QRIS aktif")

            compose.runOnIdle { reports.value = true }
            click("Unduh")
            app.session.exit()
            compose.runOnIdle { picker.deliver(Uri.fromFile(stalePdf)) }
            compose.onNodeWithText("Salinan PDF disimpan.").assertDoesNotExist()
            runBlocking { app.accounts.login("kasir", "kasir1234") }
            click("Unduh")
            app.session.exit()
            runBlocking { app.accounts.login("kasir", "kasir1234") }
            compose.runOnIdle { picker.deliver(Uri.fromFile(stalePdf)) }
            assertFalse(stalePdf.exists())
            click("Unduh")
            compose.runOnIdle { picker.deliver(Uri.fromFile(newPdf)) }
            awaitText("Salinan PDF disimpan.")
            assertTrue(newPdf.length() > 0)
        }
    }

    @Test fun fileResultsMustUseTheSessionThatLaunchedThem() = runBlocking {
        KasirFixture().use { f ->
            f.setup()
            f.cashier()
            val image = File(f.files, "image.png")
            val bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
            image.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            val archives = f.archives()
            val document = archives.create(PeriodKind.DAILY, LocalDate.of(2026, 11, 13))
            val target = File(f.files, "export.pdf")
            val origin = f.session.requireRole(Role.CASHIER)

            f.session.exit()
            fails { f.images.save(Uri.fromFile(image), origin) }
            fails { archives.export(document.id, Uri.fromFile(target), origin) }
            f.cashier()
            fails { f.images.save(Uri.fromFile(image), origin) }
            fails { archives.export(document.id, Uri.fromFile(target), origin) }
            assertNull(f.images.current())
            assertFalse(target.exists())

            val current = f.session.requireRole(Role.CASHIER)
            val saved = f.images.save(Uri.fromFile(image), current)
            assertEquals(saved, f.images.current())
            archives.export(document.id, Uri.fromFile(target), current)
            assertTrue(target.length() > 0)
        }
    }

    private fun click(text: String) {
        val selector = hasText(text) and hasClickAction() and isEnabled()
        compose.waitUntil(10_000) { compose.onAllNodes(selector).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(selector).performScrollTo().performClick()
    }

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    private class TestPicker : ActivityResultRegistry(), ActivityResultRegistryOwner {
        override val activityResultRegistry: ActivityResultRegistry get() = this
        private var pending: Int? = null

        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            check(pending == null)
            pending = requestCode
        }

        fun deliver(uri: Uri) {
            val request = checkNotNull(pending)
            pending = null
            check(dispatchResult(request, uri))
        }
    }
}
