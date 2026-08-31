# Geo Attendance Mobile App

A secure, fast, and scalable Flutter mobile application for GeoFence-based attendance tracking with dynamic Odoo integration.

## Features

- **Dynamic Odoo Connection**: Users can enter their own Odoo instance URL (e.g., `ardperfumes.odoo.com`) during login
- **No Hardcoded Credentials**: All Odoo connections are user-configured at runtime
- **Secure Storage**: Credentials stored using encrypted local storage (Keychain/Keystore)
- **GPS Geofencing**: Accurate location tracking for attendance marking
- **Real-time Authentication**: Direct integration with Odoo's JSON-RPC API
- **Cross-Platform**: Works on both Android and iOS simulators and devices

## Architecture

```
lib/
├── main.dart                 # App entry point & provider setup
├── services/
│   ├── odoo_service.dart     # Odoo API integration (dynamic URL)
│   └── storage_service.dart  # Secure credential storage
├── screens/
│   ├── login_screen.dart     # Dynamic Odoo URL login
│   └── home_screen.dart      # Attendance marking with GPS
├── models/                   # Data models
└── widgets/                  # Reusable UI components
```

## Prerequisites

### For Development & Simulator Testing

1. **Flutter SDK** (3.0+)
   ```bash
   # Install via official method or use Docker
   ```

2. **Docker & Docker Compose** (for containerized development)

## Quick Start with Docker

### Option 1: Using Docker Compose (Recommended)

```bash
# Start the development environment
docker-compose up --build

# Access the Flutter dev environment
docker-compose exec flutter_dev bash

# Inside container
cd /app
flutter pub get
flutter run -d chrome  # For web testing
# or
flutter run            # For connected device/simulator
```

### Option 2: Manual Setup

```bash
# Navigate to project
cd geo_attendance_app

# Get dependencies
flutter pub get

# Run on simulator
flutter run

# For iOS simulator
flutter run -d "iPhone 15"

# For Android emulator
flutter run -d emulator-5554
```

## How It Works

### 1. First-Time Login Flow

1. User opens the app
2. Enters their Odoo instance URL (e.g., `ardperfumes.odoo.com`)
3. Enters username and password
4. App authenticates directly with Odoo's API
5. Credentials are securely stored on the device
6. User is logged in and can mark attendance

### 2. Subsequent Logins

- App automatically validates stored credentials
- If valid, user is directly logged in
- If invalid (password changed, etc.), user is prompted to re-login

### 3. Attendance Marking

1. App requests GPS location
2. User clicks "Check In" or "Check Out"
3. Location coordinates are sent to Odoo
4. Attendance record is created in Odoo HR module

## Security Features

- **Encrypted Storage**: Uses `flutter_secure_storage` with:
  - Android: Encrypted SharedPreferences
  - iOS: Keychain with first_unlock_this_device accessibility
  
- **No Hardcoded APIs**: All Odoo connections are user-provided
- **Session Validation**: Credentials validated on app startup
- **Secure Logout**: Complete credential clearing on logout

## Configuration

### Android Permissions (Already Configured)

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

### iOS Permissions (Already Configured)

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>This app needs access to your location to mark attendance with geofencing.</string>
```

## Testing on Simulators

### iOS Simulator

```bash
# List available simulators
xcrun simctl list devices

# Run on specific simulator
flutter run -d "iPhone 15 (simulator)"
```

### Android Emulator

```bash
# List available emulators
emulator -list-avds

# Start emulator
emulator -avd Pixel_6_API_34

# Run app
flutter run
```

## Environment Variables (Optional)

Create a `.env` file for development configurations:

```env
# No hardcoded Odoo URLs needed!
# Users enter their own Odoo instance in the app
APP_NAME=Geo Attendance
APP_VERSION=1.0.0
```

## Building for Production

### Android APK

```bash
flutter build apk --release
```

### Android App Bundle

```bash
flutter build appbundle --release
```

### iOS IPA

```bash
flutter build ios --release
```

## Troubleshooting

### Location Permission Issues

**Android:**
```bash
# Check permissions
adb shell pm list permissions | grep location
```

**iOS:**
- Ensure Info.plist has location usage descriptions
- Reset simulator: Device > Erase All Content and Settings

### Odoo Connection Issues

1. Verify Odoo URL format (no https:// prefix needed)
2. Check internet connectivity
3. Verify Odoo instance allows API access
4. Confirm user has proper permissions in Odoo

### Build Issues

```bash
# Clean and rebuild
flutter clean
flutter pub get
flutter run
```

## Scalability Features

- **State Management**: Provider pattern for efficient state handling
- **Lazy Loading**: Credentials loaded only when needed
- **Connection Pooling**: HTTP connections reused efficiently
- **Error Handling**: Graceful degradation on network issues
- **Offline Support**: Local caching for offline scenarios (future enhancement)

## API Integration

The app uses Odoo's native JSON-RPC API:

- **Authentication**: `/web/session/authenticate`
- **Data Operations**: `/web/dataset/call_kw`
- **No Middleware**: Direct communication for maximum performance

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test on both platforms
5. Submit a pull request

## License

MIT License - See LICENSE file for details

## Support

For issues or questions:
1. Check troubleshooting section
2. Review Odoo API documentation
3. Contact development team

---

**Note**: This app requires an Odoo instance with HR module installed for full functionality. The app is designed to work with any Odoo instance (Odoo Online, Odoo.sh, or On-premise).
