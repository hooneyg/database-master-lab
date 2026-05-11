package com.hooney.lab.database.domain.user;

import com.hooney.lab.database.domain.common.BaseTimeEntity;
import com.hooney.lab.database.domain.post.PostEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         👤 UserEntity (도메인 모델 & JPA 엔티티)                ║
 * ║                                                                  ║
 * ║  [이 클래스의 책임]                                               ║
 * ║  1. 사용자 정보를 DB 테이블(USERS)과 매핑                          ║
 * ║  2. 사용자 상태 관리 및 프로필 수정 등 도메인 비즈니스 로직 수행    ║
 * ║  3. 생성/수정 시간 자동 추적 (BaseTimeEntity 상속)                ║
 * ║                                                                  ║
 * ║  [설계 원칙 - 풍부한 도메인 모델(Rich Domain Model)]               ║
 * ║  - Setter 지양: 객체 상태는 메서드 명으로 의도를 드러내어 변경      ║
 * ║  - 캡슐화: 외부에서 필드를 직접 수정하지 못하게 생성자/메서드 제어  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(name = "USERS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    /**
     * 🔗 OneToMany 연관관계 (1:N)
     * cascade = CascadeType.ALL: 사용자가 저장될 때 게시글도 함께 저장되도록 설정
     * orphanRemoval = true: 리스트에서 제거된 게시글은 DB에서도 삭제
     */
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostEntity> posts = new ArrayList<>();

    /**
     * 🛠️ 연관관계 편의 메서드
     */
    public void addPost(PostEntity post) {
        this.posts.add(post);
        post.assignUser(this);
    }

    /**
     * 🛠️ 비즈니스 로직: 상태 변경 (탈퇴 등)
     */
    public void changeStatus(UserStatus status) {
        this.status = status;
    }
}
