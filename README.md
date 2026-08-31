# GeoFence Attendance — Android Application

A modern, native Android mobile application built with **Kotlin** and **Jetpack Compose** that connects to an Odoo instance for GPS-based check-in/check-out with geofencing support and HR management.

## Features

- **Dynamic Server Setup**: Connect to any custom Odoo server link (e.g., `ardperfumes.odoo.com` or custom HTTPS instances) with automatic URL normalization and TLS verification.
- **Secure & Dynamic Authentication**: No hardcoded credentials; users enter their own Odoo email/username and password. Includes a **"Stay logged in"** preference to keep the session active across app restarts.
- **GPS Geofencing**: Real-time location validation within authorized office perimeters (e.g. Office Zone with precision & distance tracking).
- **One-Tap Check-In / Check-Out**: Seamless attendance status toggling with instant timestamp and daily duration calculation.
- **Attendance History**: Review chronological attendance logs with check-in, check-out, duration metrics, and on-time performance rates.
- **Time Off Management**: Track leave balances (Annual, Sick, Emergency) and submit new leave requests with custom date ranges and reasons.
- **Profile & Device Management**: View employee identity details, manage active registered device sessions, switch Odoo server instances, and configure biometric lock settings.

## Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM (Model-View-ViewModel) + StateFlow
- **Navigation**: Jetpack Navigation Compose
- **Design System**: Material You dynamic theming with clean typography, generous spacing, and custom adaptive app icons
