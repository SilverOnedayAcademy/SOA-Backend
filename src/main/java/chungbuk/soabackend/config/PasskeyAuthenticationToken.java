package chungbuk.soabackend.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class PasskeyAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String credentialId;

    // Constructor for unauthenticated token
    public PasskeyAuthenticationToken(String credentialId) {
        super(null);
        this.principal = null;
        this.credentialId = credentialId;
        setAuthenticated(false);
    }

    // Constructor for authenticated token
    public PasskeyAuthenticationToken(Object principal, String credentialId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentialId = credentialId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentialId;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getCredentialId() {
        return credentialId;
    }
}