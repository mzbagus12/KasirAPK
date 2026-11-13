package com.apkkasir.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM Product ORDER BY name COLLATE NOCASE, id")
    fun observe(): Flow<List<Product>>

    @Query("SELECT * FROM Product WHERE id = :id")
    suspend fun get(id: Long): Product?

    @Insert
    suspend fun insert(product: Product): Long

    @Query("UPDATE Product SET price = :price WHERE id = :id")
    suspend fun changePrice(id: Long, price: Long): Int
}
