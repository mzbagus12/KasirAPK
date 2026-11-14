package com.apkkasir

import android.database.sqlite.SQLiteFullException
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.apkkasir.data.KasirDatabase
import com.apkkasir.data.LocalAccounts
import com.apkkasir.data.LocalSession
import com.apkkasir.data.Role
import com.apkkasir.data.StoreReadiness
import com.apkkasir.data.inspectStore
import com.apkkasir.data.storeMigration
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StoreUpgradeTest {
    @Test fun preservesStoreAndRecoversSameCashierWithLegacyCode() = runBlocking {
        LegacyStoreFixture().use { f ->
            f.room().use { db ->
                assertEquals(StoreReadiness.READY, inspectStore(db, f.files).readiness)
                val session = LocalSession()
                val accounts = LocalAccounts(db, session)
                fails { accounts.login("pemilik", "admin1234") }
                accounts.login("kasir", "kasir1234")
                assertEquals(9L, session.account.value?.id)
                assertEquals(Role.HISTORICAL_ADMIN, db.accounts().byId(7)?.role)
                assertNotNull(db.accounts().byId(7)?.credential)
                assertEquals("pemilik", db.corrections().history(13).single().username)
                assertEquals(7L, db.sales().get(13)?.confirmedBy)
                assertEquals("N-13", db.sales().get(13)?.receiptNumber)
                assertEquals(50_000L, db.sales().get(12)?.cash)
                assertEquals(33_000L, db.sales().lines(12).single().unitPrice)
                assertEquals(f.qris.name, db.accounts().settings()?.qrisFile)
                assertEquals(f.pdf.name, db.documents().get("report")?.fileName)
                session.exit()
                accounts.recoverCashier(f.code, "kasirbaru")
                fails { accounts.recoverCashier(f.code, "lain1234") }
                accounts.login("kasir", "kasirbaru")
                assertEquals(9L, session.account.value?.id)
            }
            f.room().use { db ->
                assertEquals(StoreReadiness.READY, inspectStore(db, f.files).readiness)
                assertEquals(1, db.corrections().history(13).size)
            }
            assertEquals("2", f.scalar("PRAGMA user_version"))
            assertEquals("2", f.scalar("SELECT count(*) FROM User"))
            assertEquals("2", f.scalar("SELECT count(*) FROM Sale"))
            assertEquals("1", f.scalar("SELECT count(*) FROM ReportDocument"))
            assertEquals("QRIS fixture", f.qris.readText())
            assertEquals("PDF fixture", f.pdf.readText())
        }
    }

    @Test fun emptyAndConfiguredStoreWithoutOptionalFilesCanUpgrade() = runBlocking {
        LegacyStoreFixture(configured = false).use { f ->
            f.room().use { db ->
                assertEquals(StoreReadiness.EMPTY, inspectStore(db, f.files).readiness)
                LocalAccounts(db, LocalSession()).setup("kasir", "kasir1234")
                assertEquals(StoreReadiness.READY, inspectStore(db, f.files).readiness)
            }
            assertEquals("1", f.scalar("SELECT count(*) FROM User"))
        }
        LegacyStoreFixture().use { f ->
            f.edit("UPDATE StoreSettings SET qrisFile = NULL")
            f.edit("DELETE FROM ReportDocument")
            assertTrue(f.qris.delete())
            assertTrue(f.pdf.delete())
            f.room().use { assertEquals(StoreReadiness.READY, inspectStore(it, f.files).readiness) }
        }
    }

    @Test fun invalidLegacyRowsAndMissingFilesHoldWithoutChangingMetadata() = runBlocking {
        val cases: List<Pair<String, (LegacyStoreFixture) -> Unit>> = listOf(
            "akun" to { it.edit("DELETE FROM StoreSettings") },
            "akun" to { it.edit("DELETE FROM User WHERE role = 'CASHIER'") },
            "Relasi" to { it.edit("UPDATE Sale SET confirmedBy = 99 WHERE id = 13") },
            "Pemilik tagihan" to { it.edit("UPDATE Sale SET cashierId = 7 WHERE id = 12") },
            "QRIS" to { check(it.qris.delete()) },
            "PDF" to { check(it.pdf.delete()) },
        )
        for ((message, corrupt) in cases) LegacyStoreFixture().use { f ->
            corrupt(f)
            val before = listOf(f.qris, f.pdf).associateWith { it.takeIf { file -> file.isFile }?.readText() }
            f.room().use { db ->
                val opening = inspectStore(db, f.files)
                assertEquals(StoreReadiness.HELD, opening.readiness)
                assertTrue(opening.message.orEmpty(), opening.message.orEmpty().contains(message))
            }
            assertEquals("1", f.scalar("PRAGMA user_version"))
            assertEquals("ADMIN", f.scalar("SELECT role FROM User WHERE id = 7"))
            assertEquals("legacy.pdf", f.scalar("SELECT fileName FROM ReportDocument"))
            for ((file, content) in before) assertEquals(content, file.takeIf { it.isFile }?.readText())
        }
    }

    @Test fun failedTransactionRollsBackAndRetryUpgrades() = runBlocking {
        LegacyStoreFixture().use { f ->
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val interrupted = object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    storeMigration(f.files).migrate(db)
                    throw SQLiteFullException("Fixture interruption")
                }
            }
            Room.databaseBuilder(context, KasirDatabase::class.java, f.dbFile.name)
                .addMigrations(interrupted).build().use {
                    assertEquals(StoreReadiness.RETRY, inspectStore(it, f.files).readiness)
                }
            assertEquals("1", f.scalar("PRAGMA user_version"))
            assertEquals("ADMIN", f.scalar("SELECT role FROM User WHERE id = 7"))
            f.room().use { assertEquals(StoreReadiness.READY, inspectStore(it, f.files).readiness) }
            assertEquals("HISTORICAL_ADMIN", f.scalar("SELECT role FROM User WHERE id = 7"))
        }
    }

    @Test fun corruptDatabaseIsPreserved() = runBlocking {
        LegacyStoreFixture(configured = false).use { f ->
            val invalid = ByteArray(4096) { 7 }
            f.dbFile.writeBytes(invalid)
            f.room().use { assertEquals(StoreReadiness.HELD, inspectStore(it, f.files).readiness) }
            assertArrayEquals(invalid, f.dbFile.readBytes())
        }
    }
}

private inline fun <T> KasirDatabase.use(block: (KasirDatabase) -> T): T =
    try {
        block(this)
    } finally {
        close()
    }
