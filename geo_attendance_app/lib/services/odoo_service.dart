import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:crypto/crypto.dart';
import 'storage_service.dart';

class OdooService extends ChangeNotifier {
  final StorageService _storageService;
  
  bool _isLoading = false;
  bool _isAuthenticated = false;
  String? _errorMessage;

  bool get isLoading => _isLoading;
  bool get isAuthenticated => _isAuthenticated;
  String? get errorMessage => _errorMessage;

  OdooService(this._storageService) {
    _checkAuthStatus();
  }

  Future<void> _checkAuthStatus() async {
    _isLoading = true;
    notifyListeners();

    try {
      final hasCredentials = await _storageService.hasStoredCredentials();
      if (hasCredentials) {
        // Validate credentials by making a test call
        final credentials = await _storageService.getAllCredentials();
        final isValid = await _validateCredentials(
          odooUrl: credentials['odooUrl']!,
          apiKey: credentials['apiKey']!,
          userId: credentials['userId']!,
          dbName: credentials['dbName']!,
        );
        _isAuthenticated = isValid;
      }
    } catch (e) {
      debugPrint('Error checking auth status: $e');
      _isAuthenticated = false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> _validateCredentials({
    required String odooUrl,
    required String apiKey,
    required String userId,
    required String dbName,
  }) async {
    try {
      final response = await _makeOdooCall(
        odooUrl: odooUrl,
        method: 'get',
        model: 'res.users',
        ids: [int.tryParse(userId) ?? 0],
        apiKey: apiKey,
        dbName: dbName,
      );
      return response != null;
    } catch (e) {
      debugPrint('Credential validation failed: $e');
      return false;
    }
  }

  Future<Map<String, dynamic>?> login({
    required String odooUrl,
    required String username,
    required String password,
  }) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      // Normalize Odoo URL
      final normalizedUrl = _normalizeOdooUrl(odooUrl);

      // Step 1: Authenticate and get API Key
      final authResult = await _authenticate(
        odooUrl: normalizedUrl,
        username: username,
        password: password,
      );

      if (authResult == null) {
        throw Exception('Authentication failed. Please check your credentials.');
      }

      // Step 2: Get User Info
      final userInfo = await _getUserInfo(
        odooUrl: normalizedUrl,
        username: username,
        apiKey: authResult['apiKey'],
        dbName: authResult['dbName'],
        userId: authResult['userId'],
      );

      if (userInfo == null) {
        throw Exception('Failed to retrieve user information.');
      }

      // Step 3: Save credentials securely
      final saved = await _storageService.saveCredentials(
        odooUrl: normalizedUrl,
        username: username,
        apiKey: authResult['apiKey'],
        userId: authResult['userId'].toString(),
        dbName: authResult['dbName'],
      );

      if (!saved) {
        throw Exception('Failed to save credentials securely.');
      }

      _isAuthenticated = true;
      return userInfo;
    } catch (e) {
      _errorMessage = e.toString().replaceFirst('Exception: ', '');
      rethrow;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  String _normalizeOdooUrl(String url) {
    String normalized = url.trim();
    
    // Remove protocol if present
    if (normalized.startsWith('https://')) {
      normalized = normalized.substring(8);
    } else if (normalized.startsWith('http://')) {
      normalized = normalized.substring(7);
    }
    
    // Remove trailing slashes
    normalized = normalized.replaceAll(RegExp(r'/+$'), '');
    
    // Ensure no path is included (only domain)
    if (normalized.contains('/')) {
      normalized = normalized.split('/').first;
    }
    
    return normalized;
  }

  Future<Map<String, dynamic>?> _authenticate({
    required String odooUrl,
    required String username,
    required String password,
  }) async {
    try {
      // Use Odoo's JSON-RPC API for authentication
      final uri = Uri.https(odooUrl, '/web/session/authenticate');
      
      final response = await http.post(
        uri,
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'jsonrpc': '2.0',
          'method': 'call',
          'params': {
            'db': '', // Will be determined by Odoo
            'login': username,
            'password': password,
          },
          'id': DateTime.now().millisecondsSinceEpoch,
        }),
      ).timeout(const Duration(seconds: 30));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        
        if (data['result'] != null && data['result']['uid'] != null) {
          final result = data['result'];
          
          // For Odoo Online (SaaS), we need to extract the database name
          // The session contains the db name
          final dbName = result['db']?.toString() ?? '';
          final userId = result['uid'].toString();
          
          // Generate a secure API key hash for local storage
          // In production, you'd use Odoo's actual API keys
          final apiKeyHash = _generateApiKeyHash(username, password, dbName);
          
          return {
            'apiKey': apiKeyHash,
            'userId': userId,
            'dbName': dbName,
            'companyId': result['company_id'],
          };
        }
      }
      
      return null;
    } catch (e) {
      debugPrint('Authentication error: $e');
      return null;
    }
  }

  String _generateApiKeyHash(String username, String password, String dbName) {
    // Create a secure hash of credentials for API calls
    // This is NOT the actual password, but a derived key
    final content = '$username:$dbName:${DateTime.now().millisecondsSinceEpoch}';
    return sha256.convert(utf8.encode(content)).toString();
  }

  Future<Map<String, dynamic>?> _getUserInfo({
    required String odooUrl,
    required String username,
    required String apiKey,
    required String dbName,
    required String userId,
  }) async {
    try {
      final result = await _makeOdooCall(
        odooUrl: odooUrl,
        method: 'read',
        model: 'res.users',
        ids: [int.tryParse(userId) ?? 0],
        apiKey: apiKey,
        dbName: dbName,
        fields: ['id', 'name', 'email', 'company_id', 'image_1920'],
      );

      if (result != null && result is List && result.isNotEmpty) {
        return {
          'id': result[0]['id'],
          'name': result[0]['name'],
          'email': result[0]['email'],
          'companyId': result[0]['company_id']?[1] ?? result[0]['company_id'],
          'image': result[0]['image_1920'],
        };
      }
      
      return null;
    } catch (e) {
      debugPrint('Get user info error: $e');
      return null;
    }
  }

  Future<dynamic> _makeOdooCall({
    required String odooUrl,
    required String method,
    required String model,
    required List<int> ids,
    required String apiKey,
    required String dbName,
    List<String>? fields,
  }) async {
    try {
      final uri = Uri.https(odooUrl, '/web/dataset/call_kw');
      
      final params = {
        'model': model,
        'method': method,
        'args': [ids],
        'kwargs': {},
      };

      if (fields != null && fields.isNotEmpty) {
        params['kwargs']['fields'] = fields;
      }

      final response = await http.post(
        uri,
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'jsonrpc': '2.0',
          'method': 'call',
          'params': params,
          'id': DateTime.now().millisecondsSinceEpoch,
        }),
      ).timeout(const Duration(seconds: 30));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data['result'];
      }
      
      return null;
    } catch (e) {
      debugPrint('Odoo call error: $e');
      rethrow;
    }
  }

  Future<void> logout() async {
    await _storageService.clearCredentials();
    _isAuthenticated = false;
    notifyListeners();
  }

  // Mark attendance
  Future<Map<String, dynamic>?> markAttendance({
    required double latitude,
    required double longitude,
    required String attendanceType, // 'check_in' or 'check_out'
  }) async {
    try {
      final credentials = await _storageService.getAllCredentials();
      final odooUrl = credentials['odooUrl'];
      final apiKey = credentials['apiKey'];
      final dbName = credentials['dbName'];
      final userId = credentials['userId'];

      if (odooUrl == null || apiKey == null || dbName == null || userId == null) {
        throw Exception('Not authenticated');
      }

      // Create attendance record in Odoo
      final uri = Uri.https(odooUrl, '/web/dataset/call_kw');
      
      final response = await http.post(
        uri,
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'jsonrpc': '2.0',
          'method': 'call',
          'params': {
            'model': 'hr.attendance',
            'method': 'create',
            'args': [[]],
            'kwargs': {
              'employee_id': int.tryParse(userId) ?? 0,
              'check_in': attendanceType == 'check_in' 
                  ? DateTime.now().toIso8601String() 
                  : null,
              'check_out': attendanceType == 'check_out' 
                  ? DateTime.now().toIso8601String() 
                  : null,
              'latitude': latitude,
              'longitude': longitude,
            },
          },
          'id': DateTime.now().millisecondsSinceEpoch,
        }),
      ).timeout(const Duration(seconds: 30));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data['result'];
      }
      
      return null;
    } catch (e) {
      debugPrint('Mark attendance error: $e');
      rethrow;
    }
  }
}
