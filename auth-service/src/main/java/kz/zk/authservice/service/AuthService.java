package kz.zk.authservice.service;

import kz.zk.authservice.dto.LoginRequest;
import kz.zk.authservice.dto.RegistrationRequest;
import kz.zk.authservice.dto.TokenResponse;

public interface AuthService {

    void register(RegistrationRequest request);

    TokenResponse login(LoginRequest request);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);

    TokenResponse refreshToken(String refreshToken);
}
