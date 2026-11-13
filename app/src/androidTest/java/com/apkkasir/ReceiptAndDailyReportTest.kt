package com.apkkasir

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apkkasir.data.CorrectionKind
import com.apkkasir.data.PaymentMethod
import com.apkkasir.reports.PeriodKind
import com.apkkasir.reports.ReportPeriod
import java.time.LocalDate
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReceiptAndDailyReportTest {
    @Test fun manualQrisUsesStoreImageAndMarksReceipt() = runBlocking {
        KasirFixture().use { f ->
            f.setup()
            val product = f.item()
            val image = File(f.files, "test-store.png")
            val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
            image.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            f.images.save(Uri.fromFile(image))
            val day = ReportPeriod.of(PeriodKind.DAILY, LocalDate.of(2026, 11, 12))
            f.now = day.startMillis
            f.cashier()
            val bill = f.checkout.create(mapOf(product to 1))
            f.checkout.updateDraft(bill.id, PaymentMethod.QRIS, null)
            val receipt = f.checkout.confirm(bill.id, PaymentMethod.QRIS, null)
            assertEquals(PaymentMethod.QRIS, receipt.sale.method)
            f.admin()
            assertEquals(receipt.sale, f.report.snapshot(day).rows.single())
        }
    }

    @Test fun reportUsesFirstConfirmationAndFrozenPrice() = runBlocking {
        KasirFixture().use { f ->
            f.setup()
            val product = f.item()
            val day = ReportPeriod.of(PeriodKind.DAILY, LocalDate.of(2026, 11, 12))
            f.now = day.startMillis - 1
            f.cashier()
            val sale = f.checkout.create(mapOf(product to 1))
            f.admin()
            f.products.changePrice(product, 75_000)
            assertEquals(0, f.report.snapshot(day).count)
            f.cashier()
            f.now = day.startMillis
            f.checkout.updateDraft(sale.id, PaymentMethod.TUNAI, 50_000)
            val receipt = f.checkout.confirm(sale.id, PaymentMethod.TUNAI, 50_000)
            assertEquals(33_000L, receipt.lines.single().unitPrice)
            f.admin()
            assertEquals(receipt.sale.receiptNumber, f.report.snapshot(day).rows.single().receiptNumber)
            assertEquals("33000", f.report.snapshot(day).total.toString())
            f.corrections.apply(sale.id, CorrectionKind.CANCEL, "Keliru")
            assertEquals(0, f.report.snapshot(day).count)
            assertEquals(1, f.report.snapshot(day).rows.size)
        }
    }
}
