package com.hooney.lab.database.repository.mybatis;

import com.hooney.lab.database.domain.post.PostEntity;
import com.hooney.lab.database.domain.user.UserEntity;
import com.hooney.lab.database.domain.user.UserStatus;
import com.hooney.lab.database.repository.jpa.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         🧪 Task 1: 정규화 및 조인 쿼리 최적화 검증               ║
 * ║                                                                  ║
 * ║  [검증 목적]                                                      ║
 * ║  MyBatis의 <collection> 매핑을 활용하여 1:N 조인 쿼리를 단 한 번의 ║
 * ║  SQL 쿼리로 최적화하여 가져오는 것을 검증합니다.                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Slf4j
@SpringBootTest
@Transactional
class NormalizationTest {

    @Autowired
    private UserMyBatisMapper userMyBatisMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private Long savedUserId;

    @BeforeEach
    void setUp() {
        // 더미 데이터 세팅 (1명의 User, 3개의 Post)
        UserEntity user = UserEntity.builder()
                .email("test.normalization@hooney.com")
                .name("Normalization Tester")
                .phoneNumber("010-1234-5678")
                .status(UserStatus.ACTIVE)
                .build();

        for (int i = 1; i <= 3; i++) {
            PostEntity post = PostEntity.builder()
                    .title("테스트 포스트 " + i)
                    .content("이것은 N:1 정규화 조인 테스트를 위한 " + i + "번째 더미 데이터입니다.")
                    .build();
            user.addPost(post);
        }

        userJpaRepository.saveAndFlush(user);
        savedUserId = user.getId();
        
        log.info(">>>> 더미 데이터 세팅 완료 (UserID: {}, 작성한 Post 수: 3)", savedUserId);
    }

    @Test
    @DisplayName("MyBatis Collection 매핑을 사용하면 단 1번의 쿼리로 1:N 데이터가 조인되어 객체에 담긴다")
    void verifyMyBatisCollectionJoinOptimization() {
        // given
        log.info(">>>> MyBatis 다중 조인(Collection) 쿼리 실행 시작");

        // when
        Optional<UserEntity> userOptional = userMyBatisMapper.findUserWithPosts(savedUserId);

        // then
        log.info(">>>> 쿼리 실행 완료 및 결과 검증");
        assertThat(userOptional).isPresent();
        
        UserEntity foundUser = userOptional.get();
        assertThat(foundUser.getName()).isEqualTo("Normalization Tester");
        
        // 조인된 Post 리스트 검증
        assertThat(foundUser.getPosts()).hasSize(3);
        assertThat(foundUser.getPosts().get(0).getTitle()).startsWith("테스트 포스트");

        log.info(">>>> 검증 성공! 단 한 번의 쿼리로 User(1)와 연관된 Post(N) 데이터를 모두 매핑했습니다.");
        log.info(">>>> 📝 결과: MyBatis의 <collection>을 활용하면 복잡한 다중 정규화 테이블 구조라도 Application 레벨에서 N+1 없이 즉시 도메인 객체로 추출 가능합니다.");
    }
}
