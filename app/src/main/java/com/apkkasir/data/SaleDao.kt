package com.apkkasir.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert
    suspend fun insert(sale: Sale): Long

    @Insert
    suspend fun insertLines(lines: List<SaleLine>)

    @Update
    suspend fun update(sale: Sale)

    @Query("SELECT * FROM Sale WHERE id = :id")
    suspend fun get(id: Long): Sale?

    @Query("SELECT * FROM SaleLine WHERE saleId = :id ORDER BY position")
    suspend fun lines(id: Long): List<SaleLine>

    @Query("SELECT * FROM Sale WHERE cashierId = :cashierId ORDER BY createdAt DESC, id DESC")
    fun forCashier(cashierId: Long): Flow<List<Sale>>

    @Query("SELECT * FROM Sale WHERE confirmedAt >= :start AND confirmedAt < :end ORDER BY confirmedAt DESC, id DESC")
    suspend fun within(start: Long, end: Long): List<Sale>

    @Query("SELECT * FROM Sale WHERE confirmedAt >= :start AND confirmedAt < :end ORDER BY confirmedAt DESC, id DESC")
    fun observeWithin(start: Long, end: Long): Flow<List<Sale>>

    @Query("SELECT * FROM Sale WHERE billNumber = :number COLLATE NOCASE OR receiptNumber = :number COLLATE NOCASE")
    suspend fun byNumber(number: String): Sale?

    @Query("SELECT * FROM Sale WHERE status = 'DIBATALKAN' AND method = 'QRIS' AND createdAt >= :start AND createdAt < :end ORDER BY createdAt DESC, id DESC")
    suspend fun cancelledQris(start: Long, end: Long): List<Sale>
}
