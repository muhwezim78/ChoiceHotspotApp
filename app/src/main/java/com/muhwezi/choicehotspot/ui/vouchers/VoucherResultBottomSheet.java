package com.muhwezi.choicehotspot.ui.vouchers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.models.voucher.Voucher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.muhwezi.choicehotspot.utils.ShareUtils;
import android.widget.Toast;

public class VoucherResultBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_VOUCHERS = "vouchers";
    private List<Voucher> vouchers;

    public static VoucherResultBottomSheet newInstance(List<Voucher> vouchers) {
        VoucherResultBottomSheet fragment = new VoucherResultBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VOUCHERS, (Serializable) vouchers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vouchers = (List<Voucher>) getArguments().getSerializable(ARG_VOUCHERS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_voucher_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        VoucherAdapter adapter = new VoucherAdapter(getContext(), vouchers);
        recyclerView.setAdapter(adapter);

        Button btnClose = view.findViewById(R.id.btn_close);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        Button btnExportAll = view.findViewById(R.id.btn_export_all);
        if (btnExportAll != null) {
            btnExportAll.setOnClickListener(v -> exportAllAsPdf());
        }

        Button btnShareAll = view.findViewById(R.id.btn_share_all);
        if (btnShareAll != null) {
            btnShareAll.setOnClickListener(v -> shareAll());
        }
    }

    private void shareAll() {
        if (vouchers == null || vouchers.isEmpty())
            return;
        StringBuilder sb = new StringBuilder("Connect to 'Choice Hotspot' and use these vouchers:\n");
        for (Voucher v : vouchers) {
            sb.append("- ").append(v.getCode()).append(" (")
                    .append(v.getProfile() != null ? v.getProfile() : "Standard").append(")\n");
        }
        ShareUtils.shareText(getContext(), "Choice Hotspot Vouchers", sb.toString());
    }

    private void exportAllAsPdf() {
        if (vouchers == null || vouchers.isEmpty())
            return;

        java.util.List<String> codes = new java.util.ArrayList<>();
        for (Voucher v : vouchers)
            codes.add(v.getCode());

        android.widget.Toast.makeText(getContext(), "Downloading batch PDF...", android.widget.Toast.LENGTH_SHORT)
                .show();
        com.muhwezi.choicehotspot.repository.ApiRepository.getInstance().getBatchVoucherPdf(codes,
                new com.muhwezi.choicehotspot.api.ApiCallback<byte[]>() {
                    @Override
                    public void onSuccess(byte[] data) {
                        if (isAdded()) {
                            savePdfToDownloads(data);
                        }
                    }

                    @Override
                    public void onError(String message, Throwable error) {
                        if (isAdded()) {
                            android.widget.Toast.makeText(getContext(), "Export failed: " + message,
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void savePdfToDownloads(byte[] data) {
        String fileName = "Vouchers_Batch_" + System.currentTimeMillis() + ".pdf";
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                        android.os.Environment.DIRECTORY_DOWNLOADS);
                android.net.Uri uri = getContext().getContentResolver()
                        .insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    java.io.OutputStream os = getContext().getContentResolver().openOutputStream(uri);
                    os.write(data);
                    os.close();
                    android.widget.Toast.makeText(getContext(), "Saved to Downloads", android.widget.Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                java.io.File downloads = android.os.Environment
                        .getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
                java.io.File file = new java.io.File(downloads, fileName);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                fos.write(data);
                fos.close();
                android.widget.Toast.makeText(getContext(), "Saved to Downloads", android.widget.Toast.LENGTH_SHORT)
                        .show();
            }
        } catch (Exception e) {
            android.widget.Toast
                    .makeText(getContext(), "Save failed: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
