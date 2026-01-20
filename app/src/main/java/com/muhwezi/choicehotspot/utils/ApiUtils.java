package com.muhwezi.choicehotspot.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility methods for API operations and formatting.
 * Mirrors the JavaScript ApiService.utils methods.
 */
public class ApiUtils {

    private static final String TAG = "ApiUtils";

    // ==================== Formatting ====================

    /**
     * Format bytes to human-readable string.
     * 
     * @param bytes Number of bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    public static String formatBytes(long bytes) {
        if (bytes <= 0)
            return "0 B";

        String[] units = { "B", "KB", "MB", "GB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        digitGroups = Math.min(digitGroups, units.length - 1);

        double value = bytes / Math.pow(1024, digitGroups);
        return String.format(Locale.US, "%.2f %s", value, units[digitGroups]);
    }

    /**
     * Format currency amount.
     * 
     * @param amount   Amount to format
     * @param currency Currency code (default: UGX)
     * @return Formatted currency string
     */
    public static String formatCurrency(double amount, String currency) {
        if (currency == null || currency.isEmpty()) {
            currency = "UGX";
        }

        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        String formatted = format.format(amount);

        // Replace USD symbol with specified currency
        if (!currency.equals("USD")) {
            formatted = formatted.replace("$", currency + " ");
        }

        return formatted;
    }

    /**
     * Format currency amount with default UGX currency.
     */
    public static String formatCurrency(double amount) {
        return formatCurrency(amount, "UGX");
    }

    /**
     * Format date to readable string.
     * 
     * @param date Date to format
     * @return Formatted date string (e.g., "Jan 15, 2024")
     */
    public static String formatDate(Date date) {
        if (date == null)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        return sdf.format(date);
    }

    /**
     * Format date with time.
     * 
     * @param date Date to format
     * @return Formatted datetime string (e.g., "Jan 15, 2024 10:30 AM")
     */
    public static String formatDateTime(Date date) {
        if (date == null)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
        return sdf.format(date);
    }

    /**
     * Format ISO date string to readable format.
     * 
     * @param isoDate ISO format date string
     * @return Formatted date string
     */
    public static String formatIsoDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty())
            return "";

        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "dd/MM/yyyy HH:mm:ss",
                "dd/MM/yyyy",
                "EEE, dd MMM yyyy HH:mm:ss 'GMT'"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                Date date = sdf.parse(dateStr);
                return formatDateTime(date);
            } catch (Exception ignored) {
            }
        }

        return dateStr;
    }

    // ==================== Validation ====================

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[\\d\\s\\-()]{10,}$");

    /**
     * Validate email format.
     * 
     * @param email Email to validate
     * @return true if valid email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty())
            return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number format.
     * 
     * @param phone Phone number to validate
     * @return true if valid phone format
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty())
            return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }

    // ==================== File Operations ====================

    /**
     * Save blob data to file in Downloads folder.
     * 
     * @param context  Android context
     * @param data     Byte array data
     * @param filename Filename to save as
     * @param mimeType MIME type of the file
     * @return URI of saved file, or null if failed
     */
    public static Uri saveToDownloads(Context context, byte[] data, String filename, String mimeType) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - use MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
                values.put(MediaStore.Downloads.IS_PENDING, 1);

                Uri uri = context.getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                        if (os != null) {
                            os.write(data);
                        }
                    }

                    values.clear();
                    values.put(MediaStore.Downloads.IS_PENDING, 0);
                    context.getContentResolver().update(uri, values, null, null);

                    Log.d(TAG, "File saved to Downloads: " + filename);
                    return uri;
                }
            } else {
                // Legacy approach
                File downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDir, filename);

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(data);
                }

                Log.d(TAG, "File saved to Downloads: " + file.getAbsolutePath());
                return Uri.fromFile(file);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to save file: " + e.getMessage());
        }

        return null;
    }

    // ==================== Error Handling ====================

    /**
     * Extract error message from throwable.
     * 
     * @param error          The error
     * @param defaultMessage Default message if none found
     * @return Error message
     */
    public static String getErrorMessage(Throwable error, String defaultMessage) {
        if (error == null)
            return defaultMessage;

        String message = error.getMessage();
        if (message != null && !message.isEmpty()) {
            return message;
        }

        return defaultMessage;
    }
}
