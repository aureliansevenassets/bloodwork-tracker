package com.bloodworktracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bloodworktracker.data.database.entities.BloodValue
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodValueDao {
    
    @Query("SELECT * FROM blood_values ORDER BY category, sort_order, name_de")
    fun getAllBloodValues(): Flow<List<BloodValue>>
    
    @Query("SELECT * FROM blood_values WHERE category = :category ORDER BY sort_order, name_de")
    fun getBloodValuesByCategory(category: String): Flow<List<BloodValue>>
    
    @Query("SELECT DISTINCT category FROM blood_values ORDER BY category")
    fun getAllCategories(): Flow<List<String>>
    
    @Query("SELECT * FROM blood_values WHERE id = :id")
    suspend fun getBloodValueById(id: Long): BloodValue?
    
    @Query("SELECT * FROM blood_values WHERE abbreviation = :abbreviation LIMIT 1")
    suspend fun getBloodValueByAbbreviation(abbreviation: String): BloodValue?
    
    @Query("SELECT * FROM blood_values WHERE name_de LIKE '%' || :query || '%' OR name_en LIKE '%' || :query || '%' OR abbreviation LIKE '%' || :query || '%'")
    fun searchBloodValues(query: String): Flow<List<BloodValue>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodValue(bloodValue: BloodValue): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodValues(bloodValues: List<BloodValue>)
    
    @Update
    suspend fun updateBloodValue(bloodValue: BloodValue)
    
    @Delete
    suspend fun deleteBloodValue(bloodValue: BloodValue)
    
    @Query("DELETE FROM blood_values")
    suspend fun deleteAllBloodValues()
}