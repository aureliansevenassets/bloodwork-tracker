package com.bloodworktracker.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bloodworktracker.data.database.dao.BloodTestDao
import com.bloodworktracker.data.database.dao.BloodTestResultDao
import com.bloodworktracker.data.database.dao.BloodValueDao
import com.bloodworktracker.data.database.entities.BloodTest
import com.bloodworktracker.data.database.entities.BloodTestResult
import com.bloodworktracker.data.database.entities.BloodValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        BloodValue::class,
        BloodTest::class,
        BloodTestResult::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BloodworkDatabase : RoomDatabase() {
    
    abstract fun bloodValueDao(): BloodValueDao
    abstract fun bloodTestDao(): BloodTestDao
    abstract fun bloodTestResultDao(): BloodTestResultDao
    
    companion object {
        @Volatile
        private var INSTANCE: BloodworkDatabase? = null
        
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): BloodworkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BloodworkDatabase::class.java,
                    "bloodwork_database"
                )
                .addCallback(BloodworkDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class BloodworkDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateDatabase(database.bloodValueDao())
                    }
                }
            }
            
            suspend fun populateDatabase(bloodValueDao: BloodValueDao) {
                // Populate with comprehensive German laboratory values
                bloodValueDao.insertBloodValues(getGermanBloodValues())
            }
        }
    }
}