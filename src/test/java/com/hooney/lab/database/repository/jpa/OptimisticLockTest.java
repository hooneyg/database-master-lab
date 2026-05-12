package com.hooney.lab.database.repository.jpa;

import com.hooney.lab.database.domain.post.PostEntity;
import com.hooney.lab.database.domain.user.UserEntity;
import com.hooney.lab.database.domain.user.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         🧪 Task 3: 동시성 트랜잭션 및 낙관적 락 검증             ║
 * ║                                                                  ║
 * ║  [검증 목적]                                                      ║
 * ║  다중 스레드 환경에서 동일한 데이터(게시글 조회수)를 동시에           ║
 * ║  수정할 때 발생하는 갱신 손실(Lost Update) 문제를 예방하기 위해     ║
 * ║  JPA의 @Version 낙관적 락(Optimistic Lock)이 어떻게 동작하는지    ║
 * ║  증명합니다.                                                      ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Slf4j
@SpringBootTest
class OptimisticLockTest {

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;
    
    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long savedPostId;

    @BeforeEach
    void setUp() {
        // 더미 데이터 세팅
        UserEntity user = UserEntity.builder()
                .email("lock@hooney.com")
                .name("Lock Tester")
                .status(UserStatus.ACTIVE)
                .build();

        PostEntity post = PostEntity.builder()
                .title("동시성 테스트 게시글")
                .content("이 글의 조회수가 100이 되어야 합니다.")
                .build();
        
        user.addPost(post);
        userJpaRepository.saveAndFlush(user);
        
        savedPostId = post.getId();
    }

    @Test
    @DisplayName("동시에 100개의 요청이 조회수를 증가시킬 때, 낙관적 락 충돌(OptimisticLockingFailure)이 발생해야 한다")
    void verifyOptimisticLockingFailure() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        log.info(">>>> 동시성 테스트 시작: 100개의 스레드가 동시에 조회수 증가 시도");

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 트랜잭션 분리를 위해 TransactionTemplate 사용
                    transactionTemplate.execute(status -> {
                        PostEntity post = postJpaRepository.findById(savedPostId).orElseThrow();
                        post.incrementViews();
                        postJpaRepository.saveAndFlush(post);
                        return null;
                    });
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("예상치 못한 에러", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        PostEntity finalPost = postJpaRepository.findById(savedPostId).orElseThrow();
        
        log.info(">>>> [테스트 완료] 성공 횟수: {}, 실패(락 충돌) 횟수: {}", successCount.get(), failCount.get());
        log.info(">>>> [결과 검증] 최종 조회수: {}", finalPost.getViews());

        // 락이 정상적으로 동작했다면 여러 스레드가 충돌하여 실패(Exception)가 발생해야 함
        assertThat(failCount.get()).isGreaterThan(0);
        
        // 최종 조회수는 100(threadCount)보다 작아야 함 (동시성 제어로 인해 갱신 손실이 방어되었고 예외를 던졌으므로)
        assertThat(finalPost.getViews()).isLessThan((long) threadCount);
        
        log.info(">>>> 📝 결과: @Version 낙관적 락이 충돌을 완벽하게 감지하여 ObjectOptimisticLockingFailureException을 발생시켰습니다. " +
                 "현업에서는 이를 Catch하여 AOP 등으로 재시도(Retry) 로직을 구성합니다.");
    }
}
