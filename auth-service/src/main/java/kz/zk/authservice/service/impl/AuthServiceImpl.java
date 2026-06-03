package kz.zk.authservice.service.impl;

import jakarta.transaction.Transactional;
import kz.zk.authservice.dto.KeycloakUser;
import kz.zk.authservice.dto.LoginRequest;
import kz.zk.authservice.dto.RegistrationRequest;
import kz.zk.authservice.dto.TokenResponse;
import kz.zk.authservice.repository.UserRepository;
import kz.zk.authservice.service.AuthService;
import kz.zk.authservice.service.KeycloakService;
import kz.zk.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KeycloakService keycloakService;
    private final UserService userService;

    @Override
    public void register(RegistrationRequest request) {
        userService.validateUniqueness(request);

        String keycloakId = keycloakService.createUser(KeycloakUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .enabled(true)
                .emailVerified(false)
                .realmRoles(List.of("user"))
                .build());

        try {
            userService.saveUser(request, keycloakId);
        } catch (Exception e) {
            keycloakService.deleteUser(keycloakId);
            throw e;
        }

    }

    @Override
    public TokenResponse login(LoginRequest request) {
        return null;
    }
}