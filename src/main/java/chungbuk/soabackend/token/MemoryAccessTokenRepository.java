package chungbuk.soabackend.token;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class MemoryAccessTokenRepository implements AccessTokenRepository {

    private final ConcurrentHashMap<String, AccessToken> tokenStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<AccessToken>> memberTokenStore = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public AccessToken save(AccessToken token) {
        if (token.getId() == null) {
            token.setId(sequence.incrementAndGet());
        }

        // Store by token value
        tokenStore.put(token.getTokenValue(), token);

        // Store by member ID
        memberTokenStore.computeIfAbsent(token.getMemberId(), k -> new ArrayList<>()).add(token);

        return token;
    }

    @Override
    public Optional<AccessToken> findByTokenValue(String tokenValue) {
        return Optional.ofNullable(tokenStore.get(tokenValue));
    }

    @Override
    public List<AccessToken> findByMemberId(Long memberId) {
        return memberTokenStore.getOrDefault(memberId, new ArrayList<>());
    }

    @Override
    public List<AccessToken> findByMemberIdAndTokenType(Long memberId, TokenType tokenType) {
        return findByMemberId(memberId).stream()
                .filter(token -> token.getTokenType() == tokenType)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        // Remove from token store
        tokenStore.entrySet().removeIf(entry -> entry.getValue().getExpiresAt().isBefore(now));

        // Remove from member token store
        memberTokenStore.values().forEach(tokens -> tokens.removeIf(token -> token.getExpiresAt().isBefore(now)));
    }

    @Override
    public void revokeTokensByMemberId(Long memberId) {
        List<AccessToken> memberTokens = findByMemberId(memberId);
        memberTokens.forEach(token -> token.setRevoked(true));
    }

    @Override
    public void revokeToken(String tokenValue) {
        Optional<AccessToken> token = findByTokenValue(tokenValue);
        token.ifPresent(t -> t.setRevoked(true));
    }

    @Override
    public void deleteById(Long id) {
        tokenStore.entrySet().removeIf(entry -> entry.getValue().getId().equals(id));
        memberTokenStore.values().forEach(tokens -> tokens.removeIf(token -> token.getId().equals(id)));
    }

    @Override
    public List<AccessToken> findAll() {
        return new ArrayList<>(tokenStore.values());
    }

    public void clearStore() {
        tokenStore.clear();
        memberTokenStore.clear();
        sequence.set(0);
    }
}