package com.muhwezi.choicehotspot.models.user;

import com.google.gson.annotations.SerializedName;

/**
 * User session information.
 */
public class UserSession {

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("username")
    private String username;

    @SerializedName("mac_address")
    private String macAddress;

    @SerializedName("ip_address")
    private String ipAddress;

    @SerializedName("uptime")
    private String uptime;

    @SerializedName("bytes_in")
    private long bytesIn;

    @SerializedName("bytes_out")
    private long bytesOut;

    @SerializedName("started_at")
    private String startedAt;

    public UserSession() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public long getBytesIn() {
        return bytesIn;
    }

    public void setBytesIn(long bytesIn) {
        this.bytesIn = bytesIn;
    }

    public long getBytesOut() {
        return bytesOut;
    }

    public void setBytesOut(long bytesOut) {
        this.bytesOut = bytesOut;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }
}
