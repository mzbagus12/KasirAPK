package com.apkkasir

import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.apkkasir.data.*
import com.apkkasir.payment.*
import com.apkkasir.reports.*
import java.io.Closeable
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.flow.first

internal class KasirFixture : Closeable {
    private val target = InstrumentationRegistry.getInstrumentation().targetContext
    val files = Files.createTempDirectory(target.cacheDir.toPath(), "kasir-test").toFile()
    val context = object : ContextWrapper(target) {
        override fun getFilesDir(): File = files
        override fun getApplicationContext(): Context = this
    }
    val database = Room.inMemoryDatabaseBuilder(target, KasirDatabase::class.java).build()
    val session = LocalSession()
    val accounts = LocalAccounts(database, session)
    val products = ProductStore(database, session)
    val images = QrisImageStore(context, database, session)
    var now = 1_794_504_600_000L
    val checkout = CheckoutService(database, session, images) { now }
    val corrections = SaleCorrectionService(database, session) { now }
    val report = SalesReport(database, session)
    fun archives() = ReportArchive(context, database.documents(), report, session)

    suspend fun setup() = accounts.setup("kasir", "kasir1234")

    suspend fun cashier() {
        session.exit()
        accounts.login("kasir", "kasir1234")
    }

    suspend fun item(): Long {
        cashier()
        products.add("Kopi", 33_000)
        return database.products().observe().first().first().id
    }

    override fun close() {
        database.close()
        files.deleteRecursively()
    }
}

internal suspend fun fails(block: suspend () -> Unit) {
    var failed = false
    try {
        block()
    } catch (_: IllegalStateException) {
        failed = true
    } catch (_: IllegalArgumentException) {
        failed = true
    }
    org.junit.Assert.assertTrue("Operasi seharusnya ditolak", failed)
}
