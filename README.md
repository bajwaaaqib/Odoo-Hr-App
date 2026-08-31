# GeoFence Attendance Mobile App — Product & Technical Requirements

## 1. Product Vision

Build a secure mobile attendance application that connects to an Odoo instance and allows an authorized user to:

- Enter their Odoo server URL.
- Authenticate securely.
- View their employee attendance status.
- Check in / check out using GPS + geofencing.
- View attendance history.
- View relevant time-off information.
- Work with Odoo Employees, Attendance, and Time Off data.
- Support Odoo internal users and, where explicitly enabled by the Odoo integration, portal/contact-based users.

**Core principle:** Odoo remains the system of record. The mobile application must not independently decide employment status, leave entitlement, attendance totals, or payroll values.

---

# 2. Recommended Architecture

Use a mobile-first architecture with a small security-focused backend/gateway rather than allowing the mobile app to directly store Odoo credentials.

```text
                         ┌──────────────────────────┐
                         │       Odoo Server        │
                         │                          │
                         │ Employees               │
                         │ Attendance              │
                         │ Time Off                │
                         │ Users / Access Rights   │
                         │ GeoFence Configuration  │
                         └────────────┬─────────────┘
                                      │
                           Odoo API / HTTPS
                                      │
                                      ▼
┌──────────────────────┐     ┌──────────────────────┐
│   Mobile Application │────▶│ Secure API Gateway   │
│                      │ TLS │ / Integration Layer  │
│ Flutter              │     │                      │
│ GPS                  │     │ Auth / Token Control  │
│ Geofence             │     │ Odoo API Adapter     │
│ Secure Storage       │     │ Rate Limiting        │
│ Biometric Lock       │     │ Audit / Security     │
└──────────────────────┘     └──────────┬───────────┘
                                        │
                                        ▼
                               ┌──────────────────┐
                               │ Secure Secrets / │
                               │ Config Store     │
                               └──────────────────┘
```

### Important security decision

**Do not store an Odoo administrator password, API key, or long-lived Odoo credential inside the mobile application.**

Preferred authentication models:

1. OAuth/OIDC/SSO if the Odoo deployment supports an appropriate identity provider.
2. A dedicated integration account/token with minimum required permissions.
3. A secure backend gateway that holds integration credentials.
4. Short-lived mobile access tokens with refresh-token rotation.

For deployments where direct Odoo authentication is mandatory, use the minimum possible credential scope and never log credentials.

---

# 3. Recommended Technology Stack

## Mobile

**Flutter + Dart**

Reasons:

- Android and iOS from one codebase.
- Strong ecosystem for GPS/location.
- Good background execution support.
- Good secure-storage libraries.
- Suitable for enterprise applications.

Recommended packages/components:

- `go_router` — navigation
- `dio` — HTTPS/API client
- `flutter_secure_storage` — secure token storage
- `geolocator` — location
- `permission_handler` — runtime permissions
- local encrypted database/cache
- biometric authentication package
- device/app integrity checks where available

Do not blindly depend on a package for security. Pin versions and review dependencies.

## Backend

Recommended:

- Python
- FastAPI
- PostgreSQL
- Redis
- HTTPS/TLS
- Docker
- Nginx or a managed API gateway
- Secret manager / environment-based secret injection

The backend should be stateless wherever possible.

## Odoo

Support:

- Odoo 18 Community
- Odoo 18 Enterprise
- Future Odoo versions through an adapter layer

Create a dedicated Odoo custom module:

```text
geo_attendance/
```

The module exposes only the operations required by the application.

---

# 4. Odoo Integration Responsibilities

The Odoo module should be responsible for:

- Mapping authenticated Odoo users to employees.
- Checking employee status.
- Reading attendance configuration.
- Creating/checking attendance records.
- Reading attendance history.
- Reading approved/relevant time-off information.
- Reading allowed geofences.
- Enforcing server-side authorization.
- Recording security/audit events.

Suggested Odoo models:

```text
res.users
hr.employee
hr.attendance
hr.leave
geo.attendance.zone
geo.attendance.device
geo.attendance.event
```

---

# 5. Functional Requirements

## FR-01 — Server Setup

User opens the app for the first time.

Flow:

```text
Install App
   │
   ▼
Welcome
   │
   ▼
Enter Odoo URL
   │
   ▼
Validate HTTPS + Odoo endpoint
   │
   ▼
Authentication
```

Requirements:

- Accept HTTPS URLs.
- Reject insecure HTTP by default.
- Normalize trailing slashes.
- Validate hostname.
- Prevent arbitrary URL schemes such as `file://`, `javascript:`, etc.
- Do not fetch arbitrary endpoints supplied by the user.
- Display the organization/server name after successful validation.

---

# 6. Authentication

## Preferred flow

```text
Mobile
  │
  │ Login
  ▼
Secure Gateway
  │
  │ authenticate
  ▼
Identity / Odoo
  │
  ▼
Short-lived access token
  │
  ▼
Mobile Secure Storage
```

Requirements:

- Password must never be stored locally.
- Access token stored only in OS secure storage.
- Refresh token rotated.
- Logout invalidates sessions.
- Support multiple devices.
- Device sessions should be visible/revocable.
- Rate-limit authentication.
- Lock/slow repeated failed attempts.
- Never return whether an employee exists before authentication.

---

# 7. Employee Resolution

After authentication:

```text
Authenticated User
        │
        ▼
Odoo User
        │
        ▼
Employee Mapping
        │
        ├── Employee found ──▶ Continue
        │
        └── Not found ───────▶ Restricted state
```

The application must not allow the user to choose another employee manually.

Server must derive the employee from the authenticated identity.

---

# 8. Home Screen

Example:

```text
┌──────────────────────────────┐
│ Good Morning, Ahmed          │
│ Sales Department             │
├──────────────────────────────┤
│                              │
│       NOT CHECKED IN         │
│                              │
│   Current Location           │
│   ● Location Available       │
│                              │
│       [ CHECK IN ]           │
│                              │
├──────────────────────────────┤
│ Today's Attendance            │
│ 08:42 Check In               │
│                              │
│ Time Off                     │
│ Annual Leave: 5 days         │
│                              │
│ History                      │
└──────────────────────────────┘
```

---

# 9. Geofencing Requirements

Geofencing must be treated as a **security signal**, not as proof of physical presence by itself.

A check-in should require:

```text
Authenticated User
        +
Authorized Employee
        +
Valid Device Session
        +
Location Permission
        +
Fresh GPS Location
        +
Acceptable Accuracy
        +
Inside Authorized Zone
        +
Server Validation
        +
Odoo Validation
```

## Location checks

Capture:

- latitude
- longitude
- accuracy
- timestamp
- altitude if available
- speed if available
- device identifier/session identifier
- app version
- OS version
- location provider information where available

Never trust client-provided `inside_geofence=true`.

The server must calculate whether the coordinates are inside the authorized zone.

---

# 10. Geofence Model

Recommended Odoo model:

```text
geo.attendance.zone

id
name
active
latitude
longitude
radius_meters
company_id
allowed_employee_ids
allowed_department_ids
allowed_location_ids
effective_from
effective_to
```

For MVP, use a circular geofence:

```text
             100 m
        .-------------.
      .'               '.
     /        ●          \
    |       OFFICE        |
     \                   /
      '.               .'
        '-------------'

              ●
          Employee GPS
```

Future versions can support:

- Polygon geofences
- Multiple zones
- Site/branch-specific zones
- Department-specific zones
- Employee-specific zones
- Temporary zones

---

# 11. Geofence Validation Algorithm

Conceptual flow:

```text
GPS Coordinates
      │
      ▼
Accuracy Check
      │
      ├── Poor ──▶ Reject / Ask user to improve GPS
      │
      ▼
Freshness Check
      │
      ▼
Server obtains active zone
      │
      ▼
Calculate distance
      │
      ▼
distance <= radius?
      │
   ┌──┴───┐
   │      │
 YES      NO
   │      │
   ▼      ▼
Allow   Reject
```

Important:

- Account for GPS accuracy.
- Do not simply use a fixed 10m threshold.
- Avoid accepting stale cached locations.
- Define a maximum accepted location age.
- Consider device mock-location detection, but do not treat it as perfect.
- Server must remain authoritative.

---

# 12. Attendance Check-In

```text
User presses CHECK IN
        │
        ▼
Biometric/device confirmation (optional policy)
        │
        ▼
Request fresh location
        │
        ▼
Client basic validation
        │
        ▼
Secure API request
        │
        ▼
Gateway authenticates token
        │
        ▼
Resolve employee
        │
        ▼
Retrieve active geofence
        │
        ▼
Server distance calculation
        │
        ▼
Check Odoo attendance state
        │
        ▼
Create attendance in Odoo
        │
        ▼
Return authoritative result
```

The client must never directly decide:

```text
"Attendance created successfully"
```

until the server confirms it.

---

# 13. Check-Out

Same security model:

```text
CHECK OUT
   │
   ▼
Fresh Location
   │
   ▼
Authenticated Session
   │
   ▼
Server Geofence Validation
   │
   ▼
Odoo Current Attendance
   │
   ▼
Close Attendance
   │
   ▼
Return Odoo Result
```

Business rules such as "checkout only from office" should be configurable.

---

# 14. Time Off

The application should initially be read-oriented.

Display:

- Pending requests
- Approved leave
- Leave type
- Start date
- End date
- Duration
- Status

Future:

- Request leave
- Cancel request
- Attach documents
- Approval workflow

Do not duplicate Odoo leave calculations in the mobile app.

---

# 15. Attendance History

Display:

```text
Date        Check In   Check Out   Duration
------------------------------------------------
29 Aug      08:42      17:31       8h 49m
28 Aug      08:37      17:22       8h 45m
27 Aug      08:51      17:40       8h 49m
```

The values should come from Odoo.

---

# 16. Offline Strategy

MVP recommendation:

**Do not allow offline attendance creation.**

Reason:

Offline GPS attendance creates difficult security problems:

- Device clock manipulation
- Stale location
- Delayed submissions
- Replay attacks
- Duplicate attendance
- Fake location history

Instead:

```text
No Internet
    │
    ▼
"Attendance requires an internet connection"
```

Future controlled offline mode can use signed, nonce-based event queues, but it should not be part of MVP.

---

# 17. Security Architecture

## Threat model

Assume the attacker can:

- Reverse engineer APK/IPA.
- Modify the mobile app.
- Intercept their own device traffic.
- Manipulate GPS.
- Change device time.
- Replay API requests.
- Steal a refresh token.
- Attempt another employee ID.
- Call APIs without the official app.
- Send malformed Odoo URLs.
- Abuse rate limits.
- Root/jailbreak the device.

Never rely on the mobile application for authorization.

---

# 18. API Security

All API endpoints require authentication except tightly controlled bootstrap/health endpoints.

Recommended:

```text
Authorization: Bearer <short-lived-token>
```

Use:

- TLS 1.2+
- Short access-token lifetime
- Refresh-token rotation
- Token revocation
- Rate limiting
- Request size limits
- Schema validation
- Replay protection for attendance mutation requests
- Idempotency keys
- Audit logging

Example:

```http
POST /api/v1/attendance/check-in
Authorization: Bearer <token>
Idempotency-Key: <random-uuid>
```

The server should reject a reused idempotency key for a conflicting request.

---

# 19. Token Security

Recommended:

```text
Access Token
   │
   ├── Short lifetime
   └── Minimal claims

Refresh Token
   │
   ├── Secure storage
   ├── Rotation
   └── Revocation
```

Never place secrets in:

- Dart source
- Android resources
- iOS plist
- Git
- `.env` bundled into the application
- screenshots
- logs

A mobile app cannot keep a permanent secret truly secret.

---

# 20. Odoo API Security

Create a dedicated Odoo integration layer.

Never give the mobile application unrestricted Odoo API access.

Bad:

```text
Mobile ───────────────▶ Odoo unrestricted API
```

Preferred:

```text
Mobile
   │
   ▼
Attendance API
   │
   ▼
Odoo Adapter
   │
   ▼
Specific Odoo operations
```

Allow only operations such as:

```text
GET employee/me
GET attendance/me
POST attendance/check-in
POST attendance/check-out
GET time-off/me
GET geofences/me
```

Do not expose generic:

```text
/search_read
/create
/write
/unlink
```

to the mobile application.

---

# 21. Odoo Access Control

The Odoo module must implement:

- `ir.model.access`
- Record rules
- Company restrictions
- Employee ownership checks
- Active employee checks
- Explicit API permissions

Example principle:

```text
Authenticated User A
       │
       ▼
Employee A
       │
       ├── Attendance A ✓
       ├── Time Off A ✓
       └── Employee B ✗
```

A malicious request such as:

```json
{
  "employee_id": 12345
}
```

must never override the employee derived from the authenticated session.

---

# 22. API Request Model

Do not accept sensitive authorization fields from the client.

Bad:

```json
{
  "employee_id": 25,
  "company_id": 2,
  "inside_geofence": true
}
```

Better:

```json
{
  "latitude": 25.2048,
  "longitude": 55.2708,
  "accuracy": 12.4,
  "captured_at": "2026-08-29T08:42:12Z",
  "client_request_id": "uuid"
}
```

Server derives:

```text
user
employee
company
geofence
attendance state
authorization
```

---

# 23. Anti-Replay Protection

Every mutation request should contain:

- Unique request ID
- Timestamp
- Access token
- Idempotency key

Server validates:

```text
timestamp within allowed window
        +
request ID not previously processed
        +
token valid
        +
idempotency key unused
```

This reduces replay attacks.

---

# 24. Device Security

Store only necessary information.

Recommended device record:

```text
device_id
user_id
platform
app_version
last_seen_at
created_at
revoked_at
```

Do not use IMEI or other invasive hardware identifiers unless there is a strong legal/business requirement.

Optional enterprise controls:

- Root detection
- Jailbreak detection
- App integrity checks
- Device attestation
- Emulator detection

These should be risk signals, not the sole security mechanism.

---

# 25. Privacy

Location is sensitive personal data.

Requirements:

- Collect location only when necessary.
- Explain why location is required.
- Do not continuously track employees unless explicitly required and legally justified.
- For attendance, prefer point-in-time location capture.
- Define retention periods.
- Restrict administrator access.
- Encrypt data in transit and at rest.
- Provide an audit trail.
- Follow applicable UAE privacy/data-protection requirements and the organization's employment policies.

MVP recommendation:

**Capture location at Check-In and Check-Out only.**

Do not implement continuous employee tracking.

---

# 26. Audit Logging

Log security-relevant events:

```text
LOGIN_SUCCESS
LOGIN_FAILURE
LOGOUT
TOKEN_REFRESH
DEVICE_REGISTERED
DEVICE_REVOKED
LOCATION_PERMISSION_DENIED
CHECK_IN_ATTEMPT
CHECK_IN_ACCEPTED
CHECK_IN_REJECTED
CHECK_OUT_ATTEMPT
CHECK_OUT_ACCEPTED
CHECK_OUT_REJECTED
GEOFENCE_FAILED
ATTENDANCE_CREATED
ATTENDANCE_FAILED
SUSPICIOUS_REQUEST
```

Never log:

- passwords
- access tokens
- refresh tokens
- API keys
- full authentication headers

Location logs should be minimized and retention-controlled.

---

# 27. Error Handling

Never reveal sensitive internal information.

Bad:

```text
Odoo database error:
psycopg2...
```

Better:

```text
Unable to complete attendance.
Please try again or contact your administrator.
```

For geofence:

```text
You are outside the authorized attendance area.
```

For GPS accuracy:

```text
Location accuracy is too low.
Please move to an open area and try again.
```

---

# 28. Core Screens

```text
Splash
  │
  ▼
Server Setup
  │
  ▼
Login
  │
  ▼
Security / Permissions
  │
  ▼
Home
  ├── Check In
  ├── Check Out
  ├── Attendance History
  ├── Time Off
  ├── Profile
  └── Settings
```

---

# 29. Suggested Mobile UI

## Home

```text
┌─────────────────────────────┐
│ Company                     │
│ Ahmed                       │
├─────────────────────────────┤
│                             │
│       ● CHECKED IN          │
│                             │
│       08:42 AM              │
│                             │
│    [ CHECK OUT ]             │
│                             │
├─────────────────────────────┤
│ Location                    │
│ ✓ Office Zone               │
│ Accuracy: 8m                │
├─────────────────────────────┤
│ Today's Time: 8h 12m        │
└─────────────────────────────┘
```

---

# 30. Backend API Structure

```text
/api/v1
    /auth
        POST /login
        POST /refresh
        POST /logout

    /me
        GET /profile

    /employee
        GET /me

    /attendance
        GET  /me
        GET  /today
        POST /check-in
        POST /check-out

    /time-off
        GET /me

    /geofence
        GET /me

    /devices
        GET /
        POST /register
        POST /revoke
```

Keep API versioning from day one.

---

# 31. Backend Project Structure

```text
backend/
├── app/
│   ├── main.py
│   │
│   ├── api/
│   │   └── v1/
│   │       ├── auth.py
│   │       ├── employee.py
│   │       ├── attendance.py
│   │       ├── time_off.py
│   │       ├── geofence.py
│   │       └── devices.py
│   │
│   ├── core/
│   │   ├── config.py
│   │   ├── security.py
│   │   ├── rate_limit.py
│   │   ├── logging.py
│   │   └── exceptions.py
│   │
│   ├── models/
│   │   ├── device.py
│   │   ├── session.py
│   │   └── audit.py
│   │
│   ├── schemas/
│   │   ├── auth.py
│   │   ├── attendance.py
│   │   ├── employee.py
│   │   └── geofence.py
│   │
│   ├── services/
│   │   ├── auth_service.py
│   │   ├── attendance_service.py
│   │   ├── geofence_service.py
│   │   ├── employee_service.py
│   │   └── device_service.py
│   │
│   ├── integrations/
│   │   └── odoo/
│   │       ├── client.py
│   │       ├── auth.py
│   │       ├── employee.py
│   │       ├── attendance.py
│   │       ├── time_off.py
│   │       └── geofence.py
│   │
│   └── db/
│       ├── database.py
│       └── migrations/
│
├── tests/
│   ├── unit/
│   ├── integration/
│   ├── security/
│   └── api/
│
├── Dockerfile
├── docker-compose.yml
├── pyproject.toml
└── README.md
```

---

# 32. Flutter Project Structure

```text
mobile/
├── lib/
│   ├── main.dart
│   │
│   ├── app/
│   │   ├── app.dart
│   │   ├── router.dart
│   │   └── theme.dart
│   │
│   ├── core/
│   │   ├── config/
│   │   ├── constants/
│   │   ├── errors/
│   │   ├── network/
│   │   ├── security/
│   │   ├── storage/
│   │   └── utils/
│   │
│   ├── features/
│   │   ├── onboarding/
│   │   ├── authentication/
│   │   ├── home/
│   │   ├── attendance/
│   │   ├── geofence/
│   │   ├── time_off/
│   │   ├── profile/
│   │   └── devices/
│   │
│   └── shared/
│       ├── widgets/
│       ├── models/
│       └── services/
│
├── test/
├── android/
├── ios/
├── pubspec.yaml
└── README.md
```

---

# 33. Odoo Module Structure

```text
geo_attendance/
├── __init__.py
├── __manifest__.py
│
├── models/
│   ├── __init__.py
│   ├── geo_attendance_zone.py
│   ├── geo_attendance_device.py
│   └── geo_attendance_event.py
│
├── controllers/
│   ├── __init__.py
│   └── api.py
│
├── security/
│   ├── ir.model.access.csv
│   └── security.xml
│
├── views/
│   ├── geo_zone_views.xml
│   ├── device_views.xml
│   └── event_views.xml
│
├── data/
│   └── data.xml
│
└── tests/
    ├── test_attendance.py
    ├── test_geofence.py
    └── test_security.py
```

---

# 34. Security Boundaries

```text
                 UNTRUSTED
┌─────────────────────────────────┐
│ Mobile Device                   │
│                                 │
│ User-controlled environment     │
│ GPS can be manipulated           │
│ App can be modified              │
└───────────────┬─────────────────┘
                │
                │ TLS
                ▼
┌─────────────────────────────────┐
│ Secure API Gateway              │
│                                 │
│ Authentication                  │
│ Authorization                   │
│ Rate limiting                   │
│ Request validation              │
│ Replay protection               │
│ Audit                           │
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│ Odoo Integration Adapter        │
│                                 │
│ Least privilege                 │
│ Employee mapping                │
│ Business rules                  │
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│ Odoo                            │
│                                 │
│ Final source of truth           │
└─────────────────────────────────┘
                 TRUSTED
```

---

# 35. Important Security Rule

The following values must NEVER be trusted from the mobile client:

```text
employee_id
user_id
company_id
attendance_id
inside_geofence
is_manager
is_admin
attendance_time
leave_balance
leave_approval_status
```

The server/Odoo must derive or validate them.

---

# 36. Geofence Security Limitations

GPS cannot provide cryptographic proof that a person is physically present.

A determined attacker may use:

- Mock GPS
- Rooted device
- Jailbroken device
- Location spoofing
- Modified application
- GPS signal manipulation

Therefore use layered controls:

```text
Authentication
+
Device/session binding
+
Fresh GPS
+
Accuracy
+
Server-side geofence
+
Odoo authorization
+
Replay protection
+
Optional device attestation
+
Audit trail
```

For high-security organizations, consider an additional physical-presence factor such as:

- NFC
- BLE beacon
- QR code rotated periodically
- Wi-Fi/network validation
- kiosk verification

These should be future security enhancements rather than dependencies for the first MVP.

---

# 37. Testing Strategy

## Unit tests

Test:

- Distance calculations
- Geofence boundary
- Token validation
- Authorization
- Employee mapping
- Attendance state
- Idempotency
- Timestamp validation

## Security tests

Test:

- Employee ID manipulation
- Company ID manipulation
- Token replay
- Expired token
- Revoked token
- Duplicate check-in
- Duplicate check-out
- Invalid GPS
- Mock-location signals
- API rate abuse
- SQL injection
- NoSQL injection where relevant
- SSRF through Odoo URL configuration
- Malformed JSON
- Oversized requests
- Broken access control

## Integration tests

```text
Flutter/API
     │
     ▼
Gateway
     │
     ▼
Test Odoo
     │
     ▼
Attendance
```

---

# 38. SSRF Protection

Because the user enters an Odoo URL, the server must treat that URL as untrusted.

This is a major security area.

Do not allow the backend to request arbitrary internal addresses.

Block:

```text
localhost
127.0.0.1
0.0.0.0
169.254.169.254
private RFC1918 ranges
link-local addresses
internal DNS targets
file://
ftp://
```

Also:

- Resolve DNS safely.
- Validate redirects.
- Re-check destination after redirects.
- Restrict allowed schemes to HTTPS.
- Consider an organization/domain allowlist for enterprise deployments.
- Prevent DNS rebinding attacks.

---

# 39. Secret Management

Production:

```text
                    Secret Manager
                         │
             ┌───────────┴───────────┐
             ▼                       ▼
       Backend secrets          Odoo integration
```

Never commit:

```text
API_KEY=
PASSWORD=
JWT_SECRET=
ODOO_SECRET=
DATABASE_PASSWORD=
```

into Git.

---

# 40. Database Security

PostgreSQL:

- Encryption at rest where available.
- Separate database user.
- Minimum privileges.
- No application use of PostgreSQL superuser.
- Parameterized queries.
- Migration control.
- Backup encryption.
- Restore testing.
- Audit access to sensitive data.

---

# 41. Deployment

Recommended production architecture:

```text
                    Internet
                       │
                       ▼
                  WAF / CDN
                       │
                       ▼
                 Load Balancer
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
        API Instance 1      API Instance 2
             │                   │
             └─────────┬─────────┘
                       ▼
                    Redis
                       │
                       ▼
                 PostgreSQL
                       │
                       ▼
                Odoo Connector
                       │
                       ▼
                     Odoo
```

MVP can start with:

```text
1 API
1 PostgreSQL
1 Redis
1 Odoo
```

and scale later.

---

# 42. MVP Scope

## Version 1

### Mobile

- Server URL
- Login
- Secure session
- Employee profile
- GPS permission
- Check-in
- Check-out
- Geofence validation
- Today's attendance
- Attendance history
- Time-off read-only
- Logout
- Device/session management

### Odoo

- Employee mapping
- Geofence configuration
- Attendance API
- Attendance validation
- Time-off API
- Security rules
- Audit events

### Backend

- Authentication
- Token management
- Odoo adapter
- Geofence calculation
- Rate limiting
- Replay protection
- Audit logging
- Device registration

---

# 43. Version 2

- Leave request
- Leave cancellation
- Push notifications
- Multiple geofences
- Branch/site selection
- Manager dashboard
- Attendance correction requests
- Admin device management
- Advanced reports
- QR/NFC/BLE verification

---

# 44. Version 3

- Polygon geofencing
- Device attestation
- Advanced fraud detection
- Attendance anomaly detection
- Offline signed events
- Multi-Odoo tenant management
- Enterprise SSO
- Organization-level policy engine

---

# 45. Multi-Tenant Consideration

If this becomes a SaaS product for multiple companies:

```text
Tenant A ──┐
Tenant B ──┼──▶ Secure Platform ──▶ Their Odoo
Tenant C ──┘
```

Every request must carry a server-side tenant context.

Never trust:

```text
tenant_id
company_id
database_name
```

from the client.

Tenant must be resolved from a trusted server-side configuration/session.

---

# 46. Recommended Development Order

## Phase 1 — Odoo

1. Create `geo_attendance` module.
2. Create geofence model.
3. Create employee mapping.
4. Implement secure attendance endpoints.
5. Implement time-off endpoint.
6. Implement access rights.
7. Add tests.

## Phase 2 — Backend

1. FastAPI project.
2. Authentication.
3. Odoo adapter.
4. Token/session management.
5. Geofence service.
6. Attendance service.
7. Rate limiting.
8. Audit system.
9. Security tests.

## Phase 3 — Mobile

1. Flutter shell.
2. Server configuration.
3. Authentication.
4. Secure storage.
5. Home screen.
6. Location permissions.
7. Check-in.
8. Check-out.
9. History.
10. Time off.
11. Device management.

## Phase 4 — Security Hardening

1. Threat modeling.
2. API penetration testing.
3. SSRF testing.
4. Broken access-control testing.
5. Replay testing.
6. GPS spoofing testing.
7. Root/jailbreak testing.
8. Dependency audit.
9. Secrets audit.
10. Production security review.

---

# 47. Definition of Done — Check-In

A check-in is considered successful only when:

```text
✓ User authenticated
✓ Token valid
✓ Session active
✓ Device allowed
✓ Employee resolved server-side
✓ Employee active
✓ Location permission granted
✓ Fresh GPS captured
✓ Accuracy acceptable
✓ Server geofence validation passed
✓ Attendance state valid
✓ Request not replayed
✓ Idempotency validated
✓ Odoo accepted attendance
✓ Audit event created
✓ Mobile receives authoritative success response
```

---

# 48. Example End-to-End Sequence Diagram

```text
User        Mobile       Gateway       Odoo       GPS
 │             │            │           │          │
 │ Check In    │            │           │          │
 ├────────────▶│            │           │          │
 │             │ Get GPS    │           │          │
 │             ├─────────────────────────────────▶│
 │             │◀─────────────────────────────────┤
 │             │            │           │          │
 │             │ POST /check-in         │          │
 │             ├───────────▶│           │          │
 │             │            │ Validate  │          │
 │             │            │ token     │          │
 │             │            │           │          │
 │             │            │ Get user/employee   │
 │             │            ├──────────▶│          │
 │             │            │◀──────────┤          │
 │             │            │           │          │
 │             │            │ Calculate geofence   │
 │             │            │           │          │
 │             │            │ Create attendance    │
 │             │            ├──────────▶│          │
 │             │            │◀──────────┤          │
 │             │            │           │          │
 │             │◀───────────┤ Success   │          │
 │◀────────────┤            │           │          │
```

---

# 49. Final Architectural Principle

The most important design decision is:

> **The mobile application is an untrusted client. Odoo/backend is authoritative.**

Therefore:

```text
Mobile says:
"I am here."

Backend verifies:
"Your GPS coordinates are inside an authorized zone."

Odoo verifies:
"You are authorized to record attendance."

Only then:
"Attendance created."
```

This architecture gives you a much stronger foundation than putting Odoo credentials and business logic directly into the mobile application.

---

# 50. Initial Repository

Recommended monorepo:

```text
geo-attendance/
│
├── mobile/          # Flutter app
├── backend/         # FastAPI security/API gateway
├── odoo/
│   └── geo_attendance/
│
├── docs/
│   ├── requirements.md
│   ├── architecture.md
│   ├── security.md
│   ├── api.md
│   └── threat-model.md
│
├── infrastructure/
│   ├── docker/
│   ├── nginx/
│   └── deployment/
│
├── .github/
│   └── workflows/
│
├── .gitignore
├── SECURITY.md
└── README.md
```

This structure keeps the mobile application, security gateway, Odoo customization, documentation, and deployment infrastructure separated while still allowing one coordinated product repository.

# 51. Development Environment Setup

This section defines the standard development environment for Windows and Linux. The recommended workflow is to run Odoo, PostgreSQL, Redis, the FastAPI gateway, and the Flutter application as separate services during development.

## 51.1 Development Architecture

```text
Developer PC
│
├── Flutter Mobile App
│     └── Android Emulator / Physical Android Device
│
├── FastAPI Gateway
│     ├── PostgreSQL
│     └── Redis
│
└── Odoo 18 Community
      └── PostgreSQL
```

For a first implementation, PostgreSQL can be shared by Odoo and the gateway only if separate databases and database users are created. Prefer separate PostgreSQL databases and least-privilege users.

Odoo's official documentation recommends source installation for module development because it provides flexible control over source code, configuration, multiple Odoo versions, and custom addons. Odoo 18 requires Python 3.10 or later. citeturn0search0

---

# 52. Required Developer Tools

Install:

- Git
- VS Code or another IDE
- Python 3.10+
- PostgreSQL
- Redis
- Flutter SDK
- Dart SDK (bundled with Flutter)
- Android Studio
- Android SDK
- Android Emulator or physical Android device
- Java/JDK compatible with the installed Android tooling
- Docker Desktop (recommended, optional for local infrastructure)
- curl/Postman/Insomnia for API testing

Flutter's official setup requires Git and an editor/IDE, and the Flutter SDK's `bin` directory should be added to PATH. Run `flutter doctor` after installation to identify missing platform dependencies. citeturn0search8

---

# 53. Recommended Versions

Do not blindly use `latest` versions in production.

Maintain a project compatibility matrix such as:

```text
Odoo              18.0
Python            3.10+ (pin the tested version)
FastAPI           pinned in pyproject.toml
PostgreSQL        pinned/tested version
Redis             pinned/tested version
Flutter           pinned/tested stable release
Dart              bundled with Flutter
Android SDK       pinned/tested API levels
Java/JDK          version required by selected Android tooling
Node.js            only if a project dependency actually requires it
```

Record exact versions in `docs/development.md` and CI configuration after the first successful setup.

---

# 54. Windows Development Setup

## 54.1 Install Git

Install Git for Windows and verify:

```powershell
git --version
```

Configure identity:

```powershell
git config --global user.name "Your Name"
git config --global user.email "your-email@example.com"
```

Never commit passwords, API keys, `.env` files containing secrets, signing keys, or production credentials.

---

## 54.2 Install Python

Install Python 3.10 or a project-approved newer version.

During Windows installation, enable:

```text
Add Python to PATH
pip
```

Verify:

```powershell
python --version
pip --version
```

Odoo 18 requires Python 3.10 or later. citeturn0search0

---

## 54.3 Create Project Directory

Recommended:

```powershell
mkdir C:\dev\geo-attendance
cd C:\dev\geo-attendance
git clone <YOUR_REPOSITORY_URL> .
```

Expected structure:

```text
C:\dev\geo-attendance\
├── mobile\
├── backend\
├── odoo\
├── docs\
└── infrastructure\
```

---

# 55. Windows — Odoo 18 Source Setup

For development, use the Odoo source tree rather than treating Odoo as a black-box installed application.

Odoo's source-install documentation provides Git-based installation and shows the `18.0` branch. citeturn0search0

Example:

```powershell
cd C:\dev
git clone --branch 18.0 --single-branch https://github.com/odoo/odoo.git odoo-community-18
```

Create custom addons directory:

```powershell
mkdir C:\dev\geo-attendance\odoo\addons
```

Place the custom module here:

```text
C:\dev\geo-attendance\odoo\addons\geo_attendance\
```

---

# 56. Windows — PostgreSQL

Install PostgreSQL and create a dedicated Odoo database role.

Example using `psql`:

```sql
CREATE USER geo_odoo WITH PASSWORD 'DEVELOPMENT_ONLY_PASSWORD';
ALTER USER geo_odoo CREATEDB;
```

For production, do not give unnecessary privileges.

Create a separate gateway database/user:

```sql
CREATE DATABASE geo_gateway;
CREATE USER geo_gateway_user WITH PASSWORD 'DEVELOPMENT_ONLY_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE geo_gateway TO geo_gateway_user;
```

Do not use the PostgreSQL superuser from the application.

---

# 57. Windows — Odoo Python Environment

From the Odoo source directory:

```powershell
cd C:\dev\odoo-community-18
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip wheel setuptools
```

Install Odoo dependencies using the dependency file supplied by the selected Odoo source tree:

```powershell
pip install -r requirements.txt
```

If Windows blocks PowerShell activation, use a suitable development shell or configure the execution policy according to your organization's security policy. Do not weaken system security globally just to activate a virtual environment.

---

# 58. Windows — Run Odoo

Create a local configuration file outside Git, for example:

```text
C:\dev\geo-attendance\local\odoo.conf
```

Example:

```ini
[options]
db_host = localhost
db_port = 5432
db_user = geo_odoo
db_password = DEVELOPMENT_ONLY_PASSWORD
addons_path = C:\dev\odoo-community-18\addons,C:\dev\geo-attendance\odoo\addons
http_port = 8069
```

Run:

```powershell
cd C:\dev\odoo-community-18
.\.venv\Scripts\Activate.ps1
python odoo-bin -c C:\dev\geo-attendance\local\odoo.conf
```

Odoo should be available locally on port 8069 unless configured differently. Odoo documents running `odoo-bin` directly for source-based development. citeturn0search0

---

# 59. Linux Development Setup

The examples below target Debian/Ubuntu-style systems.

Update packages:

```bash
sudo apt update
sudo apt upgrade -y
```

Install base tools:

```bash
sudo apt install -y git curl build-essential python3 python3-venv python3-pip \
    postgresql postgresql-contrib redis-server libpq-dev
```

Odoo's official package documentation confirms PostgreSQL is required and provides Debian/Ubuntu installation guidance. citeturn0search4

---

# 60. Linux — Verify Services

```bash
python3 --version
pip3 --version
git --version
psql --version
redis-server --version
```

Enable/start services:

```bash
sudo systemctl enable --now postgresql
sudo systemctl enable --now redis-server
```

Verify:

```bash
systemctl status postgresql
systemctl status redis-server
```

---

# 61. Linux — Odoo 18 Source Setup

```bash
mkdir -p ~/dev
cd ~/dev
git clone --branch 18.0 --single-branch https://github.com/odoo/odoo.git odoo-community-18
```

Create project directories:

```bash
mkdir -p ~/dev/geo-attendance/odoo/addons
mkdir -p ~/dev/geo-attendance/local
```

Copy/link the custom module into the addons directory:

```text
~/dev/geo-attendance/odoo/addons/geo_attendance/
```

---

# 62. Linux — Odoo Virtual Environment

```bash
cd ~/dev/odoo-community-18
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip wheel setuptools
pip install -r requirements.txt
```

Odoo's official source documentation describes source installation as especially useful for developers because it makes starting/stopping Odoo and managing multiple versions easier. citeturn0search0

---

# 63. Linux — PostgreSQL Setup

Open PostgreSQL:

```bash
sudo -u postgres psql
```

Create a development role:

```sql
CREATE USER geo_odoo WITH PASSWORD 'DEVELOPMENT_ONLY_PASSWORD' CREATEDB;
CREATE USER geo_gateway_user WITH PASSWORD 'DEVELOPMENT_ONLY_PASSWORD';
CREATE DATABASE geo_gateway OWNER geo_gateway_user;
\q
```

For local development, use strong non-production credentials. For production, use a secret manager and separate database credentials.

---

# 64. Linux — Run Odoo

Example configuration:

```ini
[options]
db_host = localhost
db_port = 5432
db_user = geo_odoo
db_password = DEVELOPMENT_ONLY_PASSWORD
addons_path = /home/YOUR_USER/dev/odoo-community-18/addons,/home/YOUR_USER/dev/geo-attendance/odoo/addons
http_port = 8069
```

Run:

```bash
cd ~/dev/odoo-community-18
source .venv/bin/activate
python odoo-bin -c ~/dev/geo-attendance/local/odoo.conf
```

---

# 65. FastAPI Backend Setup

FastAPI officially supports installation through `uv` or a Python virtual environment with pip. For this project, use a lockfile-based dependency workflow so developers and CI install reproducible versions. citeturn0search3turn0search5

Recommended project setup:

```text
backend/
├── pyproject.toml
├── uv.lock
├── app/
└── tests/
```

## Windows

```powershell
cd C:\dev\geo-attendance\backend
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -e .
```

## Linux

```bash
cd ~/dev/geo-attendance/backend
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
pip install -e .
```

Alternative recommended dependency manager:

```bash
uv sync
```

FastAPI's current documentation recommends `uv` and a lock file for reproducible project dependency installation. citeturn0search3

---

# 66. Backend Dependencies

Initial dependency categories:

```text
fastapi
uvicorn
pydantic
pydantic-settings
httpx
SQLAlchemy
psycopg
alembic
redis
PyJWT or approved JOSE implementation
argon2/bcrypt if password hashing is required
python-multipart where required
pytest
pytest-asyncio
httpx (testing)
ruff
mypy (optional)
```

Exact package versions must be pinned after compatibility testing.

Do not add libraries simply because they are popular. Every dependency increases the attack surface.

---

# 67. Backend Environment Variables

Create:

```text
backend/.env.example
```

Example:

```env
APP_ENV=development
APP_NAME=geo-attendance-api
APP_VERSION=0.1.0

API_HOST=127.0.0.1
API_PORT=8000

DATABASE_URL=postgresql+psycopg://geo_gateway_user:CHANGE_ME@127.0.0.1:5432/geo_gateway
REDIS_URL=redis://127.0.0.1:6379/0

JWT_ISSUER=geo-attendance-dev
JWT_AUDIENCE=geo-attendance-mobile
ACCESS_TOKEN_MINUTES=10

ODOO_BASE_URL=http://127.0.0.1:8069
ODOO_DATABASE=geo_attendance_dev

LOG_LEVEL=INFO
```

Create local `.env` from this file but never commit `.env`:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

`.gitignore` must include:

```text
.env
.env.*
!.env.example
*.pem
*.key
```

---

# 68. Run FastAPI Backend

FastAPI's development CLI can run an application in development mode. citeturn0search6

Example:

```bash
fastapi dev app/main.py
```

Or:

```bash
uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
```

The API should expose development documentation only on the development machine/network:

```text
http://127.0.0.1:8000/docs
http://127.0.0.1:8000/openapi.json
```

Do not expose development Swagger/OpenAPI endpoints publicly without an explicit security decision.

---

# 69. Flutter Environment Setup — Windows

Install Flutter SDK using the official Flutter installation process.

After adding Flutter to PATH:

```powershell
flutter --version
flutter doctor
```

Install Android Studio and configure:

- Android SDK
- Android SDK Platform Tools
- Android SDK Build Tools
- Android Emulator
- Android SDK Command-line Tools
- Android device USB drivers where needed

Then:

```powershell
flutter doctor --android-licenses
flutter doctor
```

Resolve all Android-related errors before starting mobile development.

Flutter's documentation recommends `flutter doctor` for checking the development environment and supports Windows, Linux, Android, and other targets. citeturn0search8turn0search11

---

# 70. Flutter Environment Setup — Linux

Install Flutter and Git, then add Flutter to PATH.

Verify:

```bash
flutter --version
dart --version
flutter doctor
```

Install Android Studio and the Android SDK.

Then:

```bash
flutter doctor --android-licenses
flutter doctor
```

For Android development on Linux, make sure the required Android SDK command-line tools and emulator/device configuration are installed.

---

# 71. Create / Install Flutter Dependencies

From the repository:

```bash
cd mobile
flutter pub get
```

Check dependency health:

```bash
flutter pub outdated
flutter analyze
flutter test
```

Recommended `pubspec.yaml` dependency categories:

```text
flutter_riverpod or approved state management
 go_router
 dio
 flutter_secure_storage
 geolocator
 permission_handler
 local encrypted storage/database
 biometric authentication package
 connectivity_plus
 device_info_plus (only where justified)
 package_info_plus
```

Keep the dependency list small.

---

# 72. Android Location Configuration

The Android application must explicitly request only the location permissions required by the attendance workflow.

For point-in-time attendance, prefer foreground location.

Do not request background location in MVP unless the product genuinely requires background geofence monitoring.

Configure Android permissions according to the target Android SDK and current Google Play policies.

Example conceptual permission:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

The application should explain why location is required before requesting permission.

---

# 73. iOS Preparation

Development of iOS builds requires macOS/Xcode.

Windows and Linux can be used for Flutter Android/backend/Odoo development, but iOS compilation/signing requires an Apple development environment.

Keep iOS configuration in the same Flutter project so the mobile codebase remains cross-platform.

---

# 74. Run Flutter App

Start an Android emulator or connect a physical device.

Check devices:

```bash
flutter devices
```

Run:

```bash
flutter run
```

Run a specific device:

```bash
flutter run -d <device-id>
```

For development, configure the API base URL carefully.

Android emulator example:

```text
http://10.0.2.2:8000
```

A physical device cannot normally use `127.0.0.1` to reach the developer PC. Use the developer machine's LAN IP and ensure firewall rules permit only the required development port.

For production, always use HTTPS.

---

# 75. Local Development Service Ports

Recommended defaults:

```text
Odoo              8069
FastAPI            8000
PostgreSQL         5432
Redis              6379
```

Example:

```text
Flutter
   │
   ▼
FastAPI :8000
   │
   ├── PostgreSQL :5432
   ├── Redis :6379
   └── Odoo :8069
```

Do not expose PostgreSQL or Redis directly to the public Internet.

---

# 76. Complete Development Startup Process

## Windows

Terminal 1 — PostgreSQL/Redis:

```text
Ensure PostgreSQL and Redis services are running.
```

Terminal 2 — Odoo:

```powershell
cd C:\dev\odoo-community-18
.\.venv\Scripts\Activate.ps1
python odoo-bin -c C:\dev\geo-attendance\local\odoo.conf
```

Terminal 3 — Backend:

```powershell
cd C:\dev\geo-attendance\backend
.\.venv\Scripts\Activate.ps1
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Terminal 4 — Flutter:

```powershell
cd C:\dev\geo-attendance\mobile
flutter pub get
flutter run
```

## Linux

Terminal 1:

```bash
sudo systemctl start postgresql
sudo systemctl start redis-server
```

Terminal 2:

```bash
cd ~/dev/odoo-community-18
source .venv/bin/activate
python odoo-bin -c ~/dev/geo-attendance/local/odoo.conf
```

Terminal 3:

```bash
cd ~/dev/geo-attendance/backend
source .venv/bin/activate
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Terminal 4:

```bash
cd ~/dev/geo-attendance/mobile
flutter pub get
flutter run
```

---

# 77. Development Request Flow

Once everything is running:

```text
Mobile App
   │
   │ HTTPS/HTTP development API
   ▼
FastAPI :8000
   │
   ├── authenticate
   ├── resolve employee
   ├── validate request
   ├── validate geofence
   └── call Odoo
            │
            ▼
       Odoo :8069
            │
            ▼
      PostgreSQL :5432
```

Redis is used for short-lived state such as:

- Rate limits
- Session-related transient state
- Replay/idempotency keys
- Temporary security counters

Do not use Redis as the authoritative attendance database.

---

# 78. Odoo Module Development Cycle

Every Odoo module change should follow:

```text
Edit Python/XML
      │
      ▼
Restart Odoo
      │
      ▼
Upgrade Module
      │
      ▼
Test UI/API
      │
      ▼
Run automated tests
      │
      ▼
Commit
```

Example:

```bash
python odoo-bin -c odoo.conf -u geo_attendance -d geo_attendance_dev
```

For larger development work, keep a dedicated test database and do not develop against production.

---

# 79. Backend Development Cycle

```text
Change Python
    │
    ▼
Run formatter/linter
    │
    ▼
Unit tests
    │
    ▼
API tests
    │
    ▼
Security tests
    │
    ▼
Run locally
```

Recommended commands:

```bash
ruff check .
ruff format --check .
pytest
```

If mypy is adopted:

```bash
mypy app
```

---

# 80. Flutter Development Cycle

```text
Change Dart
   │
   ▼
flutter analyze
   │
   ▼
flutter test
   │
   ▼
Run on emulator/device
   │
   ▼
Test GPS permission
   │
   ▼
Test geofence
   │
   ▼
Test API
```

Commands:

```bash
flutter pub get
flutter analyze
flutter test
flutter run
```

Before creating a release build:

```bash
flutter clean
flutter pub get
flutter analyze
flutter test
```

---

# 81. Local Odoo Test Data

Create a dedicated development database:

```text
geo_attendance_dev
```

Create test records:

```text
Company: Demo Company
Department: Demo Department
Employee: Demo Employee
User: Demo Employee User
Attendance Zone: Demo Office
Radius: 100m
```

Test users:

```text
Employee A
Employee B
Portal User
Unauthorized User
Inactive Employee
Multi-company User
```

This allows security testing against horizontal privilege escalation.

---

# 82. Geofence Testing in Development

Use controlled coordinates.

Example:

```text
Office:
Latitude: 25.2048
Longitude: 55.2708
Radius: 100m
```

Test cases:

```text
Inside radius              → PASS
Exactly near boundary      → controlled result
Outside radius             → FAIL
Poor GPS accuracy          → FAIL/RETRY
Stale timestamp            → FAIL
Invalid coordinates        → FAIL
Missing location           → FAIL
Spoofed/mock location      → risk signal/reject according to policy
```

Do not use real employee location data for automated tests.

---

# 83. API Testing With curl

Health check:

```bash
curl http://127.0.0.1:8000/health
```

Authentication:

```bash
curl -X POST http://127.0.0.1:8000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"DEVELOPMENT_PASSWORD"}'
```

Check-in should use a unique idempotency key:

```bash
curl -X POST http://127.0.0.1:8000/api/v1/attendance/check-in \
  -H "Authorization: Bearer DEVELOPMENT_TOKEN" \
  -H "Idempotency-Key: UNIQUE_UUID" \
  -H "Content-Type: application/json" \
  -d '{
    "latitude":25.2048,
    "longitude":55.2708,
    "accuracy":8.5,
    "captured_at":"2026-08-29T08:42:12Z"
  }'
```

Never use real production credentials in shell history or examples.

---

# 84. Git Development Workflow

Use branches:

```text
main
│
├── develop
│
├── feature/authentication
├── feature/geofence
├── feature/attendance
└── security/fix-xxxxx
```

Recommended process:

```text
Issue
  │
  ▼
Feature branch
  │
  ▼
Code
  │
  ▼
Tests
  │
  ▼
Security review
  │
  ▼
Pull Request
  │
  ▼
CI
  │
  ▼
Merge
```

Never develop directly on `main`.

---

# 85. CI Pipeline

Every pull request should run:

```text
Flutter
 ├── flutter analyze
 ├── flutter test
 └── dependency audit

Backend
 ├── lint
 ├── unit tests
 ├── integration tests
 ├── security tests
 └── dependency audit

Odoo
 ├── module install test
 ├── security test
 ├── geofence test
 └── attendance test
```

Build artifacts should be produced only after tests pass.

---

# 86. Docker Development Option

Docker can simplify PostgreSQL/Redis setup while keeping Odoo and Flutter native for faster development.

Recommended first Docker setup:

```text
docker-compose.yml
├── postgres
└── redis
```

Optional later:

```text
postgres
redis
backend
odoo
nginx
```

Do not containerize everything immediately if it makes debugging harder. Start with the smallest useful environment.

---

# 87. Example Docker Infrastructure

```yaml
services:
  postgres:
    image: postgres:<PINNED_VERSION>
    environment:
      POSTGRES_USER: geo_dev
      POSTGRES_PASSWORD: CHANGE_ME
      POSTGRES_DB: geo_dev
    ports:
      - "127.0.0.1:5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:<PINNED_VERSION>
    ports:
      - "127.0.0.1:6379:6379"

volumes:
  postgres_data:
```

Pin image versions rather than using `latest`.

For production, use managed services or hardened infrastructure instead of copying this development configuration directly.

---

# 88. Environment Separation

Maintain at least:

```text
Development
Testing
Staging
Production
```

Never share:

```text
Production database
Production tokens
Production Odoo credentials
Production signing keys
```

with development.

Recommended:

```text
.env.example
.env.development       # local only
.env.test              # local/CI only
.env.staging           # secret manager
.env.production        # secret manager
```

Only `.env.example` belongs in Git.

---

# 89. Development Security Rules

1. Never use production credentials locally.
2. Never commit secrets.
3. Never disable TLS verification in application code as a permanent workaround.
4. Never expose PostgreSQL/Redis publicly.
5. Never use Odoo admin credentials from the mobile application.
6. Never trust client employee IDs.
7. Never trust client geofence results.
8. Never trust client timestamps for authoritative attendance time.
9. Never store passwords in mobile storage.
10. Never log tokens.
11. Never use production employee GPS data for testing.
12. Keep dependencies pinned.
13. Review dependency updates.
14. Run security tests before releases.
15. Keep development and production databases completely separate.

---

# 90. First-Day Setup Checklist

## Windows

```text
[ ] Git installed
[ ] Python installed
[ ] PostgreSQL installed
[ ] Redis installed/running
[ ] Flutter installed
[ ] Android Studio installed
[ ] Android SDK installed
[ ] Emulator/device working
[ ] Odoo 18 source cloned
[ ] Odoo virtual environment created
[ ] Odoo dependencies installed
[ ] Odoo running on 8069
[ ] Backend virtual environment created
[ ] Backend dependencies installed
[ ] PostgreSQL gateway database created
[ ] Redis reachable
[ ] Backend running on 8000
[ ] Flutter app starts
[ ] App can reach backend
[ ] Backend can reach Odoo
[ ] Odoo custom module installed
[ ] Demo employee created
[ ] Demo geofence created
[ ] Check-in tested
[ ] Check-out tested
```

## Linux

```text
[ ] Git installed
[ ] Python installed
[ ] PostgreSQL installed/running
[ ] Redis installed/running
[ ] Flutter installed
[ ] Android Studio/SDK installed
[ ] Emulator/device working
[ ] Odoo 18 source cloned
[ ] Odoo virtual environment created
[ ] Odoo dependencies installed
[ ] Odoo running on 8069
[ ] Backend virtual environment created
[ ] Backend dependencies installed
[ ] PostgreSQL gateway database created
[ ] Redis reachable
[ ] Backend running on 8000
[ ] Flutter app starts
[ ] App can reach backend
[ ] Backend can reach Odoo
[ ] Odoo custom module installed
[ ] Demo employee created
[ ] Demo geofence created
[ ] Check-in tested
[ ] Check-out tested
```

---

# 91. Recommended Developer Workflow

For your first implementation, the practical development order should be:

```text
DAY 1
Environment + Git + Odoo + PostgreSQL

DAY 2
Odoo geo_attendance module + security

DAY 3
FastAPI skeleton + Odoo adapter

DAY 4
Authentication + sessions

DAY 5
Employee mapping + attendance API

DAY 6
Geofence calculation + security

DAY 7
Flutter onboarding + login

DAY 8
Flutter GPS + check-in

DAY 9
Check-out + attendance history

DAY 10
Time Off + device/session management

DAY 11+
Security testing + edge cases + UI refinement
```

These are development milestones, not guaranteed delivery times.

---

# 92. Official Documentation References

Use official documentation as the source of truth when installation commands or platform requirements change:

- Odoo 18 source installation: https://www.odoo.com/documentation/18.0/administration/on_premise/source.html
- Odoo 18 administration/install documentation: https://www.odoo.com/documentation/18.0/administration.html
- Odoo 18 packaged installation: https://www.odoo.com/documentation/18.0/administration/on_premise/packages.html
- FastAPI documentation: https://fastapi.tiangolo.com/
- Flutter installation documentation: https://docs.flutter.dev/get-started/install

The Odoo source documentation specifically notes that source installation is useful for module developers and that Odoo 18 requires Python 3.10 or later. citeturn0search0

---

# 93. Final Development Principle

The development environment should make the production security architecture possible from day one.

Do not build an insecure MVP and plan to "add security later".

Build the MVP around these boundaries:

```text
Flutter
  ↓
Untrusted client
  ↓
FastAPI security boundary
  ↓
Validated business request
  ↓
Odoo adapter
  ↓
Odoo authorization/business rules
  ↓
PostgreSQL
```

This makes the project easier to test, maintain, secure, and eventually convert into a multi-company attendance product.
