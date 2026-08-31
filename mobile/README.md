# GeoFence Attendance Mobile App - Flutter

A secure mobile attendance application built with Flutter that connects to an Odoo instance for GPS-based check-in/check-out with geofencing support.

## Features

- **Server Setup**: Configure Odoo server URL with HTTPS validation
- **Secure Authentication**: Token-based authentication with secure storage
- **GPS Geofencing**: Location-based attendance validation
- **Check-In/Check-Out**: Simple one-tap attendance tracking
- **Attendance History**: View past attendance records
- **Time Off Management**: View leave balances and requests
- **Profile Management**: View employee information and manage devices

## Project Structure

```
mobile/
├── lib/
│   ├── main.dart                 # App entry point
│   ├── app/                      # App configuration
│   │   ├── app.dart             # Main app widget
│   │   ├── router.dart          # Navigation routes
│   │   └── theme.dart           # App theme
│   ├── core/                     # Core utilities
│   │   ├── config/              # Configuration
│   │   ├── constants/           # Constants
│   │   ├── network/             # Network services
│   │   ├── security/            # Security utilities
│   │   └── storage/             # Secure storage
│   ├── features/                 # Feature modules
│   │   ├── onboarding/          # Onboarding flow
│   │   ├── authentication/      # Login/auth
│   │   ├── home/                # Home screen
│   │   ├── attendance/          # Attendance features
│   │   ├── time_off/            # Time off features
│   │   ├── profile/             # Profile management
│   │   └── geofence/            # Geofencing logic
│   └── shared/                   # Shared components
├── android/                      # Android platform
├── ios/                          # iOS platform
├── assets/                       # App assets
├── test/                         # Unit tests
└── pubspec.yaml                  # Dependencies
```

## Requirements

- Flutter SDK 3.0+
- Dart SDK 3.0+
- Android Studio (for Android development)
- Xcode (for iOS development, macOS only)
- Android SDK API 21+
- iOS 12.0+ (if developing for iOS)

## Installation

### 1. Clone the Repository

```bash
cd mobile
```

### 2. Install Dependencies

```bash
flutter pub get
```

### 3. Configure Environment

The app requires a backend API gateway running. By default, it's configured for:
- Android Emulator: `http://10.0.2.2:8000`
- iOS Simulator: `http://localhost:8000`
- Physical Device: Your computer's LAN IP

Update `lib/core/config/app_config.dart` if needed.

### 4. Run the App

#### Android Emulator

```bash
flutter run
```

#### Specific Device

```bash
flutter devices
flutter run -d <device-id>
```

#### iOS Simulator (macOS only)

```bash
flutter run -d ios
```

## Permissions

The app requests the following permissions:

### Android
- `ACCESS_FINE_LOCATION` - For GPS location during check-in/out
- `ACCESS_COARSE_LOCATION` - Fallback location permission
- `INTERNET` - API communication
- `ACCESS_NETWORK_STATE` - Check connectivity
- `USE_BIOMETRIC` - Biometric authentication (optional)

### iOS
- `NSLocationWhenInUseUsageDescription` - Location during attendance
- `NSLocationAlwaysAndWhenInUseUsageDescription` - Enhanced location (if needed)

## Development

### Code Analysis

```bash
flutter analyze
```

### Run Tests

```bash
flutter test
```

### Clean Build

```bash
flutter clean
flutter pub get
```

## Architecture

The app follows clean architecture principles:

1. **Presentation Layer**: UI screens and widgets
2. **Business Logic Layer**: State management with Riverpod
3. **Data Layer**: API services and local storage
4. **Core Layer**: Utilities, constants, and configuration

## Security Features

- No password storage (token-based auth only)
- Secure token storage using flutter_secure_storage
- HTTPS-only communication (enforced)
- Token refresh mechanism
- Session management
- Biometric authentication support

## API Integration

The app communicates with a FastAPI backend gateway which handles:
- Authentication
- Odoo integration
- Geofence validation
- Attendance processing

Key endpoints:
- `POST /api/v1/auth/login` - User authentication
- `POST /api/v1/attendance/check-in` - Check in
- `POST /api/v1/attendance/check-out` - Check out
- `GET /api/v1/attendance/me` - Get attendance history
- `GET /api/v1/time-off/me` - Get time off info
- `GET /api/v1/geofence/me` - Get authorized geofences

## Building for Production

### Android APK

```bash
flutter build apk --release
```

### Android App Bundle

```bash
flutter build appbundle --release
```

### iOS (macOS only)

```bash
flutter build ios --release
```

## Troubleshooting

### Common Issues

**Flutter SDK not found:**
```bash
export PATH="$PATH:`pwd`/flutter/bin"
```

**Android licenses not accepted:**
```bash
flutter doctor --android-licenses
```

**Location permission denied:**
- Go to device Settings > Apps > GeoFence Attendance > Permissions
- Enable Location permission

**Cannot connect to backend:**
- For Android emulator, use `10.0.2.2` instead of `localhost`
- For physical device, use your computer's LAN IP
- Ensure firewall allows port 8000

### Debug Mode

Run in debug mode for detailed logs:
```bash
flutter run --verbose
```

## Testing

### Unit Tests

```bash
flutter test test/
```

### Widget Tests

Create widget tests in `test/widgets/` directory.

### Integration Tests

For end-to-end testing with backend services.

## Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Run `flutter analyze`
5. Submit a pull request

## License

This project is proprietary and confidential.

## Support

For issues or questions, please contact the development team.
