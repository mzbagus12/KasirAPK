package com.apkkasir.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AccountDao {
    @Query("SELECT * FROM User WHERE username = :username AND role = 'CASHIER'")
    suspend fun cashierByName(username: String): User?

    @Query("SELECT * FROM User WHERE role = :role")
    suspend fun byRole(role: Role): User?

    @Query("SELECT * FROM User WHERE id = :id")
    suspend fun byId(id: Long): User?

    @Insert
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM StoreSettings WHERE id = 1")
    suspend fun settings(): StoreSettings?

    @Insert
    suspend fun insertSettings(settings: StoreSettings)

    @Update
    suspend fun updateSettings(settings: StoreSettings)
}
