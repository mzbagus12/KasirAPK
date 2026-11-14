package com.apkkasir.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apkkasir.data.AppContainer
import com.apkkasir.data.AccountSession
import com.apkkasir.data.Role
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun QrisSettingsScreen(app: AppContainer, back: () -> Unit) {
    val context = LocalContext.current
    val action = rememberAction()
    var active by remember { mutableStateOf<File?>(null) }
    var selected by remember { mutableStateOf<Uri?>(null) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var pendingSession by remember { mutableStateOf<AccountSession?>(null) }
    var previewSession by remember { mutableStateOf<AccountSession?>(null) }
    LaunchedEffect(app) { active = app.qris.current() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val actor = pendingSession
        pendingSession = null
        if (uri != null && actor != null && app.session.account.value === actor) action.run {
            app.session.checkActive(actor)
            selected = null
            preview = null
            val bitmap = withContext(Dispatchers.IO) {
                decodePreview { context.contentResolver.openInputStream(uri) }
            } ?: error("Gambar tidak dapat dibaca.")
            app.session.checkActive(actor)
            previewSession = actor
            selected = uri
            preview = bitmap
        }
    }
    Page("QRIS toko", back) {
        if (active == null) Text("Belum ada gambar QRIS yang dipasang.")
        else {
            Text("QRIS aktif", style = MaterialTheme.typography.titleMedium)
            QrisFileImage(active)
        }
        OutlinedButton(enabled = !action.busy && pendingSession == null, onClick = {
            pendingSession = app.session.requireRole(Role.CASHIER)
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) { Text("Pilih gambar QRIS") }
        preview?.let {
            Text("Pratinjau gambar baru", style = MaterialTheme.typography.titleMedium)
            QrisBitmap(it)
            Text("Pastikan gambar berasal dari BRI Merchant toko.")
            Button(enabled = !action.busy, onClick = {
                val uri = selected ?: return@Button
                val actor = previewSession ?: return@Button
                action.run {
                    active = app.qris.save(uri, actor)
                    selected = null
                    preview = null
                }
            }) { Text("Simpan QRIS") }
        }
        ErrorText(action.error)
    }
}

@Composable
internal fun QrisFileImage(file: File?) {
    val bitmap by produceState<Bitmap?>(null, file) {
        value = withContext(Dispatchers.IO) {
            file?.takeIf(File::isFile)?.let { image -> decodePreview { image.inputStream() } }
        }
    }
    bitmap?.let { QrisBitmap(it) }
}

private fun decodePreview(open: () -> java.io.InputStream?): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    open()?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    val dimension = maxOf(bounds.outWidth, bounds.outHeight)
    while (dimension / sample > 1600) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return open()?.use { BitmapFactory.decodeStream(it, null, options) }
}

@Composable
private fun QrisBitmap(bitmap: Bitmap) {
    Image(
        bitmap.asImageBitmap(), contentDescription = "Kode QRIS toko",
        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp).background(Color.White).padding(8.dp),
        contentScale = ContentScale.Fit,
    )
}
