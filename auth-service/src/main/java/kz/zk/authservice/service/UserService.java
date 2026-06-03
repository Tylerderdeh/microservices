package kz.zk.authservice.service;

import kz.zk.authservice.dto.RegistrationRequest;

public interface UserService {
    void validateUniqueness(RegistrationRequest request);
    void saveUser(RegistrationRequest request, String keycloakId);
}
