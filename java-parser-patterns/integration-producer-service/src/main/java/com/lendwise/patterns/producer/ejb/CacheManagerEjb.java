package com.lendwise.patterns.producer.ejb;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton EJB for caching.
 * Parser should detect: @Singleton, @Startup, @Lock, @Schedule
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Slf4j
public class CacheManagerEjb {

    private Map<String, CreditCheckEjb.CreditCheckResult> creditCache;
    private Map<String, Object> genericCache;

    @PostConstruct
    public void init() {
        log.info("Initializing CacheManager EJB");
        creditCache = new ConcurrentHashMap<>();
        genericCache = new ConcurrentHashMap<>();
    }

    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up CacheManager EJB");
        creditCache.clear();
        genericCache.clear();
    }

    /**
     * Get cached credit result.
     * Parser detects: @Lock(READ)
     */
    @Lock(LockType.READ)
    public CreditCheckEjb.CreditCheckResult getCachedResult(String borrowerId) {
        CreditCheckEjb.CreditCheckResult result = creditCache.get(borrowerId);

        if (result != null) {
            // Check if cache entry is still valid (e.g., less than 24 hours old)
            long age = System.currentTimeMillis() - result.getTimestamp();
            if (age > 24 * 60 * 60 * 1000) {
                // Expired
                return null;
            }
        }

        return result;
    }

    /**
     * Cache credit result.
     * Parser detects: @Lock(WRITE)
     */
    @Lock(LockType.WRITE)
    public void cacheResult(String borrowerId, CreditCheckEjb.CreditCheckResult result) {
        creditCache.put(borrowerId, result);
        log.debug("Cached credit result for borrower: {}", borrowerId);
    }

    /**
     * Generic cache get.
     */
    @Lock(LockType.READ)
    public Object get(String key) {
        return genericCache.get(key);
    }

    /**
     * Generic cache put.
     */
    @Lock(LockType.WRITE)
    public void put(String key, Object value) {
        genericCache.put(key, value);
    }

    /**
     * Remove from cache.
     */
    @Lock(LockType.WRITE)
    public void remove(String key) {
        creditCache.remove(key);
        genericCache.remove(key);
    }

    /**
     * Clear all caches.
     */
    @Lock(LockType.WRITE)
    public void clearAll() {
        creditCache.clear();
        genericCache.clear();
        log.info("All caches cleared");
    }

    /**
     * Scheduled cache cleanup.
     * Parser detects: @Schedule annotation
     */
    @Schedule(hour = "2", minute = "0", persistent = false)
    public void scheduledCleanup() {
        log.info("Running scheduled cache cleanup");

        long now = System.currentTimeMillis();
        long expirationThreshold = 24 * 60 * 60 * 1000; // 24 hours

        creditCache.entrySet().removeIf(entry -> {
            long age = now - entry.getValue().getTimestamp();
            return age > expirationThreshold;
        });

        log.info("Cache cleanup completed. Remaining entries: {}", creditCache.size());
    }

    /**
     * Get cache statistics.
     */
    @Lock(LockType.READ)
    public CacheStats getStats() {
        return new CacheStats(creditCache.size(), genericCache.size());
    }

    public record CacheStats(int creditCacheSize, int genericCacheSize) {}
}
