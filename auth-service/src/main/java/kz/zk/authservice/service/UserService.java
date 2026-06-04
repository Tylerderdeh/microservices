package kz.zk.authservice.service;

import kz.zk.authservice.dto.RegistrationRequest;

import java.util.UUID;

public interface UserService {
    void validateUniqueness(RegistrationRequest request);
    void saveUser(RegistrationRequest request, String keycloakId);
    boolean isUserInactive(UUID keycloakId);
    void updateLastLoginTime(UUID keycloakId);
}
