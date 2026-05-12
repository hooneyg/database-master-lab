# 🛠️ Troubleshooting Guide

본 문서는 `database-master-lab` 개발 과정에서 데이터베이스 접근, 트랜잭션, 성능 최적화와 관련하여 발생한 주요 이슈와 해결 과정을 기록합니다.

---

## 1. N+1 문제 발생 및 최적화

### 🚨 현상 (Symptom)
- `User`를 전체 조회하는 `findAll()` 실행 시, `User`와 `1:N` 관계를 맺고 있는 `Post` 데이터를 가져오기 위해 (User 수)번의 추가 쿼리가 발생하는 현상 확인.
- 로그 분석 결과: 1개의 User 조회 쿼리 + 100개의 Post 조회 쿼리 = 101번의 SQL 질의 발생.

### 🔍 원인 분석 (Root Cause)
- Spring Data JPA의 기본 로딩 전략(Lazy Loading)으로 인해, 엔티티 리스트를 반환할 때 연관된 컬렉션을 순회하며 지연 로딩이 강제 트리거됨.

### 💡 해결 과정 (Solution)
1. **Fetch Join 적용:** QueryDSL 및 JPQL을 통해 `JOIN FETCH` 쿼리를 작성하여, 단 한 번의 쿼리로 연관된 데이터를 가져오도록 수정.
2. **`default_batch_fetch_size` 설정:** 페이징이 필요한 컬렉션 페치 조인 시 OutOfMemory 위험이 있으므로, `application.yml`에 `hibernate.default_batch_fetch_size: 1000`을 설정하여 IN 절로 연관 데이터를 최적화하여 가져옴.

---

## 2. HikariCP Connection Pool Exhaustion (커넥션 고갈)

### 🚨 현상 (Symptom)
- 부하 테스트(JMeter) 진행 시, 다수의 스레드에서 `SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 30000ms` 에러가 발생하며 서버 응답 지연.

### 🔍 원인 분석 (Root Cause)
- 한 트랜잭션 안에서 외부 API 호출 등 무거운 네트워크 작업이 포함되어 있어, DB 커넥션을 반환하지 못한 채 트랜잭션이 길게 유지됨(Long Transaction).

### 💡 해결 과정 (Solution)
1. **트랜잭션 범위 분리:** 외부 API 호출과 DB 저장 로직을 서로 다른 트랜잭션/메서드로 분리하여, 네트워크 I/O 동안 DB 커넥션이 점유되지 않도록 조치.
2. **커넥션 풀 설정 튜닝:** `application.yml`에서 HikariCP 설정(`maximum-pool-size`)을 서버 사양 및 동시 접속자 수에 맞게 적절히 상향 조정.

---

## 3. 대용량 Bulk Insert 성능 이슈 (JPA saveAll의 한계)

### 🚨 현상 (Symptom)
- `saveAll()` 메서드를 사용하여 5만 건의 레코드를 삽입할 때, 약 20초 이상 소요됨.

### 🔍 원인 분석 (Root Cause)
- JPA에서 기본 ID 전략을 `GenerationType.IDENTITY`로 사용할 경우, Hibernate가 INSERT 문을 일괄(Batch) 처리하지 않고, ID 값을 얻기 위해 레코드마다 건건이 INSERT를 수행함.

### 💡 해결 과정 (Solution)
1. **JdbcTemplate 도입:** Bulk Insert 전용 컴포넌트(`UserJdbcRepository`)를 작성하여 `jdbcTemplate.batchUpdate()`를 사용.
2. **결과:** 5만 건 삽입 소요 시간을 20초에서 약 1.5초 이내로 단축시켜 압도적인 성능 개선 입증.
