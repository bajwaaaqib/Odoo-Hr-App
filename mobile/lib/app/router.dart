import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

// Import screens (placeholders for now)
import '../features/onboarding/screens/splash_screen.dart';
import '../features/onboarding/screens/server_setup_screen.dart';
import '../features/authentication/screens/login_screen.dart';
import '../features/home/screens/home_screen.dart';
import '../features/attendance/screens/attendance_history_screen.dart';
import '../features/time_off/screens/time_off_screen.dart';
import '../features/profile/screens/profile_screen.dart';

class AppRouter {
  static final GoRouter router = GoRouter(
    initialLocation: '/splash',
    routes: [
      // Onboarding Flow
      GoRoute(
        path: '/splash',
        name: 'splash',
        builder: (context, state) => const SplashScreen(),
      ),
      GoRoute(
        path: '/server-setup',
        name: 'server-setup',
        builder: (context, state) => const ServerSetupScreen(),
      ),
      GoRoute(
        path: '/login',
        name: 'login',
        builder: (context, state) => const LoginScreen(),
      ),
      
      // Main App Flow
      GoRoute(
        path: '/home',
        name: 'home',
        builder: (context, state) => const HomeScreen(),
      ),
      GoRoute(
        path: '/attendance-history',
        name: 'attendance-history',
        builder: (context, state) => const AttendanceHistoryScreen(),
      ),
      GoRoute(
        path: '/time-off',
        name: 'time-off',
        builder: (context, state) => const TimeOffScreen(),
      ),
      GoRoute(
        path: '/profile',
        name: 'profile',
        builder: (context, state) => const ProfileScreen(),
      ),
    ],
  );
}
