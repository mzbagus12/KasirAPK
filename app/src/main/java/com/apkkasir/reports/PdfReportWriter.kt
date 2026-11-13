package com.apkkasir.reports

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.apkkasir.payment.formatRupiah
import java.io.OutputStream

internal fun writeReportPdf(
    output: OutputStream,
    kind: PeriodKind,
    period: ReportPeriod,
    createdAt: Long,
    snapshot: ReportSnapshot,
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun Canvas.text(value: String, x: Float, y: Float, size: Float = 10f, bold: Boolean = false, width: Float = 535f) {
        paint.textSize = size
        paint.color = Color.rgb(26, 38, 34)
        paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        paint.textScaleX = 1f
        val measuredWidth = paint.measureText(value)
        if (measuredWidth > width) paint.textScaleX = width / measuredWidth
        drawText(value, x, y, paint)
    }
    val pages = snapshot.rows.chunked(33).ifEmpty { listOf(emptyList()) }
    val document = PdfDocument()
    try {
        pages.forEachIndexed { index, sales ->
            val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, index + 1).create())
            val canvas = page.canvas
            canvas.text("Laporan Penjualan ${kind.label}", 30f, 43f, 21f, true)
            canvas.text("${period.start} sampai ${period.endExclusive.minusDays(1)}", 30f, 65f, 12f)
            canvas.text("Dibuat ${formatWib(createdAt)}", 30f, 83f)
            canvas.text("${snapshot.count} transaksi lunas  |  ${formatRupiah(snapshot.total)}", 30f, 107f, 13f, true)
            val x = listOf(30f, 133f, 303f, 373f, 448f)
            listOf("No. nota", "Waktu konfirmasi", "Metode", "Status", "Total tagihan")
                .forEachIndexed { column, label -> canvas.text(label, x[column], 137f, bold = true) }
            paint.color = Color.rgb(208, 220, 212)
            canvas.drawLine(30f, 145f, 565f, 145f, paint)
            if (sales.isEmpty()) canvas.text("Belum ada penjualan pada periode ini.", 30f, 170f)
            sales.forEachIndexed { row, sale ->
                val values = listOf(
                    sale.receiptNumber.orEmpty(), sale.confirmedAt?.let(::formatWib).orEmpty(),
                    sale.method?.label.orEmpty(), sale.status.label, formatRupiah(sale.total),
                )
                values.forEachIndexed { column, value ->
                    val right = x.getOrNull(column + 1) ?: 565f
                    canvas.text(value, x[column], 164f + row * 19f, width = right - x[column] - 8f)
                }
            }
            canvas.text("Halaman ${index + 1} / ${pages.size}", 30f, 815f)
            document.finishPage(page)
        }
        document.writeTo(output)
    } finally {
        document.close()
    }
}
