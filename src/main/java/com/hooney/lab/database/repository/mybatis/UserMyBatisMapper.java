package com.hooney.lab.database.repository.mybatis;

import com.hooney.lab.database.domain.user.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         🔴 UserMyBatisMapper (MyBatis SQL Mapping)             ║
 * ║                                                                  ║
 * ║  [이 인터페이스의 책임]                                            ║
 * ║  1. SQL과 자바 메서드 간의 선언적 매핑                            ║
 * ║  2. XML 기반의 강력한 동적 쿼리 및 결과 매핑(ResultMap) 관리       ║
 * ║  3. 복잡한 통계 쿼리나 조인 쿼리의 세밀한 튜닝                    ║
 * ║                                                                  ║
 * ║  [실무 포인트]                                                    ║
 * ║  - SQL 성능 최적화가 필수적인 대용량 트래픽 환경에서 선호          ║
 * ║  - 레거시 DB 구조나 프로시저 호출이 잦은 경우 유리                 ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Mapper
public interface UserMyBatisMapper {

    /**
     * ID로 사용자 조회 (복잡한 조인이나 통계 쿼리에 유리)
     */
    Optional<UserEntity> findById(@Param("id") Long id);

    /**
     * 모든 활성 사용자 조회
     */
    List<UserEntity> findAllActiveUsers();
    
    /**
     * 사용자 저장
     */
    void save(UserEntity user);
}
