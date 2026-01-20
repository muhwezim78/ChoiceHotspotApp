package com.muhwezi.choicehotspot.models.auth;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for router connection.
 */
public class RouterConnectRequest {

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

    public RouterConnectRequest() {
    }

    public RouterConnectRequest(String name, String ipAddress, String username, String password, int port) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.username = username;
        this.password = password;
        this.port = port;
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
}
