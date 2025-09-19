package chungbuk.soabackend.controller;

import chungbuk.soabackend.config.PasskeyAuthenticationToken;
import chungbuk.soabackend.dto.MemberRegistrationDto;
import chungbuk.soabackend.member.Member;
import chungbuk.soabackend.member.MemberCredential;
import chungbuk.soabackend.service.PasskeyMemberDetailsService;
import chungbuk.soabackend.service.TempRegistrationService;
import chungbuk.soabackend.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/webauthn")
public class WebAuthController {

    @Autowired
    private PasskeyMemberDetailsService memberDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TempRegistrationService tempRegistrationService;

    private final SecureRandom secureRandom = new SecureRandom();

    @PostMapping("/register/begin")
    public ResponseEntity<Map<String, Object>> beginRegistration(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String sessionId = request.get("sessionId");

        System.out.println("=== PASSKEY REGISTRATION BEGIN ===");
        System.out.println("📝 Username: " + username);
        System.out.println("🔑 Session ID: " + sessionId);

        Member member = null;
        String userDisplayName = null;
        String userEmail = null;
        Long userId = null;

        if (sessionId != null) {
            // 새로운 회원가입 플로우: 임시 데이터에서 정보 가져오기
            System.out.println("🔍 Looking up temp registration data...");
            MemberRegistrationDto tempData = tempRegistrationService.getTempRegistration(sessionId);
            if (tempData == null) {
                System.out.println("❌ Temp registration data not found for sessionId: " + sessionId);
                return ResponseEntity.badRequest().body(Map.of("error", "Registration session expired"));
            }

            userDisplayName = tempData.getName();
            userEmail = tempData.getEmail();
            userId = System.currentTimeMillis(); // 임시 ID 생성
            System.out.println("✅ Temp data found: " + userDisplayName + " (" + userEmail + ")");

        } else if (username != null) {
            // 기존 플로우: 이미 생성된 Member에서 정보 가져오기
            System.out.println("🔍 Looking up existing member...");
            member = memberDetailsService.findMemberByUsername(username);
            if (member == null) {
                System.out.println("❌ Member not found for username: " + username);
                return ResponseEntity.badRequest().body(Map.of("error", "Member not found"));
            }

            userDisplayName = member.getName();
            userEmail = member.getEmail();
            userId = member.getId();
            System.out.println("✅ Member found: " + userDisplayName + " (" + userEmail + ")");

        } else {
            System.out.println("❌ Neither username nor sessionId provided");
            return ResponseEntity.badRequest().body(Map.of("error", "Username or sessionId required"));
        }

        // Generate challenge
        byte[] challenge = new byte[32];
        secureRandom.nextBytes(challenge);
        String challengeBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(challenge);

        System.out.println("🎲 Generated challenge: " + challengeBase64.substring(0, 16) + "...");

        // Create WebAuthn registration options
        Map<String, Object> publicKeyCredentialCreationOptions = new HashMap<>();

        // Relying Party
        Map<String, String> rp = new HashMap<>();
        rp.put("name", "SOA Backend");
        rp.put("id", "localhost");
        publicKeyCredentialCreationOptions.put("rp", rp);

        // User
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id",
                Base64.getUrlEncoder().withoutPadding().encodeToString(userId.toString().getBytes()));
        userInfo.put("name", userDisplayName);
        userInfo.put("displayName", userDisplayName);
        publicKeyCredentialCreationOptions.put("user", userInfo);

        publicKeyCredentialCreationOptions.put("challenge", challengeBase64);

        // Supported algorithms
        publicKeyCredentialCreationOptions.put("pubKeyCredParams", new Object[] {
                Map.of("type", "public-key", "alg", -7), // ES256
                Map.of("type", "public-key", "alg", -257) // RS256
        });

        publicKeyCredentialCreationOptions.put("timeout", 60000);
        publicKeyCredentialCreationOptions.put("attestation", "none");

        Map<String, Object> authenticatorSelection = new HashMap<>();
        authenticatorSelection.put("authenticatorAttachment", "platform");
        authenticatorSelection.put("userVerification", "required");
        publicKeyCredentialCreationOptions.put("authenticatorSelection", authenticatorSelection);

        // Store challenge in session (in production, use proper session management)
        // For now, we'll return it and expect it back

        System.out.println("📤 Sending registration options to frontend");
        System.out.println("================================");

        return ResponseEntity.ok(Map.of("publicKey", publicKeyCredentialCreationOptions));
    }

    @PostMapping("/register/finish")
    public ResponseEntity<Map<String, Object>> finishRegistration(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== PASSKEY REGISTRATION FINISH ===");
            String username = (String) request.get("username");
            String sessionId = (String) request.get("sessionId");
            Map<String, Object> credential = (Map<String, Object>) request.get("credential");

            System.out.println("📝 Username: " + username);
            System.out.println("🔑 Session ID: " + sessionId);
            System.out.println("🔑 Credential received from frontend:");
            System.out.println("   - ID: " + credential.get("id"));
            System.out.println("   - Type: " + credential.get("type"));

            Member member = null;

            if (sessionId != null) {
                // 새로운 회원가입 플로우: 임시 데이터에서 Member 생성
                System.out.println("🔍 Creating new member from temp registration data...");
                MemberRegistrationDto tempData = tempRegistrationService.getTempRegistration(sessionId);
                if (tempData == null) {
                    System.out.println("❌ Temp registration data not found for sessionId: " + sessionId);
                    return ResponseEntity.badRequest().body(Map.of("error", "Registration session expired"));
                }

                // 새로운 Member 생성
                member = new Member();
                member.setName(tempData.getName());
                member.setEmail(tempData.getEmail());
                member.setPhone(tempData.getPhone());
                member.setRole(tempData.getRole());
                member.setGovernmentSupport(tempData.getGovernmentSupport());
                member.setBirthDate(tempData.getBirthDate());
                member.setAddress(tempData.getAddress());
                member.setCareer(tempData.getCareer());
                member.setCertification(tempData.getCertification());
                member.setApproved(false); // 기본값

                // Member 저장
                memberDetailsService.saveMember(member);
                System.out.println("✅ New member created: " + member.getName() + " (" + member.getEmail() + ")");

                // 임시 데이터 삭제
                tempRegistrationService.removeTempRegistration(sessionId);
                System.out.println("🗑️ Temp registration data cleaned up");

            } else if (username != null) {
                // 기존 플로우: 이미 생성된 Member 찾기
                System.out.println("🔍 Looking up existing member...");
                member = memberDetailsService.findMemberByUsername(username);
                if (member == null) {
                    System.out.println("❌ Member not found for username: " + username);
                    return ResponseEntity.badRequest().body(Map.of("error", "Member not found"));
                }
                System.out.println("✅ Existing member found: " + member.getName());

            } else {
                System.out.println("❌ Neither username nor sessionId provided");
                return ResponseEntity.badRequest().body(Map.of("error", "Username or sessionId required"));
            }

            String credentialId = (String) credential.get("id");
            Map<String, Object> response = (Map<String, Object>) credential.get("response");
            String publicKeyBase64 = (String) response.get("publicKey");

            System.out.println("📋 Processing credential data:");
            System.out.println("   - Credential ID: " + credentialId);
            System.out.println("   - Public Key (first 20 chars): " + (publicKeyBase64 != null
                    ? publicKeyBase64.substring(0, Math.min(20, publicKeyBase64.length())) + "..."
                    : "null"));

            // In a real implementation, you would verify the attestation
            // For this demo, we'll just store the credential

            byte[] publicKey = Base64.getUrlDecoder().decode(publicKeyBase64);

            MemberCredential memberCredential = new MemberCredential(
                    credentialId,
                    publicKey,
                    0L,
                    "Default Passkey",
                    member);

            memberDetailsService.saveCredential(memberCredential);

            // IMPORTANT: Update the Member entity to link it to the credential
            member.setCredentialId(credentialId);
            memberDetailsService.saveMember(member);

            System.out.println("💾 Passkey credential saved successfully!");
            System.out.println("   - Member: " + member.getName());
            System.out.println("   - Credential Label: " + memberCredential.getLabel());
            System.out.println("   - Member credentialId updated: " + credentialId);
            System.out.println("✅ Registration completed successfully!");
            System.out.println("================================");

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/authenticate/begin")
    public ResponseEntity<Map<String, Object>> beginAuthentication() {
        System.out.println("=== PASSKEY AUTHENTICATION BEGIN ===");

        // Generate challenge
        byte[] challenge = new byte[32];
        secureRandom.nextBytes(challenge);
        String challengeBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(challenge);

        System.out.println("🎲 Generated auth challenge: " + challengeBase64.substring(0, 16) + "...");

        Map<String, Object> publicKeyCredentialRequestOptions = new HashMap<>();
        publicKeyCredentialRequestOptions.put("challenge", challengeBase64);
        publicKeyCredentialRequestOptions.put("timeout", 60000);
        publicKeyCredentialRequestOptions.put("rpId", "localhost");
        publicKeyCredentialRequestOptions.put("userVerification", "required");

        System.out.println("📤 Sending authentication options to frontend");
        System.out.println("================================");

        return ResponseEntity.ok(Map.of("publicKey", publicKeyCredentialRequestOptions));
    }

    @PostMapping("/authenticate/finish")
    public ResponseEntity<Map<String, Object>> finishAuthentication(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== PASSKEY AUTHENTICATION FINISH ===");
            Map<String, Object> credential = (Map<String, Object>) request.get("credential");
            String credentialId = (String) credential.get("id");

            System.out.println("🔑 Authentication credential received:");
            System.out.println("   - Credential ID: " + credentialId);
            System.out.println("   - Type: " + credential.get("type"));

            MemberCredential memberCredential = memberDetailsService.findCredentialByCredentialId(credentialId);
            if (memberCredential == null) {
                System.out.println("❌ Credential not found in database: " + credentialId);
                System.out.println("🚨 SECURITY: Unauthorized passkey authentication attempt");
                System.out.println("   - Attempted credential ID: " + credentialId);
                System.out.println("   - Request IP: " + request.get("clientIP")); // if available
                System.out.println("❌ Authentication FAILED - returning error");
                System.out.println("================================");
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Authentication failed",
                        "success", false));
            }

            System.out.println("✅ Credential found for member: " + memberCredential.getMember().getName());

            // In a real implementation, you would verify the assertion
            // For this demo, we'll authenticate the user with Spring Security

            // Create and authenticate the passkey token
            PasskeyAuthenticationToken authToken = new PasskeyAuthenticationToken(credentialId);
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("🔐 User authenticated successfully!");
            System.out.println("   - Member: " + memberCredential.getMember().getName());
            System.out.println("   - Authentication set in SecurityContext");

            // Generate and store JWT token
            Member member = memberCredential.getMember();
            String accessToken = tokenService.generateAndStoreAccessToken(
                    member.getName(),
                    member.getId(),
                    member.getRole() != null ? member.getRole().toString() : "USER");

            String refreshToken = tokenService.generateAndStoreRefreshToken(
                    member.getName(),
                    member.getId(),
                    member.getRole() != null ? member.getRole().toString() : "USER");

            System.out.println("🌰 JWT tokens generated and stored:");
            System.out.println("   🌰 ACCESS TOKEN: " + accessToken);
            System.out.println("   🔄 REFRESH TOKEN: " + refreshToken);
            System.out.println("   Stored in memory repository");
            System.out.println("✅ Authentication completed successfully!");
            System.out.println("================================");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", member.getName(),
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "userId", member.getId(),
                    "role", member.getRole() != null ? member.getRole().toString() : "USER"));
        } catch (Exception e) {
            System.out.println("❌ AUTHENTICATION EXCEPTION:");
            System.out.println("   - Error: " + e.getMessage());
            System.out.println("   - Type: " + e.getClass().getSimpleName());
            System.out.println("🚨 SECURITY: Authentication failed due to exception");
            System.out.println("❌ NO TOKENS GENERATED");
            System.out.println("================================");
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Authentication failed",
                    "success", false));
        }
    }
}