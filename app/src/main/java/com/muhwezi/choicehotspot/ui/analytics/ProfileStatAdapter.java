package com.muhwezi.choicehotspot.ui.analytics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.models.analytics.AnalyticsDashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileStatAdapter extends RecyclerView.Adapter<ProfileStatAdapter.ViewHolder> {

    private List<AnalyticsDashboard.ProfileStat> profileStats = new ArrayList<>();

    public void setProfileStats(List<AnalyticsDashboard.ProfileStat> stats) {
        this.profileStats = stats != null ? stats : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnalyticsDashboard.ProfileStat stat = profileStats.get(position);

        String name = stat.profileName != null ? stat.profileName : "Unknown";
        double revenue = stat.totalRevenue;
        int count = stat.usedCount > 0 ? stat.usedCount : stat.totalSold;

        holder.tvName.setText(name);
        holder.tvSold.setText(String.format(Locale.getDefault(), "%d used", count));
        holder.tvRevenue.setText(String.format(Locale.US, "%,.0f UGX", revenue));
    }

    @Override
    public int getItemCount() {
        return profileStats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSold, tvRevenue;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_profile_name);
            tvSold = itemView.findViewById(R.id.tv_profile_sold);
            tvRevenue = itemView.findViewById(R.id.tv_profile_revenue);
        }
    }
}
