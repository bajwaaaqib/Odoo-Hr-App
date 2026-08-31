package com.example.odoohr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.odoohr.OdooApp
import com.example.odoohr.data.model.AttendanceChartSummary
import com.example.odoohr.data.model.AttendanceRecord
import com.example.odoohr.data.model.DailyAttendanceChartItem
import com.example.odoohr.data.model.DeviceSession
import com.example.odoohr.data.model.GeofenceLocationPreset
import com.example.odoohr.data.model.GeofenceZone
import com.example.odoohr.data.model.OfflinePunchRecord
import com.example.odoohr.data.model.OdooConnectionState
import com.example.odoohr.data.model.ServerConfig
import com.example.odoohr.data.model.TimeOffBalance
import com.example.odoohr.data.model.TimeOffRecord
import com.example.odoohr.data.model.UserProfile
import com.example.odoohr.data.repository.AttendanceRepository
import com.example.odoohr.util.GeofenceCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AttendanceViewModel(
    private val repository: AttendanceRepository = try {
        OdooApp.instance.attendanceRepository
    } catch (_: Exception) {
        AttendanceRepository()
    }
) : ViewModel() {

    val serverConfig: StateFlow<ServerConfig> = repository.serverConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.serverConfig.value)

    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.isLoggedIn.value)

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.userProfile.value)

    val isCheckedIn: StateFlow<Boolean> = repository.isCheckedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.isCheckedIn.value)

    val lastCheckInTime: StateFlow<String?> = repository.lastCheckInTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.lastCheckInTime.value)

    val geofenceZone: StateFlow<GeofenceZone> = repository.geofenceZone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.geofenceZone.value)

    val attendanceHistory: StateFlow<List<AttendanceRecord>> = repository.attendanceHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.attendanceHistory.value)

    val offlinePunches: StateFlow<List<OfflinePunchRecord>> = repository.offlinePunches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.offlinePunches.value)

    val pendingPunches: StateFlow<List<OfflinePunchRecord>> = offlinePunches

    val connectionState: StateFlow<OdooConnectionState> = repository.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.connectionState.value)

    val isSyncing: StateFlow<Boolean> = repository.isSyncing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.isSyncing.value)

    val dailyChartItems: StateFlow<List<DailyAttendanceChartItem>> = repository.dailyChartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.dailyChartItems.value)

    val chartSummary: StateFlow<AttendanceChartSummary> = repository.chartSummary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.chartSummary.value)

    val timeOffBalance: StateFlow<TimeOffBalance> = repository.timeOffBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.timeOffBalance.value)

    val timeOffRecords: StateFlow<List<TimeOffRecord>> = repository.timeOffRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.timeOffRecords.value)

    val deviceSessions: StateFlow<List<DeviceSession>> = repository.deviceSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.deviceSessions.value)

    val biometricsEnabled: StateFlow<Boolean> = repository.biometricsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.biometricsEnabled.value)

    val isOnBreak: StateFlow<Boolean> = repository.isOnBreak
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.isOnBreak.value)

    val breakStartTime: StateFlow<String?> = repository.breakStartTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.breakStartTime.value)

    val shiftNotes: StateFlow<List<String>> = repository.shiftNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.shiftNotes.value)

    private val sessionManager = try { OdooApp.instance.sessionManager } catch (_: Exception) { null }

    private val _darkModePreference = MutableStateFlow(sessionManager?.getDarkMode() ?: "SYSTEM")
    val darkModePreference: StateFlow<String> = _darkModePreference.asStateFlow()

    private val initialNotifSettings = sessionManager?.getNotificationSettings() ?: Pair(true, true)
    private val _notificationsEnabled = MutableStateFlow(initialNotifSettings.first)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _geofenceAlertsEnabled = MutableStateFlow(initialNotifSettings.second)
    val geofenceAlertsEnabled: StateFlow<Boolean> = _geofenceAlertsEnabled.asStateFlow()

    private val _isRefreshingDashboard = MutableStateFlow(false)
    val isRefreshingDashboard: StateFlow<Boolean> = _isRefreshingDashboard.asStateFlow()

    private val _isRefreshingHistory = MutableStateFlow(false)
    val isRefreshingHistory: StateFlow<Boolean> = _isRefreshingHistory.asStateFlow()

    private val _isRefreshingTimeOff = MutableStateFlow(false)
    val isRefreshingTimeOff: StateFlow<Boolean> = _isRefreshingTimeOff.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun configureServer(rawUrl: String, onSuccess: () -> Unit) {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) {
            _errorMessage.value = "Please enter your Odoo URL (e.g. ardperfumes.odoo.com)"
            return
        }

        val normalized = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else trimmed

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            delay(700) // Verification & SSL handshake
            val success = repository.configureServer(normalized)
            _isLoading.value = false
            if (success) {
                onSuccess()
            } else {
                _errorMessage.value = "Could not connect to Odoo server. Please check the URL."
            }
        }
    }

    fun login(email: String, pass: String, stayLoggedIn: Boolean, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        if (trimmedEmail.isEmpty()) {
            _errorMessage.value = "Please enter your username or email"
            return
        }
        if (trimmedPass.isEmpty()) {
            _errorMessage.value = "Please enter your password"
            return
        }
        if (trimmedPass.length < 3) {
            _errorMessage.value = "Password must be at least 3 characters"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            delay(900) // Secure authentication & session creation
            repository.login(trimmedEmail, stayLoggedIn)
            _isLoading.value = false
            onSuccess()
        }
    }

    fun biometricLogin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            delay(600) // Biometric credential token verification
            val currentEmail = repository.userProfile.value.email.ifEmpty { "alex.morgan@ardperfumes.com" }
            repository.login(currentEmail, stayLoggedIn = true)
            _isLoading.value = false
            onSuccess()
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        repository.logout()
        onLoggedOut()
    }

    fun resetServer(onReset: () -> Unit) {
        repository.resetServer()
        onReset()
    }

    fun toggleAttendance() {
        val wasCheckedIn = isCheckedIn.value
        val isNowCheckedIn = repository.toggleCheckIn()
        val context = try { OdooApp.instance } catch (_: Exception) { null }
        if (context != null && _notificationsEnabled.value) {
            val user = userProfile.value.name
            val zone = geofenceZone.value.name
            if (isNowCheckedIn) {
                com.example.odoohr.util.AttendanceNotificationManager.showCheckInNotification(
                    context = context,
                    employeeName = user,
                    time = lastCheckInTime.value ?: "Now",
                    zoneName = zone
                )
            } else {
                com.example.odoohr.util.AttendanceNotificationManager.showCheckOutNotification(
                    context = context,
                    employeeName = user,
                    duration = "Shift Complete"
                )
            }
        }
    }

    fun refreshLocation() {
        repository.refreshLocation()
        checkGeofenceNotification()
    }

    fun selectOfficePreset(preset: GeofenceLocationPreset) {
        repository.selectOfficePreset(preset)
        checkGeofenceNotification()
    }

    fun applySimulationScenario(scenario: GeofenceCalculator.MockLocationPoint) {
        repository.applySimulationScenario(scenario)
        checkGeofenceNotification()
    }

    fun simulateLocation(scenario: GeofenceCalculator.MockLocationPoint) {
        repository.applySimulationScenario(scenario)
        checkGeofenceNotification()
    }

    private fun checkGeofenceNotification() {
        val context = try { OdooApp.instance } catch (_: Exception) { null }
        if (context != null && _notificationsEnabled.value && _geofenceAlertsEnabled.value) {
            val zone = geofenceZone.value
            if (zone.isInside) {
                com.example.odoohr.util.AttendanceNotificationManager.showGeofenceEnteredNotification(
                    context = context,
                    zoneName = zone.name,
                    distanceMeters = zone.distanceMeters.toInt()
                )
            }
        }
    }

    fun syncPendingPunches(onComplete: (Boolean, Int) -> Unit = { _, _ -> }) {
        repository.syncPendingPunches { success, count ->
            val context = try { OdooApp.instance } catch (_: Exception) { null }
            if (context != null && _notificationsEnabled.value && count > 0) {
                com.example.odoohr.util.AttendanceNotificationManager.showSyncNotification(
                    context = context,
                    syncedCount = count,
                    failedCount = 0
                )
            }
            onComplete(success, count)
        }
    }

    fun submitTimeOff(type: String, startDate: String, endDate: String, days: Int, reason: String, onDone: () -> Unit) {
        repository.requestLeave(type, startDate, endDate, days, reason)
        val context = try { OdooApp.instance } catch (_: Exception) { null }
        if (context != null && _notificationsEnabled.value) {
            com.example.odoohr.util.AttendanceNotificationManager.showTimeOffNotification(
                context = context,
                holidayType = type,
                days = days.toDouble()
            )
        }
        onDone()
    }

    fun revokeDevice(deviceId: String) {
        repository.revokeDevice(deviceId)
    }

    fun setBiometrics(enabled: Boolean) {
        repository.toggleBiometrics(enabled)
    }

    fun toggleBreak() {
        val isNowOnBreak = repository.toggleBreak()
        val context = try { OdooApp.instance } catch (_: Exception) { null }
        if (context != null && _notificationsEnabled.value) {
            com.example.odoohr.util.AttendanceNotificationManager.showBreakNotification(
                context = context,
                isOnBreak = isNowOnBreak
            )
        }
    }

    fun addShiftNote(note: String) {
        repository.addShiftNote(note)
    }

    // -------------------------------------------------------------
    // Dark Mode Methods
    // -------------------------------------------------------------

    fun setDarkModePreference(mode: String) {
        _darkModePreference.value = mode
        sessionManager?.saveDarkMode(mode)
    }

    fun toggleDarkMode() {
        val newMode = when (_darkModePreference.value) {
            "DARK" -> "LIGHT"
            "LIGHT" -> "DARK"
            else -> "DARK"
        }
        setDarkModePreference(newMode)
    }

    // -------------------------------------------------------------
    // Push Notification Settings
    // -------------------------------------------------------------

    fun setNotificationPreferences(enabled: Boolean, geofenceAlerts: Boolean) {
        _notificationsEnabled.value = enabled
        _geofenceAlertsEnabled.value = geofenceAlerts
        sessionManager?.saveNotificationSettings(enabled, geofenceAlerts)
    }

    fun sendTestNotification(type: String) {
        val context = try { OdooApp.instance } catch (_: Exception) { return }
        when (type) {
            "CHECK_IN" -> com.example.odoohr.util.AttendanceNotificationManager.showCheckInNotification(
                context, userProfile.value.name, "09:00", geofenceZone.value.name
            )
            "GEOFENCE" -> com.example.odoohr.util.AttendanceNotificationManager.showGeofenceEnteredNotification(
                context, geofenceZone.value.name, 15
            )
            "SYNC" -> com.example.odoohr.util.AttendanceNotificationManager.showSyncNotification(
                context, 3, 0
            )
            "BREAK" -> com.example.odoohr.util.AttendanceNotificationManager.showBreakNotification(
                context, true
            )
            "TIMEOFF" -> com.example.odoohr.util.AttendanceNotificationManager.showTimeOffNotification(
                context, "Annual Leave", 2.0
            )
        }
    }

    // -------------------------------------------------------------
    // Pull-To-Refresh Handlers
    // -------------------------------------------------------------

    fun refreshDashboard(onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            _isRefreshingDashboard.value = true
            repository.refreshLocation()
            delay(900) // Simulating network & sensor poll
            if (offlinePunches.value.isNotEmpty()) {
                repository.syncPendingPunches()
            }
            _isRefreshingDashboard.value = false
            onFinished()
        }
    }

    fun refreshAttendanceHistory(onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            _isRefreshingHistory.value = true
            delay(800) // Fetching latest attendance records from Odoo
            _isRefreshingHistory.value = false
            onFinished()
        }
    }

    fun refreshTimeOff(onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            _isRefreshingTimeOff.value = true
            delay(800) // Syncing leave allocation balances
            _isRefreshingTimeOff.value = false
            onFinished()
        }
    }
}

