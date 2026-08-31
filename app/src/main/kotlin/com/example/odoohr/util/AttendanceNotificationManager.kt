package com.example.odoohr.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.odoohr.MainActivity
import com.example.odoohr.R

object AttendanceNotificationManager {

    const val CHANNEL_ATTENDANCE = "channel_attendance_events"
    const val CHANNEL_GEOFENCE = "channel_geofence_alerts"
    const val CHANNEL_SYNC = "channel_sync_events"

    private const val NOTIFICATION_ID_ATTENDANCE = 1001
    private const val NOTIFICATION_ID_GEOFENCE = 1002
    private const val NOTIFICATION_ID_SYNC = 1003
    private const val NOTIFICATION_ID_BREAK = 1004
    private const val NOTIFICATION_ID_TIME_OFF = 1005

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val attendanceChannel = NotificationChannel(
                CHANNEL_ATTENDANCE,
                "Attendance & Shifts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for shift check-ins, check-outs, and shift reminders"
                enableVibration(true)
            }

            val geofenceChannel = NotificationChannel(
                CHANNEL_GEOFENCE,
                "Geofence & Location Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when entering or leaving verified office geofence perimeters"
                enableVibration(true)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Offline Synchronization",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Updates on offline punch syncing and Odoo ERP connection status"
            }

            notificationManager.createNotificationChannels(listOf(attendanceChannel, geofenceChannel, syncChannel))
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    fun showCheckInNotification(context: Context, employeeName: String, time: String, zoneName: String) {
        if (!hasNotificationPermission(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ATTENDANCE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Checked In Successfully")
            .setContentText("$employeeName checked in at $time ($zoneName)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Good morning $employeeName! Your shift started at $time within $zoneName. GPS verified and synced with Odoo.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent(context))
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ATTENDANCE, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showCheckOutNotification(context: Context, employeeName: String, duration: String) {
        if (!hasNotificationPermission(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ATTENDANCE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Shift Completed - Checked Out")
            .setContentText("Shift duration: $duration. Attendance log saved.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent(context))
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ATTENDANCE, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showGeofenceEnteredNotification(context: Context, zoneName: String, distanceMeters: Int) {
        if (!hasNotificationPermission(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_GEOFENCE)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("Welcome to $zoneName")
            .setContentText("You are inside the office geofence ($distanceMeters m from center). Tap to punch in.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent(context))
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_GEOFENCE, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showGeofenceExitedNotification(context: Context, zoneName: String, distanceMeters: Int) {
        if (!hasNotificationPermission(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_GEOFENCE)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Left $zoneName Perimeter")
            .setContentText("You are currently $distanceMeters m away. Remember to log your checkout or pause your shift.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getPendingIntent(context))
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_GEOFENCE, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showSyncNotification(context: Context, syncedCount: Int, failedCount: Int) {
        if (!hasNotificationPermission(context)) return

        val title = if (failedCount == 0) "Offline Punches Synced" else "Sync Completed with Warnings"
        val body = if (failedCount == 0) {
            "Successfully uploaded $syncedCount attendance records to Odoo Enterprise."
        } else {
            "$syncedCount punches uploaded. $failedCount requires attention."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getPendingIntent(context))
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SYNC, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showBreakNotification(context: Context, isOnBreak: Boolean, durationMinutes: Int = 15) {
        if (!hasNotificationPermission(context)) return

        val title = if (isOnBreak) "Break Started (Paused)" else "Shift Resumed"
        val text = if (isOnBreak) {
            "You are currently on break. Tap to resume your active shift."
        } else {
            "Welcome back! Shift timer resumed."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ATTENDANCE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getPendingIntent(context))
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BREAK, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showTimeOffNotification(context: Context, holidayType: String, days: Double) {
        if (!hasNotificationPermission(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ATTENDANCE)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("Time Off Request Submitted")
            .setContentText("Your request for $days day(s) of $holidayType has been sent to HR approval.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getPendingIntent(context))
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TIME_OFF, builder.build())
        } catch (_: SecurityException) {}
    }
}
