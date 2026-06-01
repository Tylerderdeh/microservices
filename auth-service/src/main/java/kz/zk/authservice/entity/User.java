package kz.zk.authservice.entity;

import jakarta.persistence.*;
import kz.zk.authservice.entity.base.AbstractAuditableEntity;
import kz.zk.authservice.entity.enums.Role;
import kz.zk.authservice.entity.enums.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends AbstractAuditableEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID keycloakId;                    // ← ссылка на Keycloak

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private boolean emailVerified;

    // Токены — хранишь сам, Keycloak не умеет
    private String emailVerificationToken;
    private Instant emailVerificationTokenExpiry;

    private String passwordResetToken;
    private Instant passwordResetTokenExpiry;

    // Активность — тоже сам
    private Instant lastLoginAt;
    private Instant lastVerificationEmailSentAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
}