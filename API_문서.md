# SOA Backend API 문서

## 개요

이 문서는 JWT 기반 인증과 패스키(WebAuthn) 지원, 회원 관리, 토큰 순환 보안을 구현한 SOA Backend REST API에 대한 종합적인 문서입니다.

## 기본 URL

```
http://localhost:8080
```

## 인증

API는 JWT(JSON Web Token)를 사용하여 인증하며, 다음과 같은 토큰 유형을 사용합니다:

- **액세스 토큰**: 단기간(15분) API 접근용
- **리프레시 토큰**: 장기간(7일) 토큰 갱신용

### 보안 헤더

Authorization 헤더에 JWT 액세스 토큰을 포함하세요:

```
Authorization: Bearer <access_token>
```

---

## API 엔드포인트

### 1. 인증 및 권한 관리 (`/api/auth`)

#### 1.1 토큰 갱신

**POST** `/api/auth/refresh`

만료된 액세스 토큰을 갱신하고 토큰 순환 보안을 구현합니다.

**요청 본문:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**응답 (성공):**

```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "토큰이 성공적으로 갱신되고 순환되었습니다"
}
```

**응답 (오류):**

```json
{
  "error": "유효하지 않은 리프레시 토큰"
}
```

#### 1.2 로그아웃

**POST** `/api/auth/logout`

모든 사용자 토큰을 무효화하고 로그아웃합니다.

**요청 본문:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**응답:**

```json
{
  "success": true,
  "message": "성공적으로 로그아웃되었습니다"
}
```

#### 1.3 토큰 상태 조회

**GET** `/api/auth/token-status?token=<token_value>`

특정 토큰에 대한 상세 정보를 조회합니다.

**쿼리 매개변수:**

- `token` (필수): 확인할 토큰 값

**응답:**

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

### 2. 회원 관리 (`/api/members`)

#### 2.1 회원 생성

**POST** `/api/members`

새로운 회원을 생성합니다 (패스키 없는 기본 생성).

**요청 본문:**

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

**응답:**

```json
"회원이 성공적으로 생성되었습니다"
```

#### 2.2 회원 ID로 조회

**GET** `/api/members/{id}`

회원 ID로 회원 정보를 조회합니다.

**경로 매개변수:**

- `id` (필수): 회원 ID

**응답:**

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

#### 2.3 회원 ID로 패스키 정보 조회

**GET** `/api/members/{id}/passkey`

특정 회원의 패스키 정보를 조회합니다.

**경로 매개변수:**

- `id` (필수): 회원 ID

**응답 (패스키 있음):**

```json
{
  "memberId": 1,
  "memberName": "홍길동",
  "memberEmail": "hong@example.com",
  "hasPasskey": true,
  "passkeyInfo": {
    "credentialId": "ABC123...",
    "label": "기본 패스키",
    "signCount": 5,
    "publicKeyLength": 65
  }
}
```

**응답 (패스키 없음):**

```json
{
  "memberId": 1,
  "memberName": "홍길동",
  "hasPasskey": false,
  "message": "등록된 패스키가 없습니다"
}
```

#### 2.4 이메일로 패스키 정보 조회

**GET** `/api/members/by-email/{email}/passkey`

이메일 주소로 회원의 패스키 정보를 조회합니다.

**경로 매개변수:**

- `email` (필수): 회원 이메일 주소

**응답:** 위 2.3과 동일

---

### 3. WebAuthn/패스키 인증 (`/webauthn`)

#### 3.1 패스키 등록 시작

**POST** `/webauthn/register/begin`

패스키 등록 프로세스를 시작합니다.

**요청 본문 (신규 회원가입 플로우):**

```json
{
  "sessionId": "temp_session_12345"
}
```

**요청 본문 (기존 회원 플로우):**

```json
{
  "username": "홍길동"
}
```

**응답:**

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

#### 3.2 패스키 등록 완료

**POST** `/webauthn/register/finish`

패스키 등록 프로세스를 완료합니다.

**요청 본문:**

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

**응답 (성공):**

```json
{
  "success": true
}
```

**응답 (오류):**

```json
{
  "error": "등록 실패: <오류_메시지>"
}
```

#### 3.3 패스키 인증 시작

**POST** `/webauthn/authenticate/begin`

패스키 인증 프로세스를 시작합니다.

**요청 본문:** 빈 객체 `{}`

**응답:**

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

#### 3.4 패스키 인증 완료

**POST** `/webauthn/authenticate/finish`

패스키 인증을 완료하고 JWT 토큰을 반환합니다.

**요청 본문:**

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

**응답 (성공):**

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

**응답 (오류):**

```json
{
  "error": "인증 실패",
  "success": false
}
```

---

### 4. 웹 페이지 (HTML 컨트롤러)

#### 4.1 홈 페이지

**GET** `/`

메인 인덱스 페이지를 반환합니다.

#### 4.2 로그인 페이지

**GET** `/login`

패스키 인증이 포함된 로그인 페이지를 반환합니다.

#### 4.3 회원가입 페이지

**GET** `/register`

회원가입 폼을 반환합니다.

**POST** `/register`

회원가입을 처리하고 패스키 설정으로 리다이렉트합니다.

#### 4.4 패스키 설정 페이지

**GET** `/setup-passkey?sessionId=<session_id>`

신규 회원가입용 패스키 설정 페이지를 반환합니다.

#### 4.5 대시보드

**GET** `/dashboard`

사용자 대시보드를 반환합니다 (인증 필요).

---

## 데이터 모델

### 회원 (Member)

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

### 회원 자격증명 (MemberCredential)

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

### 액세스 토큰 (AccessToken)

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

## 오류 처리

### 일반적인 오류 응답

**400 잘못된 요청:**

```json
{
  "error": "유효하지 않은 요청 매개변수"
}
```

**401 인증되지 않음:**

```json
{
  "error": "유효하지 않거나 만료된 토큰"
}
```

**404 찾을 수 없음:**

```json
{
  "error": "리소스를 찾을 수 없음"
}
```

**500 내부 서버 오류:**

```json
{
  "error": "내부 서버 오류"
}
```

---

## 보안 기능

### 1. JWT 토큰 순환

- 액세스 토큰은 15분 후 만료
- 리프레시 토큰은 7일 후 만료
- 각 갱신 시 새로운 토큰을 생성하고 기존 토큰을 무효화
- 토큰 재사용 공격 방지

### 2. 패스키 인증

- WebAuthn 표준을 사용한 비밀번호 없는 인증
- 플랫폼 인증기 지원 (Touch ID, Face ID, Windows Hello)
- 공개키 암호화를 통한 안전한 인증
- 서버에 비밀번호 저장하지 않음

### 3. CORS 설정

- 교차 출처 요청을 위한 설정
- 자격 증명 및 일반 헤더 지원
- GET, POST, PUT, DELETE 메서드 허용

---

## 사용 예제

### 완전한 회원가입 플로우

1. **회원가입 폼 제출:**

```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "name=홍길동&email=hong@example.com&phone=010-1234-5678&role=GUARDIAN"
```

2. **패스키 등록 시작:**

```bash
curl -X POST http://localhost:8080/webauthn/register/begin \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "temp_session_12345"}'
```

3. **패스키 등록 완료:**

```bash
curl -X POST http://localhost:8080/webauthn/register/finish \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "temp_session_12345", "credential": {...}}'
```

### 인증 플로우

1. **인증 시작:**

```bash
curl -X POST http://localhost:8080/webauthn/authenticate/begin \
  -H "Content-Type: application/json" \
  -d '{}'
```

2. **인증 완료:**

```bash
curl -X POST http://localhost:8080/webauthn/authenticate/finish \
  -H "Content-Type: application/json" \
  -d '{"credential": {...}}'
```

3. **액세스 토큰 사용:**

```bash
curl -X GET http://localhost:8080/api/members/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 토큰 관리

1. **토큰 갱신:**

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}'
```

2. **로그아웃:**

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"accessToken": "...", "refreshToken": "..."}'
```

---

## 개발 참고사항

- 시스템은 H2/MySQL 데이터베이스를 사용하여 데이터를 저장합니다
- 패스키 자격증명은 공개키 암호화로 안전하게 저장됩니다
- 모든 민감한 작업은 보안 모니터링을 위해 로그에 기록됩니다
- 토큰 저장은 인메모리 저장소를 사용합니다 (프로덕션에서는 Redis 고려)
- WebAuthn 구현은 데모 목적으로 단순화되었습니다 (프로덕션에서는 적절한 증명 검증 추가 필요)

---

## 지원

API에 대한 기술 지원이나 질문이 있으시면 소스 코드를 참조하거나 개발팀에 문의하세요.
