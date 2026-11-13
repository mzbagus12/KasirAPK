package com.apkkasir.reports

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.apkkasir.data.LocalSession
import com.apkkasir.data.ReportDocument
import com.apkkasir.data.ReportDocumentDao
import com.apkkasir.data.Role
import java.io.File
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class ReportPage(val bitmap: Bitmap, val pageCount: Int)

class ReportArchive(
    private val context: Context,
    private val dao: ReportDocumentDao,
    private val report: SalesReport,
    private val session: LocalSession,
) {
    private val mutex = Mutex()
    private val directory = File(context.filesDir, "reports")
    private var reconciled = false

    suspend fun documents(): List<ReportDocument> = exclusive { dao.all() }

    suspend fun create(kind: PeriodKind, date: LocalDate): ReportDocument = exclusive {
        val actor = session.requireRole(Role.ADMIN)
        val period = ReportPeriod.of(kind, date)
        val id = UUID.randomUUID().toString()
        val created = System.currentTimeMillis()
        val item = ReportDocument(
            id, kind, period.start.toString(), period.endExclusive.minusDays(1).toString(),
            created, "Penjualan-${kind.label}-${period.start}-${id.take(8)}.pdf", "$id.pdf",
        )
        val temp = File(directory, "$id.tmp")
        val target = File(directory, item.fileName)
        try {
            val snapshot = report.snapshot(period)
            temp.outputStream().use { writeReportPdf(it, kind, period, created, snapshot) }
            withContext(NonCancellable) {
                session.checkActive(actor)
                check(temp.renameTo(target)) { "PDF gagal disimpan." }
                dao.insert(item)
            }
        } catch (failure: Exception) {
            reconciled = false
            temp.delete()
            throw failure
        }
        item
    }

    suspend fun page(id: String, index: Int): ReportPage = exclusive {
        val file = file(id)
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = try {
            PdfRenderer(descriptor)
        } catch (failure: Exception) {
            descriptor.close()
            throw failure
        }
        renderer.use { pdf ->
            require(index in 0 until pdf.pageCount) { "Halaman tidak tersedia." }
            pdf.openPage(index).use { page ->
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                ReportPage(bitmap, pdf.pageCount)
            }
        }
    }

    suspend fun export(id: String, uri: Uri) = exclusive {
        val source = file(id)
        val output = context.contentResolver.openOutputStream(uri, "wt") ?: error("Lokasi ekspor tidak tersedia.")
        output.use { target -> source.inputStream().use { it.copyTo(target) } }
    }

    suspend fun delete(id: String) = exclusive {
        val actor = session.requireRole(Role.ADMIN)
        val document = dao.get(id) ?: return@exclusive
        val file = File(directory, document.fileName)
        try {
            withContext(NonCancellable) {
                session.checkActive(actor)
                check(!file.exists() || file.delete()) { "PDF gagal dihapus." }
                dao.remove(id)
            }
        } catch (failure: Exception) {
            reconciled = false
            throw failure
        }
    }

    private suspend fun file(id: String): File {
        val document = dao.get(id) ?: error("Dokumen tidak ditemukan.")
        return File(directory, document.fileName).also {
            check(it.isFile) { "File PDF tidak ditemukan." }
        }
    }

    private suspend fun <T> exclusive(block: suspend () -> T): T {
        val actor = session.requireRole(Role.ADMIN)
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                session.checkActive(actor)
                if (!reconciled) reconcile()
                session.checkActive(actor)
                block()
            }
        }
    }

    private suspend fun reconcile() {
        check(directory.isDirectory || directory.mkdirs()) { "Penyimpanan arsip tidak tersedia." }
        val entries = dao.all()
        val names = entries.mapTo(mutableSetOf(), ReportDocument::fileName)
        val files = directory.listFiles() ?: error("Arsip tidak dapat diperiksa.")
        for (file in files) {
            if (file.isFile && file.name !in names && file.extension in setOf("pdf", "tmp")) {
                check(file.delete()) { "File arsip sementara gagal dibersihkan." }
            }
        }
        for (entry in entries) {
            if (!File(directory, entry.fileName).isFile) dao.remove(entry.id)
        }
        reconciled = true
    }
}
