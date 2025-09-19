package chungbuk.soabackend.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryMemberRepository implements MemberRepository {
    private static final ConcurrentHashMap<Long, Member> store = new ConcurrentHashMap<>();
    private static final AtomicLong sequence = new AtomicLong(0);




    @Override
    public Member save(Member member) {
        Long id = sequence.incrementAndGet();
        member.setId(id);
        store.put(id, member);
        return member;
    }

    @Override
    public Member findById(Long id) {
        return store.get(id);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return store.values().stream()
                .filter(m -> m.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        return store.values().stream()
                .filter(m -> m.getName().equals(username))
                .findFirst();
    }

    @Override
    public List<Member> findAll() {
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