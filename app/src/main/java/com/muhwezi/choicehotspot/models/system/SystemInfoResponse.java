package com.muhwezi.choicehotspot.models.system;

import com.google.gson.annotations.SerializedName;

public class SystemInfoResponse {
    @SerializedName("system_info")
    private SystemInfo systemInfo;

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }
}
