package com.muhwezi.choicehotspot.ui.users;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.models.user.HotspotUser;
import com.muhwezi.choicehotspot.utils.ApiUtils;
import com.muhwezi.choicehotspot.utils.ShareUtils;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(HotspotUser user);
    }

    private List<HotspotUser> allUsers = new ArrayList<>();
    private List<HotspotUser> filteredUsers = new ArrayList<>();
    private final Context context;
    private final OnUserClickListener listener;
    private String currentQuery = "";

    public UserAdapter(Context context, OnUserClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setUsers(List<HotspotUser> users) {
        this.allUsers = users != null ? users : new ArrayList<>();
        applyFilter();
    }

    public void filter(String query) {
        this.currentQuery = query != null ? query.toLowerCase().trim() : "";
        applyFilter();
    }

    private void applyFilter() {
        filteredUsers.clear();
        if (currentQuery.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            for (HotspotUser user : allUsers) {
                if (matches(user)) {
                    filteredUsers.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean matches(HotspotUser user) {
        if (user == null)
            return false;

        String username = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
        String ip = user.getIpAddress() != null ? user.getIpAddress().toLowerCase() : "";
        String comment = user.getComment() != null ? user.getComment().toLowerCase() : "";
        String details = user.getDetails() != null ? user.getDetails().toLowerCase() : "";

        return username.contains(currentQuery) ||
                ip.contains(currentQuery) ||
                details.contains(currentQuery) ||
                comment.contains(currentQuery);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        HotspotUser user = filteredUsers.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView profileText;
        Chip statusChip;
        TextView dataUsageText;
        TextView timeLeftText;
        TextView serverText;
        ImageButton btnShare;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.user_name);
            profileText = itemView.findViewById(R.id.user_profile);
            serverText = itemView.findViewById(R.id.user_server);
            statusChip = itemView.findViewById(R.id.user_status);
            dataUsageText = itemView.findViewById(R.id.user_data_usage);
            timeLeftText = itemView.findViewById(R.id.user_time_left);
            btnShare = itemView.findViewById(R.id.btn_share_user);
        }

        void bind(HotspotUser user, OnUserClickListener listener) {
            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onUserClick(user);
            });

            String name = user.getUsername() != null ? user.getUsername() : user.getIpAddress();
            if (user.getIpAddress() != null && !name.equals(user.getIpAddress())) {
                name += " (" + user.getIpAddress() + ")";
            }
            nameText.setText(name);

            profileText.setText(user.getProfile() != null ? user.getProfile() : "Unknown Profile");
            serverText.setText("Server: " + (user.getServer() != null ? user.getServer() : "Unknown"));

            if (user.isExpired()) {
                statusChip.setText("Expired");
                statusChip.setChipBackgroundColorResource(android.R.color.holo_red_light);
            } else {
                statusChip.setText(user.isActive() ? "Active" : "Inactive");
                statusChip.setChipBackgroundColorResource(
                        user.isActive() ? android.R.color.holo_green_light : android.R.color.transparent);
            }

            // Usage formatting
            long totalBytes = user.getBytesIn() + user.getBytesOut();
            // Fallback to dataUsed if bytes are 0
            if (totalBytes == 0 && user.getDataUsed() > 0) {
                totalBytes = user.getDataUsed();
            }

            dataUsageText.setText("Usage: " + ApiUtils.formatBytes(totalBytes));

            // Time Left or Uptime
            if (user.isActive() && user.getUptime() != null) {
                timeLeftText.setText("Uptime: " + user.getUptime());
            } else if (user.getExpiresAt() != null) {
                timeLeftText.setText("Expires: " + ApiUtils.formatIsoDate(user.getExpiresAt()));
            } else if (user.getDataLimit() != null) {
                timeLeftText.setText("Limit: " + user.getDataLimit());
            } else {
                timeLeftText.setText("Status: " + (user.isActive() ? "Online" : "Offline"));
            }

            // Share button for inactive users
            if (btnShare != null) {
                btnShare.setVisibility(!user.isActive() ? View.VISIBLE : View.GONE);
                btnShare.setOnClickListener(v -> {
                    String password = user.getPassword();
                    String text = ShareUtils.getUserShareText(user.getUsername(), password);
                    ShareUtils.shareText(itemView.getContext(), "Hotspot Credentials", text);
                });
            }
        }
    }
}
