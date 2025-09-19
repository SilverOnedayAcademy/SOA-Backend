# SOA Backend API Documentation

## Overview

This document provides comprehensive documentation for the SOA Backend REST API, which implements JWT-based authentication with passkey (WebAuthn) support, member management, and token rotation security.

## Base URL

```
http://localhost:8080
```

## Authentication

The API uses JWT (JSON Web Tokens) for authentication with the following token types:

- **Access Token**: Short-lived (15 minutes) for API access
- **Refresh Token**: Long-lived (7 days) for token renewal

### Security Headers

Include the JWT access token in the Authorization header:

```
Authorization: Bearer <access_token>
```

---

## API Endpoints

### 1. Authentication & Authorization (`/api/auth`)

#### 1.1 Refresh Token

**POST** `/api/auth/refresh`

Refreshes expired access tokens and implements token rotation security.

**Request Body:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (Success):**

```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Tokens refreshed and rotated successfully"
}
```

**Response (Error):**

```json
{
  "error": "Invalid refresh token"
}
```

#### 1.2 Logout

**POST** `/api/auth/logout`

Revokes all user tokens and logs out the user.

**Request Body:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**

```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

#### 1.3 Token Status

**GET** `/api/auth/token-status?token=<token_value>`

Retrieves detailed information about a specific token.

**Query Parameters:**

- `token` (required): The token value to check

**Response:**

```json
{
  "tokenId": "12345",
  "memberId": 1,
  "tokenType": "ACCESS_TOKEN",
  "issuedAt": "2025-09-19T07:00:00Z",
  "expiresAt": "2025-09-19T07:15:00Z",
  "isExpired": false,
  "isRevoked": false,
  "isValid": true
}
```

---

### 2. Member Management (`/api/members`)

#### 2.1 Create Member

**POST** `/api/members`

Creates a new member (basic creation without passkey).

**Request Body:**

```json
{
  "name": "홍길동",
  "email": "hong@example.com",
  "phone": "010-1234-5678",
  "role": "GUARDIAN",
  "birthDate": "1990-01-01",
  "address": "서울시 강남구",
  "career": "소프트웨어 개발자",
  "certification": "정보처리기사",
  "governmentSupport": true
}
```

**Response:**

```json
"Member created successfully"
```

#### 2.2 Get Member by ID

**GET** `/api/members/{id}`

Retrieves member information by member ID.

**Path Parameters:**

- `id` (required): Member ID

**Response:**

```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com",
  "phone": "010-1234-5678",
  "role": "GUARDIAN",
  "birthDate": "1990-01-01",
  "address": "서울시 강남구",
  "career": "소프트웨어 개발자",
  "certification": "정보처리기사",
  "governmentSupport": true,
  "approved": false,
  "credentialId": "ABC123..."
}
```

#### 2.3 Get Member Passkey Info by ID

**GET** `/api/members/{id}/passkey`

Retrieves passkey information for a specific member.

**Path Parameters:**

- `id` (required): Member ID

**Response (Has Passkey):**

```json
{
  "memberId": 1,
  "memberName": "홍길동",
  "memberEmail": "hong@example.com",
  "hasPasskey": true,
  "passkeyInfo": {
    "credentialId": "ABC123...",
    "label": "Default Passkey",
    "signCount": 5,
    "publicKeyLength": 65
  }
}
```

**Response (No Passkey):**

```json
{
  "memberId": 1,
  "memberName": "홍길동",
  "hasPasskey": false,
  "message": "No passkey registered"
}
```

#### 2.4 Get Member Passkey Info by Email

**GET** `/api/members/by-email/{email}/passkey`

Retrieves passkey information for a member by email address.

**Path Parameters:**

- `email` (required): Member email address

**Response:** Same as 2.3 above

---

### 3. WebAuthn/Passkey Authentication (`/webauthn`)

#### 3.1 Begin Passkey Registration

**POST** `/webauthn/register/begin`

Initiates the passkey registration process.

**Request Body (New Registration Flow):**

```json
{
  "sessionId": "temp_session_12345"
}
```

**Request Body (Existing Member Flow):**

```json
{
  "username": "홍길동"
}
```

**Response:**

```json
{
  "publicKey": {
    "rp": {
      "name": "SOA Backend",
      "id": "localhost"
    },
    "user": {
      "id": "MTIzNDU2Nzg5MA",
      "name": "홍길동",
      "displayName": "홍길동"
    },
    "challenge": "randomChallengeString123",
    "pubKeyCredParams": [
      { "type": "public-key", "alg": -7 },
      { "type": "public-key", "alg": -257 }
    ],
    "timeout": 60000,
    "attestation": "none",
    "authenticatorSelection": {
      "authenticatorAttachment": "platform",
      "userVerification": "required"
    }
  }
}
```

#### 3.2 Finish Passkey Registration

**POST** `/webauthn/register/finish`

Completes the passkey registration process.

**Request Body:**

```json
{
  "username": "홍길동",
  "sessionId": "temp_session_12345",
  "credential": {
    "id": "credentialId123",
    "rawId": "base64EncodedRawId",
    "response": {
      "clientDataJSON": "base64EncodedClientData",
      "attestationObject": "base64EncodedAttestation",
      "publicKey": "base64EncodedPublicKey",
      "publicKeyAlgorithm": -7
    },
    "type": "public-key"
  }
}
```

**Response (Success):**

```json
{
  "success": true
}
```

**Response (Error):**

```json
{
  "error": "Registration failed: <error_message>"
}
```

#### 3.3 Begin Passkey Authentication

**POST** `/webauthn/authenticate/begin`

Initiates the passkey authentication process.

**Request Body:** Empty `{}`

**Response:**

```json
{
  "publicKey": {
    "challenge": "randomChallengeString456",
    "timeout": 60000,
    "rpId": "localhost",
    "userVerification": "required"
  }
}
```

#### 3.4 Finish Passkey Authentication

**POST** `/webauthn/authenticate/finish`

Completes the passkey authentication and returns JWT tokens.

**Request Body:**

```json
{
  "credential": {
    "id": "credentialId123",
    "rawId": "base64EncodedRawId",
    "response": {
      "clientDataJSON": "base64EncodedClientData",
      "authenticatorData": "base64EncodedAuthData",
      "signature": "base64EncodedSignature",
      "userHandle": "base64EncodedUserHandle"
    },
    "type": "public-key"
  }
}
```

**Response (Success):**

```json
{
  "success": true,
  "username": "홍길동",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "role": "GUARDIAN"
}
```

**Response (Error):**

```json
{
  "error": "Authentication failed",
  "success": false
}
```

---

### 4. Web Pages (HTML Controllers)

#### 4.1 Home Page

**GET** `/`

Returns the main index page.

#### 4.2 Login Page

**GET** `/login`

Returns the login page with passkey authentication.

#### 4.3 Registration Page

**GET** `/register`

Returns the member registration form.

**POST** `/register`

Processes member registration and redirects to passkey setup.

#### 4.4 Passkey Setup Page

**GET** `/setup-passkey?sessionId=<session_id>`

Returns the passkey setup page for new registrations.

#### 4.5 Dashboard

**GET** `/dashboard`

Returns the user dashboard (requires authentication).

---

## Data Models

### Member

```json
{
  "id": "Long",
  "name": "String",
  "email": "String",
  "phone": "String",
  "role": "GUARDIAN | TUTOR",
  "birthDate": "LocalDate",
  "address": "String",
  "career": "String",
  "certification": "String",
  "governmentSupport": "Boolean",
  "approved": "Boolean",
  "credentialId": "String"
}
```

### MemberCredential

```json
{
  "id": "Long",
  "credentialId": "String",
  "publicKey": "byte[]",
  "signCount": "Long",
  "label": "String",
  "member": "Member"
}
```

### AccessToken

```json
{
  "id": "String",
  "tokenValue": "String",
  "tokenType": "ACCESS_TOKEN | REFRESH_TOKEN",
  "memberId": "Long",
  "issuedAt": "LocalDateTime",
  "expiresAt": "LocalDateTime",
  "revoked": "Boolean"
}
```

---

## Error Handling

### Common Error Responses

**400 Bad Request:**

```json
{
  "error": "Invalid request parameters"
}
```

**401 Unauthorized:**

```json
{
  "error": "Invalid or expired token"
}
```

**404 Not Found:**

```json
{
  "error": "Resource not found"
}
```

**500 Internal Server Error:**

```json
{
  "error": "Internal server error"
}
```

---

## Security Features

### 1. JWT Token Rotation

- Access tokens expire after 15 minutes
- Refresh tokens expire after 7 days
- Each refresh generates new tokens and revokes old ones
- Prevents token replay attacks

### 2. Passkey Authentication

- Uses WebAuthn standard for passwordless authentication
- Supports platform authenticators (Touch ID, Face ID, Windows Hello)
- Public key cryptography for secure authentication
- No passwords stored on server

### 3. CORS Configuration

- Configured for cross-origin requests
- Supports credentials and common headers
- Allows GET, POST, PUT, DELETE methods

---

## Usage Examples

### Complete Registration Flow

1. **Submit Registration Form:**

```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "name=홍길동&email=hong@example.com&phone=010-1234-5678&role=GUARDIAN"
```

2. **Begin Passkey Registration:**

```bash
curl -X POST http://localhost:8080/webauthn/register/begin \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "temp_session_12345"}'
```

3. **Complete Passkey Registration:**

```bash
curl -X POST http://localhost:8080/webauthn/register/finish \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "temp_session_12345", "credential": {...}}'
```

### Authentication Flow

1. **Begin Authentication:**

```bash
curl -X POST http://localhost:8080/webauthn/authenticate/begin \
  -H "Content-Type: application/json" \
  -d '{}'
```

2. **Complete Authentication:**

```bash
curl -X POST http://localhost:8080/webauthn/authenticate/finish \
  -H "Content-Type: application/json" \
  -d '{"credential": {...}}'
```

3. **Use Access Token:**

```bash
curl -X GET http://localhost:8080/api/members/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Token Management

1. **Refresh Token:**

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}'
```

2. **Logout:**

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"accessToken": "...", "refreshToken": "..."}'
```

---

## Development Notes

- The system uses H2/MySQL database for persistence
- Passkey credentials are stored securely with public key cryptography
- All sensitive operations are logged for security monitoring
- Token storage uses in-memory repository (consider Redis for production)
- WebAuthn implementation is simplified for demo purposes (add proper attestation verification for production)

---

## Support

For technical support or questions about the API, please refer to the source code or contact the development team.
