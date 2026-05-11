package com.hooney.lab.database.dto.user;

import com.hooney.lab.database.domain.user.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 📥 UserRequestDTO (Data Transfer Object)
 * 클라이언트로부터 데이터를 전달받을 때 사용합니다.
 * @Valid 어노테이션을 통해 입력 데이터의 유효성을 검증합니다.
 */
public class UserRequestDTO {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Create {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "이름은 필수입니다.")
        private String name;

        private String phoneNumber;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Update {
        @NotBlank(message = "이름은 필수입니다.")
        private String name;

        private String phoneNumber;
    }
}
