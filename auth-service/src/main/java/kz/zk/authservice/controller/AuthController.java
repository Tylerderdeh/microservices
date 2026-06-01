package kz.zk.authservice.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kz.zk.authservice.dto.LoginRequest;
import kz.zk.authservice.dto.RegistrationRequest;
import kz.zk.authservice.dto.TokenResponse;
import kz.zk.authservice.service.impl.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
//    @Operation(method = "POST", summary = "Регистрация пользователя", description = "Регистрация нового пользователя в системе")
//    @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован")
    public ResponseEntity<Void> register(@RequestBody RegistrationRequest request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/login")
//    @Operation(method = "POST", summary = "Вход пользователя", description = "Аутентификация пользователя в системе")
//    @ApiResponse(responseCode = "200", description = "Пользователь успешно аутентифицирован", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TokenResponse.class))})
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
}