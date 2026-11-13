package com.apkkasir

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apkkasir.data.Role
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetupAndAccessTest {
    @Test fun rotateAfterInterruptedRecoveryAndRejectPreviousCode() = runBlocking {
        KasirFixture().use { f ->
            val original = f.setup()
            val issuedBeforeInterruption = f.accounts.recoverAdmin(original, "adminbaru1")
            fails { f.accounts.recoverAdmin(original, "adminbaru2") }
            f.accounts.login("pemilik", "adminbaru1")
            fails { f.accounts.rotateCode("passwordsalah") }
            val newCode = f.accounts.rotateCode("adminbaru1")
            assertNotEquals(issuedBeforeInterruption, newCode)
            f.accounts.resetCashier("kasirbaru1")
            f.session.exit()
            fails { f.accounts.recoverAdmin(issuedBeforeInterruption, "adminbaru2") }
            f.accounts.login("kasir", "kasirbaru1")
            assertEquals(Role.CASHIER, f.session.account.value?.role)
            fails { f.accounts.rotateCode("kasirbaru1") }
            f.session.exit()
            f.accounts.recoverAdmin(newCode, "adminbaru2")
            assertTrue(f.accounts.isConfigured())
        }
    }

    @Test fun failedImageImportKeepsActiveImage() = runBlocking {
        KasirFixture().use { f ->
            f.setup()
            f.admin()
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
