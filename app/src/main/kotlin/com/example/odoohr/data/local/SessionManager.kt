package com.example.odoohr.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.odoohr.data.model.ServerConfig
import com.example.odoohr.data.model.UserProfile

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "odoo_hr_session_prefs"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_SERVER_CONFIGURED = "server_configured"
        private const val KEY_ORG_NAME = "org_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_STAY_LOGGED_IN = "stay_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_EMPLOYEE_ID = "employee_id"
        private const val KEY_DEPARTMENT = "department"
        private const val KEY_POSITION = "position"
        private const val KEY_IS_CHECKED_IN = "is_checked_in"
        private const val KEY_LAST_CHECK_IN = "last_check_in"
        private const val KEY_BIOMETRICS = "biometrics_enabled"
        private const val KEY_DARK_MODE = "dark_mode_preference"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_GEOFENCE_ALERTS = "geofence_alerts_enabled"
    }

    fun saveDarkMode(mode: String) {
        prefs.edit().putString(KEY_DARK_MODE, mode).apply()
    }

    fun getDarkMode(): String {
        return prefs.getString(KEY_DARK_MODE, "SYSTEM") ?: "SYSTEM"
    }

    fun saveNotificationSettings(enabled: Boolean, geofenceAlerts: Boolean) {
        prefs.edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            .putBoolean(KEY_GEOFENCE_ALERTS, geofenceAlerts)
            .apply()
    }

    fun getNotificationSettings(): Pair<Boolean, Boolean> {
        val enabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        val geofenceAlerts = prefs.getBoolean(KEY_GEOFENCE_ALERTS, true)
        return Pair(enabled, geofenceAlerts)
    }

    fun saveServerConfig(url: String, orgName: String = "Odoo Enterprise") {
        prefs.edit()
            .putString(KEY_SERVER_URL, url)
            .putBoolean(KEY_SERVER_CONFIGURED, true)
            .putString(KEY_ORG_NAME, orgName)
            .apply()
    }

    fun getServerConfig(): ServerConfig {
        val url = prefs.getString(KEY_SERVER_URL, "") ?: ""
        val isConfigured = prefs.getBoolean(KEY_SERVER_CONFIGURED, false)
        val org = prefs.getString(KEY_ORG_NAME, "Odoo Enterprise") ?: "Odoo Enterprise"
        return ServerConfig(
            url = url,
            isConfigured = isConfigured && url.isNotBlank(),
            organizationName = org
        )
    }

    fun saveLoginSession(
        email: String,
        stayLoggedIn: Boolean,
        name: String? = null,
        employeeId: String = "EMP-042",
        department: String = "Operations & Sales",
        position: String = "Team Member"
    ) {
        val derivedName = name ?: if (email.contains("@")) {
            email.substringBefore("@")
                .replace(".", " ")
                .replace("_", " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
        } else {
            "Odoo User"
        }

        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_STAY_LOGGED_IN, stayLoggedIn)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, derivedName)
            .putString(KEY_EMPLOYEE_ID, employeeId)
            .putString(KEY_DEPARTMENT, department)
            .putString(KEY_POSITION, position)
            .apply()
    }

    fun getUserProfile(): UserProfile {
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val name = prefs.getString(KEY_USER_NAME, "Employee") ?: "Employee"
        val empId = prefs.getString(KEY_EMPLOYEE_ID, "EMP-042") ?: "EMP-042"
        val dept = prefs.getString(KEY_DEPARTMENT, "Operations & Sales") ?: "Operations & Sales"
        val pos = prefs.getString(KEY_POSITION, "Team Member") ?: "Team Member"
        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .ifEmpty { "U" }
            .uppercase()

        return UserProfile(
            id = empId,
            name = name,
            email = email,
            employeeId = empId,
            department = dept,
            position = pos,
            avatarInitials = initials,
            joinedDate = "Jan 2024"
        )
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val stayLoggedIn = prefs.getBoolean(KEY_STAY_LOGGED_IN, true)
        return loggedIn && stayLoggedIn
    }

    fun saveAttendanceStatus(isCheckedIn: Boolean, lastCheckInTime: String?) {
        prefs.edit()
            .putBoolean(KEY_IS_CHECKED_IN, isCheckedIn)
            .putString(KEY_LAST_CHECK_IN, lastCheckInTime)
            .apply()
    }

    fun getAttendanceStatus(): Pair<Boolean, String?> {
        val isChecked = prefs.getBoolean(KEY_IS_CHECKED_IN, false)
        val lastCheck = prefs.getString(KEY_LAST_CHECK_IN, null)
        return Pair(isChecked, lastCheck)
    }

    fun saveBiometrics(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRICS, enabled).apply()
    }

    fun getBiometrics(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRICS, true)
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_STAY_LOGGED_IN, false)
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
