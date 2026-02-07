package com.bloodworktracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bloodworktracker.data.database.entities.BloodTestResult
import com.bloodworktracker.data.model.BloodTestResultWithValue
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodTestResultDao {
    
    @Query("SELECT * FROM blood_test_results WHERE test_id = :testId")
    suspend fun getResultsByTestId(testId: Long): List<BloodTestResult>
    
    @Query("""
        SELECT btr.*, bv.name_de, bv.name_en, bv.abbreviation, bv.unit, bv.category,
               bv.min_male, bv.max_male, bv.min_female, bv.max_female, 
               bv.min_normal, bv.max_normal, bv.critical_low, bv.critical_high
        FROM blood_test_results btr 
        JOIN blood_values bv ON btr.blood_value_id = bv.id 
        WHERE btr.test_id = :testId
        ORDER BY bv.category, bv.sort_order, bv.name_de
    """)
    suspend fun getResultsWithValuesByTestId(testId: Long): List<BloodTestResultWithValue>
    
    @Query("""
        SELECT btr.*, bt.test_date
        FROM blood_test_results btr 
        JOIN blood_tests bt ON btr.test_id = bt.id 
        WHERE btr.blood_value_id = :bloodValueId 
        ORDER BY bt.test_date ASC
    """)
    suspend fun getHistoryForBloodValue(bloodValueId: Long): List<BloodTestResult>
    
    @Query("""
        SELECT btr.value, bt.test_date
        FROM blood_test_results btr 
        JOIN blood_tests bt ON btr.test_id = bt.id 
        WHERE btr.blood_value_id = :bloodValueId 
        ORDER BY bt.test_date ASC
    """)
    suspend fun getValueHistoryForChart(bloodValueId: Long): List<ChartPoint>
    
    @Query("SELECT * FROM blood_test_results WHERE test_id = :testId AND blood_value_id = :bloodValueId")
    suspend fun getResult(testId: Long, bloodValueId: Long): BloodTestResult?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: BloodTestResult): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<BloodTestResult>)
    
    @Update
    suspend fun updateResult(result: BloodTestResult)
    
    @Delete
    suspend fun deleteResult(result: BloodTestResult)
    
    @Query("DELETE FROM blood_test_results WHERE test_id = :testId")
    suspend fun deleteResultsByTestId(testId: Long)
    
    @Query("DELETE FROM blood_test_results WHERE blood_value_id = :bloodValueId")
    suspend fun deleteResultsByBloodValueId(bloodValueId: Long)
}

data class ChartPoint(
    val value: Double,
    val testDate: java.util.Date
)