package com.example.odoohr

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.odoohr.ui.navigation.AppNavigation
import com.example.odoohr.ui.theme.GeoAttendanceTheme
import com.example.odoohr.ui.viewmodel.AttendanceViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AttendanceViewModel = viewModel()
            val darkModePref by viewModel.darkModePreference.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (darkModePref) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemDark
            }
            GeoAttendanceTheme(darkTheme = isDarkTheme) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
