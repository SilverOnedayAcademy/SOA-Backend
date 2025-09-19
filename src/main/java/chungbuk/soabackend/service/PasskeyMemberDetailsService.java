package chungbuk.soabackend.service;

import chungbuk.soabackend.member.Member;
import chungbuk.soabackend.member.MemberRepository;
import chungbuk.soabackend.member.MemberCredential;
import chungbuk.soabackend.member.MemberCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class PasskeyMemberDetailsService implements UserDetailsService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberCredentialRepository memberCredentialRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(member.getName()) // Using name as username
                .password("{noop}") // No password needed for passkey authentication
                .authorities(Collections.emptyList())
                .build();
    }

    public Member findMemberByUsername(String username) {
        return memberRepository.findByUsername(username).orElse(null);
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElse(null);
    }

    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }

    public MemberCredential saveCredential(MemberCredential credential) {
        return memberCredentialRepository.save(credential);
    }

    public MemberCredential findCredentialByCredentialId(String credentialId) {
        return memberCredentialRepository.findByCredentialId(credentialId);
    }
}
