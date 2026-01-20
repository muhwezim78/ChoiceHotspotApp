package com.muhwezi.choicehotspot.ui.users;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.models.user.HotspotUser;
import com.muhwezi.choicehotspot.utils.ApiUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;

public class UserDetailsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_USER_JSON = "user_json";
    private HotspotUser user;

    public static UserDetailsBottomSheet newInstance(HotspotUser user) {
        UserDetailsBottomSheet fragment = new UserDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_USER_JSON, new Gson().toJson(user));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String json = getArguments().getString(ARG_USER_JSON);
            user = new Gson().fromJson(json, HotspotUser.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_user_details, container, false);
    }

    private TextView serverText;
    private Chip voucherTag;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (user == null) {
            dismiss();
            return;
        }

        bindViews(view);
        fetchUserDetails();
    }

    private void fetchUserDetails() {
        String queryTerm = user.getUsername();
        if (queryTerm == null || queryTerm.isEmpty()) {
            queryTerm = user.getId();
        }

        if (queryTerm != null && !queryTerm.isEmpty()) {
            com.muhwezi.choicehotspot.api.ApiClient.getInstance().getApiService().getUserDetail(queryTerm)
                    .enqueue(new retrofit2.Callback<HotspotUser>() {
                        @Override
                        public void onResponse(
                                retrofit2.Call<HotspotUser> call,
                                retrofit2.Response<HotspotUser> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                HotspotUser detailedUser = response.body();

                                // Merge missing fields from existing user object
                                if (detailedUser.getServer() == null || detailedUser.getServer().isEmpty())
                                    detailedUser.setServer(user.getServer());
                                if (detailedUser.getMacAddress() == null || detailedUser.getMacAddress().isEmpty())
                                    detailedUser.setMacAddress(user.getMacAddress());
                                if (detailedUser.getIpAddress() == null || detailedUser.getIpAddress().isEmpty())
                                    detailedUser.setIpAddress(user.getIpAddress());
                                if ((detailedUser.getId() == null || detailedUser.getId().isEmpty()
                                        || detailedUser.getId().equals(detailedUser.getUsername()))
                                        && user.getId() != null && !user.getId().isEmpty()) {
                                    detailedUser.setId(user.getId());
                                }

                                // Smart Merge: Preserve active usage stats if detailed response has them as
                                // empty/zero
                                // The /users/{id} endpoint may return 0 usage while /active-users has real data
                                if (user.isActive()) { // Only if we believe the user is active
                                    if (detailedUser.getUptime() == null || detailedUser.getUptime().equals("0s")
                                            || detailedUser.getUptime().isEmpty()) {
                                        if (user.getUptime() != null && !user.getUptime().equals("0s")
                                                && !user.getUptime().isEmpty()) {
                                            detailedUser.setUptime(user.getUptime());
                                        }
                                    }
                                    if (detailedUser.getBytesIn() == 0 && user.getBytesIn() > 0) {
                                        detailedUser.setBytesIn(user.getBytesIn());
                                    }
                                    if (detailedUser.getBytesOut() == 0 && user.getBytesOut() > 0) {
                                        detailedUser.setBytesOut(user.getBytesOut());
                                    }
                                }

                                android.util.Log.d("UserDetails",
                                        "Merged user details: " + new Gson().toJson(detailedUser));
                                user = detailedUser;
                                if (getView() != null) {
                                    bindViews(getView());
                                }
                            } else {
                                android.util.Log.e("UserDetails", "Failed to fetch user details: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(
                                retrofit2.Call<HotspotUser> call,
                                Throwable t) {
                            android.util.Log.e("UserDetails", "Error fetching user details", t);
                        }
                    });
        }
    }

    private void bindViews(View view) {
        TextView usernameText = view.findViewById(R.id.detail_username);
        Chip statusChip = view.findViewById(R.id.detail_status_chip);
        Chip profileChip = view.findViewById(R.id.detail_profile);
        TextView passwordTypeText = view.findViewById(R.id.detail_password_type);
        TextView uptimeLimitText = view.findViewById(R.id.detail_uptime_limit);
        TextView lastSeenText = view.findViewById(R.id.detail_last_seen);
        TextView usageUptimeText = view.findViewById(R.id.detail_usage_uptime);
        TextView usageDataText = view.findViewById(R.id.detail_usage_data);
        TextView commentText = view.findViewById(R.id.detail_comment);
        TextView detailsText = view.findViewById(R.id.detail_extra_info);
        serverText = view.findViewById(R.id.detail_server);
        TextView ipAddressText = view.findViewById(R.id.detail_ip_address);
        TextView macAddressText = view.findViewById(R.id.detail_mac_address);
        voucherTag = view.findViewById(R.id.detail_voucher_tag);
        View closeButton = view.findViewById(R.id.btn_close);

        closeButton.setOnClickListener(v -> dismiss());

        String username = user.getUsername() != null ? user.getUsername() : user.getIpAddress();
        if (user.isVoucher() && username != null && !username.endsWith("VOUCHER")) {
            username += " VOUCHER";
        }
        usernameText.setText(username);

        voucherTag.setVisibility(user.isVoucher() ? View.VISIBLE : View.GONE);

        boolean isDisabled = false;
        if (user.getCurrentUsage() != null && "true".equalsIgnoreCase(user.getCurrentUsage().disabled)) {
            isDisabled = true;
        }

        if (isDisabled) {
            statusChip.setText("Disabled");
            statusChip.setChipBackgroundColorResource(android.R.color.darker_gray);
        } else if (user.isExpired()) {
            statusChip.setText("Expired");
            statusChip.setChipBackgroundColorResource(android.R.color.holo_red_light);
        } else {
            statusChip.setText(user.isActive() ? "Online" : "Offline");
            statusChip.setChipBackgroundColorResource(
                    user.isActive() ? android.R.color.holo_green_light : android.R.color.transparent);
        }

        profileChip.setText(user.getProfile() != null ? user.getProfile() : "default");

        if (user.getPasswordType() != null && !user.getPasswordType().isEmpty()) {
            passwordTypeText.setText(user.getPasswordType().toUpperCase());
        } else {
            // Infer from password if blank
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                passwordTypeText.setText("BLANK");
            } else {
                passwordTypeText.setText("N/A");
            }
        }

        uptimeLimitText.setText(user.getDataLimit() != null ? user.getDataLimit() : "Unlimited");

        lastSeenText.setText(user.getLastSeen() != null ? ApiUtils.formatIsoDate(user.getLastSeen()) : "Never");

        serverText.setText(user.getServer() != null ? user.getServer() : "Unknown");
        ipAddressText.setText(user.getIpAddress() != null ? user.getIpAddress() : "Unknown");
        macAddressText.setText(user.getMacAddress() != null ? user.getMacAddress() : "Unknown");

        if (user.getUptime() != null) {
            usageUptimeText.setText("Uptime: " + user.getUptime());
        } else {
            usageUptimeText.setText("Uptime: 0s");
        }

        long totalBytes = user.getBytesIn() + user.getBytesOut();
        if (totalBytes == 0 && user.getDataUsed() > 0)
            totalBytes = user.getDataUsed();
        usageDataText.setText("Data: " + ApiUtils.formatBytes(totalBytes));

        // Details Logic
        if (user.getDetails() != null && !user.getDetails().isEmpty()) {
            detailsText.setText(user.getDetails());
            ((View) detailsText.getParent()).setVisibility(View.VISIBLE);
        } else {
            ((View) detailsText.getParent()).setVisibility(View.GONE);
        }

        String comment = user.getComment();
        if (comment != null && !comment.isEmpty()) {
            // Check for Customer: pattern
            if (comment.contains("Customer: ")) {
                comment = comment.substring(comment.indexOf("Customer: ") + 10).trim();
            }
            commentText.setText(comment);
        } else {
            commentText.setText("No comments");
        }
    }
}
