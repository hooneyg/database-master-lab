package com.hooney.lab.database.domain.post;

import com.hooney.lab.database.domain.common.BaseTimeEntity;
import com.hooney.lab.database.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         📝 PostEntity (게시글 엔티티)                           ║
 * ║                                                                  ║
 * ║  [이 클래스의 책임]                                               ║
 * ║  1. 사용자가 작성한 게시글 정보를 관리                             ║
 * ║  2. UserEntity와 N:1 연관관계를 맺음 (다대일)                      ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(name = "POSTS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PostEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 🔗 ManyToOne 연관관계 (N:1)
     * FetchType.LAZY: 지연 로딩을 기본으로 사용하여 N+1 문제를 예방하는 첫걸음
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public void assignUser(UserEntity user) {
        this.user = user;
    }
}
