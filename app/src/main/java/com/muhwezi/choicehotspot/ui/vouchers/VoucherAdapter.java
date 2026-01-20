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
import android.widget.PopupMenu;
import com.muhwezi.choicehotspot.utils.ShareUtils;
import com.muhwezi.choicehotspot.utils.VoucherImageGenerator;
import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    public interface OnVoucherSelectionListener {
        void onSelectionChanged(int count);
    }

    private List<Voucher> vouchers = new ArrayList<>();
    private final java.util.Set<String> selectedCodes = new java.util.HashSet<>();
    private boolean selectionMode = false;
    private final Context context;
    private OnVoucherSelectionListener selectionListener;

    public VoucherAdapter(Context context) {
        this.context = context;
    }

    public VoucherAdapter(Context context, List<Voucher> vouchers) {
        this.context = context;
        this.vouchers = vouchers;
    }

    public void setSelectionListener(OnVoucherSelectionListener listener) {
        this.selectionListener = listener;
    }

    public void setVouchers(List<Voucher> vouchers) {
        this.vouchers = vouchers;
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean active) {
        this.selectionMode = active;
        if (!active)
            selectedCodes.clear();
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public List<String> getSelectedCodes() {
        return new ArrayList<>(selectedCodes);
    }

    public void selectAll() {
        for (Voucher v : vouchers) {
            selectedCodes.add(v.getCode());
        }
        notifyDataSetChanged();
        if (selectionListener != null)
            selectionListener.onSelectionChanged(selectedCodes.size());
    }

    public void clearSelection() {
        selectedCodes.clear();
        notifyDataSetChanged();
        if (selectionListener != null)
            selectionListener.onSelectionChanged(0);
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
        Chip statusChip;
        android.widget.CheckBox checkBox;
        ImageButton btnDownload;
        ImageButton btnShare;

        VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            codeText = itemView.findViewById(R.id.voucher_code);
            profileText = itemView.findViewById(R.id.voucher_profile);
            statusChip = itemView.findViewById(R.id.voucher_status);
            btnDownload = itemView.findViewById(R.id.btn_download);
            btnShare = itemView.findViewById(R.id.btn_share);
            checkBox = itemView.findViewById(R.id.voucher_checkbox);
        }

        void bind(Voucher voucher) {
            itemView.setOnLongClickListener(v -> {
                if (!selectionMode) {
                    setSelectionMode(true);
                    toggleSelection(voucher);
                    return true;
                }
                return false;
            });

            itemView.setOnClickListener(v -> {
                if (selectionMode) {
                    toggleSelection(voucher);
                }
            });

            checkBox.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
            checkBox.setChecked(selectedCodes.contains(voucher.getCode()));
            checkBox.setOnClickListener(v -> toggleSelection(voucher));

            codeText.setText(voucher.getCode());
            profileText.setText(voucher.getProfile() + " / " + ApiUtils.formatCurrency(voucher.getPrice()));

            // Hide download button in selection mode to avoid clutter
            btnDownload.setVisibility(selectionMode ? View.GONE : View.VISIBLE);
            btnShare.setVisibility(selectionMode ? View.GONE : View.VISIBLE);

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
            btnShare.setOnClickListener(v -> showShareMenu(voucher, btnShare));
        }

        private void showShareMenu(Voucher voucher, View anchor) {
            PopupMenu popup = new PopupMenu(context, anchor);
            popup.getMenu().add("Share as Text");
            popup.getMenu().add("Share as PDF");
            popup.getMenu().add("Share as Image");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.contains("Text")) {
                    ShareUtils.shareText(context, "Choice Hotspot Voucher",
                            ShareUtils.getVoucherShareText(voucher.getCode(), voucher.getProfile()));
                } else if (title.contains("PDF")) {
                    shareVoucherAsPdf(voucher);
                } else if (title.contains("Image")) {
                    shareVoucherAsImage(voucher);
                }
                return true;
            });
            popup.show();
        }

        private void shareVoucherAsPdf(Voucher voucher) {
            Toast.makeText(context, "Preparing PDF...", Toast.LENGTH_SHORT).show();
            ApiRepository.getInstance().downloadVoucherPdf(voucher.getCode(), new ApiCallback<ResponseBody>() {
                @Override
                public void onSuccess(ResponseBody data) {
                    try {
                        File cachePath = new File(context.getCacheDir(), "vouchers");
                        cachePath.mkdirs();
                        File file = new File(cachePath, "Voucher_" + voucher.getCode() + ".pdf");
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(data.bytes());
                        fos.close();
                        ShareUtils.shareFile(context, file, "application/pdf");
                    } catch (IOException e) {
                        Toast.makeText(context, "Failed to prepare PDF", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String message, Throwable error) {
                    Toast.makeText(context, "Failed to fetch PDF: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void shareVoucherAsImage(Voucher voucher) {
            Bitmap bitmap = VoucherImageGenerator.generateVoucherImage(context, voucher);
            ShareUtils.shareImage(context, bitmap, "Voucher_" + voucher.getCode());
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

        private void toggleSelection(Voucher voucher) {
            if (selectedCodes.contains(voucher.getCode())) {
                selectedCodes.remove(voucher.getCode());
            } else {
                selectedCodes.add(voucher.getCode());
            }
            notifyItemChanged(getAdapterPosition());
            if (selectionListener != null) {
                selectionListener.onSelectionChanged(selectedCodes.size());
            }
        }

        private boolean isExpired(Voucher voucher) {
            return voucher.isExpired();
        }
    }
}
