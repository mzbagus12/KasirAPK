package com.apkkasir.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReportDocumentDao {
    @Query("SELECT * FROM ReportDocument ORDER BY createdAt DESC, id DESC")
    suspend fun all(): List<ReportDocument>

    @Query("SELECT * FROM ReportDocument WHERE id = :id")
    suspend fun get(id: String): ReportDocument?

    @Insert
    suspend fun insert(document: ReportDocument)

    @Query("DELETE FROM ReportDocument WHERE id = :id")
    suspend fun remove(id: String)
}
