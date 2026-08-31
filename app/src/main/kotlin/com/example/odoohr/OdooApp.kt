package com.example.odoohr

import android.app.Application
import com.example.odoohr.data.local.OfflineCacheManager
import com.example.odoohr.data.local.SessionManager
import com.example.odoohr.data.remote.OdooApiService
import com.example.odoohr.data.repository.AttendanceRepository

class OdooApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    lateinit var offlineCacheManager: OfflineCacheManager
        private set

    lateinit var odooApiService: OdooApiService
        private set

    lateinit var attendanceRepository: AttendanceRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionManager = SessionManager(this)
        offlineCacheManager = OfflineCacheManager(this)
        odooApiService = OdooApiService()
        attendanceRepository = AttendanceRepository(
            sessionManager = sessionManager,
            offlineCacheManager = offlineCacheManager,
            odooApiService = odooApiService
        )
        com.example.odoohr.util.AttendanceNotificationManager.initNotificationChannels(this)
    }

    companion object {
        lateinit var instance: OdooApp
            private set
    }
}

