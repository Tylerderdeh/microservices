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
    public void saveUser(RegistrationRequest request, String keycloakId) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .keycloakId(UUID.fromString(keycloakId))
                .status(UserStatus.ACTIVE)
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
    @Transactional(readOnly = true)
    public void updateLastLoginTime(UUID keycloakId) {
        userRepository.findByKeycloakId(keycloakId)
                .ifPresent(user -> {
                    user.setLastLoginAt(Instant.now());
                    userRepository.save(user);
                    log.debug("Updated last login time for user: {}", user.getUsername());
                });
    }
}
