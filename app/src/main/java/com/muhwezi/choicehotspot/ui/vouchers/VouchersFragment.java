package com.muhwezi.choicehotspot.ui.vouchers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.muhwezi.choicehotspot.models.voucher.Voucher;
import com.muhwezi.choicehotspot.repository.ApiRepository;

import java.util.List;

public class VouchersFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private CircularProgressIndicator loadingIndicator;
    private ExtendedFloatingActionButton fabGenerate;
    private VoucherAdapter adapter;

    public VouchersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vouchers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);
        emptyView = view.findViewById(R.id.empty_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        fabGenerate = view.findViewById(R.id.fab_generate);

        setupRecyclerView();

        fabGenerate.setOnClickListener(v -> {
            GenerateVoucherBottomSheet bottomSheet = new GenerateVoucherBottomSheet();
            bottomSheet.show(getParentFragmentManager(), "GenerateVoucherBottomSheet");
        });

        loadVouchers();
    }

    private void setupRecyclerView() {
        adapter = new VoucherAdapter(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadVouchers() {
        showLoading(true);
        // Using getExpiredVouchers as seen in React Code context
        ApiRepository.getInstance().getExpiredVouchers(true, new ApiCallback<List<Voucher>>() {
            @Override
            public void onSuccess(List<Voucher> data) {
                if (!isAdded())
                    return;
                showLoading(false);
                if (data == null || data.isEmpty()) {
                    showEmpty(true);
                } else {
                    showEmpty(false);
                    adapter.setVouchers(data);
                }
            }

            @Override
            public void onError(String message, Throwable error) {
                if (isAdded()) {
                    showLoading(false);
                    // On error, show empty or toast
                    showEmpty(true);
                    Toast.makeText(getContext(), "Failed to load vouchers: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmpty(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
