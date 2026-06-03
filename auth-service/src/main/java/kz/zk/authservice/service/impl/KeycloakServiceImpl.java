package kz.zk.authservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import kz.zk.authservice.common.exception.BusinessValidationException;
import kz.zk.authservice.dto.KeycloakUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import kz.zk.authservice.service.KeycloakService;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private final Keycloak keycloak;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Override
    public String createUser(KeycloakUser keycloakUser) {
        UsersResource usersResource = keycloak.realm(realm).users();

        try (Response response = usersResource.create(keycloakUser.toUserRepresentation())) {
            if (response.getStatus() == 409) {
                handle409(response, keycloakUser);
            }

            if (response.getStatus() != 201) {
                log.warn("Keycloak user creation failed with status {}", response.getStatus());
                throw new BusinessValidationException("keycloak.user.create.error");
            }

            if (response.getLocation() == null) {
                log.error("Keycloak response location is null. Status: {}", response.getStatus());
                throw new BusinessValidationException("keycloak.user.create.error");
            }

            String userId = extractUserId(response);
            assignRealmRoles(usersResource, userId, keycloakUser.getRealmRoles());
            return userId;

        } catch (BusinessValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating user in Keycloak: {}", e.getMessage(), e);
            throw new BusinessValidationException("keycloak.user.create.error");
        }
    }

    private void handle409(Response response, KeycloakUser keycloakUser) {
        String errorMessage = "User creation conflict in Keycloak. Status: 409";

        try {
            ErrorRepresentation errorRep = response.readEntity(ErrorRepresentation.class);
            if (errorRep != null && errorRep.getErrorMessage() != null && !errorRep.getErrorMessage().isEmpty()) {
                errorMessage = errorRep.getErrorMessage();
            }
        } catch (Exception e) {
            log.warn("Could not parse Keycloak 409 response: {}", e.getMessage());
        }

        log.warn("Keycloak user creation failed with 409 Conflict: {}", errorMessage);

        throw switch (errorMessage) {
            case "User exists with same username" ->
                    new BusinessValidationException("keycloak.user.create.error.username.exists", keycloakUser.getUsername());
            case "User exists with same email" ->
                    new BusinessValidationException("keycloak.user.create.error.email.exists", keycloakUser.getEmail());
            default ->
                    new BusinessValidationException("keycloak.user.create.error");
        };
    }

    private String extractUserId(Response response) {
        String path = response.getLocation().getPath();
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    private void assignRealmRoles(UsersResource usersResource, String userId, List<String> roleNames) {
        List<RoleRepresentation> roles = roleNames.stream()
                .map(name -> keycloak.realm(realm).roles().get(name).toRepresentation())
                .toList();

        usersResource.get(userId).roles().realmLevel().add(roles);
    }
}
