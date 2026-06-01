package kz.zk.authservice.service;

import jakarta.transaction.Transactional;
import kz.zk.authservice.dto.LoginRequest;
import kz.zk.authservice.dto.RegistrationRequest;
import kz.zk.authservice.dto.TokenResponse;
import kz.zk.authservice.service.impl.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void register(RegistrationRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessValidationException("auth.register.email.exists");
        }

    }

    @Override
    public TokenResponse login(LoginRequest request) {
        return null;
    }
}