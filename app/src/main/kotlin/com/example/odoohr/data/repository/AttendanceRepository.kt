package com.example.odoohr.data.repository

import com.example.odoohr.data.model.AttendanceRecord
import com.example.odoohr.data.model.DeviceSession
import com.example.odoohr.data.model.GeofenceZone
import com.example.odoohr.data.model.LeaveStatus
import com.example.odoohr.data.model.ServerConfig
import com.example.odoohr.data.model.TimeOffBalance
import com.example.odoohr.data.model.TimeOffRecord
import com.example.odoohr.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AttendanceRepository {

    private val _serverConfig = MutableStateFlow(
        ServerConfig(
            url = "https://company.odoo.com",
            isConfigured = false,
            organizationName = "Odoo Enterprise"
        )
    )
    val serverConfig: StateFlow<ServerConfig> = _serverConfig.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isCheckedIn = MutableStateFlow(false)
    val isCheckedIn: StateFlow<Boolean> = _isCheckedIn.asStateFlow()

    private val _lastCheckInTime = MutableStateFlow<String?>("08:42")
    val lastCheckInTime: StateFlow<String?> = _lastCheckInTime.asStateFlow()

    private val _geofenceZone = MutableStateFlow(GeofenceZone())
    val geofenceZone: StateFlow<GeofenceZone> = _geofenceZone.asStateFlow()

    private val _attendanceHistory = MutableStateFlow(
        listOf(
            AttendanceRecord("1", "29 Aug", "08:42", "17:31", "8h 49m"),
            AttendanceRecord("2", "28 Aug", "08:37", "17:22", "8h 45m"),
            AttendanceRecord("3", "27 Aug", "08:51", "17:40", "8h 49m"),
            AttendanceRecord("4", "26 Aug", "08:45", "17:35", "8h 50m"),
            AttendanceRecord("5", "25 Aug", "08:30", "17:20", "8h 50m")
        )
    )
    val attendanceHistory: StateFlow<List<AttendanceRecord>> = _attendanceHistory.asStateFlow()

    private val _timeOffBalance = MutableStateFlow(TimeOffBalance())
    val timeOffBalance: StateFlow<TimeOffBalance> = _timeOffBalance.asStateFlow()

    private val _timeOffRecords = MutableStateFlow(
        listOf(
            TimeOffRecord(
                id = "req-1",
                type = "Annual Leave",
                status = LeaveStatus.Approved,
                startDate = "Sep 15, 2024",
                endDate = "Sep 20, 2024",
                duration = "6 days",
                reason = "Annual family vacation"
            ),
            TimeOffRecord(
                id = "req-2",
                type = "Sick Leave",
                status = LeaveStatus.Pending,
                startDate = "Oct 01, 2024",
                endDate = "Oct 02, 2024",
                duration = "2 days",
                reason = "Doctor appointment and recovery"
            )
        )
    )
    val timeOffRecords: StateFlow<List<TimeOffRecord>> = _timeOffRecords.asStateFlow()

    private val _deviceSessions = MutableStateFlow(
        listOf(
            DeviceSession(
                id = "dev-1",
                deviceName = "Google Pixel 8 Pro",
                platform = "Android 14 (API 34)",
                appVersion = "v1.0.0",
                lastSeen = "Active now",
                isCurrent = true
            ),
            DeviceSession(
                id = "dev-2",
                deviceName = "Samsung Galaxy Tab S9",
                platform = "Android 13 (API 33)",
                appVersion = "v1.0.0",
                lastSeen = "Yesterday, 18:20",
                isCurrent = false
            )
        )
    )
    val deviceSessions: StateFlow<List<DeviceSession>> = _deviceSessions.asStateFlow()

    private val _biometricsEnabled = MutableStateFlow(true)
    val biometricsEnabled: StateFlow<Boolean> = _biometricsEnabled.asStateFlow()

    fun configureServer(url: String, orgName: String = "Odoo Enterprise"): Boolean {
        if (!url.startsWith("https://")) return false
        _serverConfig.value = ServerConfig(
            url = url.trimEnd('/'),
            isConfigured = true,
            organizationName = orgName
        )
        return true
    }

    fun login(email: String): Boolean {
        _isLoggedIn.value = true
        _userProfile.value = _userProfile.value.copy(
            email = email,
            name = if (email.contains("@")) email.substringBefore("@").replace(".", " ").capitalizeWords() else "Ahmed Mohamed"
        )
        return true
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun toggleCheckIn(): Boolean {
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val todayStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())

        if (_isCheckedIn.value) {
            // Check out
            _isCheckedIn.value = false
            val inTime = _lastCheckInTime.value ?: "08:42"
            val newRecord = AttendanceRecord(
                id = UUID.randomUUID().toString(),
                date = todayStr,
                checkInTime = inTime,
                checkOutTime = now,
                duration = "8h 12m"
            )
            _attendanceHistory.value = listOf(newRecord) + _attendanceHistory.value
        } else {
            // Check in
            _isCheckedIn.value = true
            _lastCheckInTime.value = now
        }
        return _isCheckedIn.value
    }

    fun requestLeave(type: String, startDate: String, endDate: String, durationDays: Int, reason: String) {
        val newRecord = TimeOffRecord(
            id = UUID.randomUUID().toString(),
            type = type,
            status = LeaveStatus.Pending,
            startDate = startDate,
            endDate = endDate,
            duration = "$durationDays days",
            reason = reason
        )
        _timeOffRecords.value = listOf(newRecord) + _timeOffRecords.value
    }

    fun revokeDevice(deviceId: String) {
        _deviceSessions.value = _deviceSessions.value.filter { it.id != deviceId }
    }

    fun toggleBiometrics(enabled: Boolean) {
        _biometricsEnabled.value = enabled
    }

    fun refreshLocation() {
        // Simulates fresh GPS check
        _geofenceZone.value = _geofenceZone.value.copy(
            distanceMeters = (8..25).random().toDouble(),
            accuracyMeters = (5..10).random().toDouble(),
            isInside = true
        )
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}
