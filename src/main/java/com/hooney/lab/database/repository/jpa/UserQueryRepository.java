package com.hooney.lab.database.repository.jpa;

import com.hooney.lab.database.domain.user.QUserEntity;
import com.hooney.lab.database.domain.user.UserEntity;
import com.hooney.lab.database.domain.user.UserStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         🔍 UserQueryRepository (QueryDSL 기반)                 ║
 * ║                                                                  ║
 * ║  [이 클래스의 책임]                                               ║
 * ║  1. 복잡한 동적 쿼리(Dynamic Query) 처리                          ║
 * ║  2. 타입 세이프(Type-safe)한 쿼리 작성으로 런타임 에러 방지        ║
 * ║  3. 코드 재사용성 향상을 위한 BooleanExpression 모듈화             ║
 * ║                                                                  ║
 * ║  [JPA의 약점 보완]                                                ║
 * ║  - 문자열 기반의 JPQL 한계를 극복하고 자바 코드로 쿼리 제어        ║
 * ║  - 가독성 높은 쿼리 체이닝 인터페이스 제공                         ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Repository
@RequiredArgsConstructor
public class UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 사용자 조건 검색 (동적 쿼리)
     * 이름, 상태값 유무에 따라 WHERE 절을 동적으로 구성합니다.
     */
    public List<UserEntity> searchUsers(String name, UserStatus status) {
        QUserEntity user = QUserEntity.userEntity;

        return queryFactory
                .selectFrom(user)
                .where(
                        nameEq(name),
                        statusEq(status)
                )
                .fetch();
    }

    /**
     * ♾️ 무한 스크롤 전용 No-offset 페이징 (Cursor Paging)
     * lastUserId: 이전에 조회된 마지막 사용자의 ID (커서)
     * limit: 페이지 사이즈
     */
    public List<UserEntity> searchUsersWithCursor(Long lastUserId, int limit) {
        QUserEntity user = QUserEntity.userEntity;

        return queryFactory
                .selectFrom(user)
                .where(
                        ltUserId(lastUserId) // Cursor 조건: lastUserId보다 작은 ID만 조회
                )
                .orderBy(user.id.desc()) // 최신순 정렬 기준
                .limit(limit)
                .fetch();
    }

    /**
     * ⚠️ 전통적인 Offset 페이징 (비교 대조군)
     * 페이지가 뒤로 갈수록 앞에 있는 데이터를 모두 읽어야 하므로 성능이 기하급수적으로 저하됨
     */
    public List<UserEntity> searchUsersWithOffset(int offset, int limit) {
        QUserEntity user = QUserEntity.userEntity;

        return queryFactory
                .selectFrom(user)
                .orderBy(user.id.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    // --- 조건절 모듈화 ---

    private BooleanExpression ltUserId(Long lastUserId) {
        return lastUserId != null ? QUserEntity.userEntity.id.lt(lastUserId) : null;
    }

    // --- 조건절 모듈화 (재사용성 극대화) ---

    private BooleanExpression nameEq(String name) {
        return StringUtils.hasText(name) ? QUserEntity.userEntity.name.eq(name) : null;
    }

    private BooleanExpression statusEq(UserStatus status) {
        return status != null ? QUserEntity.userEntity.status.eq(status) : null;
    }
}
