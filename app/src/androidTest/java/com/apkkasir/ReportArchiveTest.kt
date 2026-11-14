package com.apkkasir

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apkkasir.data.CorrectionKind
import com.apkkasir.data.PaymentMethod
import com.apkkasir.reports.*
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportArchiveTest {
    @Test fun archiveReconcilesInterruptedOperationsAndPreservesOtherCopies() = runBlocking {
        KasirFixture().use { f ->
            f.setup()
            val product = f.item()
            val date = LocalDate.of(2026, 11, 12)
            val daily = ReportPeriod.of(PeriodKind.DAILY, date)
            f.now = daily.startMillis
            f.cashier()
            val sale = f.checkout.create(mapOf(product to 1))
            f.checkout.updateDraft(sale.id, PaymentMethod.TUNAI, 33_000)
            f.checkout.confirm(sale.id, PaymentMethod.TUNAI, 33_000)
            f.session.exit()
            fails { f.archives().documents() }
            f.cashier()
            val archive = f.archives()
            val first = archive.create(PeriodKind.DAILY, date)
            val folder = File(f.files, "reports")
            val original = File(folder, first.fileName).readBytes()
            val external = File(f.files, "export.pdf").apply { writeBytes(original) }
            f.corrections.apply(sale.id, CorrectionKind.METHOD, "Metode semula salah", PaymentMethod.QRIS)
            val revised = archive.create(PeriodKind.DAILY, date)
            val monthly = archive.create(PeriodKind.MONTHLY, date)
            assertArrayEquals(original, File(folder, first.fileName).readBytes())
            val page = archive.page(revised.id, 0)
            assertEquals(1, page.pageCount)
            page.bitmap.recycle()
            val partial = File(folder, "unfinished.tmp").apply { writeText("partial") }
            val orphan = File(folder, "orphan.pdf").apply { writeText("orphan") }
            assertTrue(File(folder, revised.fileName).delete())
            val reopened = f.archives()
            assertEquals(setOf(first.id, monthly.id), reopened.documents().map { it.id }.toSet())
            assertFalse(partial.exists())
            assertFalse(orphan.exists())
            reopened.delete(first.id)
            assertArrayEquals(original, external.readBytes())
            assertEquals(1, f.report.snapshot(daily).count)
        }
    }
}
