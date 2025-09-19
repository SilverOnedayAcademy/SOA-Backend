package chungbuk.soabackend.member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MemoryMemberCredentialRepository implements MemberCredentialRepository {
    private static final ConcurrentHashMap<Long, MemberCredential> store = new ConcurrentHashMap<>();
    private static final AtomicLong sequence = new AtomicLong(0);

    @Override
    public MemberCredential save(MemberCredential credential) {
        if (credential.getId() == null) {
            Long id = sequence.incrementAndGet();
            credential.setId(id);
        }
        store.put(credential.getId(), credential);
        return credential;
    }

    @Override
    public MemberCredential findById(Long id) {
        return store.get(id);
    }

    @Override
    public MemberCredential findByCredentialId(String credentialId) {
        return store.values().stream()
                .filter(c -> c.getCredentialId().equals(credentialId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<MemberCredential> findByMember(Member member) {
        return store.values().stream()
                .filter(c -> c.getMember().getId().equals(member.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberCredential> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void delete(Long id) {
        store.remove(id);
    }

    public void clearStore() {
        store.clear();
        sequence.set(0);
    }
}