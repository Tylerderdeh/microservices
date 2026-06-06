package kz.zk.authservice.service.impl;

import kz.zk.authservice.common.exception.BusinessValidationException;
import kz.zk.authservice.dto.KeycloakUser;
import kz.zk.authservice.dto.LoginRequest;
import kz.zk.authservice.dto.RegistrationRequest;
import kz.zk.authservice.dto.TokenResponse;
import kz.zk.authservice.entity.User;
import kz.zk.authservice.repository.UserRepository;
import kz.zk.authservice.service.AuthService;
import kz.zk.authservice.service.EmailService;
import kz.zk.authservice.service.KeycloakService;
import kz.zk.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KeycloakService keycloakService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;

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

        String verificationToken = UUID.randomUUID().toString();
        String locale = LocaleContextHolder.getLocale().getLanguage();

        try {
            userService.saveUser(request, keycloakId, verificationToken);
        } catch (Exception e) {
            keycloakService.deleteUser(keycloakId);
            throw e;
        }

        emailService.sendVerificationEmail(request.getEmail(), verificationToken, locale);

    }

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new BusinessValidationException("auth.login.failed"));

        if (userService.isUserInactive(user.getKeycloakId())) {
            throw new BusinessValidationException("user.inactive");
        }

        TokenResponse response = keycloakService.login(user.getUsername(), request.getPassword());

        userService.updateLastLoginTime(user.getKeycloakId());

        return response;
    }

    @Override
    public void verifyEmail(String token) {
        User user = userService.verifyAndUpdateEmail(token);

        keycloakService.verifyEmail(user.getKeycloakId().toString());
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessValidationException("auth.email.not_found"));

        if (keycloakService.isEmailVerified(user.getKeycloakId().toString())) {
            throw new BusinessValidationException("auth.email.already_verified");
        }

        if (user.getLastVerificationEmailSentAt() != null &&
                Instant.now().isBefore(user.getLastVerificationEmailSentAt().plusSeconds(60))) {
            throw new BusinessValidationException("auth.email.resend_too_soon");
        }

        String locale = LocaleContextHolder.getLocale().getLanguage();

        userService.prepareResendVerification(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getEmailVerificationToken(), locale);
        log.info("Resent verification email to: {}", email);
    }
}