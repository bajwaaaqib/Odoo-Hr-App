package com.example.odoohr.data.model

data class UserProfile(
    val id: String = "EMP-001",
    val name: String = "Ahmed Mohamed",
    val email: String = "ahmed@company.com",
    val employeeId: String = "EMP-001",
    val department: String = "Sales Department",
    val position: String = "Sales Manager",
    val avatarInitials: String = "A",
    val joinedDate: String = "Jan 15, 2022"
)

enum class SyncStatus {
    SYNCED,
    PENDING_SYNC,
    SYNCING,
    FAILED
}

data class AttendanceRecord(
    val id: String,
    val date: String,
    val checkInTime: String,
    val checkOutTime: String?,
    val duration: String,
    val isLive: Boolean = false,
    val locationName: String = "Dubai HQ - Zone A",
    val verificationStatus: String = "GPS Geofence Verified",
    val isOvertime: Boolean = false,
    val overtimeMinutes: Int = 0,
    val shiftNote: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val latitude: Double = 25.2048,
    val longitude: Double = 55.2708
)

data class OfflinePunchRecord(
    val id: String,
    val type: String, // "CHECK_IN" or "CHECK_OUT"
    val timestamp: Long,
    val timeFormatted: String,
    val dateFormatted: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val zoneName: String,
    val distanceMeters: Double,
    val isInsideGeofence: Boolean,
    val note: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val retryAttempts: Int = 0,
    val lastError: String? = null
)

enum class LeaveStatus {
    Approved,
    Pending,
    Rejected
}

data class TimeOffRecord(
    val id: String,
    val type: String,
    val status: LeaveStatus,
    val startDate: String,
    val endDate: String,
    val duration: String,
    val reason: String = ""
)

data class TimeOffBalance(
    val annualDays: Int = 5,
    val sickDays: Int = 10,
    val emergencyDays: Int = 3
)

data class GeofenceZone(
    val id: String = "zone-hq-1",
    val name: String = "Office Zone",
    val latitude: Double = 25.2048,
    val longitude: Double = 55.2708,
    val radiusMeters: Double = 100.0,
    val isInside: Boolean = true,
    val distanceMeters: Double = 14.5,
    val accuracyMeters: Double = 8.0,
    val locationStatusText: String = "Office Zone (Inside 100m perimeter)",
    val userLatitude: Double = 25.2049,
    val userLongitude: Double = 55.2709,
    val isMockLocation: Boolean = false
)

data class GeofenceLocationPreset(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
    val isOfficeLocation: Boolean = true
)

data class DeviceSession(
    val id: String,
    val deviceName: String,
    val platform: String,
    val appVersion: String,
    val lastSeen: String,
    val isCurrent: Boolean = false
)

data class ServerConfig(
    val url: String = "https://demo.odoo.com",
    val isConfigured: Boolean = true,
    val organizationName: String = "Odoo Enterprise Cloud",
    val databaseName: String = "odoo_production"
)

enum class OdooConnectionState {
    ONLINE,
    OFFLINE_CACHE,
    CONNECTING,
    SYNCING,
    ERROR
}

data class OdooSession(
    val uid: Int = 0,
    val sessionId: String = "",
    val database: String = "",
    val userName: String = "",
    val userLogin: String = "",
    val partnerId: Int = 0,
    val employeeId: Int = 0,
    val serverVersion: String = "17.0 Community/Enterprise"
)

data class DailyAttendanceChartItem(
    val dayLabel: String, // "Mon", "Tue", etc.
    val dateLabel: String, // "26 Aug"
    val fullDate: String, // "Monday, Aug 26"
    val regularHours: Float, // e.g. 8.0f
    val overtimeHours: Float, // e.g. 0.8f
    val totalHours: Float, // e.g. 8.8f
    val targetHours: Float = 8.0f,
    val isToday: Boolean = false,
    val isWeekend: Boolean = false,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val locationName: String = "Dubai HQ"
)

data class AttendanceChartSummary(
    val weeklyTotalHours: Float = 42.5f,
    val weeklyTargetHours: Float = 40.0f,
    val weeklyOvertimeHours: Float = 2.5f,
    val averageDailyHours: Float = 8.5f,
    val onTimePercentage: Int = 98,
    val totalDaysWorked: Int = 5,
    val currentStreakDays: Int = 14
)

