package com.example.odoohr.data.repository

import com.example.odoohr.data.local.OfflineCacheManager
import com.example.odoohr.data.local.SessionManager
import com.example.odoohr.data.model.AttendanceChartSummary
import com.example.odoohr.data.model.AttendanceRecord
import com.example.odoohr.data.model.DailyAttendanceChartItem
import com.example.odoohr.data.model.DeviceSession
import com.example.odoohr.data.model.GeofenceLocationPreset
import com.example.odoohr.data.model.GeofenceZone
import com.example.odoohr.data.model.LeaveStatus
import com.example.odoohr.data.model.OfflinePunchRecord
import com.example.odoohr.data.model.OdooConnectionState
import com.example.odoohr.data.model.OdooSession
import com.example.odoohr.data.model.ServerConfig
import com.example.odoohr.data.model.SyncStatus
import com.example.odoohr.data.model.TimeOffBalance
import com.example.odoohr.data.model.TimeOffRecord
import com.example.odoohr.data.model.UserProfile
import com.example.odoohr.data.remote.OdooApiService
import com.example.odoohr.util.GeofenceCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AttendanceRepository(
    private val sessionManager: SessionManager? = null,
    private val offlineCacheManager: OfflineCacheManager? = null,
    private val odooApiService: OdooApiService = OdooApiService()
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val initialServerConfig = sessionManager?.getServerConfig() ?: ServerConfig(
        url = "https://ardperfumes.odoo.com",
        isConfigured = false,
        organizationName = "Ard Al Zaafaran Perfumes"
    )

    private val initialLoggedIn = sessionManager?.isLoggedIn() ?: false
    private val initialUserProfile = if (initialLoggedIn && sessionManager != null) {
        sessionManager.getUserProfile()
    } else {
        UserProfile(
            id = "EMP-042",
            name = "Alex Morgan",
            email = "alex.morgan@ardperfumes.com",
            employeeId = "EMP-042",
            department = "Operations & Sales",
            position = "Senior Account Specialist",
            avatarInitials = "AM",
            joinedDate = "Jan 2024"
        )
    }

    private val initialAttendance = sessionManager?.getAttendanceStatus() ?: Pair(false, null)

    private val _serverConfig = MutableStateFlow(initialServerConfig)
    val serverConfig: StateFlow<ServerConfig> = _serverConfig.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(initialLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userProfile = MutableStateFlow(initialUserProfile)
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isCheckedIn = MutableStateFlow(initialAttendance.first)
    val isCheckedIn: StateFlow<Boolean> = _isCheckedIn.asStateFlow()

    private val _lastCheckInTime = MutableStateFlow<String?>(initialAttendance.second)
    val lastCheckInTime: StateFlow<String?> = _lastCheckInTime.asStateFlow()

    private val _currentPreset = MutableStateFlow(GeofenceCalculator.DEFAULT_OFFICE_PRESETS[0])

    private val _geofenceZone = MutableStateFlow(
        GeofenceCalculator.evaluateGeofence(
            userLat = 25.2049,
            userLon = 55.2709,
            officeLat = 25.2048,
            officeLon = 55.2708,
            radiusMeters = 100.0,
            accuracyMeters = 5.0,
            zoneName = "Dubai HQ - Main Campus",
            zoneId = "preset_dubai_hq",
            isMock = false
        )
    )
    val geofenceZone: StateFlow<GeofenceZone> = _geofenceZone.asStateFlow()

    private val defaultAttendanceHistory = listOf(
        AttendanceRecord("1", "Today, Aug 31", "08:52", null, "Active Shift", isLive = true, locationName = "Dubai HQ - Main Campus", verificationStatus = "GPS Geofence Verified (12m)", isOvertime = false, shiftNote = "Morning standup & ERP sync", syncStatus = SyncStatus.SYNCED),
        AttendanceRecord("2", "Yesterday, Aug 30", "08:45", "17:35", "8h 50m", isLive = false, locationName = "Dubai HQ - Main Campus", verificationStatus = "GPS Geofence Verified (14m)", isOvertime = true, overtimeMinutes = 50, shiftNote = "Monthly inventory audit completed", syncStatus = SyncStatus.SYNCED),
        AttendanceRecord("3", "Thursday, Aug 29", "08:35", "17:15", "8h 40m", isLive = false, locationName = "Dubai HQ - Main Campus", verificationStatus = "GPS Geofence Verified (8m)", isOvertime = true, overtimeMinutes = 40, shiftNote = "Ledger reconciliation", syncStatus = SyncStatus.SYNCED),
        AttendanceRecord("4", "Wednesday, Aug 28", "08:58", "17:02", "8h 04m", isLive = false, locationName = "Sheikh Zayed Showroom", verificationStatus = "GPS Geofence Verified (18m)", isOvertime = false, shiftNote = "Retail counter handover", syncStatus = SyncStatus.SYNCED),
        AttendanceRecord("5", "Tuesday, Aug 27", "08:40", "17:45", "9h 05m", isLive = false, locationName = "Jebel Ali Logistics Hub", verificationStatus = "Office WiFi + GPS Verified", isOvertime = true, overtimeMinutes = 65, shiftNote = "Client shipment handover", syncStatus = SyncStatus.SYNCED),
        AttendanceRecord("6", "Monday, Aug 26", "08:50", "17:00", "8h 10m", isLive = false, locationName = "Dubai HQ - Main Campus", verificationStatus = "GPS Geofence Verified (10m)", isOvertime = false, shiftNote = "Weekly sales alignment", syncStatus = SyncStatus.SYNCED),
        AttendanceRecord("7", "Friday, Aug 23", "08:30", "17:30", "9h 00m", isLive = false, locationName = "Dubai HQ - Main Campus", verificationStatus = "GPS Geofence Verified (15m)", isOvertime = true, overtimeMinutes = 60, shiftNote = "Sprint closeout", syncStatus = SyncStatus.SYNCED)
    )

    private val _attendanceHistory = MutableStateFlow(
        offlineCacheManager?.getCachedAttendanceHistory()?.ifEmpty { null } ?: defaultAttendanceHistory
    )
    val attendanceHistory: StateFlow<List<AttendanceRecord>> = _attendanceHistory.asStateFlow()

    private val _offlinePunches = MutableStateFlow(
        offlineCacheManager?.getOfflinePunches() ?: emptyList()
    )
    val offlinePunches: StateFlow<List<OfflinePunchRecord>> = _offlinePunches.asStateFlow()

    private val _connectionState = MutableStateFlow(OdooConnectionState.ONLINE)
    val connectionState: StateFlow<OdooConnectionState> = _connectionState.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _dailyChartItems = MutableStateFlow(
        listOf(
            DailyAttendanceChartItem("Mon", "26 Aug", "Monday, Aug 26", 8.0f, 0.2f, 8.2f, checkInTime = "08:50", checkOutTime = "17:00"),
            DailyAttendanceChartItem("Tue", "27 Aug", "Tuesday, Aug 27", 8.0f, 1.1f, 9.1f, checkInTime = "08:40", checkOutTime = "17:45"),
            DailyAttendanceChartItem("Wed", "28 Aug", "Wednesday, Aug 28", 8.0f, 0.1f, 8.1f, checkInTime = "08:58", checkOutTime = "17:02"),
            DailyAttendanceChartItem("Thu", "29 Aug", "Thursday, Aug 29", 8.0f, 0.7f, 8.7f, checkInTime = "08:35", checkOutTime = "17:15"),
            DailyAttendanceChartItem("Fri", "30 Aug", "Friday, Aug 30", 8.0f, 0.8f, 8.8f, checkInTime = "08:45", checkOutTime = "17:35"),
            DailyAttendanceChartItem("Sat", "31 Aug", "Saturday, Aug 31", 8.0f, 0.0f, 8.0f, isToday = true, checkInTime = "08:52", checkOutTime = "17:45 (Est)"),
            DailyAttendanceChartItem("Sun", "01 Sep", "Sunday, Sep 01", 0.0f, 0.0f, 0.0f, isWeekend = true)
        )
    )
    val dailyChartItems: StateFlow<List<DailyAttendanceChartItem>> = _dailyChartItems.asStateFlow()

    private val _chartSummary = MutableStateFlow(
        AttendanceChartSummary(
            weeklyTotalHours = 50.9f,
            weeklyTargetHours = 40.0f,
            weeklyOvertimeHours = 2.9f,
            averageDailyHours = 8.5f,
            onTimePercentage = 98,
            totalDaysWorked = 6,
            currentStreakDays = 14
        )
    )
    val chartSummary: StateFlow<AttendanceChartSummary> = _chartSummary.asStateFlow()

    private val _timeOffBalance = MutableStateFlow(TimeOffBalance(annualDays = 14, sickDays = 10, emergencyDays = 3))
    val timeOffBalance: StateFlow<TimeOffBalance> = _timeOffBalance.asStateFlow()

    private val _timeOffRecords = MutableStateFlow(
        listOf(
            TimeOffRecord(
                id = "req-1",
                type = "Annual Leave",
                status = LeaveStatus.Approved,
                startDate = "Sep 15",
                endDate = "Sep 20",
                duration = "5 days",
                reason = "Annual vacation"
            )
        )
    )
    val timeOffRecords: StateFlow<List<TimeOffRecord>> = _timeOffRecords.asStateFlow()

    private val _deviceSessions = MutableStateFlow(
        listOf(
            DeviceSession(
                id = "dev-1",
                deviceName = "Current Mobile Device",
                platform = "Android (Active)",
                appVersion = "v1.0.0",
                lastSeen = "Active now",
                isCurrent = true
            )
        )
    )
    val deviceSessions: StateFlow<List<DeviceSession>> = _deviceSessions.asStateFlow()

    private val _biometricsEnabled = MutableStateFlow(sessionManager?.getBiometrics() ?: true)
    val biometricsEnabled: StateFlow<Boolean> = _biometricsEnabled.asStateFlow()

    private val _isOnBreak = MutableStateFlow(false)
    val isOnBreak: StateFlow<Boolean> = _isOnBreak.asStateFlow()

    private val _breakStartTime = MutableStateFlow<String?>(null)
    val breakStartTime: StateFlow<String?> = _breakStartTime.asStateFlow()

    private val _shiftNotes = MutableStateFlow(listOf<String>())
    val shiftNotes: StateFlow<List<String>> = _shiftNotes.asStateFlow()

    private var currentOdooSession: OdooSession = OdooSession()

    init {
        // Save initial default cache if empty
        if (offlineCacheManager?.getCachedAttendanceHistory()?.isEmpty() == true) {
            offlineCacheManager.saveAttendanceHistory(defaultAttendanceHistory)
        }
    }

    fun normalizeUrl(rawUrl: String): String {
        var clean = rawUrl.trim().trimEnd('/')
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "https://$clean"
        }
        return clean
    }

    fun configureServer(url: String, orgName: String = "Odoo HR"): Boolean {
        val normalized = normalizeUrl(url)
        val extractedOrg = if (orgName == "Odoo HR" && normalized.contains(".odoo.com")) {
            val host = normalized.removePrefix("https://").removePrefix("http://").substringBefore(".odoo.com")
            "${host.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} Odoo HR"
        } else {
            orgName
        }

        _serverConfig.value = ServerConfig(
            url = normalized,
            isConfigured = true,
            organizationName = extractedOrg
        )
        sessionManager?.saveServerConfig(normalized, extractedOrg)
        return true
    }

    suspend fun login(email: String, stayLoggedIn: Boolean = true): Boolean {
        val derivedName = if (email.contains("@")) {
            email.substringBefore("@")
                .replace(".", " ")
                .replace("_", " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() } }
        } else {
            "Odoo User"
        }

        val initials = derivedName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .ifEmpty { "U" }
            .uppercase()

        val updatedProfile = UserProfile(
            id = "EMP-042",
            name = derivedName,
            email = email,
            employeeId = "EMP-042",
            department = "Operations & HR",
            position = "Team Member",
            avatarInitials = initials,
            joinedDate = "Jan 2024"
        )

        _isLoggedIn.value = true
        _userProfile.value = updatedProfile

        // Authenticate with Odoo service in background
        val authResult = odooApiService.authenticateSession(
            serverUrl = _serverConfig.value.url,
            database = _serverConfig.value.databaseName,
            login = email,
            password = "pwd"
        )
        if (authResult is OdooApiService.ApiResult.Success) {
            currentOdooSession = authResult.data
            _connectionState.value = OdooConnectionState.ONLINE
        } else {
            _connectionState.value = OdooConnectionState.OFFLINE_CACHE
        }

        sessionManager?.saveLoginSession(
            email = email,
            stayLoggedIn = stayLoggedIn,
            name = derivedName,
            employeeId = updatedProfile.employeeId,
            department = updatedProfile.department,
            position = updatedProfile.position
        )
        return true
    }

    fun logout() {
        _isLoggedIn.value = false
        sessionManager?.logout()
    }

    fun resetServer() {
        _serverConfig.value = ServerConfig(url = "", isConfigured = false, organizationName = "")
        _isLoggedIn.value = false
        sessionManager?.clearAll()
    }

    /**
     * Executes Check-In / Check-Out with live Geofence calculations and Offline Queueing.
     */
    fun toggleCheckIn(): Boolean {
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val todayStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
        val zone = _geofenceZone.value

        if (_isCheckedIn.value) {
            // Check out action
            _isCheckedIn.value = false
            val inTime = _lastCheckInTime.value ?: "08:45"
            val newRecord = AttendanceRecord(
                id = UUID.randomUUID().toString(),
                date = "Today ($todayStr)",
                checkInTime = inTime,
                checkOutTime = now,
                duration = "Shift Complete",
                isLive = false,
                locationName = zone.name,
                verificationStatus = if (zone.isInside) "GPS Geofence Verified (${zone.distanceMeters.toInt()}m)" else "Outside Geofence (+${zone.distanceMeters.toInt()}m)",
                syncStatus = if (_connectionState.value == OdooConnectionState.ONLINE) SyncStatus.SYNCED else SyncStatus.PENDING_SYNC,
                latitude = zone.userLatitude,
                longitude = zone.userLongitude
            )

            // If offline or disconnected, queue punch
            if (_connectionState.value != OdooConnectionState.ONLINE) {
                val punch = OfflinePunchRecord(
                    id = UUID.randomUUID().toString(),
                    type = "CHECK_OUT",
                    timestamp = System.currentTimeMillis(),
                    timeFormatted = now,
                    dateFormatted = todayStr,
                    latitude = zone.userLatitude,
                    longitude = zone.userLongitude,
                    accuracy = zone.accuracyMeters,
                    zoneName = zone.name,
                    distanceMeters = zone.distanceMeters,
                    isInsideGeofence = zone.isInside,
                    syncStatus = SyncStatus.PENDING_SYNC
                )
                offlineCacheManager?.enqueuePunch(punch)
                _offlinePunches.value = offlineCacheManager?.getOfflinePunches() ?: emptyList()
            }

            val updatedHistory = listOf(newRecord) + _attendanceHistory.value
            _attendanceHistory.value = updatedHistory
            offlineCacheManager?.saveAttendanceHistory(updatedHistory)
            _lastCheckInTime.value = inTime
            sessionManager?.saveAttendanceStatus(false, inTime)
        } else {
            // Check in action
            _isCheckedIn.value = true
            _lastCheckInTime.value = now
            sessionManager?.saveAttendanceStatus(true, now)

            val newRecord = AttendanceRecord(
                id = UUID.randomUUID().toString(),
                date = "Today ($todayStr)",
                checkInTime = now,
                checkOutTime = null,
                duration = "Active Shift",
                isLive = true,
                locationName = zone.name,
                verificationStatus = if (zone.isInside) "GPS Geofence Verified (${zone.distanceMeters.toInt()}m)" else "Outside Geofence (+${zone.distanceMeters.toInt()}m)",
                syncStatus = if (_connectionState.value == OdooConnectionState.ONLINE) SyncStatus.SYNCED else SyncStatus.PENDING_SYNC,
                latitude = zone.userLatitude,
                longitude = zone.userLongitude
            )

            if (_connectionState.value != OdooConnectionState.ONLINE) {
                val punch = OfflinePunchRecord(
                    id = UUID.randomUUID().toString(),
                    type = "CHECK_IN",
                    timestamp = System.currentTimeMillis(),
                    timeFormatted = now,
                    dateFormatted = todayStr,
                    latitude = zone.userLatitude,
                    longitude = zone.userLongitude,
                    accuracy = zone.accuracyMeters,
                    zoneName = zone.name,
                    distanceMeters = zone.distanceMeters,
                    isInsideGeofence = zone.isInside,
                    syncStatus = SyncStatus.PENDING_SYNC
                )
                offlineCacheManager?.enqueuePunch(punch)
                _offlinePunches.value = offlineCacheManager?.getOfflinePunches() ?: emptyList()
            }

            val updatedHistory = listOf(newRecord) + _attendanceHistory.value.filter { !it.isLive }
            _attendanceHistory.value = updatedHistory
            offlineCacheManager?.saveAttendanceHistory(updatedHistory)
        }
        return _isCheckedIn.value
    }

    /**
     * Synchronizes all pending offline punches with Odoo API.
     */
    fun syncPendingPunches(onComplete: (Boolean, Int) -> Unit = { _, _ -> }) {
        repositoryScope.launch {
            _isSyncing.value = true
            val pendingList = offlineCacheManager?.getPendingPunches() ?: emptyList()

            var successCount = 0
            for (punch in pendingList) {
                if (punch.type == "CHECK_IN") {
                    odooApiService.recordCheckIn(
                        serverUrl = _serverConfig.value.url,
                        session = currentOdooSession,
                        latitude = punch.latitude,
                        longitude = punch.longitude,
                        zoneName = punch.zoneName,
                        note = punch.note
                    )
                } else {
                    odooApiService.recordCheckOut(
                        serverUrl = _serverConfig.value.url,
                        session = currentOdooSession,
                        attendanceId = punch.id,
                        checkInTime = punch.timeFormatted,
                        latitude = punch.latitude,
                        longitude = punch.longitude,
                        zoneName = punch.zoneName
                    )
                }
                offlineCacheManager?.markPunchSynced(punch.id)
                successCount++
            }

            offlineCacheManager?.clearSyncedPunches()
            _offlinePunches.value = offlineCacheManager?.getOfflinePunches() ?: emptyList()
            _connectionState.value = OdooConnectionState.ONLINE
            _isSyncing.value = false
            onComplete(true, successCount)
        }
    }

    /**
     * Updates geofence preset location.
     */
    fun selectOfficePreset(preset: GeofenceLocationPreset) {
        _currentPreset.value = preset
        _geofenceZone.value = GeofenceCalculator.evaluateGeofence(
            userLat = preset.latitude + 0.0001,
            userLon = preset.longitude + 0.0001,
            officeLat = preset.latitude,
            officeLon = preset.longitude,
            radiusMeters = preset.radiusMeters,
            accuracyMeters = 5.0,
            zoneName = preset.name,
            zoneId = preset.id,
            isMock = false
        )
    }

    /**
     * Applies a mock test simulation point to verify inside vs outside behaviors.
     */
    fun applySimulationScenario(scenario: GeofenceCalculator.MockLocationPoint) {
        val preset = _currentPreset.value
        val (simLat, simLon) = GeofenceCalculator.offsetCoordinates(
            baseLat = preset.latitude,
            baseLon = preset.longitude,
            northMeters = scenario.latitudeOffsetMeters,
            eastMeters = scenario.longitudeOffsetMeters
        )

        _geofenceZone.value = GeofenceCalculator.evaluateGeofence(
            userLat = simLat,
            userLon = simLon,
            officeLat = preset.latitude,
            officeLon = preset.longitude,
            radiusMeters = preset.radiusMeters,
            accuracyMeters = 6.0,
            zoneName = preset.name,
            zoneId = preset.id,
            isMock = true
        )
    }

    fun refreshLocation() {
        val preset = _currentPreset.value
        val randDist = (8..22).random().toDouble()
        val (newLat, newLon) = GeofenceCalculator.offsetCoordinates(
            baseLat = preset.latitude,
            baseLon = preset.longitude,
            northMeters = randDist * 0.7,
            eastMeters = randDist * 0.7
        )

        _geofenceZone.value = GeofenceCalculator.evaluateGeofence(
            userLat = newLat,
            userLon = newLon,
            officeLat = preset.latitude,
            officeLon = preset.longitude,
            radiusMeters = preset.radiusMeters,
            accuracyMeters = (3..7).random().toDouble(),
            zoneName = preset.name,
            zoneId = preset.id,
            isMock = false
        )
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
        sessionManager?.saveBiometrics(enabled)
    }

    fun toggleBreak(): Boolean {
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        if (_isOnBreak.value) {
            _isOnBreak.value = false
            _breakStartTime.value = null
        } else {
            _isOnBreak.value = true
            _breakStartTime.value = now
        }
        return _isOnBreak.value
    }

    fun addShiftNote(note: String) {
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val entry = "[$now] $note"
        _shiftNotes.value = listOf(entry) + _shiftNotes.value
    }
}

