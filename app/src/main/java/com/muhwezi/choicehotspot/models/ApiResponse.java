package com.muhwezi.choicehotspot.models;

import com.google.gson.annotations.SerializedName;

/**
 * Generic API response wrapper.
 * 
 * @param <T> The type of data contained in the response.
 */
public class ApiResponse<T> {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName(value = "data", alternate = { "active_users", "profiles", "vouchers", "expired_users",
            "profile_stats", "revenue_data", "active_vouchers", "system_info", "base_rates", "financial",
            "active_revenue", "dashboard" })
    private T data;

    @SerializedName("error")
    private String error;

    @SerializedName("details")
    private String details;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
