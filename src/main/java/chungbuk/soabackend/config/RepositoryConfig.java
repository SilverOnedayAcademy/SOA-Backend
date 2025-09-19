package chungbuk.soabackend.config;

import chungbuk.soabackend.member.MemberRepository;
import chungbuk.soabackend.member.MemberCredentialRepository;
import chungbuk.soabackend.member.MemoryMemberRepository;
import chungbuk.soabackend.member.MemoryMemberCredentialRepository;
import chungbuk.soabackend.token.AccessTokenRepository;
import chungbuk.soabackend.token.MemoryAccessTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    @Bean
    public MemberCredentialRepository memberCredentialRepository() {
        return new MemoryMemberCredentialRepository();
    }

    @Bean
    public AccessTokenRepository accessTokenRepository() {
        return new MemoryAccessTokenRepository();
    }
}