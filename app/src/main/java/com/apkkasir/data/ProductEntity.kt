package com.apkkasir.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val price: Long)
