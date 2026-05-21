# 📊 데이터베이스 쿼리 튜닝 및 벤치마크 시나리오 가이드 (Query Benchmark)

본 문서는 `database-master-lab` 내에서 동작하는 데이터베이스 성능 최적화 기법을 실증하기 위한 쿼리 튜닝 예제, 인덱스 스캔(Index Scan) 유도 시나리오 및 벤치마크 부하 테스트 방법론을 제공합니다.

---

## 1. 🔍 실무 쿼리 튜닝 예제 (Slow Query Tuning)

### 🚨 대상 장애 상황
대규모 주문(Order) 테이블과 회원(User) 테이블을 조인(Join)하여 최근 10일간 결제액이 100만 원 이상인 활성 회원의 목록 및 총 주문 건수를 집계할 때, 조회 속도가 5초 이상 소요되는 성능 저하 현상 발생.

### ❌ 튜닝 전 (비효율적 쿼리)
```sql
-- 대량의 풀 테이블 스캔(Full Table Scan) 및 임시 테이블(Temporary Table) 생성 유발
SELECT u.id, u.username, COUNT(o.id) as order_count, SUM(o.amount) as total_amount
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE o.created_at >= DATE_SUB(NOW(), INTERVAL 10 DAY)
  AND o.status = 'COMPLETED'
GROUP BY u.id, u.username
HAVING total_amount >= 1000000;
```
* **문제점:** 
  - `orders` 테이블의 `created_at` 및 `status` 필드에 인덱스가 없어 데이터 전체를 스캔함.
  - 대용량 데이터 정렬을 위해 디스크 임시 테이블(Using temporary; Using filesort)을 활용함.

###  튜닝 후 (최적화 쿼리)
1. **복합 인덱스(Composite Index) 생성:**
   `orders` 테이블에 검색 조건과 조인 키를 고려한 인덱스를 구성합니다.
   ```sql
   CREATE INDEX idx_orders_created_status_user ON orders (created_at, status, user_id, amount);
   ```
2. **드라이빙 테이블 최적화 및 커버링 인덱스(Covering Index) 적용:**
   ```sql
   -- 인덱스 내부에서 필터링과 조인을 완결하여 실제 데이터 블록 I/O 최소화
   SELECT u.id, u.username, o_summary.order_count, o_summary.total_amount
   FROM users u
   JOIN (
       SELECT user_id, COUNT(id) as order_count, SUM(amount) as total_amount
       FROM orders
       WHERE created_at >= DATE_SUB(NOW(), INTERVAL 10 DAY)
         AND status = 'COMPLETED'
       GROUP BY user_id
       HAVING total_amount >= 1000000
   ) o_summary ON u.id = o_summary.user_id;
   ```

---

## 2. ⚡ 인덱스 스캔 유도 시나리오 (Index Scan vs Full Scan)

`EXPLAIN` 실행 계획 분석을 통해 데이터베이스 엔진이 인덱스를 정상적으로 활용하는지 확인합니다.

### 💡 주요 점검 항목
- **Type이 `ALL` (Full Table Scan)인 경우:** 인덱스가 타지 않으므로 데이터가 많을수록 응답 성능이 기하급수적으로 느려집니다.
- **Type이 `range` 또는 `ref`인 경우:** 인덱스를 활용하여 효율적으로 범위를 제한하여 스캔합니다.

### 🧪 실증 실습 명령어
데이터베이스 컨테이너에 접속 후 실행 계획을 확인합니다:
```bash
# MySQL 컨테이너 접속
docker exec -it database-master-db mysql -u root -p

# 실행 계획 확인
EXPLAIN SELECT * FROM orders WHERE created_at >= '2026-05-01' AND status = 'COMPLETED';
```

---

## 3. 📈 벤치마크 테스트 방법론 (Benchmark Methodology)

성능 개선 결과를 정량적으로 검증하기 위해 **sysbench** 또는 **Apache JMeter**를 활용하여 부하를 가하고 응답 속도를 측정합니다.

### 🛠️ sysbench를 이용한 OLTP 부하 테스트
1. **테스트 데이터 준비 (Prepare):**
   ```bash
   sysbench oltp_read_write --db-driver=mysql --mysql-host=127.0.0.1 --mysql-port=3306 --mysql-user=root --mysql-password=root --mysql-db=testdb --tables=10 --table-size=100000 prepare
   ```
2. **부하 생성 및 벤치마크 실행 (Run):**
   16개 동시 스레드로 60초 동안 읽기/쓰기 복합 테스트를 수행합니다.
   ```bash
   sysbench oltp_read_write --db-driver=mysql --mysql-host=127.0.0.1 --mysql-port=3306 --mysql-user=root --mysql-password=root --mysql-db=testdb --threads=16 --time=60 run
   ```
3. **결과 지표 분석 (Analysis):**
   - **TPS (Transactions Per Second):** 초당 처리 가능한 트랜잭션 수 (높을수록 좋음)
   - **Latency (95th/99th percentile):** 요청 응답의 지연 시간 (낮을수록 좋음)

이러한 지표를 튜닝 전후로 비교하여 최적화 효과를 데이터로 실증합니다.
