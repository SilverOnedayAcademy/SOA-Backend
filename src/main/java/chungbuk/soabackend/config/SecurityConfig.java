package chungbuk.soabackend.config;

import chungbuk.soabackend.service.PasskeyMemberDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private PasskeyMemberDetailsService memberDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private PasskeyAuthenticationProvider passkeyAuthenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity in testing
                .authorizeHttpRequests(authz -> authz
                        // Public web pages and static resources
                        .requestMatchers("/", "/login", "/register", "/setup-passkey", "/webauthn/**", "/css/**",
                                "/js/**", "/static/**")
                        .permitAll()
                        // Auth endpoints (refresh token, logout) - no authentication needed
                        .requestMatchers("/api/auth/refresh", "/api/auth/token-status").permitAll()
                        // Other API endpoints require authentication (will use JWT)
                        .requestMatchers("/api/**").authenticated()
                        // All other requests require authentication
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .maximumSessions(1))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .formLogin(form -> form.disable()) // Disable traditional form login completely
                .httpBasic(basic -> basic.disable()) // Disable HTTP Basic authentication
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Redirect to login page for unauthenticated requests
                            response.sendRedirect("/login");
                        }))
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(passkeyAuthenticationProvider));
    }
}