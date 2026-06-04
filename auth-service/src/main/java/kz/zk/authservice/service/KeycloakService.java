package kz.zk.authservice.service;

import kz.zk.authservice.dto.KeycloakUser;
import kz.zk.authservice.dto.TokenResponse;

public interface KeycloakService {
        String createUser(KeycloakUser user);
        void deleteUser(String keycloakId);
        TokenResponse login(String username, String password);
}
