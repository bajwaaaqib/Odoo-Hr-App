package com.example.odoohr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.odoohr.data.model.AttendanceRecord
import com.example.odoohr.data.model.DeviceSession
import com.example.odoohr.data.model.GeofenceZone
import com.example.odoohr.data.model.ServerConfig
import com.example.odoohr.data.model.TimeOffBalance
import com.example.odoohr.data.model.TimeOffRecord
import com.example.odoohr.data.model.UserProfile
import com.example.odoohr.data.repository.AttendanceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AttendanceViewModel(
    private val repository: AttendanceRepository = AttendanceRepository()
) : ViewModel() {

    val serverConfig: StateFlow<ServerConfig> = repository.serverConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.serverConfig.value)

    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.userProfile.value)

    val isCheckedIn: StateFlow<Boolean> = repository.isCheckedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lastCheckInTime: StateFlow<String?> = repository.lastCheckInTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val geofenceZone: StateFlow<GeofenceZone> = repository.geofenceZone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.geofenceZone.value)

    val attendanceHistory: StateFlow<List<AttendanceRecord>> = repository.attendanceHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timeOffBalance: StateFlow<TimeOffBalance> = repository.timeOffBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.timeOffBalance.value)

    val timeOffRecords: StateFlow<List<TimeOffRecord>> = repository.timeOffRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deviceSessions: StateFlow<List<DeviceSession>> = repository.deviceSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val biometricsEnabled: StateFlow<Boolean> = repository.biometricsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun configureServer(url: String, onSuccess: () -> Unit) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            _errorMessage.value = "Please enter your Odoo server URL"
            return
        }
        if (!trimmed.startsWith("https://")) {
            _errorMessage.value = "URL must start with https://"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            delay(800) // Simulate TLS / handshake verification
            val success = repository.configureServer(trimmed)
            _isLoading.value = false
            if (success) {
                onSuccess()
            } else {
                _errorMessage.value = "Unable to connect to Odoo server. Check URL."
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            _errorMessage.value = "Please enter your email"
            return
        }
        if (!trimmedEmail.contains("@")) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }
        if (pass.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            delay(1000) // Simulate authentication & token exchange
            repository.login(trimmedEmail)
            _isLoading.value = false
            onSuccess()
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        repository.logout()
        onLoggedOut()
    }

    fun toggleAttendance() {
        repository.toggleCheckIn()
    }

    fun refreshLocation() {
        repository.refreshLocation()
    }

    fun submitTimeOff(type: String, startDate: String, endDate: String, days: Int, reason: String, onDone: () -> Unit) {
        repository.requestLeave(type, startDate, endDate, days, reason)
        onDone()
    }

    fun revokeDevice(deviceId: String) {
        repository.revokeDevice(deviceId)
    }

    fun setBiometrics(enabled: Boolean) {
        repository.toggleBiometrics(enabled)
    }
}
