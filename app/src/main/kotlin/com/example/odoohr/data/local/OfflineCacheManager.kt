package com.example.odoohr.data.local

import android.content.Context
import com.example.odoohr.data.local.room.AppDatabase
import com.example.odoohr.data.local.room.AttendanceHistoryEntity
import com.example.odoohr.data.local.room.OfflinePunchEntity
import com.example.odoohr.data.model.AttendanceRecord
import com.example.odoohr.data.model.OfflinePunchRecord
import com.example.odoohr.data.model.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * High-performance Offline Cache & Sync Manager backed by Room Database and SQLite.
 * Adheres strictly to the Room Database Integration skill mandates.
 */
class OfflineCacheManager(context: Context) {

    private val database: AppDatabase = AppDatabase.getDatabase(context)
    private val punchDao = database.offlinePunchDao()
    private val historyDao = database.attendanceHistoryDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory active mirrors for instant synchronous queries when needed
    private val inMemoryPunches = mutableListOf<OfflinePunchRecord>()
    private val inMemoryHistory = mutableListOf<AttendanceRecord>()

    init {
        // Hydrate in-memory lists from Room on initialization
        scope.launch {
            try {
                val dbPunches = punchDao.getAllPunches()
                synchronized(inMemoryPunches) {
                    inMemoryPunches.clear()
                    inMemoryPunches.addAll(dbPunches.map { it.toDomainModel() })
                }

                val dbHistory = historyDao.getAllRecords()
                synchronized(inMemoryHistory) {
                    inMemoryHistory.clear()
                    inMemoryHistory.addAll(dbHistory.map { it.toDomainModel() })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // -------------------------------------------------------------
    // Reactive Room Flows
    // -------------------------------------------------------------

    fun getOfflinePunchesFlow(): Flow<List<OfflinePunchRecord>> {
        return punchDao.getAllPunchesFlow().map { list -> list.map { it.toDomainModel() } }
    }

    fun getPendingPunchesFlow(): Flow<List<OfflinePunchRecord>> {
        return punchDao.getPendingPunchesFlow().map { list -> list.map { it.toDomainModel() } }
    }

    fun getAttendanceHistoryFlow(): Flow<List<AttendanceRecord>> {
        return historyDao.getAllRecordsFlow().map { list -> list.map { it.toDomainModel() } }
    }

    // -------------------------------------------------------------
    // Offline Punch Queue Operations
    // -------------------------------------------------------------

    fun enqueuePunch(punch: OfflinePunchRecord) {
        synchronized(inMemoryPunches) {
            val index = inMemoryPunches.indexOfFirst { it.id == punch.id }
            if (index >= 0) {
                inMemoryPunches[index] = punch
            } else {
                inMemoryPunches.add(0, punch)
            }
        }

        scope.launch {
            try {
                punchDao.insertPunch(OfflinePunchEntity.fromDomainModel(punch))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getOfflinePunches(): List<OfflinePunchRecord> {
        synchronized(inMemoryPunches) {
            if (inMemoryPunches.isNotEmpty()) return inMemoryPunches.toList()
        }
        return try {
            runBlocking(Dispatchers.IO) {
                punchDao.getAllPunches().map { it.toDomainModel() }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getPendingPunches(): List<OfflinePunchRecord> {
        return getOfflinePunches().filter { it.syncStatus == SyncStatus.PENDING_SYNC || it.syncStatus == SyncStatus.FAILED }
    }

    fun getPendingCount(): Int {
        return getPendingPunches().size
    }

    fun markPunchSynced(punchId: String) {
        synchronized(inMemoryPunches) {
            val index = inMemoryPunches.indexOfFirst { it.id == punchId }
            if (index >= 0) {
                inMemoryPunches[index] = inMemoryPunches[index].copy(syncStatus = SyncStatus.SYNCED)
            }
        }

        scope.launch {
            try {
                punchDao.updateSyncStatus(punchId, SyncStatus.SYNCED.name, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markPunchFailed(punchId: String, error: String) {
        synchronized(inMemoryPunches) {
            val index = inMemoryPunches.indexOfFirst { it.id == punchId }
            if (index >= 0) {
                val p = inMemoryPunches[index]
                inMemoryPunches[index] = p.copy(
                    syncStatus = SyncStatus.FAILED,
                    retryAttempts = p.retryAttempts + 1,
                    lastError = error
                )
            }
        }

        scope.launch {
            try {
                punchDao.updateSyncStatus(punchId, SyncStatus.FAILED.name, error)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearSyncedPunches() {
        synchronized(inMemoryPunches) {
            inMemoryPunches.removeAll { it.syncStatus == SyncStatus.SYNCED }
        }

        scope.launch {
            try {
                punchDao.clearSyncedPunches()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // -------------------------------------------------------------
    // Attendance History Operations
    // -------------------------------------------------------------

    fun saveAttendanceHistory(records: List<AttendanceRecord>) {
        synchronized(inMemoryHistory) {
            inMemoryHistory.clear()
            inMemoryHistory.addAll(records)
        }

        scope.launch {
            try {
                historyDao.insertAll(records.map { AttendanceHistoryEntity.fromDomainModel(it) })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getCachedAttendanceHistory(): List<AttendanceRecord> {
        synchronized(inMemoryHistory) {
            if (inMemoryHistory.isNotEmpty()) return inMemoryHistory.toList()
        }
        return try {
            runBlocking(Dispatchers.IO) {
                historyDao.getAllRecords().map { it.toDomainModel() }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clearAll() {
        synchronized(inMemoryPunches) { inMemoryPunches.clear() }
        synchronized(inMemoryHistory) { inMemoryHistory.clear() }

        scope.launch {
            try {
                punchDao.clearAll()
                historyDao.clearAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
