package com.bloodworktracker.data.repository

import com.bloodworktracker.data.database.dao.BloodTestDao
import com.bloodworktracker.data.database.dao.BloodTestResultDao
import com.bloodworktracker.data.database.dao.BloodValueDao
import com.bloodworktracker.data.database.entities.BloodTest
import com.bloodworktracker.data.database.entities.BloodTestResult
import com.bloodworktracker.data.database.entities.BloodValue
import com.bloodworktracker.data.database.entities.ValueStatus
import com.bloodworktracker.data.model.BloodTestWithResults
import com.bloodworktracker.data.model.BloodTestResultWithValue
import com.bloodworktracker.data.model.BloodValueCategory
import com.bloodworktracker.data.model.ChartData
import com.bloodworktracker.data.model.ChartDataPoint
import com.bloodworktracker.data.model.ConstellationAnalysis
import com.bloodworktracker.data.model.Gender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class BloodworkRepository(
    private val bloodValueDao: BloodValueDao,
    private val bloodTestDao: BloodTestDao,
    private val bloodTestResultDao: BloodTestResultDao
) {
    
    // Blood Values
    fun getAllBloodValues(): Flow<List<BloodValue>> = bloodValueDao.getAllBloodValues()
    
    fun getBloodValuesByCategory(): Flow<List<BloodValueCategory>> {
        return bloodValueDao.getAllBloodValues().map { values ->
            values.groupBy { it.category }.map { (category, categoryValues) ->
                BloodValueCategory(category, categoryValues.sortedBy { it.sortOrder })
            }.sortedBy { it.name }
        }
    }
    
    fun getAllCategories(): Flow<List<String>> = bloodValueDao.getAllCategories()
    
    fun searchBloodValues(query: String): Flow<List<BloodValue>> = 
        bloodValueDao.searchBloodValues(query)
    
    suspend fun getBloodValueById(id: Long): BloodValue? = 
        bloodValueDao.getBloodValueById(id)
    
    // Blood Tests
    fun getAllBloodTests(): Flow<List<BloodTest>> = bloodTestDao.getAllBloodTests()
    
    fun getAllBloodTestsWithResults(): Flow<List<BloodTestWithResults>> = 
        bloodTestDao.getAllBloodTestsWithResults()
    
    suspend fun getBloodTestWithResults(id: Long): BloodTestWithResults? = 
        bloodTestDao.getBloodTestWithResults(id)
    
    suspend fun insertBloodTest(bloodTest: BloodTest): Long = 
        bloodTestDao.insertBloodTest(bloodTest)
    
    suspend fun updateBloodTest(bloodTest: BloodTest) = 
        bloodTestDao.updateBloodTest(bloodTest)
    
    suspend fun deleteBloodTest(bloodTest: BloodTest) = 
        bloodTestDao.deleteBloodTest(bloodTest)
    
    // Blood Test Results
    suspend fun getResultsWithValuesByTestId(testId: Long): List<BloodTestResultWithValue> = 
        bloodTestResultDao.getResultsWithValuesByTestId(testId)
    
    suspend fun insertResult(result: BloodTestResult): Long = 
        bloodTestResultDao.insertResult(result)
    
    suspend fun insertResults(results: List<BloodTestResult>) = 
        bloodTestResultDao.insertResults(results)
    
    suspend fun updateResult(result: BloodTestResult) = 
        bloodTestResultDao.updateResult(result)
    
    suspend fun deleteResult(result: BloodTestResult) = 
        bloodTestResultDao.deleteResult(result)
    
    // Chart Data
    suspend fun getChartDataForBloodValue(bloodValueId: Long): ChartData? {
        val bloodValue = getBloodValueById(bloodValueId) ?: return null
        val chartPoints = bloodTestResultDao.getValueHistoryForChart(bloodValueId)
        
        val dataPoints = chartPoints.map { point ->
            val status = calculateValueStatus(point.value, bloodValue, Gender.MALE) // Default to male
            ChartDataPoint(point.testDate, point.value, status)
        }
        
        return ChartData(bloodValue, dataPoints)
    }
    
    // Value Status Calculation
    fun calculateValueStatus(
        value: Double, 
        bloodValue: BloodValue, 
        gender: Gender = Gender.MALE
    ): ValueStatus {
        val (minNormal, maxNormal) = when {
            bloodValue.minMale != null && bloodValue.maxMale != null && gender == Gender.MALE -> 
                bloodValue.minMale to bloodValue.maxMale
            bloodValue.minFemale != null && bloodValue.maxFemale != null && gender == Gender.FEMALE -> 
                bloodValue.minFemale to bloodValue.maxFemale
            bloodValue.minNormal != null && bloodValue.maxNormal != null -> 
                bloodValue.minNormal to bloodValue.maxNormal
            else -> return ValueStatus.NORMAL // No reference range available
        }
        
        return when {
            bloodValue.criticalLow != null && value < bloodValue.criticalLow -> ValueStatus.CRITICAL_LOW
            bloodValue.criticalHigh != null && value > bloodValue.criticalHigh -> ValueStatus.CRITICAL_HIGH
            value < minNormal -> ValueStatus.LOW
            value > maxNormal -> ValueStatus.HIGH
            else -> ValueStatus.NORMAL
        }
    }
    
    // Constellation Analysis
    suspend fun analyzeConstellation(testId: Long): List<ConstellationAnalysis> {
        val results = getResultsWithValuesByTestId(testId)
        val analyses = mutableListOf<ConstellationAnalysis>()
        
        // Liver Analysis
        val liverValues = results.filter { it.category == "Leberwerte" }
        if (liverValues.any { it.status == ValueStatus.HIGH || it.status == ValueStatus.CRITICAL_HIGH }) {
            val elevatedValues = liverValues.filter { 
                it.status == ValueStatus.HIGH || it.status == ValueStatus.CRITICAL_HIGH 
            }
            
            val severity = when {
                elevatedValues.any { it.status == ValueStatus.CRITICAL_HIGH } -> 
                    com.bloodworktracker.data.model.AnalysisSeverity.CRITICAL
                elevatedValues.size >= 3 -> 
                    com.bloodworktracker.data.model.AnalysisSeverity.WARNING
                else -> 
                    com.bloodworktracker.data.model.AnalysisSeverity.INFO
            }
            
            analyses.add(ConstellationAnalysis(
                title = "Erhöhte Leberwerte",
                description = "Mehrere Leberwerte sind erhöht. Dies kann auf eine Leberschädigung hinweisen.",
                severity = severity,
                affectedValues = elevatedValues.map { it.abbreviation },
                recommendations = listOf(
                    "Alkoholkonsum reduzieren",
                    "Medikamente überprüfen",
                    "Weitere Leberuntersuchung erwägen",
                    "Rücksprache mit dem Arzt"
                )
            ))
        }
        
        // Diabetes Analysis
        val diabetesValues = results.filter { it.category == "Diabetes" }
        val highGlucose = diabetesValues.find { it.abbreviation == "GLU" && it.value > 126.0 }
        val highHbA1c = diabetesValues.find { it.abbreviation == "HbA1c" && it.value > 6.5 }
        
        if (highGlucose != null || highHbA1c != null) {
            val severity = when {
                highHbA1c != null && highHbA1c.value > 9.0 -> 
                    com.bloodworktracker.data.model.AnalysisSeverity.CRITICAL
                highGlucose != null && highGlucose.value > 200.0 -> 
                    com.bloodworktracker.data.model.AnalysisSeverity.WARNING
                else -> 
                    com.bloodworktracker.data.model.AnalysisSeverity.INFO
            }
            
            analyses.add(ConstellationAnalysis(
                title = "Diabetesverdacht",
                description = "Die Blutzuckerwerte deuten auf einen möglichen Diabetes hin.",
                severity = severity,
                affectedValues = listOfNotNull(
                    if (highGlucose != null) "GLU" else null,
                    if (highHbA1c != null) "HbA1c" else null
                ),
                recommendations = listOf(
                    "Diabetologische Abklärung",
                    "Ernährungsberatung",
                    "Gewichtskontrolle",
                    "Regelmäßige Blutzuckermessung"
                )
            ))
        }
        
        // Kidney Analysis
        val kidneyValues = results.filter { it.category == "Nierenwerte" }
        val highCreatinine = kidneyValues.find { it.abbreviation == "CREA" && it.status != ValueStatus.NORMAL }
        val lowGfr = kidneyValues.find { it.abbreviation == "GFR" && it.value < 60.0 }
        
        if (highCreatinine != null || lowGfr != null) {
            val severity = when {
                lowGfr != null && lowGfr.value < 30.0 -> 
                    com.bloodworktracker.data.model.AnalysisSeverity.CRITICAL
                lowGfr != null && lowGfr.value < 60.0 -> 
                    com.bloodworktracker.data.model.AnalysisSeverity.WARNING
                else -> 
                    com.bloodworktracker.data.model.AnalysisSeverity.INFO
            }
            
            analyses.add(ConstellationAnalysis(
                title = "Eingeschränkte Nierenfunktion",
                description = "Die Nierenwerte zeigen eine mögliche Funktionseinschränkung.",
                severity = severity,
                affectedValues = listOfNotNull(
                    if (highCreatinine != null) "CREA" else null,
                    if (lowGfr != null) "GFR" else null
                ),
                recommendations = listOf(
                    "Nephrologische Kontrolle",
                    "Blutdruckkontrolle",
                    "Medikamente überprüfen",
                    "Trinkmenge anpassen"
                )
            ))
        }
        
        // Anemia Analysis
        val bloodCount = results.filter { it.category == "Kleines Blutbild" }
        val lowHb = bloodCount.find { it.abbreviation == "Hb" && it.status == ValueStatus.LOW }
        val lowRbc = bloodCount.find { it.abbreviation == "RBC" && it.status == ValueStatus.LOW }
        val lowHkt = bloodCount.find { it.abbreviation == "Hkt" && it.status == ValueStatus.LOW }
        
        if (lowHb != null || (lowRbc != null && lowHkt != null)) {
            analyses.add(ConstellationAnalysis(
                title = "Anämie-Verdacht",
                description = "Die Blutwerte deuten auf eine mögliche Blutarmut hin.",
                severity = com.bloodworktracker.data.model.AnalysisSeverity.WARNING,
                affectedValues = listOfNotNull(
                    if (lowHb != null) "Hb" else null,
                    if (lowRbc != null) "RBC" else null,
                    if (lowHkt != null) "Hkt" else null
                ),
                recommendations = listOf(
                    "Eisenstatus prüfen",
                    "Vitamin B12 und Folsäure bestimmen",
                    "Stuhltest auf okkultes Blut",
                    "Hämatologische Abklärung"
                )
            ))
        }
        
        return analyses
    }
}