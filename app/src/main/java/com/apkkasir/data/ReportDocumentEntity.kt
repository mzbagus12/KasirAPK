package com.apkkasir.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.apkkasir.reports.PeriodKind

@Entity(indices = [Index(value = ["fileName"], unique = true)])
data class ReportDocument(
    @PrimaryKey val id: String,
    val kind: PeriodKind,
    val from: String,
    val through: String,
    val createdAt: Long,
    val displayName: String,
    val fileName: String,
)
