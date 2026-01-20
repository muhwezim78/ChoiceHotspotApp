package com.muhwezi.choicehotspot.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API Client singleton for managing Retrofit instance and network
 * configuration.
 * Mirrors the JavaScript ApiService configuration with auth tokens, retry
 * logic, and caching.
 */
public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "https://choicehotspot.online";
    private static final long TIMEOUT_SECONDS = 300; // 5 minutes, matching JS version
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private static final String PREFS_NAME = "ChoiceHotspotPrefs";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_IS_ADMIN = "isAdmin";

    private static ApiClient instance;
    private final Retrofit retrofit;
    private final ApiService apiService;
    private final ApiCache cache;
    private final SharedPreferences prefs;
    private final Gson gson;

    private ApiClient(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.cache = new ApiCache();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setLenient()
                .registerTypeAdapter(Long.class, new com.google.gson.JsonDeserializer<Long>() {
                    @Override
                    public Long deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT,
                            com.google.gson.JsonDeserializationContext context)
                            throws com.google.gson.JsonParseException {
                        if (json.isJsonNull())
                            return 0L;
                        if (json.isJsonPrimitive()) {
                            if (json.getAsJsonPrimitive().isNumber()) {
                                return json.getAsLong();
                            } else if (json.getAsJsonPrimitive().isString()) {
                                try {
                                    return Long.parseLong(json.getAsString());
                                } catch (NumberFormatException e) {
                                    return 0L;
                                }
                            }
                        }
                        return 0L;
                    }
                })
                .registerTypeAdapter(long.class, new com.google.gson.JsonDeserializer<Long>() {
                    @Override
                    public Long deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT,
                            com.google.gson.JsonDeserializationContext context)
                            throws com.google.gson.JsonParseException {
                        if (json.isJsonNull())
                            return 0L;
                        if (json.isJsonPrimitive()) {
                            if (json.getAsJsonPrimitive().isNumber()) {
                                return json.getAsLong();
                            } else if (json.getAsJsonPrimitive().isString()) {
                                try {
                                    return Long.parseLong(json.getAsString());
                                } catch (NumberFormatException e) {
                                    return 0L;
                                }
                            }
                        }
                        return 0L;
                    }
                })
                .create();

        OkHttpClient client = buildOkHttpClient();

        this.retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        this.apiService = retrofit.create(ApiService.class);

        Log.d(TAG, "🔌 API Client initialized with base URL: " + BASE_URL);
    }

    /**
     * Initialize the ApiClient singleton. Must be called before getInstance().
     * Typically called in Application.onCreate().
     */
    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
    }

    /**
     * Get the singleton instance of ApiClient.
     * 
     * @throws IllegalStateException if init() has not been called.
     */
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ApiClient not initialized. Call ApiClient.init(context) first.");
        }
        return instance;
    }

    /**
     * Get the ApiService for making API calls.
     */
    public ApiService getApiService() {
        return apiService;
    }

    /**
     * Get the API cache for manual cache operations.
     */
    public ApiCache getCache() {
        return cache;
    }

    /**
     * Get the Gson instance for JSON serialization.
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Get the base URL.
     */
    public static String getBaseUrl() {
        return BASE_URL;
    }

    // ==================== Token Management ====================

    /**
     * Save the authentication token.
     */
    public void setAuthToken(String token) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply();
        Log.d(TAG, "Auth token saved");
    }

    /**
     * Get the current authentication token.
     */
    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    /**
     * Clear the authentication token (logout).
     */
    public void clearAuthToken() {
        prefs.edit()
                .remove(KEY_AUTH_TOKEN)
                .remove(KEY_IS_ADMIN)
                .apply();
        cache.clear();
        Log.d(TAG, "Auth token cleared");
    }

    /**
     * Check if user is authenticated.
     */
    public boolean isAuthenticated() {
        return getAuthToken() != null;
    }

    /**
     * Set admin status.
     */
    public void setIsAdmin(boolean isAdmin) {
        prefs.edit().putBoolean(KEY_IS_ADMIN, isAdmin).apply();
    }

    /**
     * Check if current user is admin.
     */
    public boolean isAdmin() {
        return prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    // ==================== OkHttp Client Setup ====================

    private OkHttpClient buildOkHttpClient() {
        // Logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Log.d(TAG, message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor())
                .addInterceptor(new RetryInterceptor())
                .addInterceptor(loggingInterceptor)
                .build();
    }

    /**
     * Interceptor to add authorization header to requests.
     */
    private class AuthInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request originalRequest = chain.request();
            String token = getAuthToken();

            if (token == null) {
                return chain.proceed(originalRequest);
            }

            Request.Builder builder = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json");

            return chain.proceed(builder.build());
        }
    }

    /**
     * Interceptor to retry failed requests.
     */
    private class RetryInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            Response response = null;
            IOException lastException = null;

            int retryCount = 0;

            while (retryCount < MAX_RETRIES) {
                try {
                    if (response != null) {
                        response.close();
                    }
                    response = chain.proceed(request);

                    // Success or client error (4xx) - don't retry
                    if (response.isSuccessful() || (response.code() >= 400 && response.code() < 500)) {
                        return response;
                    }

                    // Server error (5xx) - retry
                    if (response.code() >= 500) {
                        retryCount++;
                        Log.w(TAG, "Server error " + response.code() + ", retry " + retryCount + "/" + MAX_RETRIES);

                        if (retryCount < MAX_RETRIES) {
                            try {
                                Thread.sleep(RETRY_DELAY_MS * retryCount);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IOException("Retry interrupted", e);
                            }
                        }
                    } else {
                        // Other response codes - return as-is
                        return response;
                    }

                } catch (IOException e) {
                    lastException = e;
                    retryCount++;
                    Log.w(TAG, "Network error: " + e.getMessage() + ", retry " + retryCount + "/" + MAX_RETRIES);

                    if (retryCount < MAX_RETRIES) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS * retryCount);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new IOException("Retry interrupted", ie);
                        }
                    }
                }
            }

            // If we have a response (even error), return it
            if (response != null) {
                return response;
            }

            // Otherwise throw the last exception
            if (lastException != null) {
                throw lastException;
            }

            throw new IOException("Request failed after " + MAX_RETRIES + " retries");
        }
    }
}
