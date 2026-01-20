package com.muhwezi.choicehotspot.api;

/**
 * Generic callback interface for async API operations.
 * 
 * @param <T> Type of the successful response data
 */
public interface ApiCallback<T> {

    /**
     * Called when the API call succeeds.
     * 
     * @param data The response data
     */
    void onSuccess(T data);

    /**
     * Called when the API call fails.
     * 
     * @param message Error message
     * @param error   The original error/throwable (may be null)
     */
    void onError(String message, Throwable error);
}
