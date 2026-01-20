package com.muhwezi.choicehotspot.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.muhwezi.choicehotspot.models.voucher.Voucher;

/**
 * Helper to generate a Bitmap image representation of a voucher.
 */
public class VoucherImageGenerator {

    public static Bitmap generateVoucherImage(Context context, Voucher voucher) {
        int width = 800;
        int height = 500;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Background
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, width, height, bgPaint);

        // Border
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#2196F3")); // Blue
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(10);
        canvas.drawRect(5, 5, width - 5, height - 5, borderPaint);

        // Header Section
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(48);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setAntiAlias(true);
        canvas.drawText("Choice Hotspot Voucher", 50, 80, titlePaint);

        // SSID Information
        Paint ssidPaint = new Paint();
        ssidPaint.setColor(Color.DKGRAY);
        ssidPaint.setTextSize(32);
        ssidPaint.setAntiAlias(true);
        canvas.drawText("Network: Choice Hotspot", 50, 130, ssidPaint);

        // Divider Line
        Paint dividerPaint = new Paint();
        dividerPaint.setColor(Color.LTGRAY);
        dividerPaint.setStrokeWidth(2);
        canvas.drawLine(50, 160, width - 50, 160, dividerPaint);

        // Voucher Code Section
        Paint codeLabelPaint = new Paint();
        codeLabelPaint.setColor(Color.GRAY);
        codeLabelPaint.setTextSize(28);
        canvas.drawText("VOUCHER CODE", 50, 210, codeLabelPaint);

        Paint codePaint = new Paint();
        codePaint.setColor(Color.parseColor("#E91E63")); // Pink/Red
        codePaint.setTextSize(72);
        codePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText(voucher.getCode(), 50, 280, codePaint);

        // Profile & Price Section
        Paint infoPaint = new Paint();
        infoPaint.setColor(Color.BLACK);
        infoPaint.setTextSize(36);
        canvas.drawText("Profile: " + (voucher.getProfile() != null ? voucher.getProfile() : "Standard"), 50, 350,
                infoPaint);
        canvas.drawText("Price: UGX " + voucher.getPrice(), 50, 400, infoPaint);

        // Instructions Section
        Paint notePaint = new Paint();
        notePaint.setColor(Color.GRAY);
        notePaint.setTextSize(24);
        notePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        canvas.drawText("Connect to 'Choice Hotspot' and enter code to surf.", 50, 450, notePaint);

        return bitmap;
    }
}
