package chungbuk.soabackend.member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);

    Member findById(Long id);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUsername(String username);

    List<Member> findAll();

    void delete(Long id);
}