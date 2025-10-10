package kr.kro.airbob.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class AuthRequestDto {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank
        private String email;
        @NotBlank
        private String password;
    }
}
