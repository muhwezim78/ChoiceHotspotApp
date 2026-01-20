package com.muhwezi.choicehotspot.models.auth;

import com.google.gson.annotations.SerializedName;

/**
 * Response from admin login endpoint.
 * Different from LoginResponse because user is a String (email), not an object.
 */
public class AdminLoginResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private String user; // Email string

    @SerializedName("error")
    private String error;

    public AdminLoginResponse() {
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
