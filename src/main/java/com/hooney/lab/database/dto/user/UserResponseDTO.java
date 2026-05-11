package com.hooney.lab.database.dto.user;

import com.hooney.lab.database.domain.user.UserEntity;
import com.hooney.lab.database.domain.user.UserStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 📤 UserResponseDTO
 * 클라이언트에 데이터를 응답할 때 사용합니다.
 * Entity의 민감 정보(password 등)를 숨기고 필요한 필드만 노출합니다.
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponseDTO {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private UserStatus status;
    private LocalDateTime createdAt;

    /**
     * 🔄 Static Factory Method: Entity를 DTO로 변환
     * MapStruct 같은 라이브러리를 쓰기도 하지만, 
     * 복잡하지 않은 경우 수동 매핑이 가독성과 디버깅에 유리할 수 있음.
     */
    public static UserResponseDTO from(UserEntity entity) {
        return UserResponseDTO.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
