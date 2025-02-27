package com.gizmo.brennon.core.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class CacheManager implements Service {
    private final Logger logger;
    private final Map<String, CacheEntry<?>> cache;
    private ScheduledExecutorService scheduler;

    @Inject
    public CacheManager(Logger logger) {
        this.logger = logger;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public void enable() throws Exception {
        this.scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::cleanupCache, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void disable() throws Exception {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler.awaitTermination(1, TimeUnit.MINUTES);
        }
        cache.clear();
    }

    public <T> void put(String key, T value, long ttlMillis) {
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + ttlMillis));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        CacheEntry<?> entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return Optional.of((T) entry.getValue());
        }
        cache.remove(key);
        return Optional.empty();
    }

    public void remove(String key) {
        cache.remove(key);
    }

    private void cleanupCache() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private static class CacheEntry<T> {
        private final T value;
        private final long expiryTime;

        public CacheEntry(T value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }

        public T getValue() {
            return value;
        }

        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }

        public boolean isExpired(long now) {
            return now > expiryTime;
        }
    }
}
