package com.muhwezi.choicehotspot.models.user;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AllUsersResponse {
    @SerializedName("all_users")
    private List<HotspotUser> allUsers;

    @SerializedName("pagination")
    private PaginationInfo pagination;

    public List<HotspotUser> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(List<HotspotUser> allUsers) {
        this.allUsers = allUsers;
    }

    public PaginationInfo getPagination() {
        return pagination;
    }

    public void setPagination(PaginationInfo pagination) {
        this.pagination = pagination;
    }

    public static class PaginationInfo {
        @SerializedName("has_next")
        private boolean hasNext;
        @SerializedName("has_prev")
        private boolean hasPrev;
        @SerializedName("page")
        private int page;
        @SerializedName("pages")
        private int pages;
        @SerializedName("total")
        private int total;

        public boolean isHasNext() {
            return hasNext;
        }

        public boolean isHasPrev() {
            return hasPrev;
        }

        public int getPage() {
            return page;
        }

        public int getPages() {
            return pages;
        }

        public int getTotal() {
            return total;
        }
    }
}
