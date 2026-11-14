package com.apkkasir

import android.content.Context
import android.content.ContextWrapper
import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.apkkasir.data.openStoreDatabase
import java.io.Closeable
import java.io.File
import java.nio.file.Files
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import org.json.JSONObject

internal class LegacyStoreFixture(configured: Boolean = true) : Closeable {
    private val target = InstrumentationRegistry.getInstrumentation().targetContext
    val files = Files.createTempDirectory(target.cacheDir.toPath(), "legacy-store").toFile()
    private val name = "legacy-${UUID.randomUUID()}.db"
    val dbFile: File = target.getDatabasePath(name)
    private val context = object : ContextWrapper(target) {
        override fun getFilesDir(): File = files
    }
    val qris = File(files, "qris-legacy.image")
    val pdf = File(files, "reports/legacy.pdf")
    val code = "legacy-recovery-code"

    init {
        val schema = InstrumentationRegistry.getInstrumentation().context.assets
            .open("com.apkkasir.data.KasirDatabase/1.json").bufferedReader().use { it.readText() }
        dbFile.parentFile?.mkdirs()
        val layout = JSONObject(schema).getJSONObject("database")
        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { db ->
            val entities = layout.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices") ?: continue
                for (j in 0 until indices.length()) {
                    db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
                }
            }
            val setup = layout.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) {
                db.execSQL(setup.getString(i).replace("\${ROOM_IDENTITY_HASH}", layout.getString("identityHash")))
            }
            db.version = 1
            if (configured) seed(db)
        }
    }

    private fun seed(db: SQLiteDatabase) {
        db.execSQL("INSERT INTO User VALUES (7, 'pemilik', 'ADMIN', ?)", arrayOf(verifier("admin1234")))
        db.execSQL("INSERT INTO User VALUES (9, 'kasir', 'CASHIER', ?)", arrayOf(verifier("kasir1234")))
        db.execSQL("INSERT INTO StoreSettings VALUES (1, ?, ?)", arrayOf(verifier(code), qris.name))
        db.execSQL("INSERT INTO Product (id, name, price) VALUES (3, 'Kopi', 33000)")
        db.execSQL("INSERT INTO Sale VALUES (12, 'T-12', 9, 10, 33000, 'MENUNGGU', 'TUNAI', 50000, NULL, NULL, NULL)")
        db.execSQL("INSERT INTO Sale VALUES (13, 'T-13', 9, 10, 33000, 'LUNAS', 'QRIS', NULL, 'N-13', 20, 7)")
        db.execSQL("INSERT INTO SaleLine VALUES (12, 0, 3, 'Kopi', 33000, 1, 33000)")
        db.execSQL("INSERT INTO SaleLine VALUES (13, 0, 3, 'Kopi', 33000, 1, 33000)")
        db.execSQL("INSERT INTO SaleCorrection VALUES (5, 13, 'RECOVER', 'DIBATALKAN', 'LUNAS', 'QRIS', 'QRIS', NULL, NULL, 'Pembayaran diterima', 7, 20)")
        db.execSQL("INSERT INTO ReportDocument VALUES ('report', 'DAILY', '2026-11-13', '2026-11-13', 20, 'Laporan.pdf', 'legacy.pdf')")
        qris.writeText("QRIS fixture")
        pdf.parentFile?.mkdirs()
        pdf.writeText("PDF fixture")
    }

    fun room() = openStoreDatabase(context, name)

    fun edit(sql: String) = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
        .use { it.execSQL(sql) }

    fun scalar(sql: String): String = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        .use { db -> db.rawQuery(sql, null).use { check(it.moveToFirst()); it.getString(0) } }

    override fun close() {
        target.deleteDatabase(name)
        files.deleteRecursively()
    }
}

private fun verifier(value: String): String {
    val salt = ByteArray(16) { (it + 1).toByte() }
    val spec = PBEKeySpec(value.toCharArray(), salt, 600_000, 256)
    val digest = try {
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    } finally {
        spec.clearPassword()
    }
    val base64 = java.util.Base64.getEncoder()
    return "600000:${base64.encodeToString(salt)}:${base64.encodeToString(digest)}"
}
