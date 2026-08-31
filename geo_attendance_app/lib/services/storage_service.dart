import 'package:flutter/foundation.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

class StorageService extends ChangeNotifier {
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage(
    aOptions: AndroidOptions(
      encryptedSharedPreferences: true,
    ),
    iOptions: IOSOptions(
      accessibility: KeychainAccessibility.first_unlock_this_device,
    ),
  );

  static const String _keyOdooUrl = 'odoo_url';
  static const String _keyUsername = 'username';
  static const String _keyApiKey = 'api_key';
  static const String _keyUserId = 'user_id';
  static const String _keyDbName = 'db_name';

  // Getters
  String? get odooUrl => null; // Will be loaded async
  String? get username => null;
  bool get isAuthenticated => false;

  Future<String?> getOdooUrl() async {
    return await _secureStorage.read(key: _keyOdooUrl);
  }

  Future<String?> getUsername() async {
    return await _secureStorage.read(key: _keyUsername);
  }

  Future<String?> getApiKey() async {
    return await _secureStorage.read(key: _keyApiKey);
  }

  Future<String?> getUserId() async {
    return await _secureStorage.read(key: _keyUserId);
  }

  Future<String?> getDbName() async {
    return await _secureStorage.read(key: _keyDbName);
  }

  Future<bool> saveCredentials({
    required String odooUrl,
    required String username,
    required String apiKey,
    required String userId,
    required String dbName,
  }) async {
    try {
      await _secureStorage.write(key: _keyOdooUrl, value: odooUrl);
      await _secureStorage.write(key: _keyUsername, value: username);
      await _secureStorage.write(key: _keyApiKey, value: apiKey);
      await _secureStorage.write(key: _keyUserId, value: userId);
      await _secureStorage.write(key: _keyDbName, value: dbName);
      notifyListeners();
      return true;
    } catch (e) {
      debugPrint('Error saving credentials: $e');
      return false;
    }
  }

  Future<void> clearCredentials() async {
    await _secureStorage.deleteAll();
    notifyListeners();
  }

  Future<bool> hasStoredCredentials() async {
    final url = await _secureStorage.read(key: _keyOdooUrl);
    final apiKey = await _secureStorage.read(key: _keyApiKey);
    return url != null && url.isNotEmpty && apiKey != null && apiKey.isNotEmpty;
  }

  Future<Map<String, String?>> getAllCredentials() async {
    return {
      'odooUrl': await getOdooUrl(),
      'username': await getUsername(),
      'apiKey': await getApiKey(),
      'userId': await getUserId(),
      'dbName': await getDbName(),
    };
  }
}
