package com.muhwezi.choicehotspot.models.user;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request for bulk user actions.
 */
public class BulkActionRequest {

    @SerializedName("action")
    private String action;

    @SerializedName("usernames")
    private List<String> usernames;

    @SerializedName("profile")
    private String profile;

    @SerializedName("comment")
    private String comment;

    public BulkActionRequest() {
    }

    public BulkActionRequest(String action, List<String> usernames) {
        this.action = action;
        this.usernames = usernames;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
