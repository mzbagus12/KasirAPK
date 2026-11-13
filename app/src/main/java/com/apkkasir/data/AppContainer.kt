package com.apkkasir.data

import android.content.Context
import androidx.room.Room
import com.apkkasir.payment.CheckoutService
import com.apkkasir.payment.SaleCorrectionService
import com.apkkasir.reports.ReportArchive
import com.apkkasir.reports.SalesReport

class AppContainer(
    context: Context,
    private val database: KasirDatabase = Room.databaseBuilder(
        context.applicationContext, KasirDatabase::class.java, "kasir.db",
    ).build(),
) {
    private val app = context.applicationContext
    val session = LocalSession()
    val accounts = LocalAccounts(database, session)
    val products = ProductStore(database.products(), session)
    val qris = QrisImageStore(app, database, session)
    val checkout = CheckoutService(database, session, qris)
    val corrections = SaleCorrectionService(database, session)
    val reports = SalesReport(database, session)
    val archives = ReportArchive(app, database.documents(), reports, session)
}
