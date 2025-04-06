package com.example.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 연결 및 템플릿 설정
 * 도메인 엔티티용 RedisTemplate과 분산락용 String RedisTemplate 제공
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 로컬 Redis에 연결
        return new LettuceConnectionFactory("localhost", 6379);
    }
    
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
    
    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        // 커스텀 ObjectMapper를 사용하는 생성자 활용
        return new GenericJackson2JsonRedisSerializer(redisObjectMapper());
    }
    
    /**
     * 도메인 엔티티용 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        
        // 커스텀 GenericJackson2JsonRedisSerializer 사용
        GenericJackson2JsonRedisSerializer serializer = genericJackson2JsonRedisSerializer();
        
        // 키와 값에 대한 직렬화 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);
        
        // Hash 키와 값에 대한 직렬화 설정
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);
        
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
    
    /**
     * 분산락 및 단순 문자열 저장용 RedisTemplate
     */
    @Bean(name = "lockStringRedisTemplate")
    public RedisTemplate<String, String> lockStringRedisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        
        // String 직렬화 사용
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
