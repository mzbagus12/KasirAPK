package com.apkkasir.data

import android.content.Context
import com.apkkasir.payment.CheckoutService
import com.apkkasir.payment.SaleCorrectionService
import com.apkkasir.reports.ReportArchive
import com.apkkasir.reports.SalesReport

class AppContainer(
    context: Context,
    private val database: KasirDatabase = openStoreDatabase(context.applicationContext),
) {
    private val app = context.applicationContext
    val session = LocalSession()
    val accounts = LocalAccounts(database, session)
    val products = ProductStore(database, session)
    val qris = QrisImageStore(app, database, session)
    val checkout = CheckoutService(database, session, qris)
    val corrections = SaleCorrectionService(database, session)
    val reports = SalesReport(database, session)
    val archives = ReportArchive(app, database.documents(), reports, session)

    internal suspend fun inspectStore(): StoreOpening = inspectStore(database, app.filesDir)
}
