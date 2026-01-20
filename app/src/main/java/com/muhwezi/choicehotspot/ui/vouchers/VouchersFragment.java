package com.muhwezi.choicehotspot.ui.vouchers;

import android.net.Uri;
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
    private ExtendedFloatingActionButton fabGenerate, fabExport;
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
        fabExport = view.findViewById(R.id.fab_export);

        setupRecyclerView();

        fabGenerate.setOnClickListener(v -> {
            GenerateVoucherBottomSheet bottomSheet = new GenerateVoucherBottomSheet();
            bottomSheet.show(getParentFragmentManager(), "GenerateVoucherBottomSheet");
        });

        fabExport.setOnClickListener(v -> {
            exportSelectedVouchers();
        });

        loadVouchers();
    }

    private void setupRecyclerView() {
        adapter = new VoucherAdapter(getContext());
        adapter.setSelectionListener(count -> {
            if (count > 0) {
                fabExport.setVisibility(View.VISIBLE);
                fabExport.setText("Export (" + count + ")");
                fabGenerate.hide();
            } else {
                fabExport.setVisibility(View.GONE);
                fabGenerate.show();
                adapter.setSelectionMode(false);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void exportSelectedVouchers() {
        List<String> selectedCodes = adapter.getSelectedCodes();
        if (selectedCodes.isEmpty())
            return;

        Toast.makeText(getContext(), "Preparing batch PDF...", Toast.LENGTH_SHORT).show();
        ApiRepository.getInstance().getBatchVoucherPdf(selectedCodes, new ApiCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] data) {
                if (!isAdded())
                    return;
                saveBatchPdf(data);
                adapter.clearSelection();
                adapter.setSelectionMode(false);
            }

            @Override
            public void onError(String message, Throwable error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Export failed: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveBatchPdf(byte[] data) {
        String fileName = "Vouchers_Batch_" + System.currentTimeMillis() + ".pdf";
        try {
            android.os.ParcelFileDescriptor pfd = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                        android.os.Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContext().getContentResolver()
                        .insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    java.io.OutputStream os = getContext().getContentResolver().openOutputStream(uri);
                    os.write(data);
                    os.close();
                }
            } else {
                java.io.File downloads = android.os.Environment
                        .getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
                java.io.File file = new java.io.File(downloads, fileName);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                fos.write(data);
                fos.close();
            }
            Toast.makeText(getContext(), "Saved to Downloads", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
