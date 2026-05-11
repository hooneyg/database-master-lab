package com.hooney.lab.database.repository.jpa;

import com.hooney.lab.database.domain.user.UserEntity;
import com.hooney.lab.database.domain.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         🍃 UserJpaRepository (Spring Data JPA)                 ║
 * ║                                                                  ║
 * ║  [이 인터페이스의 책임]                                            ║
 * ║  1. 기본적인 CRUD 메서드 자동 제공 (findById, save 등)             ║
 * ║  2. 메서드 명명 규칙에 기반한 동적 쿼리 생성                       ║
 * ║  3. @Query를 이용한 객체 지향 쿼리(JPQL) 정의                       ║
 * ║                                                                  ║
 * ║  [실무 포인트]                                                    ║
 * ║  - 복잡하지 않은 단순 조회는 메서드 이름만으로 생산성 극대화        ║
 * ║  - 컴파일 시점에 쿼리 문법 오류를 잡을 수 있는 장점                ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    /**
     * [Query Method]
     * 메서드 이름만으로 쿼리를 자동 생성합니다.
     * SELECT * FROM USERS WHERE email = ?
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * [@Query 사용]
     * 복잡한 쿼리나 조인이 필요할 때 직접 JPQL을 작성합니다.
     * DB 종류에 상관없이 객체 중심의 쿼리가 가능합니다.
     */
    @Query("SELECT u FROM UserEntity u WHERE u.name = :name AND u.status = 'ACTIVE'")
    Optional<UserEntity> findActiveUserByName(@Param("name") String name);

    /**
     * ⚡ 벌크 수정 연산: 모든 사용자의 상태를 한 번에 변경
     * @Modifying: SELECT가 아닌 INSERT/UPDATE/DELETE 임을 명시
     * clearAutomatically = true: 벌크 연산 직후 영속성 컨텍스트를 비워줌 (매우 중요!)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserEntity u SET u.status = :status WHERE u.status = 'ACTIVE'")
    int bulkStatusUpdate(@Param("status") UserStatus status);
}
