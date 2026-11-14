package com.apkkasir.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.withTransaction
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class QrisImageStore(
    private val context: Context,
    private val database: KasirDatabase,
    private val session: LocalSession,
) {
    private val mutex = Mutex()

    suspend fun current(): File? {
        val actor = session.requireRole(Role.CASHIER)
        return withContext(Dispatchers.IO) {
            database.accounts().settings()?.qrisFile?.let { File(context.filesDir, it) }
                ?.takeIf(File::isFile).also { session.checkActive(actor) }
        }
    }

    suspend fun save(uri: Uri, actor: AccountSession = session.requireRole(Role.CASHIER)): File = mutex.withLock {
        session.checkActive(actor)
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, "qris-${UUID.randomUUID()}.image")
            val old = try {
                val input = context.contentResolver.openInputStream(uri) ?: error("Gambar tidak dapat dibuka.")
                input.use { source ->
                    file.outputStream().use { target ->
                        val buffer = ByteArray(8192)
                        var size = 0L
                        while (true) {
                            val count = source.read(buffer)
                            if (count < 0) break
                            size += count
                            require(size <= 15 * 1024 * 1024) { "Ukuran gambar melebihi 15 MB." }
                            target.write(buffer, 0, count)
                        }
                    }
                }
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.path, bounds)
                require(bounds.outWidth > 0 && bounds.outHeight > 0) { "File gambar tidak valid." }
                require(bounds.outWidth.toLong() * bounds.outHeight <= 24_000_000L) { "Dimensi gambar terlalu besar." }
                val bitmap = BitmapFactory.decodeFile(file.path) ?: error("Gambar tidak dapat dibaca.")
                bitmap.recycle()
                withContext(NonCancellable) {
                    database.withTransaction {
                        session.checkActive(actor)
                        val settings = checkNotNull(database.accounts().settings())
                        database.accounts().updateSettings(settings.copy(qrisFile = file.name))
                        settings.qrisFile
                    }
                }
            } catch (failure: Exception) {
                file.delete()
                throw failure
            }
            old?.let { File(context.filesDir, it).delete() }
            file
        }
    }
}
