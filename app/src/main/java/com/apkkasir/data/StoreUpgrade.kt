package com.apkkasir.data

import android.content.Context
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteFullException
import android.database.sqlite.SQLiteTableLockedException
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal enum class StoreReadiness { EMPTY, READY, RETRY, HELD }

internal data class StoreOpening(val readiness: StoreReadiness, val message: String? = null)

private class InconsistentStore(message: String) : IllegalStateException(message)

private const val HISTORICAL_CREDENTIAL = "disabled"

internal fun openStoreDatabase(context: Context, name: String = "kasir.db"): KasirDatabase =
    Room.databaseBuilder(context, KasirDatabase::class.java, name)
        .openHelperFactory { config ->
            val original = config.callback
            val callback = object : SupportSQLiteOpenHelper.Callback(original.version) {
                override fun onConfigure(db: SupportSQLiteDatabase) = original.onConfigure(db)
                override fun onCreate(db: SupportSQLiteDatabase) = original.onCreate(db)
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
                    original.onUpgrade(db, oldVersion, newVersion)

                override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
                    original.onDowngrade(db, oldVersion, newVersion)

                override fun onOpen(db: SupportSQLiteDatabase) = original.onOpen(db)

                override fun onCorruption(db: SupportSQLiteDatabase) {
                    throw InconsistentStore("Basis data toko rusak. Pembaruan ditahan.")
                }
            }
            FrameworkSQLiteOpenHelperFactory().create(
                SupportSQLiteOpenHelper.Configuration.builder(config.context)
                    .name(config.name)
                    .callback(callback)
                    .noBackupDirectory(config.useNoBackupDirectory)
                    .allowDataLossOnRecovery(false)
                    .build(),
            )
        }
        .addMigrations(storeMigration(context.filesDir))
        .build()

internal fun storeMigration(files: File): Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!validateStore(db, files, legacy = true)) return
        db.execSQL(
            "UPDATE User SET role = 'HISTORICAL_ADMIN', credential = ? WHERE role = 'ADMIN'",
            arrayOf(HISTORICAL_CREDENTIAL),
        )
    }
}

internal suspend fun inspectStore(database: KasirDatabase, files: File): StoreOpening =
    withContext(Dispatchers.IO) {
        try {
            val configured = validateStore(database.openHelper.writableDatabase, files, legacy = false)
            StoreOpening(if (configured) StoreReadiness.READY else StoreReadiness.EMPTY)
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: InconsistentStore) {
            StoreOpening(StoreReadiness.HELD, failure.message)
        } catch (_: SQLiteDatabaseCorruptException) {
            StoreOpening(StoreReadiness.HELD, "Basis data toko rusak. Pembaruan ditahan.")
        } catch (failure: Exception) {
            if (failure is SQLiteCantOpenDatabaseException || failure is SQLiteDatabaseLockedException ||
                failure is SQLiteTableLockedException || failure is SQLiteDiskIOException ||
                failure is SQLiteFullException
            ) {
                StoreOpening(StoreReadiness.RETRY, "Data toko belum dapat dibuka. Coba lagi.")
            } else {
                StoreOpening(StoreReadiness.HELD, "Data toko tidak dapat diperiksa. Pembaruan ditahan.")
            }
        }
    }

private fun validateStore(db: SupportSQLiteDatabase, files: File, legacy: Boolean): Boolean {
    val settings = db.query("SELECT id, recoveryVerifier, qrisFile FROM StoreSettings").use { rows ->
        if (!rows.moveToFirst()) null else {
            val result = rows.getString(1) to rows.getString(2)
            if (rows.getLong(0) != 1L || rows.moveToNext()) inconsistentAccounts()
            result
        }
    }
    val users = db.query("SELECT id, username, role, credential FROM User").use { rows ->
        buildList {
            while (rows.moveToNext()) {
                add(StoredAccount(rows.getLong(0), rows.getString(1), rows.getString(2), rows.getString(3)))
            }
        }
    }
    val reports = File(files, "reports")
    if (settings == null) {
        val hasRows = listOf("Product", "Sale", "SaleLine", "SaleCorrection", "ReportDocument")
            .any { table -> db.query("SELECT 1 FROM $table LIMIT 1").use { it.moveToFirst() } }
        val hasFiles = files.listFiles().orEmpty().any { it.name.startsWith("qris-") } ||
            reports.listFiles().orEmpty().any { it.extension == "pdf" || it.extension == "tmp" }
        if (users.isNotEmpty() || hasRows || hasFiles) inconsistentAccounts()
        return false
    }

    val cashier = users.singleOrNull { it.role == "CASHIER" } ?: inconsistentAccounts()
    val other = users.filter { it.id != cashier.id }
    val historicalRole = if (legacy) "ADMIN" else "HISTORICAL_ADMIN"
    if (users.any { it.name.isBlank() } || !validVerifier(cashier.credential) ||
        !validVerifier(settings.first) || other.size > 1 || (legacy && other.size != 1) ||
        other.any { it.role != historicalRole || (!legacy && it.credential != HISTORICAL_CREDENTIAL) }
    ) inconsistentAccounts()

    if (!legacy) return true
    db.query("PRAGMA foreign_key_check").use {
        if (it.moveToFirst()) throw InconsistentStore("Relasi data toko tidak lengkap. Pembaruan ditahan.")
    }
    db.query("SELECT 1 FROM Sale WHERE cashierId <> ? LIMIT 1", arrayOf(cashier.id)).use {
        if (it.moveToFirst()) throw InconsistentStore("Pemilik tagihan tidak sesuai. Pembaruan ditahan.")
    }
    settings.second?.let { name ->
        if (!existsIn(files, name)) throw InconsistentStore("Gambar QRIS aktif hilang. Pembaruan ditahan.")
    }
    db.query("SELECT fileName FROM ReportDocument").use { rows ->
        while (rows.moveToNext()) {
            if (!existsIn(reports, rows.getString(0))) {
                throw InconsistentStore("Berkas PDF arsip hilang. Pembaruan ditahan.")
            }
        }
    }
    return true
}

private data class StoredAccount(val id: Long, val name: String, val role: String, val credential: String)

private fun existsIn(directory: File, name: String): Boolean =
    name == File(name).name && File(directory, name).isFile

private fun inconsistentAccounts(): Nothing =
    throw InconsistentStore("Data akun atau pengaturan toko tidak lengkap. Pembaruan ditahan.")
