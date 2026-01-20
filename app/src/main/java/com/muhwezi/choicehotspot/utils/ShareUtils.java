package com.muhwezi.choicehotspot.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class for sharing content via Android Intents.
 */
public class ShareUtils {

    public static void shareText(Context context, String title, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, "Share via"));
    }

    public static void shareFile(Context context, File file, String mimeType) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share File"));
    }

    public static void shareImage(Context context, Bitmap bitmap, String filename) {
        try {
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, filename + ".png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            shareFile(context, file, "image/png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getVoucherShareText(String code, String profile) {
        return "Connect to 'Choice Hotspot' and use Voucher: " + code + " (" + profile + ")";
    }

    public static String getUserShareText(String username, String password) {
        return "Login to 'Choice Hotspot' with Username: " + username + ", Password: " + password;
    }
}
