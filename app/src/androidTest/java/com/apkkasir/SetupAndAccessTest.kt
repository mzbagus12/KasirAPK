package com.apkkasir

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apkkasir.data.Role
import java.io.File
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetupAndAccessTest {
    @Test fun rotateAfterInterruptedRecoveryAndRejectPreviousCode() = runBlocking {
        KasirFixture().use { f ->
            val original = f.setup()
            fails { f.accounts.setup("lain", "kasir1234") }
            val id = f.database.accounts().byRole(Role.CASHIER)?.id
            val issuedBeforeInterruption = f.accounts.recoverCashier(original, "kasirbaru1")
            fails { f.accounts.recoverCashier(original, "kasirbaru2") }
            f.accounts.login("kasir", "kasirbaru1")
            fails { f.accounts.rotateCode("passwordsalah") }
            val newCode = f.accounts.rotateCode("kasirbaru1")
            assertNotEquals(issuedBeforeInterruption, newCode)
            fails { f.accounts.changePassword("passwordsalah", "kasirbaru2") }
            f.accounts.changePassword("kasirbaru1", "kasirbaru2")
            f.session.exit()
            fails { f.accounts.recoverCashier(issuedBeforeInterruption, "kasirbaru3") }
            fails { f.accounts.login("kasir", "kasirbaru1") }
            f.accounts.login("kasir", "kasirbaru2")
            assertEquals(Role.CASHIER, f.session.account.value?.role)
            assertEquals(id, f.session.account.value?.id)
            fails { f.accounts.recoverCashier(newCode, "kasirbaru3") }
            f.session.exit()
            f.accounts.recoverCashier(newCode, "kasirbaru3")
            f.accounts.login("kasir", "kasirbaru3")
            assertEquals(id, f.session.account.value?.id)
        }
    }

    @Test fun pendingLoginCannotEnterAfterRecovery() = runBlocking {
        KasirFixture().use { f ->
            val code = f.setup()
            val id = f.database.accounts().byRole(Role.CASHIER)?.id
            val checked = CompletableDeferred<Unit>()
            val proceed = CompletableDeferred<Unit>()
            val pending = async {
                fails {
                    f.accounts.login("kasir", "kasir1234") {
                        checked.complete(Unit)
                        proceed.await()
                    }
                }
            }
            try {
                checked.await()
                f.accounts.recoverCashier(code, "passwordbaru")
            } finally {
                proceed.complete(Unit)
            }
            pending.await()
            assertNull(f.session.account.value)
            f.accounts.login("kasir", "passwordbaru")
            assertEquals(id, f.session.account.value?.id)
        }
    }

    @Test fun onlyOneConcurrentRecoveryConsumesCode() = runBlocking {
        KasirFixture().use { f ->
            val code = f.setup()
            val first = async { runCatching { f.accounts.recoverCashier(code, "kasirbaru1") } }
            val second = async { runCatching { f.accounts.recoverCashier(code, "kasirbaru2") } }
            assertEquals(1, listOf(first.await(), second.await()).count { it.isSuccess })
        }
    }

    @Test fun failedImageImportKeepsActiveImage() = runBlocking {
        KasirFixture().use { f ->
            f.setup()
            f.cashier()
            assertNull(f.images.current())
            val image = File(f.files, "image.png")
            val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
            image.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            val installed = f.images.save(Uri.fromFile(image))
            val bad = File(f.files, "not-image.txt").apply { writeText("bukan gambar") }
            fails { f.images.save(Uri.fromFile(bad)) }
            assertEquals(installed, f.images.current())
        }
    }
}
