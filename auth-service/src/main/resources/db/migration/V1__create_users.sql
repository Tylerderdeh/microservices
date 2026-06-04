CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE auth.users
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    keycloak_id   UUID         NOT NULL UNIQUE,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    status        VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE'
                               CONSTRAINT users_status_check CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED')),
    last_login_at TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255)
);

CREATE TABLE auth.user_roles
(
    user_id UUID        NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
    roles   VARCHAR(32) NOT NULL
                        CONSTRAINT user_roles_role_check CHECK (roles IN ('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MODERATOR')),
    PRIMARY KEY (user_id, roles)
);

CREATE INDEX idx_users_keycloak_id ON auth.users (keycloak_id);
CREATE INDEX idx_users_email       ON auth.users (LOWER(email));
CREATE INDEX idx_users_username    ON auth.users (LOWER(username));