package chungbuk.soabackend.member;

import chungbuk.soabackend.member.Member;
import chungbuk.soabackend.member.MemberRepository;
import chungbuk.soabackend.member.MemoryMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

public class MemoryMemberRepositoryTest {
    private MemoryMemberRepository repository;
    @BeforeEach
    void setUp() {
        repository = new MemoryMemberRepository();
//        ((MemoryMemberRepository) repository).clearStore(); // 다운캐스팅
        repository.clearStore();
    }

    @Test
    void save_and_findById() {
        Member member = new Member();
        member.setCredentialId("이재용 이재명 이상명 레쓰고");
        member.setEmail("guardian@test.com");
        member.setName("홍길동");
        member.setRole(MemberRole.GUARDIAN);
        Member saved = repository.save(member);

        Member found = repository.findById(saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("홍길동");
        assertThat(found.getCredentialId()).isEqualTo("이재용 이재명 이상명 레쓰고");
        System.out.println(found.getCredentialId());
        assertThat(found.getRole()).isEqualTo(MemberRole.GUARDIAN);
    }

    @Test
    void findByEmail() {
        Member member = new Member();
        member.setEmail("tutor@test.com");
        member.setName("이순신");
        member.setRole(MemberRole.TUTOR);

        repository.save(member);

        Member found = repository.findByEmail("tutor@test.com").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("이순신");
    }

    @Test
    void findAll_and_delete() {
        Member m1 = new Member();
        m1.setEmail("m1@test.com");
        m1.setName("멤버1");
        repository.save(m1);

        Member m2 = new Member();
        m2.setEmail("m2@test.com");
        m2.setName("멤버2");
        repository.save(m2);

        List<Member> all = repository.findAll();
        assertThat(all.size()).isEqualTo(2);

        repository.delete(m1.getId());
        List<Member> afterDelete = repository.findAll();
        assertThat(afterDelete.size()).isEqualTo(1);
        assertThat(afterDelete.get(0).getName()).isEqualTo("멤버2");
    }
}