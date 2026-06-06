package kz.zk.authservice.service;

import kz.zk.authservice.dto.RegistrationRequest;
import kz.zk.authservice.entity.User;

import java.util.UUID;

public interface UserService {
    void validateUniqueness(RegistrationRequest request);
    void saveUser(RegistrationRequest request, String keycloakId, String verificationToken);
    boolean isUserInactive(UUID keycloakId);
    void updateLastLoginTime(UUID keycloakId);
    User verifyAndUpdateEmail(String token);
    User prepareResendVerification(User user);
}
