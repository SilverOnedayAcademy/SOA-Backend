package chungbuk.soabackend.service;

import chungbuk.soabackend.token.AccessToken;
import chungbuk.soabackend.token.AccessTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TokenCleanupService {

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Scheduled(fixedRate = 60000) // Run every 1 minute
    public void showTokenStatus() {
        List<AccessToken> allTokens = accessTokenRepository.findAll();

        if (allTokens.isEmpty()) {
            return;
        }

        System.out.println("⏰ TOKEN STATUS CHECK (" + LocalDateTime.now() + "):");
        System.out.println("   📊 Total tokens: " + allTokens.size());

        long validTokens = allTokens.stream().filter(AccessToken::isValid).count();
        long expiredTokens = allTokens.stream().filter(AccessToken::isExpired).count();
        long revokedTokens = allTokens.stream().filter(AccessToken::isRevoked).count();

        long accessTokens = allTokens.stream().filter(t -> t.getTokenType().toString().equals("ACCESS")).count();
        long refreshTokens = allTokens.stream().filter(t -> t.getTokenType().toString().equals("REFRESH")).count();

        System.out.println("   ✅ Valid tokens: " + validTokens);
        System.out.println("   ⏳ Expired tokens: " + expiredTokens);
        System.out.println("   🔒 Revoked tokens: " + revokedTokens);
        System.out.println("   🌰 Access tokens: " + accessTokens);
        System.out.println("   🔄 Refresh tokens: " + refreshTokens);

        // Show details for each token
        for (AccessToken token : allTokens) {
            String status = token.isValid() ? "✅ VALID" : token.isExpired() ? "⏳ EXPIRED" : "🔒 REVOKED";

            long secondsUntilExpiry = java.time.Duration.between(
                    LocalDateTime.now(),
                    token.getExpiresAt()).getSeconds();

            String tokenTypeIcon = token.getTokenType().toString().equals("ACCESS") ? "🌰" : "🔄";

            System.out.println("   " + tokenTypeIcon + " " + token.getTokenType() + " Token ID " + token.getId() +
                    ": " + status + " (expires in " + secondsUntilExpiry + "s)");
        }
        System.out.println("   ================================");
    }

    @Scheduled(fixedRate = 300000) // Clean up every 5 minutes
    public void cleanupExpiredTokens() {
        System.out.println("🧹 CLEANING UP EXPIRED TOKENS...");
        accessTokenRepository.deleteExpiredTokens();
        System.out.println("   ✅ Cleanup completed");
    }
}