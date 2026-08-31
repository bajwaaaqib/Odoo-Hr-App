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

data class AttendanceRecord(
    val id: String,
    val date: String,
    val checkInTime: String,
    val checkOutTime: String?,
    val duration: String,
    val isLive: Boolean = false
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
    val locationStatusText: String = "Office Zone (Inside 100m perimeter)"
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
    val organizationName: String = "Odoo Enterprise Cloud"
)
