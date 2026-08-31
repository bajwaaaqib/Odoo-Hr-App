package com.example.odoohr.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceHistoryDao {

    @Query("SELECT * FROM attendance_history ORDER BY updatedAtMillis DESC")
    fun getAllRecordsFlow(): Flow<List<AttendanceHistoryEntity>>

    @Query("SELECT * FROM attendance_history ORDER BY updatedAtMillis DESC")
    suspend fun getAllRecords(): List<AttendanceHistoryEntity>

    @Query("SELECT * FROM attendance_history WHERE isLive = 1 LIMIT 1")
    suspend fun getLiveActiveRecord(): AttendanceHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AttendanceHistoryEntity>)

    @Update
    suspend fun updateRecord(record: AttendanceHistoryEntity)

    @Query("DELETE FROM attendance_history WHERE recordId = :id")
    suspend fun deleteRecordById(id: String)

    @Query("DELETE FROM attendance_history")
    suspend fun clearAll()
}
