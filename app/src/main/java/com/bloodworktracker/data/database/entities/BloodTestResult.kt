package com.bloodworktracker.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "blood_test_results",
    foreignKeys = [
        ForeignKey(
            entity = BloodTest::class,
            parentColumns = ["id"],
            childColumns = ["test_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BloodValue::class,
            parentColumns = ["id"],
            childColumns = ["blood_value_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["test_id"]),
        Index(value = ["blood_value_id"]),
        Index(value = ["test_id", "blood_value_id"], unique = true)
    ]
)
data class BloodTestResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "test_id")
    val testId: Long,
    
    @ColumnInfo(name = "blood_value_id")
    val bloodValueId: Long,
    
    @ColumnInfo(name = "value")
    val value: Double,
    
    @ColumnInfo(name = "status")
    val status: ValueStatus = ValueStatus.NORMAL,
    
    @ColumnInfo(name = "notes")
    val notes: String = ""
)

enum class ValueStatus {
    NORMAL,      // Within reference range
    HIGH,        // Above normal range
    LOW,         // Below normal range
    CRITICAL_HIGH, // Critically high
    CRITICAL_LOW   // Critically low
}