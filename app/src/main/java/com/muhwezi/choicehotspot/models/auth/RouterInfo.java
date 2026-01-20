package com.muhwezi.choicehotspot.models.auth;

import com.google.gson.annotations.SerializedName;

/**
 * Router connection information.
 */
public class RouterInfo {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("ip_address")
    private String ipAddress;

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("port")
    private int port;

    @SerializedName("is_connected")
    private boolean isConnected;

    @SerializedName("last_connected")
    private String lastConnected;

    @SerializedName("status")
    private String status;

    public RouterInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getLastConnected() {
        return lastConnected;
    }

    public void setLastConnected(String lastConnected) {
        this.lastConnected = lastConnected;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
