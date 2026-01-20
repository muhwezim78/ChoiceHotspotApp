package com.muhwezi.choicehotspot.api;

import com.muhwezi.choicehotspot.models.ApiResponse;
import com.muhwezi.choicehotspot.models.analytics.AnalyticsDashboard;
import com.muhwezi.choicehotspot.models.auth.*;
import com.muhwezi.choicehotspot.models.financial.*;
import com.muhwezi.choicehotspot.models.pricing.*;
import com.muhwezi.choicehotspot.models.profile.Profile;
import com.muhwezi.choicehotspot.models.system.*;
import com.muhwezi.choicehotspot.models.user.*;
import com.muhwezi.choicehotspot.models.voucher.*;

import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Retrofit API Service interface defining all API endpoints.
 * Mirrors the JavaScript ApiService endpoints.
 */
public interface ApiService {

        // ==================== Authentication & User Management ====================

        @POST("/auth/register")
        Call<ApiResponse<User>> register(@Body RegisterRequest request);

        @POST("/auth/login")
        Call<LoginResponse> login(@Body LoginRequest request);

        @POST("/admin/login-only")
        Call<AdminLoginResponse> adminLogin(@Body LoginRequest request);

        @POST("/auth/logout")
        Call<ApiResponse<Void>> logout();

        @GET("/auth/profile")
        Call<User> getProfile();

        @POST("/auth/password/reset/initiate")
        Call<ApiResponse<Void>> initiatePasswordReset(@Body Map<String, String> body);

        @POST("/auth/password/reset/confirm")
        Call<ApiResponse<Void>> confirmPasswordReset(@Body Map<String, String> body);

        @POST("/auth/router/connect")
        Call<ApiResponse<RouterInfo>> connectRouter(@Body RouterConnectRequest request);

        @GET("/auth/routers")
        Call<ApiResponse<List<RouterInfo>>> getUserRouters();

        @GET("/auth/router/{routerName}/credentials")
        Call<RouterInfo> getRouterCredentials(@Path("routerName") String routerName);

        @POST("/auth/router/{routerName}/test")
        Call<ApiResponse<Void>> testRouterConnection(@Path("routerName") String routerName);

        @POST("/auth/subscription/generate")
        Call<ApiResponse<List<SubscriptionCode>>> generateSubscription(@Body SubscriptionGenerateRequest request);

        @POST("/auth/subscription/verify")
        Call<ApiResponse<Void>> verifySubscription(@Body Map<String, String> body);

        @GET("/auth/subscription/status")
        Call<ApiResponse<Map<String, Object>>> checkSubscriptionStatus();

        @GET("/auth/subscriptions")
        Call<ApiResponse<List<SubscriptionCode>>> getUserSubscriptions();

        @GET("/auth/admin/users")
        Call<ApiResponse<List<User>>> getAdminUsers();

        @POST("/auth/admin/user/{userId}/deactivate")
        Call<ApiResponse<Void>> deactivateUser(@Path("userId") String userId);

        // ==================== Financial Endpoints ====================

        @GET("/financial/stats")
        Call<FinancialStats> getFinancialStats();

        @GET("/financial/revenue-data")
        Call<RevenueResponse> getRevenueData(@Query("days") int days);

        @GET("/financial/profile-stats")
        Call<ApiResponse<List<ProfileStats>>> getProfileStats();

        @GET("/financial/active-revenue")
        Call<ApiResponse<ActiveRevenue>> getActiveRevenue();

        // ==================== Voucher Endpoints ====================

        @POST("/vouchers/generate")
        Call<ApiResponse<List<Voucher>>> generateVouchers(@Body VoucherGenerateRequest request);

        @GET("/vouchers/expired")
        Call<ApiResponse<List<Voucher>>> getExpiredVouchers();

        @GET("/vouchers/{voucherCode}")
        Call<Voucher> getVoucherDetail(@Path("voucherCode") String voucherCode);

        @GET("/vouchers/{voucherCode}/pdf")
        Call<ResponseBody> getVoucherPdf(
                        @Path("voucherCode") String voucherCode,
                        @Query("style") String style,
                        @Query("download") String download);

        @POST("/vouchers/batch/pdf")
        Call<ResponseBody> getBatchVoucherPdf(
                        @Body Map<String, List<String>> body,
                        @Query("download") String download);

        @POST("/vouchers/bulk-delete")
        Call<ApiResponse<Void>> bulkDeleteVouchers(@Body Map<String, List<String>> body);

        @GET("/vouchers/stats")
        Call<VoucherStats> getVoucherStats();

        // ==================== User Management Endpoints ====================

        @GET("/all-users")
        Call<AllUsersResponse> getAllUsers();

        @GET("/active-users")
        Call<ApiResponse<List<HotspotUser>>> getActiveUsers();

        @GET("/users/expired")
        Call<ApiResponse<List<HotspotUser>>> getExpiredUsers();

        @GET("/users/{username}")
        Call<com.muhwezi.choicehotspot.models.ApiResponse<HotspotUser>> getUserDetail(
                        @Path("username") String username);

        @PUT("/users/{username}/comments")
        Call<ApiResponse<Void>> updateUserComment(
                        @Path("username") String username,
                        @Body Map<String, String> body);

        @GET("/users/{username}/sessions")
        Call<ApiResponse<List<UserSession>>> getUserSessions(@Path("username") String username);

        @POST("/users/bulk-actions")
        Call<ApiResponse<Void>> bulkUserActions(@Body BulkActionRequest request);

        // ==================== System Endpoints ====================

        @GET("/system/info")
        Call<SystemInfoResponse> getSystemInfo();

        @GET("/system/health")
        Call<SystemHealth> getSystemHealth();

        @POST("/system/backup")
        Call<ApiResponse<Map<String, Object>>> createBackup();

        @POST("/system/restore")
        Call<ApiResponse<Void>> restoreBackup(@Body Map<String, Object> body);

        @GET("/system/logs")
        Call<ApiResponse<List<String>>> getSystemLogs(@Query("lines") int lines);

        // ==================== Pricing Endpoints ====================

        @GET("/pricing/rates")
        Call<PricingRatesResponse> getPricingRates();

        @PUT("/pricing/rates")
        Call<ApiResponse<Void>> updatePricingRates(@Body Map<String, Object> body);

        @GET("/pricing/discounts")
        Call<ApiResponse<List<Map<String, Object>>>> getDiscounts();

        @POST("/pricing/bulk-update")
        Call<ApiResponse<Void>> bulkUpdatePricing(@Body Map<String, Object> body);

        // ==================== Profile Endpoints ====================

        @GET("/profiles/enhanced")
        Call<ApiResponse<List<Profile>>> getEnhancedProfiles();

        @POST("/profiles/create")
        Call<ApiResponse<Profile>> createProfile(@Body Map<String, Object> body);

        @PUT("/profiles/{profileName}")
        Call<ApiResponse<Profile>> updateProfile(
                        @Path("profileName") String profileName,
                        @Body Map<String, Object> body);

        @DELETE("/profiles/{profileName}")
        Call<ApiResponse<Void>> deleteProfile(@Path("profileName") String profileName);

        @GET("/profiles/{profileName}/usage")
        Call<ApiResponse<Map<String, Object>>> getProfileUsageStats(@Path("profileName") String profileName);

        // ==================== Analytics Endpoints ====================

        @GET("/analytics/dashboard")
        Call<AnalyticsDashboard> getAnalyticsDashboard();

        @GET("/analytics/reports")
        Call<ApiResponse<Map<String, Object>>> getAnalyticsReports(
                        @Query("report_type") String reportType,
                        @QueryMap Map<String, String> params);

        @POST("/analytics/export")
        Call<ResponseBody> exportAnalyticsData(@Body Map<String, Object> body);
}
