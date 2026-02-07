package com.bloodworktracker

import android.app.Application
import com.bloodworktracker.data.database.BloodworkDatabase
import com.bloodworktracker.data.repository.BloodworkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BloodworkApplication : Application() {
    
    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())
    
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { BloodworkDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { 
        BloodworkRepository(
            database.bloodValueDao(),
            database.bloodTestDao(),
            database.bloodTestResultDao()
        ) 
    }
}