package com.bloodworktracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bloodworktracker.data.database.entities.BloodTest
import com.bloodworktracker.data.database.entities.BloodTestResult
import com.bloodworktracker.data.model.BloodTestWithResults
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BloodTestDao {
    
    @Query("SELECT * FROM blood_tests ORDER BY test_date DESC")
    fun getAllBloodTests(): Flow<List<BloodTest>>
    
    @Query("SELECT * FROM blood_tests WHERE id = :id")
    suspend fun getBloodTestById(id: Long): BloodTest?
    
    @Transaction
    @Query("SELECT * FROM blood_tests WHERE id = :id")
    suspend fun getBloodTestWithResults(id: Long): BloodTestWithResults?
    
    @Transaction
    @Query("SELECT * FROM blood_tests ORDER BY test_date DESC")
    fun getAllBloodTestsWithResults(): Flow<List<BloodTestWithResults>>
    
    @Query("SELECT * FROM blood_tests WHERE test_date >= :startDate AND test_date <= :endDate ORDER BY test_date DESC")
    fun getBloodTestsByDateRange(startDate: Date, endDate: Date): Flow<List<BloodTest>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodTest(bloodTest: BloodTest): Long
    
    @Update
    suspend fun updateBloodTest(bloodTest: BloodTest)
    
    @Delete
    suspend fun deleteBloodTest(bloodTest: BloodTest)
    
    @Query("DELETE FROM blood_tests WHERE id = :id")
    suspend fun deleteBloodTestById(id: Long)
}