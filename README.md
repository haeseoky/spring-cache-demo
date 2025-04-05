# 분산락을 이용한 선착순 이벤트 구현 (클린 아키텍처)

이 프로젝트는 클린 아키텍처 기반으로 Redis를 이용한 분산락을 통해 선착순 이벤트를 안전하게 처리하는 방법을 구현한 예제입니다.

## 프로젝트 구조

```
com.example
  ├── domain - 도메인 계층 (엔티티, 비즈니스 규칙)
  │   ├── product - 상품 관련 도메인
  │   │   ├── entity
  │   │   │   └── Product.java - 상품 엔티티
  │   │   ├── repository
  │   │   │   └── ProductRepository.java - 상품 저장소 인터페이스
  │   │   └── service
  │   │       └── ProductService.java - 상품 도메인 서비스
  │   │
  │   └── event - 이벤트 관련 도메인
  │       ├── entity
  │       │   ├── Event.java - 이벤트 엔티티
  │       │   └── EventParticipation.java - 이벤트 참여 엔티티
  │       ├── repository
  │       │   └── EventRepository.java - 이벤트 저장소 인터페이스
  │       └── service
  │           └── FirstComeEventDomainService.java - 선착순 도메인 서비스
  │
  ├── application - 애플리케이션 계층 (유스케이스)
  │   ├── product
  │   │   ├── dto
  │   │   │   └── ProductDto.java - 상품 DTO
  │   │   └── service
  │   │       ├── CacheService.java - 캐시 서비스 인터페이스
  │   │       ├── StaleWhileRevalidateService.java - SWR 구현
  │   │       └── ProductApplicationService.java - 상품 애플리케이션 서비스
  │   │
  │   └── event
  │       ├── dto
  │       │   └── EventDto.java - 이벤트 DTO
  │       └── service
  │           └── FirstComeApplicationService.java - 선착순 애플리케이션 서비스
  │
  ├── infrastructure - 인프라스트럭처 계층 (외부 시스템 연동)
  │   ├── config
  │   │   └── RedisConfig.java - Redis 설정
  │   ├── repository
  │   │   ├── ProductRepositoryImpl.java - 상품 저장소 구현체
  │   │   └── EventRepositoryImpl.java - 이벤트 저장소 구현체
  │   ├── cache
  │   │   └── RedisCacheService.java - Redis 캐시 서비스 구현체
  │   └── lock
  │       ├── DistributedLock.java - 분산락 인터페이스
  │       ├── RedisSpinLock.java - 스핀락 구현체
  │       ├── RedisLuaLock.java - Lua 스크립트 락 구현체
  │       └── RedissonLock.java - Redisson 스타일 락 구현체
  │
  └── interfaces - 인터페이스 계층 (컨트롤러)
      ├── product
      │   └── ProductController.java - 상품 API 컨트롤러
      └── event
          └── FirstComeController.java - 선착순 API 컨트롤러
```

## 주요 기능

1. **분산락 구현**
   - `RedisSpinLock`: 스핀락 방식으로 구현한 분산락
   - `RedisLuaLock`: Lua 스크립트를 이용한 분산락
   - `RedissonLock`: Redisson 스타일의 워치독 메커니즘이 포함된 분산락

2. **캐시 스탬피드 방지**
   - `StaleWhileRevalidateService`: SWR 패턴으로 캐시 스탬피드 방지
   - `RedisCacheService`: 일반적인 Redis 캐시 서비스

3. **선착순 이벤트 구현**
   - 락 기반 선착순 처리: 분산락을 이용한 동시성 제어
   - Redis INCR 기반 선착순 처리: 원자적 카운터를 이용한 동시성 제어

## API 엔드포인트

### 이벤트 API
- `POST /api/events/init/{eventId}`: 이벤트 초기화
- `POST /api/events/participate/spinlock/{eventId}`: 스핀락을 사용한 선착순 이벤트 참여
- `POST /api/events/participate/lualock/{eventId}`: Lua 락을 사용한 선착순 이벤트 참여
- `POST /api/events/participate/redisson/{eventId}`: Redisson 락을 사용한 선착순 이벤트 참여
- `POST /api/events/participate/incr/{eventId}`: Redis INCR 명령어를 사용한 선착순 이벤트 참여
- `GET /api/events/simulate/{eventId}`: 동시에 다수의 요청 시뮬레이션
- `GET /api/events/status/{eventId}`: 이벤트 현황 조회

### 상품 API
- `GET /api/products/{id}`: ID로 상품 조회
- `GET /api/products/top`: 인기 상품 목록 조회
- `POST /api/products`: 상품 저장
- `DELETE /api/products/{id}/cache`: 상품 캐시 제거

## 클린 아키텍처 특징

이 프로젝트는 다음과 같은 클린 아키텍처 원칙을 따릅니다:

1. **의존성 규칙**: 안쪽 계층(도메인)은 바깥쪽 계층(인프라)에 의존하지 않습니다.
   - 도메인 계층은 인프라스트럭처 계층에 의존하지 않습니다.
   - 애플리케이션 계층은 도메인 계층에만 의존합니다.
   - 인프라스트럭처 계층과 인터페이스 계층은 애플리케이션 계층에 의존합니다.

2. **관심사 분리**:
   - 도메인 계층: 핵심 비즈니스 로직과 엔티티 정의
   - 애플리케이션 계층: 유스케이스 구현, 도메인 서비스 조정
   - 인프라스트럭처 계층: 외부 시스템 연동 (Redis, DB 등)
   - 인터페이스 계층: API 엔드포인트 제공

3. **도메인 중심 설계**: 핵심 비즈니스 로직은 도메인 계층에서 구현됩니다.

4. **테스트 용이성**: 의존성 역전 원칙을 통해 각 계층을 독립적으로 테스트할 수 있습니다.

## 분산락 구현 방식 비교

### 1. 스핀락(Spin Lock)
- **원리**: 락을 획득할 때까지 반복적으로 시도
- **장점**: 구현이 간단함
- **단점**: 지속적인 재시도로 인한 서버 부하 증가, Redis 서버에 많은 요청 발생

### 2. Lua 스크립트 락
- **원리**: Lua 스크립트를 통해 락 획득/해제를 원자적으로 처리
- **장점**: 원자성이 보장되어 더 안전함, 스핀락보다 부하가 적음
- **단점**: 락 획득 실패 시 재시도 로직이 필요함

### 3. Redisson 스타일 락
- **원리**: 워치독 메커니즘으로 락 타임아웃을 자동 갱신
- **장점**: 긴 작업 실행 중에도 락이 해제되는 문제 방지, 데드락 방지
- **단점**: 구현 복잡성 증가, 워치독 스레드 관리 필요

### 4. Redis INCR 명령어 방식
- **원리**: Redis의 원자적 증가 명령어(INCR)를 이용해 락 없이 카운팅
- **장점**: 락 획득/해제의 오버헤드 없음, 매우 빠른 처리 가능
- **단점**: 복잡한 비즈니스 로직 적용이 어려움, 단순 카운팅에만 적합

## 환경 설정

1. Redis 서버 실행 (localhost:6379)
2. 프로젝트 실행: `./gradlew bootRun`
3. API 테스트: Postman 또는 curl 사용

## 선택 가이드

- **단순 카운팅/순서 보장**: INCR 방식 사용
- **짧은 작업/단순 로직**: Lua 스크립트 락 사용
- **복잡한 비즈니스 로직/긴 작업**: Redisson 스타일 락 사용
- **개발 단계/테스트**: 스핀락 사용 (간단하게 구현 가능)
