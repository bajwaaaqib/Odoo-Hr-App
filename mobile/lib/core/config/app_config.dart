class AppConfig {
  static const String appName = 'GeoFence Attendance';
  static const String appVersion = '1.0.0';
  
  // API Configuration - Will be set during onboarding
  static String? _odooBaseUrl;
  static String? _apiBaseUrl;
  
  // Default development URL
  static const String defaultApiUrl = 'http://10.0.2.2:8000'; // Android emulator
  
  static Future<void> init() async {
    // Initialize configuration from secure storage
    await _loadConfiguration();
  }
  
  static Future<void> _loadConfiguration() async {
    // Load saved server URL from secure storage
    // Implementation in storage service
  }
  
  static String? get odooBaseUrl => _odooBaseUrl;
  static String? get apiBaseUrl => _apiBaseUrl;
  
  static void setServerUrls(String odooUrl, String apiUrl) {
    _odooBaseUrl = odooUrl;
    _apiBaseUrl = apiUrl;
  }
  
  static bool get isConfigured => _odooBaseUrl != null && _apiBaseUrl != null;
}
