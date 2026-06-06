package kz.zk.authservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import kz.zk.authservice.common.exception.BusinessValidationException;
import kz.zk.authservice.dto.KeycloakUser;
import kz.zk.authservice.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import kz.zk.authservice.service.KeycloakService;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private final Keycloak keycloak;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.front-client-id}")
    private String frontClientId;

    @Value("${keycloak.front-client-secret}")
    private String frontClientSecret;

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

    @Override
    public void deleteUser(String keycloakId) {
        try (Response response = keycloak.realm(realm).users().delete(keycloakId)) {
            if (response.getStatus() != 204) {
                String errorMessage = "Failed to delete user in Keycloak. Status: " + response.getStatus();
                if (response.hasEntity()) {
                    errorMessage += ", Response: " + response.readEntity(String.class);
                }
                log.error(errorMessage);
                throw new BusinessValidationException("keycloak.user.delete.error");
            }
        } catch (Exception e) {
            log.error("Error deleting user in Keycloak: {}", e.getMessage(), e);
            throw new BusinessValidationException("keycloak.user.delete.error");
        }
    }

    @Override
    public TokenResponse login(String username, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", frontClientId);
        formData.add("client_secret", frontClientSecret);
        formData.add("username", username);
        formData.add("password", password);

        String tokenUri = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        log.info("Attempting login for user: {}", username);

        try {
            return restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(TokenResponse.class);

        } catch (HttpClientErrorException e) {
            handleLoginError(e, username);
            return null; // unreachable — handleLoginError always throws
        } catch (Exception e) {
            log.error("Unexpected error during login for user {}: {}", username, e.getMessage(), e);
            throw new BusinessValidationException("auth.login.service_error");
        }
    }

    @Override
    public void verifyEmail(String keycloakId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(keycloakId);
            UserRepresentation userRep = userResource.toRepresentation();
            userRep.setEmailVerified(true);
            userResource.update(userRep);
        } catch (Exception e) {
            log.error("Error verifying email in Keycloak: {}", e.getMessage(), e);
            throw new BusinessValidationException("keycloak.user.update.error");
        }
    }

    @Override
    public boolean isEmailVerified(String string) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(string);
            UserRepresentation representation = userResource.toRepresentation();
            return representation.isEmailVerified();
        } catch (Exception e) {
            log.error("Error checking email in Keycloak: {}", e.getMessage(), e);
            throw new BusinessValidationException("keycloak.user.update.error");
        }
    }

    private void handleLoginError(HttpClientErrorException ex, String username) {
        String responseBody = ex.getResponseBodyAsString();
        log.error("Login failed for user {}: status={}, body={}", username, ex.getStatusCode(), responseBody);

        String error = "";
        String errorDescription = "";

        try {
            JsonNode body = objectMapper.readTree(responseBody);
            error = body.path("error").asText("");
            errorDescription = body.path("error_description").asText("").toLowerCase();
        } catch (Exception parseEx) {
            log.warn("Could not parse Keycloak error response: {}", parseEx.getMessage());
        }

        if ("invalid_grant".equals(error)) {
            if (errorDescription.contains("temporarily") || errorDescription.contains("locked")) {
                log.warn("User {} is temporarily locked", username);
                throw new BusinessValidationException("auth.user.locked_temporarily");
            }
            if (errorDescription.contains("disabled")) {
                log.warn("User {} is disabled", username);
                throw new BusinessValidationException("auth.user.disabled");
            }
            if (errorDescription.contains("invalid user credentials")) {
                checkBruteForce(username);
                throw new BusinessValidationException("auth.login.failed");
            }
            throw new BusinessValidationException("auth.login.failed");
        }

        if ("unauthorized_client".equals(error) || "invalid_client".equals(error)) {
            log.error("Keycloak client config error for client {}: {}", frontClientId, errorDescription);
            throw new BusinessValidationException("auth.client.config_error");
        }

        throw new BusinessValidationException("auth.login.failed");
    }

    private void checkBruteForce(String username) {
        try {
            List<UserRepresentation> users = keycloak.realm(realm).users().searchByUsername(username, true);
            if (users.isEmpty()) return;

            String userId = users.get(0).getId();
            Map<String, Object> bruteForceStatus = keycloak.realm(realm).attackDetection().bruteForceUserStatus(userId);

            if (bruteForceStatus != null && Boolean.TRUE.equals(bruteForceStatus.get("disabled"))) {
                log.warn("User {} is locked due to brute force", username);
                throw new BusinessValidationException("auth.user.locked_temporarily");
            }
        } catch (BusinessValidationException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not check brute force status for user {}: {}", username, e.getMessage());
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
