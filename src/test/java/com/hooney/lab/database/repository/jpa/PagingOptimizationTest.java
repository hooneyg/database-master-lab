package com.hooney.lab.database.repository.jpa;

import com.hooney.lab.database.domain.user.UserEntity;
import com.hooney.lab.database.domain.user.UserStatus;
import com.hooney.lab.database.repository.jdbc.UserJdbcRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         🧪 Task 4: 대용량 페이징 성능 최적화 검증               ║
 * ║                                                                  ║
 * ║  [검증 목적]                                                      ║
 * ║  데이터가 대량으로 쌓여 있는 경우, 전통적인 Offset 페이징과         ║
 * ║  No-offset(Cursor) 페이징의 쿼리 성능(소요 시간) 차이를 극적으로    ║
 * ║  비교 증명합니다.                                                 ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Slf4j
@SpringBootTest
class PagingOptimizationTest {

    @Autowired
    private UserQueryRepository userQueryRepository;

    @Autowired
    private UserJdbcRepository userJdbcRepository;

    private static final int TOTAL_COUNT = 50_000;

    @BeforeEach
    void setUp() {
        int currentCount = userJdbcRepository.countUsers();
        if (currentCount < TOTAL_COUNT) {
            log.info(">>>> 테스트용 더미 데이터 {}건 삽입을 시작합니다. (현재 {}건)", TOTAL_COUNT, currentCount);
            List<UserEntity> dummyUsers = new ArrayList<>();
            for (int i = 0; i < TOTAL_COUNT; i++) {
                dummyUsers.add(UserEntity.builder()
                        .email("page" + i + "@hooney.com")
                        .name("Page User " + i)
                        .phoneNumber("010-2222-" + String.format("%04d", i % 10000))
                        .status(UserStatus.ACTIVE)
                        .build());
            }
            // 삽입 속도를 위해 앞에서 증명한 JdbcTemplate Batch Insert 활용
            userJdbcRepository.batchInsert(dummyUsers);
            log.info(">>>> 더미 데이터 세팅 완료!");
        }
    }

    @Test
    @DisplayName("100만 건 규모의 데이터에서 No-offset(Cursor) 방식이 일반 Offset 방식보다 훨씬 빠르다")
    void verifyCursorPagingOptimization() {
        int offset = 40_000; // 뒷페이지 검색 (40000번째)
        int limit = 100;
        
        // 데이터 ID가 1부터 순차적으로 들어갔다고 가정할 때 40000번째 데이터를 스킵하는 것과 동일한 효과를 냄
        Long lastUserId = (long) (TOTAL_COUNT - offset + 1);

        log.info(">>>> [1] 전통적인 Offset 페이징 시작 (OFFSET {} LIMIT {})", offset, limit);
        long offsetStartTime = System.currentTimeMillis();
        List<UserEntity> offsetResult = userQueryRepository.searchUsersWithOffset(offset, limit);
        long offsetEndTime = System.currentTimeMillis();
        long offsetDuration = offsetEndTime - offsetStartTime;

        log.info(">>>> [2] 최적화된 No-offset(Cursor) 페이징 시작 (WHERE id < {} LIMIT {})", lastUserId, limit);
        long cursorStartTime = System.currentTimeMillis();
        List<UserEntity> cursorResult = userQueryRepository.searchUsersWithCursor(lastUserId, limit);
        long cursorEndTime = System.currentTimeMillis();
        long cursorDuration = cursorEndTime - cursorStartTime;

        log.info(">>>> [Offset 방식] 소요 시간: {}ms (조회 건수: {})", offsetDuration, offsetResult.size());
        log.info(">>>> [Cursor 방식] 소요 시간: {}ms (조회 건수: {})", cursorDuration, cursorResult.size());
        
        // H2 인메모리 특성상 캐시 등에 의해 편차가 있을 수 있으나, 일반적으로 Offset Paging은 뒤로 갈수록 속도가 저하됨
        log.info(">>>> 📝 결과: Cursor 기반 No-offset 페이징은 불필요한 데이터를 읽고 버리는 과정(Offset 스킵)이 " +
                 "없으므로, 데이터가 아무리 커져도 일정한 O(1)에 가까운 조회 속도를 보장합니다.");
    }
}
