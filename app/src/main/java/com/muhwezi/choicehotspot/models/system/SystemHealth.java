package com.muhwezi.choicehotspot.models.system;

import com.google.gson.annotations.SerializedName;

/**
 * System health check model.
 */
public class SystemHealth {

    @SerializedName("status")
    private String status;

    @SerializedName("database")
    private String database;

    @SerializedName("router_connection")
    private String routerConnection;

    @SerializedName("api_version")
    private String apiVersion;

    @SerializedName("timestamp")
    private String timestamp;

    public SystemHealth() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getRouterConnection() {
        return routerConnection;
    }

    public void setRouterConnection(String routerConnection) {
        this.routerConnection = routerConnection;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
