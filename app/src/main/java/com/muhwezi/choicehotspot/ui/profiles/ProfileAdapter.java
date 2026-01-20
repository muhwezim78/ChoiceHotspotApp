package com.muhwezi.choicehotspot.ui.profiles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.models.profile.Profile;
import com.muhwezi.choicehotspot.utils.ApiUtils;

import java.util.ArrayList;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<Profile> profiles = new ArrayList<>();
    private final Context context;

    public ProfileAdapter(Context context) {
        this.context = context;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profiles.get(position);
        holder.bind(profile);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView detailsText;
        TextView priceText;
        TextView userCountText;

        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.profile_name);
            detailsText = itemView.findViewById(R.id.profile_details);
            priceText = itemView.findViewById(R.id.profile_price);
            userCountText = itemView.findViewById(R.id.user_count);
        }

        void bind(Profile profile) {
            nameText.setText(profile.getName());

            String details = "Validity: " + profile.getTimeLimit();
            if (profile.getRateLimit() != null && !profile.getRateLimit().isEmpty()) {
                details += " | Rate: " + profile.getRateLimit();
            }
            detailsText.setText(details);

            priceText.setText(ApiUtils.formatCurrency(profile.getPrice()));

            userCountText.setText("Active Users: " + profile.getActiveUsers());
        }
    }
}
