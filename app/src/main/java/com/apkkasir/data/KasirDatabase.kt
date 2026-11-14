package com.apkkasir.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class, StoreSettings::class, Product::class, Sale::class, SaleLine::class,
        SaleCorrection::class, ReportDocument::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class KasirDatabase : RoomDatabase() {
    abstract fun accounts(): AccountDao
    abstract fun products(): ProductDao
    abstract fun sales(): SaleDao
    abstract fun corrections(): SaleCorrectionDao
    abstract fun documents(): ReportDocumentDao
}
