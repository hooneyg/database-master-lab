# 🔄 DAO vs DTO vs Entity: 왜 분리해야 하는가?

> **"Entity를 API 응답으로 보내는 것은, 당신의 집 설계도와 금고 열쇠를 지나가는 행인에게 보여주는 것과 같다."**

현대적인 백엔드 아키텍처에서 데이터 객체를 계층별로 분리하는 것은 선택이 아닌 **필수**입니다. 본 가이드는 `database-master-lab`에서 정의한 객체들의 역할과 분리 이유를 설명합니다.

---

## 🏗️ 객체별 역할 정의 (Responsibilities)

### 1. Entity (Domain Object)
- **역할**: 실제 데이터베이스 테이블과 1:1 매핑되는 객체입니다.
- **특징**:
  - 비즈니스 로직의 중심입니다. (Rich Domain Model)
  - JPA에 의해 생명주기가 관리됩니다.
  - 외부(API)에 노출되어서는 안 됩니다.
- **예시**: `UserEntity.java`

### 2. DTO (Data Transfer Object)
- **역할**: 계층(Controller ↔ Service ↔ Repository) 간 데이터 전달을 위한 객체입니다.
- **특징**:
  - 오직 데이터 전달만을 위해 존재하며 비즈니스 로직을 갖지 않습니다.
  - API 스펙에 맞춰 최적화된 구조를 가집니다.
  - `RequestDTO`(입력 검증)와 `ResponseDTO`(결과 가공)로 나뉩니다.
- **예시**: `UserRequestDTO.java`, `UserResponseDTO.java`

---

## 💡 DTO 분리가 필요한 6가지 결정적 이유

### 1. 보안 (Security)
Entity는 `password`, `ssn`, 내부 `id` 등 민감한 정보를 포함할 가능성이 높습니다. Entity를 그대로 반환하면 의도치 않게 이 정보들이 노출됩니다. DTO를 통해 **필요한 데이터만 선별적으로 공개**해야 합니다.

### 2. 순환 참조 방지 (Circular Reference)
JPA 양방향 연관관계(`@OneToMany`, `@ManyToOne`)가 설정된 Entity를 JSON으로 변환할 때, 서로를 계속 참조하며 무한 루프에 빠져 `StackOverflowError`가 발생할 수 있습니다. DTO는 이 관계를 끊어주는 역할을 합니다.

### 3. API 스펙의 안정성 (Flexibility)
DB 설계(Entity)가 변경된다고 해서 클라이언트가 사용하는 API(DTO)가 즉시 깨져서는 안 됩니다. DTO가 중간에서 완충 역할을 하여, **DB 구조가 바뀌어도 API 스펙을 일정하게 유지**할 수 있습니다.

### 4. 유효성 검증의 분리 (Validation)
`@NotBlank`, `@Email` 같은 입력 검증 로직은 API 계층의 관심사입니다. 이를 Entity에 넣으면 도메인 모델이 오염됩니다. DTO에 검증 로직을 집중시킴으로써 **비즈니스 로직과 검증 로직을 분리**할 수 있습니다.

### 5. 가용성 및 성능 (Performance)
하나의 Entity에 대해 다양한 API가 존재할 수 있습니다. (예: 마이페이지 조회 vs 목록 조회) 각 API마다 요구하는 데이터가 다른데, 매번 전체 Entity를 조회하고 전달하는 것은 낭비입니다. DTO는 **각 요청에 최적화된 데이터 세트**를 구성하게 해줍니다.

### 6. 전송 비용 절감
Entity에는 수십 개의 컬럼이 있을 수 있지만, 클라이언트는 이름과 이메일만 필요할 수 있습니다. DTO를 통해 **데이터 사이즈를 최소화**하여 네트워크 전송 비용을 절감합니다.

---

## 🛠️ 실무 팁: 변환(Mapping)은 어디서?

- **Service Layer**: 가장 권장되는 위치입니다. Service가 Entity를 받아 비즈니스 로직을 처리한 후, Controller에게 DTO로 변환하여 넘겨줍니다. 이렇게 하면 Controller는 Entity의 존재를 아예 모르게 됩니다. (계층 간 완벽한 격리)
- **도구**: `MapStruct` 같은 라이브러리를 사용하면 반복적인 `new DTO(entity.get...)` 코드를 줄일 수 있지만, 로직이 단순하다면 **Static Factory Method**(`UserResponseDTO.from(entity)`) 방식이 가독성 면에서 훌륭합니다.

---

> **결론**: DTO 분리는 코드가 많아지는 "귀찮은 작업"이 아니라, 시스템의 **안정성, 보안성, 유지보수성**을 보장하는 강력한 방어막입니다.
