package chungbuk.soabackend.member;

import java.util.List;

public interface MemberCredentialRepository {
    MemberCredential save(MemberCredential credential);
    MemberCredential findById(Long id);
    MemberCredential findByCredentialId(String credentialId);
    List<MemberCredential> findByMember(Member member);
    List<MemberCredential> findAll();
    void delete(Long id);
}
