package com.hooney.lab.database.repository.jdbc;

import com.hooney.lab.database.domain.user.UserEntity;
import com.hooney.lab.database.domain.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         🔵 UserJdbcRepository (Spring JdbcTemplate)            ║
 * ║                                                                  ║
 * ║  [이 클래스의 책임]                                               ║
 * ║  1. 가장 원초적인 SQL 실행 제어 (추상화 최소화)                    ║
 * ║  2. RowMapper를 통한 수동 결과 매핑으로 성능 오버헤드 최소화       ║
 * ║  3. Bulk Insert 등 배치 처리에 최적화                             ║
 * ║                                                                  ║
 * ║  [실무 포인트]                                                    ║
 * ║  - 가장 가볍고 빠르며, 의존성이 적음                              ║
 * ║  - 데이터 마이그레이션이나 초고성능 배치가 필요한 경우 강력 추천   ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Repository
@RequiredArgsConstructor
public class UserJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * RowMapper: DB 결과(ResultSet)를 자바 객체(Entity)로 수동 매핑
     */
    private final RowMapper<UserEntity> userRowMapper = (rs, rowNum) -> UserEntity.builder()
            .id(rs.getLong("id"))
            .email(rs.getString("email"))
            .name(rs.getString("name"))
            .phoneNumber(rs.getString("phone_number"))
            .status(UserStatus.valueOf(rs.getString("status")))
            .build();

    public Optional<UserEntity> findById(Long id) {
        String sql = "SELECT * FROM USERS WHERE id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, id));
    }

    public int countUsers() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM USERS", Integer.class);
    }

    /**
     * [Task 2] 대량 데이터 삽입 성능 검증용 Bulk Insert
     * JdbcTemplate의 batchUpdate를 활용하여 단 한 번의 네트워크 I/O로 수만 건의 데이터를 삽입
     */
    public void batchInsert(java.util.List<UserEntity> users) {
        String sql = "INSERT INTO USERS (email, name, phone_number, status, created_at, modified_at) " +
                     "VALUES (?, ?, ?, ?, NOW(), NOW())";
        
        jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                UserEntity user = users.get(i);
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getName());
                ps.setString(3, user.getPhoneNumber());
                ps.setString(4, user.getStatus().name());
            }

            @Override
            public int getBatchSize() {
                return users.size();
            }
        });
    }
}
