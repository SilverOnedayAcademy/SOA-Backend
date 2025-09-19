package chungbuk.soabackend.token;

import java.time.LocalDateTime;

public class AccessToken {
    private Long id;
    private String tokenValue;
    private Long memberId;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private boolean isRevoked;
    private TokenType tokenType;

    public AccessToken() {
    }

    public AccessToken(String tokenValue, Long memberId, LocalDateTime issuedAt, LocalDateTime expiresAt,
            TokenType tokenType) {
        this.tokenValue = tokenValue;
        this.memberId = memberId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.tokenType = tokenType;
        this.isRevoked = false;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked && !isExpired();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }
}