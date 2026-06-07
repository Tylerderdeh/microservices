package kz.zk.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.representations.idm.CredentialRepresentation;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUser {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean emailVerified;
    private List<String> realmRoles;
    private String password;
    private Map<String, List<String>> attributes;


    public org.keycloak.representations.idm.UserRepresentation toUserRepresentation() {
        org.keycloak.representations.idm.UserRepresentation userRep = new org.keycloak.representations.idm.UserRepresentation();
        userRep.setFirstName(firstName);
        userRep.setLastName(lastName);
        userRep.setUsername(username);
        userRep.setEmail(email);
        userRep.setEnabled(enabled);
        userRep.setEmailVerified(emailVerified);
        userRep.setRealmRoles(realmRoles);

        if (password != null) {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            userRep.setCredentials(Collections.singletonList(credential));
        }

        return userRep;
    }
} 