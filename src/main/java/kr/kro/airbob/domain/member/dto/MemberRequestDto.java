package kr.kro.airbob.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class MemberRequestDto {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class SignupMemberRequestDto {
        @NotBlank
        @Size(min = 1, max = 20)
        private String nickname;
        @NotBlank
        @Email
        private String email;
        @NotBlank
        @Size(min = 8, max = 20)
        private String password;
        private String thumbnailImageUrl;
    }
}
