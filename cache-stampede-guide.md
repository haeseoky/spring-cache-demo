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

## 3. 해결 방법

### 3.1 Mutex Lock 방식

첫 번째 요청만 원본 데이터를 계산하고, 다른 요청은 대기하는 방식입니다.

**장점**:
- 명확하고 직관적인 해결책
- 한 번만 데이터를 계산하여 리소스를 효율적으로 사용

**단점**:
- 락 관리의 오버헤드
- 첫 번째 요청이 오래 걸릴 경우 다른 요청의 대기 시간 증가

### 3.2 캐시 워밍(Cache Warming)

캐시가 만료되기 전에 미리 갱신하는 방식입니다.

**장점**:
- 사용자 요청 시 캐시 미스 가능성 감소
- 예측 가능한 시스템 부하

**단점**:
- 사용되지 않는 데이터도 갱신할 가능성
- 추가적인 스케줄링 시스템 필요

### 3.3 PER(Probabilistic Early Recomputation) 알고리즘

캐시 만료 시간에 가까워질수록 확률적으로 미리 재계산하는 방식입니다.

**장점**:
- 불필요한 재계산 최소화
- 부하를 시간상 분산

**단점**:
- 구현 복잡성
- 정확한 확률 조정이 필요

### 3.4 Stale-While-Revalidate 패턴

만료된 데이터를 반환하면서 백그라운드에서 새로운 데이터를 갱신하는 방식입니다.

**장점**:
- 사용자에게 일관된 응답 시간 제공
- 백그라운드에서 갱신하므로 사용자 체감 성능 좋음

**단점**:
- 약간 오래된 데이터를 제공할 수 있음
- 구현 복잡성 증가

## 4. Spring Boot에서의 구현 예제

다음은 Spring Boot 환경에서 Cache Stampede 방지를 위한 여러 방법의 구현 예제입니다.

### 4.1 Mutex Lock 방식 예제

```java
@Service
public class ProductService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;

    public ProductService(RedisTemplate<String, Object> redisTemplate, ProductRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }

    public Product getProductById(Long id) {
        String cacheKey = "product:" + id;
        String lockKey = "lock:" + cacheKey;

        // 캐시에서 먼저 조회
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (product != null) {
            return product;
        }

        // 락 획득 시도
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(10));
        if (Boolean.TRUE.equals(acquired)) {
            try {
                // 락을 획득한 후 한번 더 캐시 확인 (다른 스레드가 이미 캐시를 업데이트했을 수 있음)
                product = (Product) redisTemplate.opsForValue().get(cacheKey);
                if (product != null) {
                    return product;
                }

                // DB에서 데이터 조회
                product = productRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                // 캐시에 저장
                redisTemplate.opsForValue().set(cacheKey, product, Duration.ofHours(1));
                return product;
            } finally {
                // 락 해제
                redisTemplate.delete(lockKey);
            }
        } else {
            // 락을 획득하지 못한 경우, 잠시 대기 후 재시도
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return getProductById(id); // 재귀적으로 다시 시도
        }
    }
}
```

### 4.2 Cache Warming 예제

```java
@Service
public class CacheWarmingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;

    public CacheWarmingService(RedisTemplate<String, Object> redisTemplate, ProductRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }

    // 스케줄러를 사용하여 인기 상품의 캐시를 주기적으로 갱신
    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void warmPopularProductsCache() {
        log.info("캐시 워밍 작업 시작");
        List<Long> popularProductIds = getPopularProductIds();
        
        for (Long id : popularProductIds) {
            String cacheKey = "product:" + id;
            
            // DB에서 데이터 조회
            Product product = productRepository.findById(id)
                    .orElse(null);
            
            if (product != null) {
                // 캐시에 저장
                redisTemplate.opsForValue().set(cacheKey, product, Duration.ofHours(2));
                log.info("제품 ID {} 캐시 갱신 완료", id);
            }
        }
        log.info("캐시 워밍 작업 완료");
    }
    
    private List<Long> getPopularProductIds() {
        // 인기 상품 ID 목록을 반환하는 로직
        // 예: 최근 조회수, 판매량 등을 기준으로
        return productRepository.findTopProductIds(100);
    }
}
```

### 4.3 PER(Probabilistic Early Recomputation) 알고리즘 예제

```java
@Service
public class PerCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final Random random = new Random();

    public PerCacheService(RedisTemplate<String, Object> redisTemplate, ProductRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }

    public Product getProductById(Long id) {
        String cacheKey = "product:" + id;
        
        // 캐시에서 조회
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        
        // 캐시에 값이 있는 경우
        if (product != null) {
            // 캐시 만료 시간 조회
            Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.MILLISECONDS);
            if (ttl != null && ttl > 0) {
                // 초기 TTL (1시간 = 3600000ms)
                long initialTtl = 3600000;
                // 남은 TTL 비율 계산
                double remainingRatio = (double) ttl / initialTtl;
                
                // 남은 시간이 적을수록 재계산 확률이 높아짐
                if (remainingRatio < 0.2 && shouldRefreshCache(remainingRatio)) {
                    // 비동기로 캐시 갱신
                    CompletableFuture.runAsync(() -> {
                        refreshCache(id, cacheKey);
                    });
                }
            }
            return product;
        }
        
        // 캐시에 값이 없는 경우
        return refreshCache(id, cacheKey);
    }
    
    private boolean shouldRefreshCache(double remainingRatio) {
        // 남은 TTL 비율이 낮을수록 갱신 확률이 높아짐
        // 예: 20% 남았을 때 20% 확률로 갱신, 10% 남았을 때 50% 확률로 갱신
        double refreshProbability = 1 - remainingRatio;
        return random.nextDouble() < refreshProbability;
    }
    
    private Product refreshCache(Long id, String cacheKey) {
        // DB에서 데이터 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // 캐시에 저장
        redisTemplate.opsForValue().set(cacheKey, product, Duration.ofHours(1));
        return product;
    }
}
```

### 4.4 Stale-While-Revalidate 패턴 예제

```java
@Service
public class StaleWhileRevalidateService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;

    public StaleWhileRevalidateService(RedisTemplate<String, Object> redisTemplate, ProductRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }

    public Product getProductById(Long id) {
        String cacheKey = "product:" + id;
        String staleCacheKey = "stale:" + cacheKey;
        
        // 신선한 캐시에서 조회
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        
        if (product != null) {
            // 신선한 데이터 반환
            return product;
        }
        
        // 오래된 캐시에서 조회
        Product staleProduct = (Product) redisTemplate.opsForValue().get(staleCacheKey);
        
        // 백그라운드에서 캐시 갱신
        if (staleProduct != null) {
            // 오래된 데이터가 있으면 비동기로 갱신하고 오래된 데이터 반환
            CompletableFuture.runAsync(() -> {
                refreshCache(id, cacheKey, staleCacheKey);
            });
            return staleProduct;
        }
        
        // 캐시에 아무것도 없는 경우는 동기적으로 로드 (첫 로딩)
        return refreshCache(id, cacheKey, staleCacheKey);
    }
    
    private Product refreshCache(Long id, String cacheKey, String staleCacheKey) {
        // DB에서 데이터 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // 신선한 캐시에 저장 (TTL: 5분)
        redisTemplate.opsForValue().set(cacheKey, product, Duration.ofMinutes(5));
        
        // 오래된 캐시에 저장 (TTL: 1시간)
        redisTemplate.opsForValue().set(staleCacheKey, product, Duration.ofHours(1));
        
        return product;
    }
}
```

## 5. 결론

Cache Stampede는 고트래픽 서비스에서 흔히 발생할 수 있는 문제이지만, 적절한 전략을 사용하면 효과적으로 해결할 수 있습니다. 어떤 방법을 선택할지는 서비스의 특성, 부하 패턴, 데이터 신선도에 대한 요구사항 등에 따라 달라집니다.

실무에서는 이러한 방법들을 조합하여 사용하는 것이 일반적입니다:
- 매우 중요한 데이터는 캐시 워밍을 통해 미리 갱신
- 덜 중요하지만 자주 사용되는 데이터는 PER 알고리즘 적용
- 사용자 경험이 중요한 경우 Stale-While-Revalidate 패턴 사용

성능과 안정성을 모두 고려한 캐시 전략을 구축하여 서비스의 품질을 향상시키시기 바랍니다.
