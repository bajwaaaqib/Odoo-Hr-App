class ApiConstants {
  // API Endpoints
  static const String authLogin = '/api/v1/auth/login';
  static const String authRefresh = '/api/v1/auth/refresh';
  static const String authLogout = '/api/v1/auth/logout';
  
  static const String meProfile = '/api/v1/me/profile';
  
  static const String employeeMe = '/api/v1/employee/me';
  
  static const String attendanceMe = '/api/v1/attendance/me';
  static const String attendanceToday = '/api/v1/attendance/today';
  static const String attendanceCheckIn = '/api/v1/attendance/check-in';
  static const String attendanceCheckOut = '/api/v1/attendance/check-out';
  
  static const String timeOffMe = '/api/v1/time-off/me';
  
  static const String geofenceMe = '/api/v1/geofence/me';
  
  static const String devicesList = '/api/v1/devices';
  static const String devicesRegister = '/api/v1/devices/register';
  static const String devicesRevoke = '/api/v1/devices/revoke';
  
  // Timeout configurations
  static const int connectionTimeoutMs = 30000;
  static const int receiveTimeoutMs = 30000;
  
  // HTTP Status codes
  static const int unauthorizedStatus = 401;
  static const int forbiddenStatus = 403;
  static const int notFoundStatus = 404;
  static const int conflictStatus = 409;
}
