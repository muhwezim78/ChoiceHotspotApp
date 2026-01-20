package com.muhwezi.choicehotspot.models.system;

import com.google.gson.annotations.SerializedName;

/**
 * System information model.
 */
public class SystemInfo {

    @SerializedName("router_name")
    private String routerName;

    @SerializedName("model")
    private String routerModel;

    @SerializedName("version")
    private String routerOsVersion;

    @SerializedName("uptime")
    private String uptime;

    @SerializedName("cpu_load")
    private String cpuLoad;

    @SerializedName("memory_usage")
    private String memoryUsage;

    @SerializedName("firmware")
    private String firmware;

    @SerializedName("cpu_count")
    private String cpuCount;

    @SerializedName("architecture")
    private String architecture;

    @SerializedName("platform")
    private String platform;

    @SerializedName("serial_number")
    private String serialNumber;

    public SystemInfo() {
    }

    public String getRouterName() {
        return routerName;
    }

    public void setRouterName(String routerName) {
        this.routerName = routerName;
    }

    public String getRouterModel() {
        return routerModel;
    }

    public void setRouterModel(String routerModel) {
        this.routerModel = routerModel;
    }

    public String getRouterOsVersion() {
        return routerOsVersion;
    }

    public void setRouterOsVersion(String routerOsVersion) {
        this.routerOsVersion = routerOsVersion;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public String getCpuLoad() {
        return cpuLoad != null ? cpuLoad + "%" : "0%";
    }

    public void setCpuLoad(String cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public String getMemoryUsage() {
        return memoryUsage != null ? memoryUsage : "0%";
    }

    public void setMemoryUsage(String memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public String getVersion() {
        return routerOsVersion;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public String getCpuCount() {
        return cpuCount != null ? cpuCount : "1";
    }

    public void setCpuCount(String cpuCount) {
        this.cpuCount = cpuCount;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
