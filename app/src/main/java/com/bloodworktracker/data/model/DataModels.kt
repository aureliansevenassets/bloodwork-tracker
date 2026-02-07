package com.bloodworktracker.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import com.bloodworktracker.data.database.entities.BloodTest
import com.bloodworktracker.data.database.entities.BloodTestResult
import com.bloodworktracker.data.database.entities.BloodValue
import com.bloodworktracker.data.database.entities.ValueStatus

data class BloodTestWithResults(
    @Embedded val bloodTest: BloodTest,
    @Relation(
        parentColumn = "id",
        entityColumn = "test_id"
    )
    val results: List<BloodTestResult>
)

data class BloodTestResultWithValue(
    val id: Long,
    @ColumnInfo(name = "test_id") val testId: Long,
    @ColumnInfo(name = "blood_value_id") val bloodValueId: Long,
    val value: Double,
    val status: ValueStatus,
    val notes: String,
    @ColumnInfo(name = "name_de") val nameDe: String,
    @ColumnInfo(name = "name_en") val nameEn: String,
    val abbreviation: String,
    val unit: String,
    val category: String,
    @ColumnInfo(name = "min_male") val minMale: Double?,
    @ColumnInfo(name = "max_male") val maxMale: Double?,
    @ColumnInfo(name = "min_female") val minFemale: Double?,
    @ColumnInfo(name = "max_female") val maxFemale: Double?,
    @ColumnInfo(name = "min_normal") val minNormal: Double?,
    @ColumnInfo(name = "max_normal") val maxNormal: Double?,
    @ColumnInfo(name = "critical_low") val criticalLow: Double?,
    @ColumnInfo(name = "critical_high") val criticalHigh: Double?
)

data class BloodValueCategory(
    val name: String,
    val values: List<BloodValue>
)

data class ChartData(
    val bloodValue: BloodValue,
    val dataPoints: List<ChartDataPoint>
)

data class ChartDataPoint(
    val date: java.util.Date,
    val value: Double,
    val status: ValueStatus
)

data class ConstellationAnalysis(
    val title: String,
    val description: String,
    val severity: AnalysisSeverity,
    val affectedValues: List<String>,
    val recommendations: List<String>
)

enum class AnalysisSeverity {
    INFO,
    WARNING,
    CRITICAL
}

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}
