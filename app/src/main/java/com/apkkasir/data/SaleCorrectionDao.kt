package com.apkkasir.data

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SaleCorrectionDao {
    @Insert
    suspend fun insert(correction: SaleCorrection)

    @Query("SELECT SaleCorrection.*, User.username AS username FROM SaleCorrection INNER JOIN User ON User.id = SaleCorrection.adminId WHERE saleId = :saleId ORDER BY at DESC, SaleCorrection.id DESC")
    suspend fun history(saleId: Long): List<CorrectionRow>

    @Query("SELECT * FROM SaleCorrection WHERE saleId = :saleId ORDER BY id DESC LIMIT 1")
    suspend fun latest(saleId: Long): SaleCorrection?
}

data class CorrectionRow(@Embedded val correction: SaleCorrection, val username: String)
