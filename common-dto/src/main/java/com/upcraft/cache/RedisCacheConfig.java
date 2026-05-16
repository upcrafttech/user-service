package com.upcraft.cache;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                .withCacheConfiguration("employeeCache",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(24)))
                .withCacheConfiguration("taxRatesCache",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(30)))
                .withCacheConfiguration("leaveTypesCache",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(30)))
                .withCacheConfiguration("salaryStructuresCache",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(7)));
    }
}
