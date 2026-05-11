package com.hooney.lab.database.repository.jpa;

import com.hooney.lab.database.domain.post.PostEntity;
import com.hooney.lab.database.domain.user.UserEntity;
import com.hooney.lab.database.domain.user.UserStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         🎯 Module 3: N+1 문제 해결 킬링벌스 테스트               ║
 * ║                                                                  ║
 * ║  [N+1 문제란?]                                                   ║
 * ║  - 1번의 쿼리로 N개의 데이터를 가져왔는데, 연관된 데이터를 참조     ║
 * ║    할 때마다 N번의 추가 쿼리가 발생하는 성능 저하 현상              ║
 * ║                                                                  ║
 * ║  [해결책]                                                       ║
 * ║  1. Fetch Join: SQL JOIN을 사용하여 한 번에 묶어 가져옴             ║
 * ║  2. @EntityGraph: JPA 표준 어노테이션으로 Fetch Join 구현          ║
 * ║  3. Batch Size: IN 절을 통해 N번의 쿼리를 1번으로 묶음               ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@SpringBootTest
@Transactional
class UserNPlusOneTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비: 5명의 사용자, 각각 2개의 게시글
        for (int i = 0; i < 5; i++) {
            UserEntity user = UserEntity.builder()
                    .email("user" + i + "@test.com")
                    .name("User" + i)
                    .status(UserStatus.ACTIVE)
                    .build();

            user.addPost(PostEntity.builder().title("Post 1 by User" + i).content("Content").build());
            user.addPost(PostEntity.builder().title("Post 2 by User" + i).content("Content").build());

            userJpaRepository.save(user);
        }
        em.flush();
        em.clear();
        System.out.println(" ══════════════ 데이터 준비 완료 ══════════════ ");
    }

    @Test
    @DisplayName("⚠️ N+1 발생 케이스: findAll() 후 연관 데이터 접근")
    void nPlusOne_Occurrence() {
        // 1. 사용자 전체 조회 (쿼리 1번 발생)
        List<UserEntity> users = userJpaRepository.findAll();

        // 2. 각 사용자의 게시글에 접근 (사용자가 N명이면 N번의 추가 쿼리 발생!)
        System.out.println(" >>> 게시글 접근 시작 (이때 쿼리가 발생함) ");
        for (UserEntity user : users) {
            System.out.println("User: " + user.getName() + ", Posts Count: " + user.getPosts().size());
        }
        
        // 결과: 1 (전체조회) + 5 (각 사용자별 게시글 조회) = 총 6번의 쿼리 발생!
    }

    @Test
    @DisplayName("✅ N+1 해결책: Fetch Join 사용 (쿼리 단 1번!)")
    void nPlusOne_Solution_FetchJoin() {
        // 별도로 작성한 Fetch Join 쿼리 메서드 호출
        // SELECT u FROM UserEntity u JOIN FETCH u.posts
        List<UserEntity> users = em.createQuery(
                "select u from UserEntity u join fetch u.posts", UserEntity.class)
                .getResultList();

        System.out.println(" >>> Fetch Join 결과 확인 ");
        for (UserEntity user : users) {
            System.out.println("User: " + user.getName() + ", Posts Count: " + user.getPosts().size());
        }
        
        // 결과: JOIN을 통해 한 번에 가져오므로 쿼리는 단 1번 발생!
    }
}
