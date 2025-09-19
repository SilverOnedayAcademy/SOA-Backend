package chungbuk.soabackend.token;

import java.util.List;
import java.util.Optional;

public interface AccessTokenRepository {

    AccessToken save(AccessToken token);

    Optional<AccessToken> findByTokenValue(String tokenValue);

    List<AccessToken> findByMemberId(Long memberId);

    List<AccessToken> findByMemberIdAndTokenType(Long memberId, TokenType tokenType);

    void deleteExpiredTokens();

    void revokeTokensByMemberId(Long memberId);

    void revokeToken(String tokenValue);

    void deleteById(Long id);

    List<AccessToken> findAll();
}