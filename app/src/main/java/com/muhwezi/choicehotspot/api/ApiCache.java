package com.muhwezi.choicehotspot.api;

import android.util.Log;
import android.util.LruCache;

/**
 * In-memory API cache with TTL support.
 * Mirrors the JavaScript ApiCache implementation.
 */
public class ApiCache {

    private static final String TAG = "ApiCache";
    private static final long DEFAULT_TTL_MS = 5 * 60 * 1000; // 5 minutes
    private static final int MAX_SIZE = 100;

    private final LruCache<String, CacheEntry> cache;

    public ApiCache() {
        this.cache = new LruCache<>(MAX_SIZE);
    }

    /**
     * Get cached data if not expired.
     * 
     * @param key Cache key
     * @return Cached data or null if not found or expired
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry entry = cache.get(key);

        if (entry == null) {
            return null;
        }

        // Check if expired
        if (System.currentTimeMillis() > entry.expiry) {
            cache.remove(key);
            Log.d(TAG, "Cache expired for key: " + key);
            return null;
        }

        Log.d(TAG, "Cache hit for key: " + key);
        return (T) entry.data;
    }

    /**
     * Store data in cache with default TTL.
     * 
     * @param key  Cache key
     * @param data Data to cache
     */
    public void set(String key, Object data) {
        set(key, data, DEFAULT_TTL_MS);
    }

    /**
     * Store data in cache with custom TTL.
     * 
     * @param key   Cache key
     * @param data  Data to cache
     * @param ttlMs Time-to-live in milliseconds
     */
    public void set(String key, Object data, long ttlMs) {
        CacheEntry entry = new CacheEntry(data, System.currentTimeMillis() + ttlMs);
        cache.put(key, entry);
        Log.d(TAG, "Cache set for key: " + key + " (TTL: " + ttlMs + "ms)");
    }

    /**
     * Delete a specific cache entry.
     * 
     * @param key Cache key
     */
    public void delete(String key) {
        cache.remove(key);
        Log.d(TAG, "Cache deleted for key: " + key);
    }

    /**
     * Clear all cache entries.
     */
    public void clear() {
        cache.evictAll();
        Log.d(TAG, "Cache cleared");
    }

    /**
     * Clear cache entries matching a pattern.
     * 
     * @param pattern Pattern to match (uses contains)
     */
    public void clearPattern(String pattern) {
        int count = 0;
        for (String key : cache.snapshot().keySet()) {
            if (key.contains(pattern)) {
                cache.remove(key);
                count++;
            }
        }
        Log.d(TAG, "Cleared " + count + " cache entries matching pattern: " + pattern);
    }

    /**
     * Get current cache size.
     */
    public int size() {
        return cache.size();
    }

    /**
     * Cache entry wrapper with expiry time.
     */
    private static class CacheEntry {
        final Object data;
        final long expiry;

        CacheEntry(Object data, long expiry) {
            this.data = data;
            this.expiry = expiry;
        }
    }
}
