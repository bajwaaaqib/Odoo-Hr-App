package com.example.odoohr.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflinePunchDao {

    @Query("SELECT * FROM offline_punches ORDER BY createdAtMillis ASC")
    fun getAllPunchesFlow(): Flow<List<OfflinePunchEntity>>

    @Query("SELECT * FROM offline_punches ORDER BY createdAtMillis ASC")
    suspend fun getAllPunches(): List<OfflinePunchEntity>

    @Query("SELECT * FROM offline_punches WHERE syncStatus = 'PENDING_SYNC' OR syncStatus = 'FAILED' ORDER BY createdAtMillis ASC")
    fun getPendingPunchesFlow(): Flow<List<OfflinePunchEntity>>

    @Query("SELECT * FROM offline_punches WHERE syncStatus = 'PENDING_SYNC' OR syncStatus = 'FAILED' ORDER BY createdAtMillis ASC")
    suspend fun getPendingPunches(): List<OfflinePunchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPunch(punch: OfflinePunchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(punches: List<OfflinePunchEntity>)

    @Update
    suspend fun updatePunch(punch: OfflinePunchEntity)

    @Query("UPDATE offline_punches SET syncStatus = :status, retryAttempts = retryAttempts + 1, lastError = :lastError WHERE clientPunchId = :punchId")
    suspend fun updateSyncStatus(punchId: String, status: String, lastError: String? = null)

    @Query("DELETE FROM offline_punches WHERE clientPunchId = :punchId")
    suspend fun deletePunchById(punchId: String)

    @Query("DELETE FROM offline_punches WHERE syncStatus = 'SYNCED'")
    suspend fun clearSyncedPunches()

    @Query("DELETE FROM offline_punches")
    suspend fun clearAll()
}
