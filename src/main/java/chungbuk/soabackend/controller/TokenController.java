package chungbuk.soabackend.controller;

import chungbuk.soabackend.service.TokenService;
import chungbuk.soabackend.service.JwtService;
import chungbuk.soabackend.token.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        System.out.println("🔄 REFRESH TOKEN REQUEST:");
        System.out.println("   🎫 Refresh token (first 30 chars): " +
                refreshToken.substring(0, Math.min(30, refreshToken.length())) + "...");

        try {
            // Validate refresh token
            if (!tokenService.isTokenValid(refreshToken)) {
                System.out.println("   ❌ Refresh token is invalid");
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid refresh token"));
            }

            // Extract user info from refresh token
            String username = jwtService.extractUsername(refreshToken);
            Long userId = jwtService.extractUserId(refreshToken);
            String role = jwtService.extractRole(refreshToken);

            System.out.println("   ✅ Refresh token is valid");
            System.out.println("   👤 Username: " + username);
            System.out.println("   🆔 User ID: " + userId);
            System.out.println("   🎭 Role: " + role);

            // Generate new access token AND new refresh token
            String newAccessToken = tokenService.generateAndStoreAccessToken(username, userId, role);
            String newRefreshToken = tokenService.generateAndStoreRefreshToken(username, userId, role);

            // Revoke the old refresh token for security
            tokenService.revokeToken(refreshToken);

            System.out.println("   🌰 New access token generated!");
            System.out.println("   🔄 New refresh token generated!");
            System.out.println("   🔒 Old refresh token revoked");
            System.out.println("   ✅ Token rotation completed successfully");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken,
                    "message", "Tokens refreshed and rotated successfully"));

        } catch (Exception e) {
            System.out.println("   ❌ Token refresh failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Token refresh failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody Map<String, String> request) {
        String accessToken = request.get("accessToken");
        String refreshToken = request.get("refreshToken");

        System.out.println("🚪 LOGOUT REQUEST:");

        try {
            // Extract user ID from token
            Long userId = jwtService.extractUserId(accessToken != null ? accessToken : refreshToken);
            System.out.println("   👤 User ID: " + userId);

            // Revoke all user tokens
            tokenService.revokeUserTokens(userId);

            System.out.println("   🔒 All user tokens revoked");
            System.out.println("   ✅ Logout successful");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Logged out successfully"));

        } catch (Exception e) {
            System.out.println("   ❌ Logout failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Logout failed: " + e.getMessage()));
        }
    }

    @GetMapping("/token-status")
    public ResponseEntity<Map<String, Object>> getTokenStatus(@RequestParam String token) {
        System.out.println("📊 TOKEN STATUS REQUEST:");

        Optional<AccessToken> accessToken = tokenService.findTokenByValue(token);

        if (accessToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token not found"));
        }

        AccessToken tokenEntity = accessToken.get();

        return ResponseEntity.ok(Map.of(
                "tokenId", tokenEntity.getId(),
                "memberId", tokenEntity.getMemberId(),
                "tokenType", tokenEntity.getTokenType().toString(),
                "issuedAt", tokenEntity.getIssuedAt().toString(),
                "expiresAt", tokenEntity.getExpiresAt().toString(),
                "isExpired", tokenEntity.isExpired(),
                "isRevoked", tokenEntity.isRevoked(),
                "isValid", tokenEntity.isValid()));
    }
}