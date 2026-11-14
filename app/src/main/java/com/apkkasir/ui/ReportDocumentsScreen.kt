package com.apkkasir.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.apkkasir.data.AppContainer
import com.apkkasir.data.AccountSession
import com.apkkasir.data.ReportDocument
import com.apkkasir.data.Role
import com.apkkasir.reports.*
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReportDocumentsScreen(app: AppContainer, back: () -> Unit) {
    val action = rememberAction()
    var period by remember { mutableStateOf(PeriodKind.DAILY) }
    var date by remember { mutableStateOf(LocalDate.now(Jakarta).toString()) }
    var documents by remember { mutableStateOf<List<ReportDocument>?>(null) }
    var viewer by remember { mutableStateOf<ReportDocument?>(null) }
    var deleting by remember { mutableStateOf<ReportDocument?>(null) }
    var exporting by remember { mutableStateOf<Pair<String, AccountSession>?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(app) { action.run { documents = app.archives.documents() } }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val request = exporting
        exporting = null
        if (request != null && uri != null && app.session.account.value === request.second) action.run {
            app.archives.export(request.first, uri, request.second)
            message = "Salinan PDF disimpan."
        }
    }
    if (viewer != null) {
        ReportViewer(app, checkNotNull(viewer)) { viewer = null }
        return
    }
    Page("Dokumen PDF", back) {
        ContentCard {
            SectionTitle("Buat laporan")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodKind.entries.forEach { kind ->
                    FilterChip(selected = period == kind, onClick = { period = kind }, label = { Text(kind.label) },
                        modifier = Modifier.heightIn(min = 48.dp))
                }
            }
            OutlinedTextField(date, { date = it }, Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Tanggal acuan (YYYY-MM-DD)") })
            runCatching { ReportPeriod.of(period, LocalDate.parse(date.trim())) }.getOrNull()?.let {
                Text("${it.start} sampai ${it.endExclusive.minusDays(1)} · WIB")
            }
            Button(enabled = !action.busy && documents != null, onClick = {
                action.run {
                    app.archives.create(period, parseDate(date))
                    documents = app.archives.documents()
                    message = "PDF tersimpan di arsip."
                }
            }) { Text("Buat PDF") }
            message?.let { Text(it) }
            ErrorText(action.error)
        }
        SectionTitle("Arsip laporan")
        if (documents == null) {
            if (action.busy) CircularProgressIndicator()
            else OutlinedButton(onClick = { action.run { documents = app.archives.documents() } }) { Text("Coba lagi") }
        } else if (documents.isNullOrEmpty()) {
            Text("Belum ada PDF tersimpan.")
        }
        documents.orEmpty().forEach { document ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(document.kind.label, style = MaterialTheme.typography.titleSmall)
                    Text("${document.from} sampai ${document.through}")
                    Text("Dibuat ${formatWib(document.createdAt)}", style = MaterialTheme.typography.bodySmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(enabled = !action.busy, onClick = { viewer = document }) { Text("Buka") }
                        TextButton(enabled = !action.busy && exporting == null, onClick = {
                            exporting = document.id to app.session.requireRole(Role.CASHIER)
                            exporter.launch(document.displayName)
                        }) { Text("Unduh") }
                        TextButton(enabled = !action.busy, onClick = { deleting = document }) { Text("Hapus") }
                    }
                }
            }
        }
    }
    deleting?.let { document ->
        AlertDialog(
            onDismissRequest = { if (!action.busy) deleting = null },
            title = { Text("Hapus PDF dari arsip?") },
            text = { Text("Laporan ${document.kind.label.lowercase()} ${document.from} sampai ${document.through}.") },
            confirmButton = {
                TextButton(enabled = !action.busy, onClick = {
                    deleting = null
                    action.run {
                        app.archives.delete(document.id)
                        documents = app.archives.documents()
                        message = "PDF dihapus dari arsip."
                    }
                }) { Text("Hapus PDF") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Batal") } },
        )
    }
}

@Composable
private fun ReportViewer(app: AppContainer, document: ReportDocument, back: () -> Unit) {
    val action = rememberAction()
    var page by remember { mutableStateOf<ReportPage?>(null) }
    var index by remember { mutableIntStateOf(0) }
    fun load(target: Int) = action.run {
        page = app.archives.page(document.id, target)
        index = target
    }
    LaunchedEffect(document.id) { load(0) }
    Page("Laporan ${document.kind.label.lowercase()}", back) {
        if (action.busy && page == null) CircularProgressIndicator()
        ErrorText(action.error)
        if (action.error != null) OutlinedButton(onClick = { load(index) }) { Text("Coba lagi") }
        page?.let {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(enabled = index > 0 && !action.busy, onClick = { load(index - 1) }) { Text("Sebelumnya") }
                TextButton(enabled = index < it.pageCount - 1 && !action.busy, onClick = { load(index + 1) }) {
                    Text("Berikutnya")
                }
            }
            Text("Halaman ${index + 1} dari ${it.pageCount}")
            Image(it.bitmap.asImageBitmap(), "Halaman laporan ${index + 1}",
                Modifier.fillMaxWidth().aspectRatio(it.bitmap.width.toFloat() / it.bitmap.height), contentScale = ContentScale.Fit)
        }
    }
}
