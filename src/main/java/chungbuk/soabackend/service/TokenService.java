package chungbuk.soabackend.service;

import chungbuk.soabackend.token.AccessToken;
import chungbuk.soabackend.token.AccessTokenRepository;
import chungbuk.soabackend.token.TokenType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TokenService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    public String generateAndStoreAccessToken(String username, Long userId, String role) {
        System.out.println("🌰 GENERATING ACCESS TOKEN:");
        System.out.println("   👤 Username: " + username);
        System.out.println("   🆔 User ID: " + userId);
        System.out.println("   🎭 Role: " + role);

        // Generate JWT token
        String jwtToken = jwtService.generateToken(username, userId, role);

        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusMinutes(15);

        System.out.println("   📅 Issued at: " + issuedAt);
        System.out.println("   ⏰ Expires at: " + expiresAt);
        System.out.println("   ⏳ Valid for: 15 minutes");

        // Create AccessToken entity
        AccessToken accessToken = new AccessToken(
                jwtToken,
                userId,
                issuedAt,
                expiresAt, // 15 minutes expiration
                TokenType.ACCESS);

        // Store in repository
        accessTokenRepository.save(accessToken);

        System.out.println("   💾 Token stored in repository");
        System.out.println(
                "   🌰 Token (first 30 chars): " + jwtToken.substring(0, Math.min(30, jwtToken.length())) + "...");

        return jwtToken;
    }

    public String generateAndStoreRefreshToken(String username, Long userId, String role) {
        System.out.println("🔄 GENERATING REFRESH TOKEN:");
        System.out.println("   👤 Username: " + username);
        System.out.println("   🆔 User ID: " + userId);
        System.out.println("   🎭 Role: " + role);

        // Generate refresh token (longer expiration)
        String refreshToken = jwtService.generateToken(username, userId, role);

        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusDays(7);

        System.out.println("   📅 Issued at: " + issuedAt);
        System.out.println("   ⏰ Expires at: " + expiresAt);
        System.out.println("   ⏳ Valid for: 7 days");

        // Create RefreshToken entity
        AccessToken refreshTokenEntity = new AccessToken(
                refreshToken,
                userId,
                issuedAt,
                expiresAt, // 7 days expiration
                TokenType.REFRESH);

        // Store in repository
        accessTokenRepository.save(refreshTokenEntity);

        System.out.println("   💾 Refresh token stored in repository");
        System.out.println("   🔄 Token (first 30 chars): "
                + refreshToken.substring(0, Math.min(30, refreshToken.length())) + "...");

        return refreshToken;
    }

    public boolean isTokenValid(String tokenValue) {
        System.out.println("🔍 TOKEN VALIDATION CHECK:");
        System.out.println(
                "   Token (first 20 chars): " + tokenValue.substring(0, Math.min(20, tokenValue.length())) + "...");

        Optional<AccessToken> token = accessTokenRepository.findByTokenValue(tokenValue);

        if (token.isEmpty()) {
            System.out.println("   ❌ Token not found in repository");
            return false;
        }

        AccessToken accessToken = token.get();
        System.out.println("   ✅ Token found in repository");
        System.out.println("   📅 Issued at: " + accessToken.getIssuedAt());
        System.out.println("   ⏰ Expires at: " + accessToken.getExpiresAt());
        System.out.println("   🕐 Current time: " + LocalDateTime.now());
        System.out.println("   🔒 Is revoked: " + accessToken.isRevoked());
        System.out.println("   ⏳ Is expired: " + accessToken.isExpired());
        System.out.println("   🌰 Token type: " + accessToken.getTokenType());

        // Check if token is valid (not revoked and not expired)
        if (!accessToken.isValid()) {
            if (accessToken.isRevoked()) {
                System.out.println("   🚨 SECURITY ALERT: Revoked token used! Possible token theft detected.");
                // In production, you might want to revoke all user tokens here
                revokeUserTokens(accessToken.getMemberId());
                System.out.println("   🔒 All user tokens revoked as security measure");
            }
            System.out.println("   ❌ Token is invalid (revoked or expired)");
            return false;
        }

        // Also validate JWT token itself
        try {
            String username = jwtService.extractUsername(tokenValue);
            boolean jwtValid = jwtService.validateToken(tokenValue, username);
            System.out.println("   🌰 JWT validation: " + (jwtValid ? "✅ Valid" : "❌ Invalid"));
            System.out.println("   👤 Username from JWT: " + username);
            return jwtValid;
        } catch (Exception e) {
            System.out.println("   ❌ JWT validation failed: " + e.getMessage());
            return false;
        }
    }

    public void revokeUserTokens(Long userId) {
        accessTokenRepository.revokeTokensByMemberId(userId);
    }

    public void revokeToken(String tokenValue) {
        accessTokenRepository.revokeToken(tokenValue);
    }

    public List<AccessToken> getUserTokens(Long userId) {
        return accessTokenRepository.findByMemberId(userId);
    }

    public List<AccessToken> getUserAccessTokens(Long userId) {
        return accessTokenRepository.findByMemberIdAndTokenType(userId, TokenType.ACCESS);
    }

    public List<AccessToken> getUserRefreshTokens(Long userId) {
        return accessTokenRepository.findByMemberIdAndTokenType(userId, TokenType.REFRESH);
    }

    public void cleanupExpiredTokens() {
        accessTokenRepository.deleteExpiredTokens();
    }

    public Optional<AccessToken> findTokenByValue(String tokenValue) {
        return accessTokenRepository.findByTokenValue(tokenValue);
    }
}