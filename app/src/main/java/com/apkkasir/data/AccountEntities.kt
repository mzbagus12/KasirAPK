package com.apkkasir.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Role { CASHIER, HISTORICAL_ADMIN }

@Entity(indices = [Index(value = ["username"], unique = true), Index(value = ["role"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val role: Role,
    val credential: String,
)

@Entity
data class StoreSettings(
    @PrimaryKey val id: Int = 1,
    val recoveryVerifier: String,
    val qrisFile: String? = null,
)
