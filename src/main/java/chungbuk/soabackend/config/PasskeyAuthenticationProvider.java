package chungbuk.soabackend.config;

import chungbuk.soabackend.member.Member;
import chungbuk.soabackend.member.MemberCredential;
import chungbuk.soabackend.service.PasskeyMemberDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class PasskeyAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private PasskeyMemberDetailsService memberDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof PasskeyAuthenticationToken) {
            PasskeyAuthenticationToken passkeyAuth = (PasskeyAuthenticationToken) authentication;
            String credentialId = passkeyAuth.getCredentialId();

            // Find member by credential ID
            MemberCredential credential = memberDetailsService.findCredentialByCredentialId(credentialId);
            if (credential == null) {
                throw new BadCredentialsException("Invalid passkey credential");
            }

            Member member = credential.getMember();
            if (member == null) {
                throw new BadCredentialsException("Member not found for credential");
            }

            // Create authenticated token
            User userDetails = new User(
                member.getName(),
                "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            return new PasskeyAuthenticationToken(
                userDetails,
                credentialId,
                userDetails.getAuthorities()
            );
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PasskeyAuthenticationToken.class.isAssignableFrom(authentication);
    }
}