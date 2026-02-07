package com.bloodworktracker.data.model

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
    val testId: Long,
    val bloodValueId: Long,
    val value: Double,
    val status: ValueStatus,
    val notes: String,
    val nameDe: String,
    val nameEn: String,
    val abbreviation: String,
    val unit: String,
    val category: String,
    val minMale: Double?,
    val maxMale: Double?,
    val minFemale: Double?,
    val maxFemale: Double?,
    val minNormal: Double?,
    val maxNormal: Double?,
    val criticalLow: Double?,
    val criticalHigh: Double?
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