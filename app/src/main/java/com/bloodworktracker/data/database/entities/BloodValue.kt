package com.bloodworktracker.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_values")
data class BloodValue(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name_de")
    val nameDe: String,
    
    @ColumnInfo(name = "name_en")
    val nameEn: String,
    
    @ColumnInfo(name = "abbreviation")
    val abbreviation: String,
    
    @ColumnInfo(name = "unit")
    val unit: String,
    
    @ColumnInfo(name = "category")
    val category: String,
    
    @ColumnInfo(name = "description_de")
    val descriptionDe: String,
    
    @ColumnInfo(name = "description_en")
    val descriptionEn: String,
    
    // Reference ranges - separate for male/female if different
    @ColumnInfo(name = "min_male")
    val minMale: Double?,
    
    @ColumnInfo(name = "max_male")
    val maxMale: Double?,
    
    @ColumnInfo(name = "min_female")
    val minFemale: Double?,
    
    @ColumnInfo(name = "max_female")
    val maxFemale: Double?,
    
    // If ranges are the same for both genders
    @ColumnInfo(name = "min_normal")
    val minNormal: Double?,
    
    @ColumnInfo(name = "max_normal")
    val maxNormal: Double?,
    
    @ColumnInfo(name = "critical_low")
    val criticalLow: Double?,
    
    @ColumnInfo(name = "critical_high")
    val criticalHigh: Double?,
    
    @ColumnInfo(name = "high_meaning_de")
    val highMeaningDe: String,
    
    @ColumnInfo(name = "high_meaning_en")
    val highMeaningEn: String,
    
    @ColumnInfo(name = "low_meaning_de")
    val lowMeaningDe: String,
    
    @ColumnInfo(name = "low_meaning_en")
    val lowMeaningEn: String,
    
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
)