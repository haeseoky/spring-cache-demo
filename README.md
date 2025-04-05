# Cache Stampede 예제 프로젝트

이 프로젝트는 Cache Stampede 문제와 다양한 해결 방법을 Spring Boot와 Redis를 이용하여 구현한 예제입니다.

## 프로젝트 개요

- **Cache Stampede란**: 캐시 항목이 만료되었거나 아직 캐시에 존재하지 않을 때 다수의 사용자 요청이 동시에 발생하여 원본 데이터 소스(예: 데이터베이스)에 과도한 부하를 주는 문제입니다.

- **구현 방식**:
  - Mutex Lock 방식
  - 캐시 워밍(Cache Warming) 방식
  - PER(Probabilistic Early Recomputation) 알고리즘
  - Stale-While-Revalidate 패턴

## 주요 컴포넌트

### 도메인 모델
- `Product.java`: 상품 도메인 모델

### 데이터 액세스
- `ProductRepository.java`: 상품 데이터 접근을 위한 Repository (인메모리 구현)
- `RedisConfig.java`: Redis 연결 및 설정

### 캐시 서비스
- `MutexLockCacheService.java`: Mutex Lock 방식 구현
- `CacheWarmingService.java`: 캐시 워밍 방식 구현
- `PerCacheService.java`: PER 알고리즘 구현
- `StaleWhileRevalidateService.java`: Stale-While-Revalidate 패턴 구현

### API
- `ProductController.java`: 상품 API 및 시뮬레이션 엔드포인트

## 시작하기

### 사전 요구사항
- JDK 11 이상
- Redis 서버 (로컬 또는 Docker로 실행)

### Redis Docker로 실행하기
```bash
docker run --name redis -p 6379:6379 -d redis
```

### 애플리케이션 실행하기
```bash
./gradlew bootRun
```

## API 사용 방법

### 다양한 캐시 전략으로 상품 조회
- Mutex Lock: `GET /api/products/mutex/{id}`
- 캐시 워밍: `GET /api/products/warming/{id}`
- PER 알고리즘: `GET /api/products/per/{id}`
- Stale-While-Revalidate: `GET /api/products/swr/{id}`

### Cache Stampede 시뮬레이션
여러 동시 요청을 시뮬레이션하여 각 전략의 효과를 비교할 수 있습니다.

```
GET /api/products/simulate?productId=1&strategy=mutex&concurrentRequests=20
```

- `productId`: 조회할 상품 ID (기본값: 1)
- `strategy`: 캐시 전략 (mutex, warming, per, swr 중 선택, 기본값: mutex)
- `concurrentRequests`: 동시 요청 수 (기본값: 10)

## 주의사항

- 이 프로젝트는 학습 및 테스트 목적으로 만들어졌습니다.
- 실제 프로덕션 환경에서는 추가적인 보안 및 성능 최적화가 필요합니다.
- Redis가 실행 중이어야 애플리케이션이 정상적으로 동작합니다.
