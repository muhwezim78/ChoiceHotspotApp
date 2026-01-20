package com.muhwezi.choicehotspot.ui.profiles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.api.ApiCallback;
import com.muhwezi.choicehotspot.models.profile.Profile;
import com.muhwezi.choicehotspot.repository.ApiRepository;

import java.util.List;

public class ProfilesFragment extends Fragment {

    private RecyclerView recyclerView;
    private CircularProgressIndicator loadingIndicator;
    private ExtendedFloatingActionButton fabAdd;
    private ProfileAdapter profileAdapter;
    private com.muhwezi.choicehotspot.ui.vouchers.VoucherAdapter voucherAdapter;
    private boolean isVouchersTab = false;

    public ProfilesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profiles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        fabAdd = view.findViewById(R.id.fab_add);

        view.findViewById(R.id.btn_profiles).setOnClickListener(v -> switchTab(false));
        view.findViewById(R.id.btn_vouchers).setOnClickListener(v -> switchTab(true));

        setupRecyclerView();

        fabAdd.setOnClickListener(v -> {
            if (isVouchersTab) {
                showGenerateVoucherDialog();
            } else {
                Toast.makeText(getContext(), "Add Profile Feature Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        loadProfiles();
    }

    private void setupRecyclerView() {
        profileAdapter = new ProfileAdapter(getContext());
        voucherAdapter = new com.muhwezi.choicehotspot.ui.vouchers.VoucherAdapter(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(profileAdapter);
    }

    private void switchTab(boolean vouchers) {
        this.isVouchersTab = vouchers;
        if (vouchers) {
            recyclerView.setAdapter(voucherAdapter);
            loadVouchers();
        } else {
            recyclerView.setAdapter(profileAdapter);
            loadProfiles();
        }
    }

    private void loadProfiles() {
        showLoading(true);
        fabAdd.setText("Add Profile");

        ApiRepository.getInstance().getEnhancedProfiles(true, new ApiCallback<List<Profile>>() {
            @Override
            public void onSuccess(List<Profile> data) {
                if (!isAdded())
                    return;
                showLoading(false);
                if (data != null) {
                    profileAdapter.setProfiles(data);
                }
            }

            @Override
            public void onError(String message, Throwable error) {
                if (isAdded()) {
                    showLoading(false);
                    Toast.makeText(getContext(), "Failed to load profiles: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadVouchers() {
        showLoading(true);
        fabAdd.setText("Generate Voucher");
        fabAdd.setIconResource(android.R.drawable.ic_menu_add);

        ApiRepository.getInstance()
                .getVouchersLocal(new ApiCallback<List<com.muhwezi.choicehotspot.models.voucher.Voucher>>() {
                    @Override
                    public void onSuccess(List<com.muhwezi.choicehotspot.models.voucher.Voucher> data) {
                        if (!isAdded())
                            return;
                        showLoading(false);
                        if (data != null) {
                            voucherAdapter.setVouchers(data);
                        }
                    }

                    @Override
                    public void onError(String message, Throwable error) {
                        if (isAdded()) {
                            showLoading(false);
                            Toast.makeText(getContext(), "Failed to load local vouchers", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showGenerateVoucherDialog() {
        com.muhwezi.choicehotspot.ui.vouchers.GenerateVoucherBottomSheet bottomSheet = new com.muhwezi.choicehotspot.ui.vouchers.GenerateVoucherBottomSheet();
        bottomSheet.show(getChildFragmentManager(), "GenerateVoucherBottomSheet");
    }

    private void showLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
}
