package kz.zk.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kz.zk.authservice.dto.*;
import kz.zk.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(method = "POST", summary = "Регистрация пользователя", description = "Регистрация нового пользователя в системе")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован")
    public ResponseEntity<Void> register(@RequestBody RegistrationRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PostMapping("/login")
    @Operation(method = "POST", summary = "Вход пользователя", description = "Аутентификация пользователя в системе")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно аутентифицирован", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TokenResponse.class))})
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authService.login(request);
        Cookie cookie = new Cookie("refresh_token", tokenResponse.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);
        tokenResponse.setRefreshToken(null);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/verify-email")
    @Operation(method = "POST", summary = "Подтверждение email", description = "Подтверждение email по токену верификации")
    @ApiResponse(responseCode = "200", description = "Email успешно подтвержден")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailTokenRequest request) {
        authService.verifyEmail(request.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification-email")
    @Operation(method = "POST", summary = "Повторная отправка письма для подтверждения email", description = "Повторная отправка письма для подтверждения email пользователю")
    @ApiResponse(responseCode = "200", description = "Письмо для подтверждения email успешно отправлено")
    public ResponseEntity<Void> resendVerificationEmail(@Valid @RequestBody ResendVerificationEmailRequest request) {
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    @Operation(method = "POST", summary = "Обновление токена доступа", description = "Обновление JWT токена доступа")
    @ApiResponse(responseCode = "200", description = "Токен доступа успешно обновлен", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TokenResponse.class))})
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        if (request == null || request.getCookies() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("refresh_token"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TokenResponse tokenResponse = authService.refreshToken(refreshToken);
        Cookie cookie = new Cookie("refresh_token", tokenResponse.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);
        tokenResponse.setRefreshToken(null);
        return ResponseEntity.ok(tokenResponse);
    }
}