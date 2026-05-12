package com.hooney.lab.database.repository.jdbc;

import com.hooney.lab.database.domain.user.UserEntity;
import com.hooney.lab.database.domain.user.UserStatus;
import com.hooney.lab.database.repository.jpa.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         🧪 Task 2: 대량 데이터 처리 성능 검증 (Bulk Insert)         ║
 * ║                                                                  ║
 * ║  [검증 목적]                                                      ║
 * ║  수만 건의 데이터를 삽입할 때, JPA의 saveAll()과 JdbcTemplate의   ║
 * ║  batchUpdate() 간의 성능(소요 시간) 차이를 극적으로 보여줍니다.     ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Slf4j
@SpringBootTest
class BulkInsertPerformanceTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private UserJdbcRepository userJdbcRepository;

    private static final int BULK_COUNT = 10_000;

    @Test
    @DisplayName("JPA saveAll() vs JdbcTemplate batchInsert() 성능 비교")
    void compareBulkInsertPerformance() {
        log.info(">>>> 데이터 {}건 삽입 성능 테스트 준비", BULK_COUNT);

        List<UserEntity> usersForJpa = new ArrayList<>();
        List<UserEntity> usersForJdbc = new ArrayList<>();

        for (int i = 0; i < BULK_COUNT; i++) {
            usersForJpa.add(UserEntity.builder()
                    .email("jpa" + i + "@hooney.com")
                    .name("JPA User " + i)
                    .phoneNumber("010-0000-" + String.format("%04d", i % 10000))
                    .status(UserStatus.ACTIVE)
                    .build());

            usersForJdbc.add(UserEntity.builder()
                    .email("jdbc" + i + "@hooney.com")
                    .name("JDBC User " + i)
                    .phoneNumber("010-1111-" + String.format("%04d", i % 10000))
                    .status(UserStatus.ACTIVE)
                    .build());
        }

        // 1. JPA saveAll() 성능 측정 (일반적으로 Hibernate가 N건의 Insert 쿼리를 날림)
        long jpaStartTime = System.currentTimeMillis();
        userJpaRepository.saveAll(usersForJpa);
        userJpaRepository.flush();
        long jpaEndTime = System.currentTimeMillis();
        long jpaDuration = jpaEndTime - jpaStartTime;

        log.info(">>>> [JPA saveAll] {}건 삽입 완료. 소요 시간: {}ms", BULK_COUNT, jpaDuration);

        // 2. JdbcTemplate batchUpdate() 성능 측정 (단 1번의 쿼리로 N건 밀어넣기)
        long jdbcStartTime = System.currentTimeMillis();
        userJdbcRepository.batchInsert(usersForJdbc);
        long jdbcEndTime = System.currentTimeMillis();
        long jdbcDuration = jdbcEndTime - jdbcStartTime;

        log.info(">>>> [JdbcTemplate batchUpdate] {}건 삽입 완료. 소요 시간: {}ms", BULK_COUNT, jdbcDuration);

        log.info(">>>> 📝 결과: JdbcTemplate Batch가 JPA 대비 약 {}배 더 빠릅니다!", 
                (double) jpaDuration / Math.max(1, jdbcDuration));
        
        // 성능 차이가 확연히 나야 정상 (JPA는 영속성 컨텍스트 관리로 인해 대량 삽입에 불리함)
    }
}
