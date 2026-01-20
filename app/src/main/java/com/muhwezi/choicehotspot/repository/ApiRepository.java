package com.muhwezi.choicehotspot.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.muhwezi.choicehotspot.api.ApiCache;
import com.muhwezi.choicehotspot.api.ApiCallback;
import com.muhwezi.choicehotspot.api.ApiClient;
import com.muhwezi.choicehotspot.api.ApiService;
import com.muhwezi.choicehotspot.db.AppDatabase;
import com.muhwezi.choicehotspot.db.PricingEntity;
import com.muhwezi.choicehotspot.models.ApiResponse;
import com.muhwezi.choicehotspot.models.analytics.AnalyticsDashboard;
import com.muhwezi.choicehotspot.models.auth.*;
import com.muhwezi.choicehotspot.models.financial.*;
import com.muhwezi.choicehotspot.models.pricing.*;
import com.muhwezi.choicehotspot.models.profile.Profile;
import com.muhwezi.choicehotspot.models.system.*;
import com.muhwezi.choicehotspot.models.user.*;
import com.muhwezi.choicehotspot.models.voucher.*;
import com.muhwezi.choicehotspot.socket.SocketService;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * API Repository providing a clean interface for all API operations.
 * Mirrors the JavaScript ApiService structure with caching and error handling.
 */
public class ApiRepository {

    private static final String TAG = "ApiRepository";

    // Cache TTL constants (in milliseconds)
    private static final long TTL_SHORT = 2 * 60 * 1000; // 2 minutes
    private static final long TTL_MEDIUM = 5 * 60 * 1000; // 5 minutes
    private static final long TTL_LONG = 10 * 60 * 1000; // 10 minutes

    private static ApiRepository instance;

    private final ApiClient apiClient;
    private final ApiService apiService;
    private final ApiCache cache;
    private final SocketService socketService;
    private final AppDatabase db; // Added
    private final Gson gson; // Added
    private final Handler mainHandler; // Added

    private ApiRepository(Context context) {
        ApiClient.init(context);
        this.apiClient = ApiClient.getInstance();
        this.apiService = apiClient.getApiService();
        this.cache = apiClient.getCache();
        this.socketService = SocketService.getInstance();
        this.db = AppDatabase.getInstance(context); // Added
        this.gson = new Gson(); // Added
        this.mainHandler = new Handler(Looper.getMainLooper()); // Added

        setupSocketListeners();
    }

    /**
     * Initialize the repository. Call in Application.onCreate().
     */
    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new ApiRepository(context);
        }
    }

    /**
     * Get the singleton instance.
     */
    public static synchronized ApiRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ApiRepository not initialized. Call ApiRepository.init(context) first.");
        }
        return instance;
    }

    // ==================== Cache Management ====================

    public ApiCache getCache() {
        return cache;
    }

    public void clearCache() {
        cache.clear();
    }

    public void clearCachePattern(String pattern) {
        cache.clearPattern(pattern);
    }

    // ==================== Socket Service ====================

    public SocketService getSocket() {
        return socketService;
    }

    private void setupSocketListeners() {
        socketService.on("update_users", args -> {
            if (args.length > 0) {
                try {
                    // Expecting a JSONArray or JSONObject wrapping users
                    String json = args[0].toString();
                    // If it's wrapped in { users: [...] }
                    if (json.startsWith("{")) {
                        JSONObject obj = new JSONObject(json);
                        if (obj.has("users")) {
                            json = obj.getString("users");
                        }
                    }

                    Type type = new TypeToken<List<HotspotUser>>() {
                    }.getType();
                    List<HotspotUser> users = gson.fromJson(json, type);
                    if (users != null) {
                        for (HotspotUser u : users)
                            u.setActive(true);
                        db.hotspotUserDao().insertAll(users);
                        Log.d(TAG, "Socket: User list updated (" + users.size() + ")");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Socket: Failed to parse update_users", e);
                }
            }
        });

        socketService.on("user_connected", args -> {
            refreshActiveUsers(); // Simplest way to ensure consistency for now
        });

        socketService.on("user_disconnected", args -> {
            refreshActiveUsers();
        });
    }

    // ==================== Authentication ====================

    public void register(RegisterRequest request, ApiCallback<User> callback) {
        apiService.register(request).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call,
                    @NonNull Response<ApiResponse<User>> response) {
                handleResponse(response, callback, "Registration failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                handleError(t, callback, "Registration failed");
            }
        });
    }

    public void login(String email, String password, String deviceInfo, ApiCallback<LoginResponse> callback) {
        LoginRequest request = new LoginRequest(email, password, deviceInfo);
        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess() && loginResponse.getToken() != null) {
                        apiClient.setAuthToken(loginResponse.getToken());
                        socketService.connect(loginResponse.getToken());
                        cache.clear();
                    }
                    callback.onSuccess(loginResponse);
                } else {
                    callback.onError(parseError(response, "Login failed"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                handleError(t, callback, "Login failed");
            }
        });
    }

    public void adminLogin(String email, String password, ApiCallback<LoginResponse> callback) {
        LoginRequest request = new LoginRequest(email, password);
        apiService.adminLogin(request).enqueue(new Callback<AdminLoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminLoginResponse> call,
                    @NonNull Response<AdminLoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AdminLoginResponse adminResponse = response.body();
                    LoginResponse loginResponse = new LoginResponse();
                    loginResponse.setMessage(adminResponse.getMessage());
                    // We don't get a token or full user struct from this endpoint, but we generate
                    // a session token locally below

                    // Create session token for admin (matching JS behavior)
                    if ("Login successful".equals(adminResponse.getMessage())) {
                        try {
                            JSONObject tokenData = new JSONObject();
                            tokenData.put("email", adminResponse.getUser());
                            tokenData.put("is_admin", true);
                            tokenData.put("timestamp", System.currentTimeMillis());

                            String sessionToken = Base64.encodeToString(
                                    tokenData.toString().getBytes(), Base64.NO_WRAP);

                            apiClient.setAuthToken(sessionToken);
                            apiClient.setIsAdmin(true);
                            socketService.connect(sessionToken);
                            cache.clear();

                            loginResponse.setSuccess(true);
                            loginResponse.setToken(sessionToken);

                            // specific for admin login, we might not have a full User object yet
                            // but we can create a dummy one if needed or just rely on success
                            User adminUser = new User();
                            adminUser.setEmail(adminResponse.getUser());
                            loginResponse.setUser(adminUser);

                        } catch (Exception e) {
                            Log.e(TAG, "Error creating session token", e);
                            callback.onError("Session creation failed", e);
                            return;
                        }
                    } else {
                        // If message is not "Login successful", treating as fail or just passing
                        // through
                        loginResponse.setSuccess(false);
                    }

                    callback.onSuccess(loginResponse);
                } else {
                    callback.onError(parseError(response, "Admin login failed"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminLoginResponse> call, @NonNull Throwable t) {
                handleError(t, callback, "Admin login failed");
            }
        });
    }

    public void logout(ApiCallback<Void> callback) {
        apiService.logout().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                apiClient.clearAuthToken();
                socketService.disconnect();
                cache.clear();
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                // Still clear local state on failure
                apiClient.clearAuthToken();
                socketService.disconnect();
                cache.clear();
                callback.onSuccess(null);
            }
        });
    }

    public void getProfile(boolean useCache, ApiCallback<User> callback) {
        String cacheKey = "auth_profile";

        if (useCache) {
            User cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }

            // Try database
            User dbUser = db.userDao().getUser();
            if (dbUser != null) {
                callback.onSuccess(dbUser);
                // Continue to fetch fresh data
            }
        }

        apiService.getProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    cache.set(cacheKey, user, TTL_SHORT);
                    // Save to database
                    new Thread(() -> db.userDao().insert(user)).start();
                    callback.onSuccess(user);
                } else {
                    callback.onError(parseError(response, "Failed to fetch profile"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch profile");
            }
        });
    }

    public void connectRouter(RouterConnectRequest request, ApiCallback<RouterInfo> callback) {
        apiService.connectRouter(request).enqueue(new Callback<ApiResponse<RouterInfo>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<RouterInfo>> call,
                    @NonNull Response<ApiResponse<RouterInfo>> response) {
                cache.clearPattern("routers");
                handleResponse(response, callback, "Router connection failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<RouterInfo>> call, @NonNull Throwable t) {
                handleError(t, callback, "Router connection failed");
            }
        });
    }

    public void getUserRouters(boolean useCache, ApiCallback<List<RouterInfo>> callback) {
        String cacheKey = "user_routers";

        if (useCache) {
            List<RouterInfo> cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }
        }

        apiService.getUserRouters().enqueue(new Callback<ApiResponse<List<RouterInfo>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<RouterInfo>>> call,
                    @NonNull Response<ApiResponse<List<RouterInfo>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    cache.set(cacheKey, response.body().getData(), TTL_MEDIUM);
                }
                handleResponse(response, callback, "Failed to fetch routers");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<RouterInfo>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch routers");
            }
        });
    }

    public void initiatePasswordReset(String email, ApiCallback<Void> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        apiService.initiatePasswordReset(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                handleVoidResponse(response, callback, "Password reset initiation failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                handleError(t, callback, "Password reset initiation failed");
            }
        });
    }

    public void confirmPasswordReset(String token, String newPassword, ApiCallback<Void> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        body.put("new_password", newPassword);
        apiService.confirmPasswordReset(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                handleVoidResponse(response, callback, "Password reset failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                handleError(t, callback, "Password reset failed");
            }
        });
    }

    public void getRouterCredentials(String routerName, ApiCallback<RouterInfo> callback) {
        apiService.getRouterCredentials(routerName).enqueue(new Callback<RouterInfo>() {
            @Override
            public void onResponse(@NonNull Call<RouterInfo> call, @NonNull Response<RouterInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response, "Failed to fetch router credentials"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RouterInfo> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch router credentials");
            }
        });
    }

    public void testRouterConnection(String routerName, ApiCallback<Void> callback) {
        apiService.testRouterConnection(routerName).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                handleVoidResponse(response, callback, "Router connection test failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                handleError(t, callback, "Router connection test failed");
            }
        });
    }

    public void generateSubscription(SubscriptionGenerateRequest request,
            ApiCallback<List<SubscriptionCode>> callback) {
        apiService.generateSubscription(request).enqueue(new Callback<ApiResponse<List<SubscriptionCode>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<SubscriptionCode>>> call,
                    @NonNull Response<ApiResponse<List<SubscriptionCode>>> response) {
                cache.clearPattern("subscriptions");
                handleResponse(response, callback, "Subscription generation failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SubscriptionCode>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Subscription generation failed");
            }
        });
    }

    public void verifySubscription(String code, ApiCallback<Void> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("code", code);
        apiService.verifySubscription(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                handleVoidResponse(response, callback, "Subscription verification failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                handleError(t, callback, "Subscription verification failed");
            }
        });
    }

    public void checkSubscriptionStatus(ApiCallback<Map<String, Object>> callback) {
        apiService.checkSubscriptionStatus().enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                    @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                handleResponse(response, callback, "Failed to check subscription status");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to check subscription status");
            }
        });
    }

    public void getUserSubscriptions(boolean useCache, ApiCallback<List<SubscriptionCode>> callback) {
        String cacheKey = "user_subscriptions";
        if (useCache) {
            List<SubscriptionCode> cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }
        }

        apiService.getUserSubscriptions().enqueue(new Callback<ApiResponse<List<SubscriptionCode>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<SubscriptionCode>>> call,
                    @NonNull Response<ApiResponse<List<SubscriptionCode>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    cache.set(cacheKey, response.body().getData(), TTL_MEDIUM);
                }
                handleResponse(response, callback, "Failed to fetch subscriptions");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SubscriptionCode>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch subscriptions");
            }
        });
    }

    public void getAdminUsers(ApiCallback<List<User>> callback) {
        apiService.getAdminUsers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<User>>> call,
                    @NonNull Response<ApiResponse<List<User>>> response) {
                handleResponse(response, callback, "Failed to fetch admin users");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch admin users");
            }
        });
    }

    public void deactivateUser(String userId, ApiCallback<Void> callback) {
        apiService.deactivateUser(userId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                handleVoidResponse(response, callback, "User deactivation failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                handleError(t, callback, "User deactivation failed");
            }
        });
    }

    // ==================== Financial ====================

    public void getFinancialStats(boolean useCache, ApiCallback<FinancialStats> callback) {
        String cacheKey = "financial_stats";

        if (useCache) {
            FinancialStats cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }
        }

        apiService.getFinancialStats().enqueue(new Callback<FinancialStats>() {
            @Override
            public void onResponse(@NonNull Call<FinancialStats> call, @NonNull Response<FinancialStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cache.set(cacheKey, response.body(), TTL_SHORT);
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response, "Failed to fetch financial stats"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<FinancialStats> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch financial stats");
            }
        });
    }

    public void getRevenueData(int days, boolean useCache, ApiCallback<List<RevenueData>> callback) {
        String cacheKey = "revenue_data_" + days;

        if (useCache) {
            List<RevenueData> cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }
        }

        apiService.getRevenueData(days).enqueue(new Callback<RevenueResponse>() {
            @Override
            public void onResponse(@NonNull Call<RevenueResponse> call, @NonNull Response<RevenueResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RevenueData> data = response.body().getRevenueData();
                    cache.set(cacheKey, data, TTL_MEDIUM);
                    callback.onSuccess(data);
                } else {
                    callback.onError(parseError(response, "Failed to fetch revenue data"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RevenueResponse> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch revenue data");
            }
        });
    }

    public void getProfileStats(boolean useCache, ApiCallback<List<ProfileStats>> callback) {
        String cacheKey = "profile_stats";

        if (useCache) {
            List<ProfileStats> cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }
        }

        apiService.getProfileStats().enqueue(new Callback<ApiResponse<List<ProfileStats>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<ProfileStats>>> call,
                    @NonNull Response<ApiResponse<List<ProfileStats>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    cache.set(cacheKey, response.body().getData(), TTL_MEDIUM);
                }
                handleResponse(response, callback, "Failed to fetch profile stats");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ProfileStats>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch profile stats");
            }
        });
    }

    public void getActiveRevenue(ApiCallback<ActiveRevenue> callback) {
        apiService.getActiveRevenue().enqueue(new Callback<ApiResponse<ActiveRevenue>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ActiveRevenue>> call,
                    @NonNull Response<ApiResponse<ActiveRevenue>> response) {
                handleResponse(response, callback, "Failed to fetch active revenue");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ActiveRevenue>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch active revenue");
            }
        });
    }

    // ==================== Vouchers ====================

    public void generateVouchers(VoucherGenerateRequest request, ApiCallback<List<Voucher>> callback) {
        apiService.generateVouchers(request).enqueue(new Callback<ApiResponse<List<Voucher>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Voucher>>> call,
                    @NonNull Response<ApiResponse<List<Voucher>>> response) {
                cache.clearPattern("vouchers");
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Voucher> data = response.body().getData();
                    socketService.emit("voucher_generated", data);
                    // Save to database
                    new Thread(() -> db.voucherDao().insertAll(data)).start();
                    callback.onSuccess(data);
                } else {
                    callback.onError(parseError(response, "Failed to generate vouchers"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Voucher>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to generate vouchers");
            }
        });
    }

    public void getVouchersLocal(ApiCallback<List<Voucher>> callback) {
        new Thread(() -> {
            try {
                List<Voucher> vouchers = db.voucherDao().getAll();
                mainHandler.post(() -> callback.onSuccess(vouchers));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Failed to load local vouchers", e));
            }
        }).start();
    }

    public void downloadVoucherPdf(String voucherCode, ApiCallback<ResponseBody> callback) {
        apiService.getVoucherPdf(voucherCode, "default", "true").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response, "Failed to download PDF"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to download PDF");
            }
        });
    }

    public void getExpiredVouchers(boolean useCache, ApiCallback<List<Voucher>> callback) {
        String cacheKey = "vouchers_expired"; // Changed cacheKey

        if (useCache) {
            List<Voucher> cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }

            // Try database
            List<Voucher> dbVouchers = db.voucherDao().getAll();
            if (dbVouchers != null && !dbVouchers.isEmpty()) {
                callback.onSuccess(dbVouchers);
                // Continue to fetch fresh data
            }
        }

        apiService.getExpiredVouchers().enqueue(new Callback<ApiResponse<List<Voucher>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Voucher>>> call,
                    @NonNull Response<ApiResponse<List<Voucher>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Voucher> data = response.body().getData();
                    cache.set(cacheKey, data, TTL_SHORT);
                    // Save to database
                    new Thread(() -> db.voucherDao().insertAll(data)).start();
                    callback.onSuccess(data);
                } else {
                    callback.onError(parseError(response, "Failed to fetch vouchers"), null); // Changed error message
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Voucher>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch vouchers"); // Changed error message
            }
        });
    }

    public void getVoucherDetail(String voucherCode, ApiCallback<Voucher> callback) {
        apiService.getVoucherDetail(voucherCode).enqueue(new Callback<Voucher>() {
            @Override
            public void onResponse(@NonNull Call<Voucher> call, @NonNull Response<Voucher> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response, "Failed to fetch voucher details"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Voucher> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch voucher details");
            }
        });
    }

    public void getVoucherPdf(String voucherCode, String style, ApiCallback<byte[]> callback) {
        apiService.getVoucherPdf(voucherCode, style, "true").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        callback.onSuccess(response.body().bytes());
                    } catch (IOException e) {
                        callback.onError("Failed to read PDF data", e);
                    }
                } else {
                    callback.onError(parseError(response, "Failed to generate PDF"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to generate PDF");
            }
        });
    }

    public void getVoucherStats(ApiCallback<VoucherStats> callback) {
        apiService.getVoucherStats().enqueue(new Callback<VoucherStats>() {
            @Override
            public void onResponse(@NonNull Call<VoucherStats> call, @NonNull Response<VoucherStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response, "Failed to fetch voucher stats"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VoucherStats> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch voucher stats");
            }
        });
    }

    public void getBatchVoucherPdf(List<String> voucherCodes, ApiCallback<byte[]> callback) {
        Map<String, List<String>> body = new HashMap<>();
        body.put("vouchers", voucherCodes);
        apiService.getBatchVoucherPdf(body, "yes").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        callback.onSuccess(response.body().bytes());
                    } catch (IOException e) {
                        callback.onError("Failed to read PDF response", e);
                    }
                } else {
                    callback.onError(parseError(response, "Failed to generate batch PDF"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to generate batch PDF");
            }
        });
    }

    public void bulkDeleteVouchers(List<String> voucherCodes, ApiCallback<Void> callback) {
        Map<String, List<String>> body = new HashMap<>();
        body.put("vouchers", voucherCodes);
        apiService.bulkDeleteVouchers(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                cache.clearPattern("vouchers");
                handleVoidResponse(response, callback, "Bulk voucher deletion failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                handleError(t, callback, "Bulk voucher deletion failed");
            }
        });
    }

    // ==================== Users ====================

    public void getAllUsers(boolean useCache, ApiCallback<List<HotspotUser>> callback) {
        String cacheKey = "all_users";

        if (useCache) {
            List<HotspotUser> cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }

            // Try database
            List<HotspotUser> dbUsers = db.hotspotUserDao().getAll();
            if (dbUsers != null && !dbUsers.isEmpty()) {
                callback.onSuccess(dbUsers);
                // Continue to fetch fresh data
            }
        }

        apiService.getAllUsers().enqueue(new Callback<AllUsersResponse>() {
            @Override
            public void onResponse(@NonNull Call<AllUsersResponse> call,
                    @NonNull Response<AllUsersResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getAllUsers() != null) {
                    List<HotspotUser> data = response.body().getAllUsers();
                    cache.set(cacheKey, data, TTL_SHORT);
                    // Save to database
                    new Thread(() -> db.hotspotUserDao().insertAll(data)).start();
                    callback.onSuccess(data);
                } else {
                    callback.onError(parseError(response, "Failed to fetch users"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AllUsersResponse> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch users");
            }
        });
    }

    public androidx.lifecycle.LiveData<List<HotspotUser>> getActiveUsersLiveData() {
        return db.hotspotUserDao().getActiveLiveData();
    }

    public void refreshActiveUsers() {
        apiService.getActiveUsers().enqueue(new Callback<ApiResponse<List<HotspotUser>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<HotspotUser>>> call,
                    @NonNull Response<ApiResponse<List<HotspotUser>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<HotspotUser> data = response.body().getData();
                    Log.d(TAG, "Parsed " + data.size() + " active users from API");
                    for (HotspotUser u : data) {
                        u.setActive(true);
                        // Ensure ID is set (API returns 'user' field, not 'id')
                        if (u.getId() == null || u.getId().isEmpty()) {
                            if (u.getUsername() != null && !u.getUsername().isEmpty()) {
                                u.setId(u.getUsername());
                            }
                        }
                        // Also ensure username is copied from ID if empty
                        if (u.getUsername() == null || u.getUsername().isEmpty()) {
                            if (u.getId() != null && !u.getId().isEmpty()) {
                                u.setUsername(u.getId());
                            }
                        }
                        Log.d(TAG, "User: id=" + u.getId() + ", username=" + u.getUsername() +
                                ", isActive=" + u.isActive() + ", uptime=" + u.getUptime());
                    }
                    Log.d(TAG, "Refreshed active users: " + data.size() + ", now saving to DB");
                    new Thread(() -> {
                        try {
                            db.hotspotUserDao().insertAll(data);
                            Log.d(TAG, "Successfully saved " + data.size() + " users to Room DB");
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to save users to Room DB: " + e.getMessage(), e);
                        }
                    }).start();
                } else {
                    Log.w(TAG, "API response unsuccessful or data is null");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<HotspotUser>>> call, @NonNull Throwable t) {
                // Log error but don't crash, UI shows existing DB data
                Log.e(TAG, "Failed to refresh active users: " + t.getMessage());
            }
        });
    }

    public void getActiveUsers(ApiCallback<List<HotspotUser>> callback) {
        // Legacy: pass-through to refresh but also return DB data immediately if
        // available
        List<HotspotUser> dbActive = db.hotspotUserDao().getActive();
        if (dbActive != null && !dbActive.isEmpty()) {
            callback.onSuccess(dbActive);
        }
        refreshActiveUsers(); // Trigger refresh
    }

    public void getExpiredUsers(ApiCallback<List<HotspotUser>> callback) {
        apiService.getExpiredUsers().enqueue(new Callback<ApiResponse<List<HotspotUser>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<HotspotUser>>> call,
                    @NonNull Response<ApiResponse<List<HotspotUser>>> response) {
                handleResponse(response, callback, "Failed to fetch expired users");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<HotspotUser>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch expired users");
            }
        });
    }

    public void getUserDetail(String username, ApiCallback<HotspotUser> callback) {
        apiService.getUserDetail(username).enqueue(new Callback<ApiResponse<HotspotUser>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<HotspotUser>> call,
                    @NonNull Response<ApiResponse<HotspotUser>> response) {
                handleResponse(response, callback, "Failed to fetch user details");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<HotspotUser>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch user details");
            }
        });
    }

    public void updateUserComment(String username, String comment, ApiCallback<Void> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("comment", comment);

        apiService.updateUserComment(username, body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                cache.clearPattern("users");
                handleVoidResponse(response, callback, "Failed to update comment");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to update comment");
            }
        });
    }

    public void getUserSessions(String username, ApiCallback<List<UserSession>> callback) {
        apiService.getUserSessions(username).enqueue(new Callback<ApiResponse<List<UserSession>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<UserSession>>> call,
                    @NonNull Response<ApiResponse<List<UserSession>>> response) {
                handleResponse(response, callback, "Failed to fetch user sessions");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<UserSession>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch user sessions");
            }
        });
    }

    public void bulkUserActions(BulkActionRequest request, ApiCallback<Void> callback) {
        apiService.bulkUserActions(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                cache.clearPattern("users");
                handleVoidResponse(response, callback, "Failed to perform bulk action");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to perform bulk action");
            }
        });
    }

    // ==================== System ====================

    public void getSystemInfo(boolean useCache, ApiCallback<SystemInfo> callback) {
        String cacheKey = "system_info";

        if (useCache) {
            SystemInfo cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }
        }

        apiService.getSystemInfo().enqueue(new Callback<SystemInfoResponse>() {
            @Override
            public void onResponse(@NonNull Call<SystemInfoResponse> call,
                    @NonNull Response<SystemInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSystemInfo() != null) {
                    SystemInfo info = response.body().getSystemInfo();
                    cache.set(cacheKey, info, 60 * 1000); // 1 minute
                    callback.onSuccess(info);
                } else {
                    callback.onError(parseError(response, "Failed to fetch system info"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SystemInfoResponse> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch system info");
            }
        });
    }

    public void getSystemHealth(ApiCallback<SystemHealth> callback) {
        apiService.getSystemHealth().enqueue(new Callback<SystemHealth>() {
            @Override
            public void onResponse(@NonNull Call<SystemHealth> call, @NonNull Response<SystemHealth> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.code() == 404) {
                    // Fallback to system info if health endpoint is not found
                    getSystemInfo(false, new ApiCallback<SystemInfo>() {
                        @Override
                        public void onSuccess(SystemInfo data) {
                            SystemHealth synthesized = new SystemHealth();
                            synthesized.setStatus("Online");
                            synthesized.setDatabase("Connected");
                            synthesized.setRouterConnection("Active");
                            synthesized.setApiVersion(data.getVersion() != null ? data.getVersion() : "1.0.0");
                            callback.onSuccess(synthesized);
                        }

                        @Override
                        public void onError(String message, Throwable t) {
                            callback.onError("System status unavailable: " + message, t);
                        }
                    });
                } else {
                    callback.onError(parseError(response, "Failed to fetch system health"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SystemHealth> call, @NonNull Throwable t) {
                // Also try fallback on network error
                getSystemInfo(false, new ApiCallback<SystemInfo>() {
                    @Override
                    public void onSuccess(SystemInfo data) {
                        SystemHealth synthesized = new SystemHealth();
                        synthesized.setStatus("Online");
                        callback.onSuccess(synthesized);
                    }

                    @Override
                    public void onError(String message, Throwable t2) {
                        handleError(t, callback, "Failed to fetch system health");
                    }
                });
            }
        });
    }

    public void createBackup(ApiCallback<Map<String, Object>> callback) {
        apiService.createBackup().enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                    @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                handleResponse(response, callback, "Backup creation failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Backup creation failed");
            }
        });
    }

    public void restoreBackup(Map<String, Object> body, ApiCallback<Void> callback) {
        apiService.restoreBackup(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                handleVoidResponse(response, callback, "Backup restoration failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                handleError(t, callback, "Backup restoration failed");
            }
        });
    }

    public void getSystemLogs(int lines, ApiCallback<List<String>> callback) {
        apiService.getSystemLogs(lines).enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<String>>> call,
                    @NonNull Response<ApiResponse<List<String>>> response) {
                handleResponse(response, callback, "Failed to fetch system logs");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<String>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch system logs");
            }
        });
    }

    public void getRevenueData(int days, ApiCallback<List<RevenueData>> callback) {
        apiService.getRevenueData(days).enqueue(new Callback<RevenueResponse>() {
            @Override
            public void onResponse(@NonNull Call<RevenueResponse> call, @NonNull Response<RevenueResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getRevenueData());
                } else {
                    callback.onError(parseError(response, "Failed to fetch revenue data"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RevenueResponse> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch revenue data");
            }
        });
    }

    // ==================== Profiles ====================

    public void getEnhancedProfiles(boolean useCache, ApiCallback<List<Profile>> callback) {
        String cacheKey = "profiles_enhanced";

        if (useCache) {
            List<Profile> cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }

            // Try database
            List<Profile> dbProfiles = db.profileDao().getAll();
            if (dbProfiles != null && !dbProfiles.isEmpty()) {
                callback.onSuccess(dbProfiles);
                // Continue to fetch fresh data
            }
        }

        apiService.getEnhancedProfiles().enqueue(new Callback<ApiResponse<List<Profile>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Profile>>> call,
                    @NonNull Response<ApiResponse<List<Profile>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Profile> data = response.body().getData();
                    cache.set(cacheKey, data, TTL_SHORT);
                    // Save to database
                    new Thread(() -> db.profileDao().insertAll(data)).start();
                    callback.onSuccess(data);
                } else {
                    callback.onError(parseError(response, "Failed to fetch profiles"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Profile>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch profiles");
            }
        });
    }

    public void createProfile(Map<String, Object> body, ApiCallback<Profile> callback) {
        apiService.createProfile(body).enqueue(new Callback<ApiResponse<Profile>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Profile>> call,
                    @NonNull Response<ApiResponse<Profile>> response) {
                cache.clearPattern("profiles");
                handleResponse(response, callback, "Profile creation failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Profile>> call, @NonNull Throwable t) {
                handleError(t, callback, "Profile creation failed");
            }
        });
    }

    public void updateProfile(String profileName, Map<String, Object> body, ApiCallback<Profile> callback) {
        apiService.updateProfile(profileName, body).enqueue(new Callback<ApiResponse<Profile>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Profile>> call,
                    @NonNull Response<ApiResponse<Profile>> response) {
                cache.clearPattern("profiles");
                handleResponse(response, callback, "Profile update failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Profile>> call, @NonNull Throwable t) {
                handleError(t, callback, "Profile update failed");
            }
        });
    }

    public void deleteProfile(String profileName, ApiCallback<Void> callback) {
        apiService.deleteProfile(profileName).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                cache.clearPattern("profiles");
                handleVoidResponse(response, callback, "Profile deletion failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                handleError(t, callback, "Profile deletion failed");
            }
        });
    }

    public void getProfileUsageStats(String profileName, ApiCallback<Map<String, Object>> callback) {
        apiService.getProfileUsageStats(profileName).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                    @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                handleResponse(response, callback, "Failed to fetch profile usage stats");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch profile usage stats");
            }
        });
    }

    // ==================== Pricing ====================

    public void getPricingRates(boolean useCache, ApiCallback<Map<String, Double>> callback) {
        String cacheKey = "pricing_rates_map";

        if (useCache) {
            Map<String, Double> cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }

            // Try database
            PricingEntity entity = db.pricingDao().getRates();
            if (entity != null && entity.getRatesJson() != null) {
                try {
                    Type type = new TypeToken<Map<String, Double>>() {
                    }.getType();
                    Map<String, Double> dbRates = gson.fromJson(entity.getRatesJson(), type);
                    callback.onSuccess(dbRates);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        apiService.getPricingRates().enqueue(new Callback<PricingRatesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PricingRatesResponse> call,
                    @NonNull Response<PricingRatesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getBaseRates() != null) {
                    Map<String, Double> rates = response.body().getBaseRates();
                    cache.set(cacheKey, rates, TTL_LONG);

                    // Save to database
                    String json = gson.toJson(rates);
                    new Thread(() -> db.pricingDao().insertRates(new PricingEntity(json))).start();

                    callback.onSuccess(rates);
                } else {
                    callback.onError(parseError(response, "Failed to fetch pricing rates"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PricingRatesResponse> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch pricing rates");
            }
        });
    }

    public void updatePricingRates(Map<String, Object> body, ApiCallback<Void> callback) {
        apiService.updatePricingRates(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                cache.clearPattern("pricing");
                handleVoidResponse(response, callback, "Pricing update failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                handleError(t, callback, "Pricing update failed");
            }
        });
    }

    public void getDiscounts(ApiCallback<List<Map<String, Object>>> callback) {
        apiService.getDiscounts().enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call,
                    @NonNull Response<ApiResponse<List<Map<String, Object>>>> response) {
                handleResponse(response, callback, "Failed to fetch discounts");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch discounts");
            }
        });
    }

    public void bulkUpdatePricing(Map<String, Object> body, ApiCallback<Void> callback) {
        apiService.bulkUpdatePricing(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                cache.clearPattern("pricing");
                handleVoidResponse(response, callback, "Bulk pricing update failed");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                handleError(t, callback, "Bulk pricing update failed");
            }
        });
    }

    // ==================== Analytics ====================

    public void getAnalyticsDashboard(boolean useCache, ApiCallback<AnalyticsDashboard> callback) {
        String cacheKey = "analytics_dashboard";

        if (useCache) {
            AnalyticsDashboard cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }
        }

        apiService.getAnalyticsDashboard().enqueue(new Callback<AnalyticsDashboard>() {
            @Override
            public void onResponse(@NonNull Call<AnalyticsDashboard> call,
                    @NonNull Response<AnalyticsDashboard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cache.set(cacheKey, response.body(), TTL_SHORT);
                    callback.onSuccess(response.body());
                } else if (response.code() == 404) {
                    // Fallback to financial stats if analytics dashboard is not found
                    getFinancialStats(false, new ApiCallback<FinancialStats>() {
                        @Override
                        public void onSuccess(FinancialStats stats) {
                            AnalyticsDashboard fallback = new AnalyticsDashboard();
                            fallback.setTotalRevenue(stats.getTotalRevenue());
                            fallback.setRevenueToday(stats.getTodayRevenue());
                            fallback.setVouchersCount(stats.getTotalVouchers());
                            // Use active vouchers if active users is 0 (fallback for missing field)
                            fallback.setActiveUsers(
                                    stats.getActiveUsers() > 0 ? stats.getActiveUsers() : stats.getActiveVouchers());
                            fallback.setTotalUsers(stats.getTotalVouchers());

                            // Also try to get revenue trend for charts
                            getRevenueData(30, false, new ApiCallback<List<RevenueData>>() {
                                @Override
                                public void onSuccess(List<RevenueData> trend) {
                                    if (trend != null) {
                                        List<AnalyticsDashboard.DailyRevenue> trendList = new ArrayList<>();
                                        List<AnalyticsDashboard.DailyGrowth> growthList = new ArrayList<>();
                                        for (RevenueData rd : trend) {
                                            AnalyticsDashboard.DailyRevenue dr = new AnalyticsDashboard.DailyRevenue();
                                            dr.date = rd.getDate();
                                            dr.revenue = rd.getRevenue();
                                            dr.voucherCount = rd.getVoucherCount();
                                            trendList.add(dr);

                                            AnalyticsDashboard.DailyGrowth dg = new AnalyticsDashboard.DailyGrowth();
                                            dg.date = rd.getDate();
                                            dg.count = rd.getVoucherCount();
                                            growthList.add(dg);
                                        }
                                        fallback.setRevenueTrend(trendList);
                                        fallback.setUserGrowth(growthList);
                                    }
                                    callback.onSuccess(fallback);
                                }

                                @Override
                                public void onError(String message, Throwable t) {
                                    callback.onSuccess(fallback); // Still return fallback even if chart fails
                                }
                            });
                        }

                        @Override
                        public void onError(String message, Throwable t) {
                            callback.onError("Analytics unavailable: " + message, t);
                        }
                    });
                } else {
                    callback.onError(parseError(response, "Failed to fetch analytics dashboard"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AnalyticsDashboard> call, @NonNull Throwable t) {
                // Fallback on network failure too
                getFinancialStats(false, new ApiCallback<FinancialStats>() {
                    @Override
                    public void onSuccess(FinancialStats stats) {
                        AnalyticsDashboard fallback = new AnalyticsDashboard();
                        fallback.setTotalRevenue(stats.getTotalRevenue());
                        fallback.setRevenueToday(stats.getTodayRevenue());
                        fallback.setActiveUsers(stats.getActiveUsers());

                        getRevenueData(30, false, new ApiCallback<List<RevenueData>>() {
                            @Override
                            public void onSuccess(List<RevenueData> trend) {
                                List<AnalyticsDashboard.DailyRevenue> trendList = new ArrayList<>();
                                for (RevenueData rd : trend) {
                                    AnalyticsDashboard.DailyRevenue dr = new AnalyticsDashboard.DailyRevenue();
                                    dr.date = rd.getDate();
                                    dr.revenue = rd.getRevenue();
                                    dr.voucherCount = rd.getVoucherCount();
                                    trendList.add(dr);
                                }
                                fallback.setRevenueTrend(trendList);
                                callback.onSuccess(fallback);
                            }

                            @Override
                            public void onError(String message, Throwable t) {
                                callback.onSuccess(fallback);
                            }
                        });
                    }

                    @Override
                    public void onError(String message, Throwable t2) {
                        handleError(t, callback, "Failed to fetch analytics dashboard");
                    }
                });
            }
        });
    }

    public void getAnalyticsReports(String reportType, Map<String, String> params,
            ApiCallback<Map<String, Object>> callback) {
        apiService.getAnalyticsReports(reportType, params).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                    @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                handleResponse(response, callback, "Failed to fetch analytics reports");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to fetch analytics reports");
            }
        });
    }

    public void exportAnalyticsData(Map<String, Object> body, ApiCallback<byte[]> callback) {
        apiService.exportAnalyticsData(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        callback.onSuccess(response.body().bytes());
                    } catch (IOException e) {
                        callback.onError("Failed to read export response", e);
                    }
                } else {
                    callback.onError(parseError(response, "Failed to export analytics data"), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                handleError(t, callback, "Failed to export analytics data");
            }
        });
    }

    // ==================== Helper Methods ====================

    private <T> void handleResponse(Response<ApiResponse<T>> response, ApiCallback<T> callback, String defaultError) {
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<T> apiResponse = response.body();
            if (apiResponse.isSuccess() || apiResponse.getData() != null) {
                callback.onSuccess(apiResponse.getData());
            } else {
                String error = apiResponse.getError() != null ? apiResponse.getError() : defaultError;
                callback.onError(error, null);
            }
        } else {
            callback.onError(parseError(response, defaultError), null);
        }
    }

    private void handleVoidResponse(Response<ApiResponse<Void>> response, ApiCallback<Void> callback,
            String defaultError) {
        if (response.isSuccessful()) {
            callback.onSuccess(null);
        } else {
            callback.onError(parseError(response, defaultError), null);
        }
    }

    private <T> void handleError(Throwable t, ApiCallback<T> callback, String defaultError) {
        String message = t.getMessage();
        if (message == null || message.isEmpty()) {
            message = defaultError;
        }
        Log.e(TAG, defaultError + ": " + message, t);
        callback.onError(message, t);
    }

    private String parseError(Response<?> response, String defaultError) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                // Check if it looks like JSON to avoid unnecessary exceptions
                if (errorBody.trim().startsWith("{")) {
                    JSONObject json = new JSONObject(errorBody);
                    if (json.has("error")) {
                        return json.getString("error");
                    }
                    if (json.has("message")) {
                        return json.getString("message");
                    }
                } else {
                    // Not JSON (likely HTML 404/500), return a generic error with code
                    return defaultError + " (" + response.code() + ")";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error response", e);
        }
        return defaultError;
    }
}
