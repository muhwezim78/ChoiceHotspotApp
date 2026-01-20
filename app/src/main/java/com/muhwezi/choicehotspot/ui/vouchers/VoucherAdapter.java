package com.muhwezi.choicehotspot.ui.vouchers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.models.voucher.Voucher;
import com.muhwezi.choicehotspot.utils.ApiUtils;
import com.muhwezi.choicehotspot.repository.ApiRepository;
import com.muhwezi.choicehotspot.api.ApiCallback;
import okhttp3.ResponseBody;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import java.io.OutputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private List<Voucher> vouchers = new ArrayList<>();
    private final Context context;

    public VoucherAdapter(Context context) {
        this.context = context;
    }

    public VoucherAdapter(Context context, List<Voucher> vouchers) {
        this.context = context;
        this.vouchers = vouchers;
    }

    public void setVouchers(List<Voucher> vouchers) {
        this.vouchers = vouchers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = vouchers.get(position);
        holder.bind(voucher);
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView codeText;
        TextView profileText;
        TextView priceText;
        Chip statusChip;
        ImageButton btnDownload;

        VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            codeText = itemView.findViewById(R.id.voucher_code);
            profileText = itemView.findViewById(R.id.voucher_profile);
            statusChip = itemView.findViewById(R.id.voucher_status);
            btnDownload = itemView.findViewById(R.id.btn_download);
        }

        void bind(Voucher voucher) {
            codeText.setText(voucher.getCode());
            profileText.setText(voucher.getProfile() + " / " + ApiUtils.formatCurrency(voucher.getPrice()));

            // Handle status
            String status = "Unused";
            int colorRes = android.R.color.holo_green_dark;
            int surfaceColor = 0x1A4CAF50;

            if (voucher.isUsed()) {
                status = "Used";
                colorRes = android.R.color.darker_gray;
                surfaceColor = 0x1A9E9E9E;
            } else if (isExpired(voucher)) {
                status = "Expired";
                colorRes = android.R.color.holo_red_dark;
                surfaceColor = 0x1AF44336;
            }

            statusChip.setText(status);
            statusChip.setTextColor(ContextCompat.getColor(context, colorRes));
            statusChip.setChipBackgroundColor(ColorStateList.valueOf(surfaceColor));
            statusChip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)));

            btnDownload.setOnClickListener(v -> downloadPdf(voucher));
        }

        private void downloadPdf(Voucher voucher) {
            Toast.makeText(context, "Downloading PDF...", Toast.LENGTH_SHORT).show();
            ApiRepository.getInstance().downloadVoucherPdf(voucher.getCode(), new ApiCallback<ResponseBody>() {
                @Override
                public void onSuccess(ResponseBody data) {
                    savePdfToDownloads(voucher.getCode(), data);
                }

                @Override
                public void onError(String message, Throwable error) {
                    Toast.makeText(context, "Download failed: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void savePdfToDownloads(String fileName, ResponseBody body) {
            String name = "Voucher_" + fileName + ".pdf";
            try {
                OutputStream fos;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    Uri contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                    Uri uri = context.getContentResolver().insert(contentUri, contentValues);
                    fos = context.getContentResolver().openOutputStream(uri);
                } else {
                    java.io.File downloadDir = Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    java.io.File file = new java.io.File(downloadDir, name);
                    fos = new java.io.FileOutputStream(file);
                }

                if (fos != null) {
                    InputStream is = body.byteStream();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    fos.close();
                    is.close();
                    Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        private boolean isExpired(Voucher voucher) {
            return voucher.isExpired();
        }
    }
}
