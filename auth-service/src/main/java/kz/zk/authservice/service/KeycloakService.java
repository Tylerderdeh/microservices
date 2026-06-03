package kz.zk.authservice.service;

import kz.zk.authservice.dto.KeycloakUser;

public interface KeycloakService {
        String createUser(KeycloakUser user);
}
