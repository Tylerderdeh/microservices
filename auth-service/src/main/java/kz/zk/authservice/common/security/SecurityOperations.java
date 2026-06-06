package kz.zk.authservice.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
public class SecurityOperations {

    public static Optional<String> getUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
                Jwt credentials = (Jwt) authentication.getCredentials();
                if (credentials != null) {
                    return Optional.of(credentials.getClaimAsString("preferred_username"));
                }
        }
        return Optional.empty();
    }
}