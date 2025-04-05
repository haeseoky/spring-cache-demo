# Cache Stampede 문제와 해결 방법

## 1. Cache Stampede란?

Cache Stampede(캐시 스탬피드)는 캐시 항목이 만료되었거나 아직 캐시에 존재하지 않을 때 다수의 사용자 요청이 동시에 발생하여 원본 데이터 소스(예: 데이터베이스)에 과도한 부하를 주는 문제를 말합니다. 

특히 다음과 같은 상황에서 발생합니다:
- 인기 있는 항목의 캐시가 만료된 시점에 많은 요청이 동시에 들어옴
- 트래픽이 급증하는 시간대에 동시에 여러 캐시 항목이 만료됨
- 캐시 키가 없는 상태에서 수많은 동시 요청이 데이터베이스에 동일한 쿼리를 수행

## 2. 문제점

Cache Stampede로 인한 주요 문제점:

1. **데이터베이스 과부하**: 다수의 요청이 동시에 데이터베이스에 접근하여 부하를 가중시킴
2. **응답 시간 증가**: 데이터베이스 부하로 인해 전체 시스템의 응답 시간이 급격히 증가
3. **자원 낭비**: 동일한 데이터를 여러 인스턴스가 중복 계산
4. **서비스 불안정**: 최악의 경우 서비스 중단이나 데이터베이스 다운으로 이어질 수 있음

## 3. 클린 아키텍처 기반 해결 방법

이 프로젝트에서는 클린 아키텍처를 기반으로 다양한 캐시 스탬피드 방지 방법을 구현했습니다. 각 방법은 특정 계층에 책임을 분리하여 구현되어 있습니다.

### 3.1 Mutex Lock 방식

첫 번째 요청만 원본 데이터를 계산하고, 다른 요청은 대기하는 방식입니다.

**장점**:
- 명확하고 직관적인 해결책
- 한 번만 데이터를 계산하여 리소스를 효율적으로 사용

**단점**:
- 락 관리의 오버헤드
- 첫 번째 요청이 오래 걸릴 경우 다른 요청의 대기 시간 증가

**클린 아키텍처 구현**:
- 애플리케이션 계층: `MutexLockCacheService`
- 인프라스트럭처 계층: `RedisLuaLock`

### 3.2 캐시 워밍(Cache Warming)

캐시가 만료되기 전에 미리 갱신하는 방식입니다.

**장점**:
- 사용자 요청 시 캐시 미스 가능성 감소
- 예측 가능한 시스템 부하

**단점**:
- 사용되지 않는 데이터도 갱신할 가능성
- 추가적인 스케줄링 시스템 필요

**클린 아키텍처 구현**:
- 애플리케이션 계층: `CacheWarmingService`

### 3.3 PER(Probabilistic Early Recomputation) 알고리즘

캐시 만료 시간에 가까워질수록 확률적으로 미리 재계산하는 방식입니다.

**장점**:
- 불필요한 재계산 최소화
- 부하를 시간상 분산

**단점**:
- 구현 복잡성
- 정확한 확률 조정이 필요

**클린 아키텍처 구현**:
- 애플리케이션 계층: `PerCacheService`

### 3.4 Stale-While-Revalidate 패턴

만료된 데이터를 반환하면서 백그라운드에서 새로운 데이터를 갱신하는 방식입니다.

**장점**:
- 사용자에게 일관된 응답 시간 제공
- 백그라운드에서 갱신하므로 사용자 체감 성능 좋음

**단점**:
- 약간 오래된 데이터를 제공할 수 있음
- 구현 복잡성 증가

**클린 아키텍처 구현**:
- 애플리케이션 계층: `StaleWhileRevalidateService`

## 4. 클린 아키텍처 기반 구현 예제

다음은 클린 아키텍처에 맞게 구현된 예제입니다.

### 4.1 Mutex Lock 방식 예제

```java
// 애플리케이션 계층 구현
@Service
public class MutexLockCacheService implements CacheService<ProductDto> {
    
    private final CacheService<ProductDto> delegateCacheService;
    private final ProductRepository productRepository;
    private final DistributedLock distributedLock;
    
    // Mutex Lock을 사용한 캐시 관리
    @Override
    public ProductDto getOrLoad(String key, java.util.function.Supplier<ProductDto> loader, Duration ttl) {
        String cacheKey = CACHE_KEY_PREFIX + key;
        String lockKey = LOCK_KEY_PREFIX + key;
        
        // 1. 캐시에서 먼저 조회
        ProductDto value = delegateCacheService.getOrLoad(cacheKey, () -> null, ttl);
        if (value != null) {
            return value;
        }
        
        // 2. 락 획득 시도
        try {
            return distributedLock.executeWithLock(lockKey, LOCK_TIMEOUT, () -> {
                // 3. 락 획득 후 캐시 다시 확인 (다른 스레드가 이미 업데이트했을 수 있음)
                ProductDto cachedValue = delegateCacheService.getOrLoad(cacheKey, () -> null, ttl);
                if (cachedValue != null) {
                    return cachedValue;
                }
                
                // 4. 원본 로더 실행
                ProductDto loadedValue = loader.get();
                
                // 5. 캐시에 저장
                if (loadedValue != null) {
                    delegateCacheService.put(cacheKey, loadedValue, ttl);
                }
                
                return loadedValue;
            });
        } catch (Exception e) {
            // 락 획득 실패 시 기본 로더 실행
            return loader.get();
        }
    }
}
```

### 4.2 Stale-While-Revalidate 패턴 예제

```java
// 애플리케이션 계층 구현
@Service
public class StaleWhileRevalidateService implements CacheService<ProductDto> {
    
    private static final String FRESH_CACHE_PREFIX = "swr:fresh:";
    private static final String STALE_CACHE_PREFIX = "swr:stale:";
    private static final Duration STALE_TTL = Duration.ofHours(1);
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public ProductDto getOrLoad(String key, Supplier<ProductDto> loader, Duration ttl) {
        String freshKey = FRESH_CACHE_PREFIX + key;
        String staleKey = STALE_CACHE_PREFIX + key;
        
        // 1. 신선한 캐시에서 조회
        ProductDto product = (ProductDto) redisTemplate.opsForValue().get(freshKey);
        
        if (product != null) {
            return product;
        }
        
        // 2. 오래된 캐시에서 조회
        ProductDto staleProduct = (ProductDto) redisTemplate.opsForValue().get(staleKey);
        
        // 3. 오래된 캐시에서 데이터를 찾은 경우
        if (staleProduct != null) {
            // 4. 백그라운드에서 캐시 갱신
            CompletableFuture.runAsync(() -> {
                refreshCache(key, freshKey, staleKey, loader, ttl);
            });
            
            return staleProduct;
        }
        
        // 5. 캐시에 아무것도 없는 경우 동기적으로 로드 (첫 로딩)
        return refreshCache(key, freshKey, staleKey, loader, ttl);
    }
    
    // 캐시 갱신 메서드
    private ProductDto refreshCache(String key, String freshKey, String staleKey, 
                                  Supplier<ProductDto> loader, Duration ttl) {
        ProductDto product = loader.get();
        
        if (product != null) {
            // 신선한 캐시에 저장
            put(freshKey, product, ttl);
            
            // 오래된 캐시에 저장 (더 긴 TTL)
            put(staleKey, product, STALE_TTL);
        }
        
        return product;
    }
}
```

## 5. 클린 아키텍처의 이점

클린 아키텍처 기반으로 캐시 전략을 구현함으로써 다음과 같은 이점을 얻을 수 있습니다:

1. **관심사 분리**
   - 캐시 전략은 애플리케이션 계층에 정의
   - 캐시 저장소 구현은 인프라스트럭처 계층에 정의
   - 비즈니스 로직은 도메인 계층에 정의

2. **테스트 용이성**
   - 각 전략을 개별적으로 테스트 가능
   - Mock 객체를 이용한 의존성 주입으로 격리된 테스트 가능

3. **유연한 확장**
   - 새로운 캐시 전략 추가가 용이함
   - 캐시 저장소를 변경해도 애플리케이션 로직에 영향 없음
   - 인터페이스를 통한 의존성 주입으로 구현체 교체 용이

4. **명확한 책임 구분**
   - 캐시 전략에 대한 책임과 데이터 접근에 대한 책임이 분리됨
   - 코드 가독성과 유지보수성 향상

## 6. 결론

Cache Stampede는 고트래픽 서비스에서 흔히 발생할 수 있는 문제이지만, 클린 아키텍처를 기반으로 적절한 전략을 구현하면 효과적으로 해결할 수 있습니다. 어떤 방법을 선택할지는 서비스의 특성, 부하 패턴, 데이터 신선도에 대한 요구사항 등에 따라 달라집니다.

실무에서는 이러한 방법들을 조합하여 사용하는 것이 일반적입니다:
- 매우 중요한 데이터는 캐시 워밍을 통해 미리 갱신
- 덜 중요하지만 자주 사용되는 데이터는 PER 알고리즘 적용
- 사용자 경험이 중요한 경우 Stale-While-Revalidate 패턴 사용

클린 아키텍처를 적용함으로써 각 전략의 구현을 명확히 분리하고, 향후 새로운 전략을 추가하거나 기존 전략을 수정하는 데 있어 유연성을 제공합니다. 또한 각 계층의 책임이 명확하게 구분되어 있어, 변경 시 영향 범위를 최소화할 수 있습니다.
