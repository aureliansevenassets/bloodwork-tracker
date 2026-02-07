package com.bloodworktracker.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "blood_tests")
data class BloodTest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "test_date")
    val testDate: Date,
    
    @ColumnInfo(name = "lab_name")
    val labName: String = "",
    
    @ColumnInfo(name = "doctor_name")
    val doctorName: String = "",
    
    @ColumnInfo(name = "notes")
    val notes: String = "",
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)