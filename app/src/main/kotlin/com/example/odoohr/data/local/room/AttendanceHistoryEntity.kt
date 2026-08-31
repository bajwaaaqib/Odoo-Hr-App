package com.example.odoohr.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.odoohr.data.model.AttendanceRecord
import com.example.odoohr.data.model.SyncStatus

@Entity(tableName = "attendance_history")
data class AttendanceHistoryEntity(
    @PrimaryKey
    val recordId: String,
    val date: String,
    val checkInTime: String,
    val checkOutTime: String?,
    val duration: String,
    val locationName: String,
    val verificationStatus: String,
    val isLive: Boolean = false,
    val isOvertime: Boolean = false,
    val overtimeMinutes: Int = 0,
    val shiftNote: String? = null,
    val syncStatus: String = "SYNCED",
    val latitude: Double = 25.2048,
    val longitude: Double = 55.2708,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): AttendanceRecord {
        return AttendanceRecord(
            id = recordId,
            date = date,
            checkInTime = checkInTime,
            checkOutTime = checkOutTime,
            duration = duration,
            locationName = locationName,
            verificationStatus = verificationStatus,
            isLive = isLive,
            isOvertime = isOvertime,
            overtimeMinutes = overtimeMinutes,
            shiftNote = shiftNote,
            syncStatus = try {
                SyncStatus.valueOf(syncStatus)
            } catch (_: Exception) {
                SyncStatus.SYNCED
            },
            latitude = latitude,
            longitude = longitude
        )
    }

    companion object {
        fun fromDomainModel(record: AttendanceRecord): AttendanceHistoryEntity {
            return AttendanceHistoryEntity(
                recordId = record.id,
                date = record.date,
                checkInTime = record.checkInTime,
                checkOutTime = record.checkOutTime,
                duration = record.duration,
                locationName = record.locationName,
                verificationStatus = record.verificationStatus,
                isLive = record.isLive,
                isOvertime = record.isOvertime,
                overtimeMinutes = record.overtimeMinutes,
                shiftNote = record.shiftNote,
                syncStatus = record.syncStatus.name,
                latitude = record.latitude,
                longitude = record.longitude
            )
        }
    }
}
