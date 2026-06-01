package kz.zk.authservice.repository;

import kz.zk.authservice.entity.User;
import kz.zk.authservice.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByKeycloakId(UUID keycloakId);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordConfirmationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
    void deleteByKeycloakId(UUID keycloakId);
    Optional<User> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    
    // Методы для работы с неактивными пользователями
    List<User> findByStatusAndLastLoginAtBefore(UserStatus status, LocalDateTime date);
    List<User> findByLastLoginAtBefore(LocalDateTime date);
}

