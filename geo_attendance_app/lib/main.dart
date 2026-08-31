import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'services/odoo_service.dart';
import 'services/storage_service.dart';
import 'screens/login_screen.dart';
import 'screens/home_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const GeoAttendanceApp());
}

class GeoAttendanceApp extends StatelessWidget {
  const GeoAttendanceApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => StorageService()),
        ChangeNotifierProvider(
          create: (context) => OdooService(
            context.read<StorageService>(),
          ),
        ),
      ],
      child: MaterialApp(
        title: 'Geo Attendance',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
          useMaterial3: true,
        ),
        home: const AuthWrapper(),
      ),
    );
  }
}

class AuthWrapper extends StatelessWidget {
  const AuthWrapper({super.key});

  @override
  Widget build(BuildContext context) {
    final odooService = context.watch<OdooService>();

    if (odooService.isLoading) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (odooService.isAuthenticated) {
      return const HomeScreen();
    }

    return const LoginScreen();
  }
}
