package kz.zk.authservice.service.impl;

import kz.zk.authservice.common.exception.BusinessValidationException;
import kz.zk.authservice.dto.RegistrationRequest;
import kz.zk.authservice.entity.User;
import kz.zk.authservice.entity.enums.UserStatus;
import kz.zk.authservice.repository.UserRepository;
import kz.zk.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public void validateUniqueness(RegistrationRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessValidationException("auth.register.email.exists");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new BusinessValidationException("auth.register.username.exists");
        }
    }

    @Override
    @Transactional
    public void saveUser(RegistrationRequest request, String keycloakId, String verificationToken) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .keycloakId(UUID.fromString(keycloakId))
                .status(UserStatus.ACTIVE)
                .emailVerificationToken(verificationToken)
                .emailVerificationTokenExpiry(Instant.now().plusSeconds(24 * 3600)) // токен действителен 24 часа
                .build();

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserInactive(UUID keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .map(user -> user.getStatus() == UserStatus.INACTIVE)
                .orElse(false);
    }

    @Override
    @Transactional
    public void updateLastLoginTime(UUID keycloakId) {
        userRepository.findByKeycloakId(keycloakId)
                .ifPresent(user -> {
                    user.setLastLoginAt(Instant.now());
                    userRepository.save(user);
                    log.debug("Updated last login time for user: {}", user.getUsername());
                });
    }

    @Override
    @Transactional
    public User verifyAndUpdateEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BusinessValidationException("auth.token.invalid"));

        if (user.getEmailVerificationTokenExpiry().isBefore(Instant.now())) {
            throw new BusinessValidationException("auth.token.expired");
        }

        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        return user;
    }

    @Override
    @Transactional
    public User prepareResendVerification(User user) {
        String verificationToken = UUID.randomUUID().toString();
        Instant expiryTime = Instant.now().plusSeconds(24 * 3600);

        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(expiryTime);
        user.setLastVerificationEmailSentAt(Instant.now());
        return user;
    }
}
