package com.example.odoohr.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.odoohr.data.model.OfflinePunchRecord
import com.example.odoohr.data.model.SyncStatus

@Entity(tableName = "offline_punches")
data class OfflinePunchEntity(
    @PrimaryKey
    val clientPunchId: String,
    val punchType: String,
    val timestampMillis: Long,
    val timeFormatted: String,
    val dateFormatted: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val zoneName: String,
    val distanceMeters: Double,
    val isInsideGeofence: Boolean,
    val note: String? = null,
    val syncStatus: String,
    val retryAttempts: Int = 0,
    val lastError: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): OfflinePunchRecord {
        return OfflinePunchRecord(
            id = clientPunchId,
            type = punchType,
            timestamp = timestampMillis,
            timeFormatted = timeFormatted,
            dateFormatted = dateFormatted,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            zoneName = zoneName,
            distanceMeters = distanceMeters,
            isInsideGeofence = isInsideGeofence,
            note = note,
            syncStatus = when (syncStatus) {
                "SYNCED" -> SyncStatus.SYNCED
                "FAILED" -> SyncStatus.FAILED
                "SYNCING" -> SyncStatus.SYNCING
                else -> SyncStatus.PENDING_SYNC
            },
            retryAttempts = retryAttempts,
            lastError = lastError
        )
    }

    companion object {
        fun fromDomainModel(record: OfflinePunchRecord): OfflinePunchEntity {
            return OfflinePunchEntity(
                clientPunchId = record.id,
                punchType = record.type,
                timestampMillis = record.timestamp,
                timeFormatted = record.timeFormatted,
                dateFormatted = record.dateFormatted,
                latitude = record.latitude,
                longitude = record.longitude,
                accuracy = record.accuracy,
                zoneName = record.zoneName,
                distanceMeters = record.distanceMeters,
                isInsideGeofence = record.isInsideGeofence,
                note = record.note,
                syncStatus = record.syncStatus.name,
                retryAttempts = record.retryAttempts,
                lastError = record.lastError
            )
        }
    }
}
