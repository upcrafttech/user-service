package com.upcraft.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheHelper {

    private final CacheManager cacheManager;

    public void evictCache(String cacheName) {
        Optional.ofNullable(cacheManager.getCache(cacheName))
                .ifPresent(Cache::clear);
        log.info("Cleared cache: {}", cacheName);
    }

    public void evictCacheKey(String cacheName, Object key) {
        Optional.ofNullable(cacheManager.getCache(cacheName))
                .ifPresent(cache -> cache.evict(key));
        log.info("Evicted key: {} from cache: {}", key, cacheName);
    }
}
