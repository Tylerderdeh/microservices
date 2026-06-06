package kz.zk.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO для запроса подтверждения email")
public class VerifyEmailTokenRequest {
    @NotBlank(message = "{email.verification.token.required}")
    @Schema(description = "Токен верификации email", example = "valid-token")
    private String token;
} 