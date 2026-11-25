package com.auca.lostandfound.util;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import com.auca.lostandfound.R;

/**
 * Helpers for applying colour-coded badges to status and report-type labels.
 */
public class UiUtils {

    private UiUtils() {}

    /**
     * Applies a rounded pill background + text colour to a TextView
     * based on the item's report type ("LOST" or "FOUND").
     */
    public static void applyReportTypeBadge(Context ctx, TextView tv, String reportType) {
        if ("FOUND".equalsIgnoreCase(reportType)) {
            applyBadge(ctx, tv,
                    ctx.getColor(R.color.type_found_bg),
                    ctx.getColor(R.color.type_found_text));
            tv.setText("FOUND");
        } else {
            applyBadge(ctx, tv,
                    ctx.getColor(R.color.type_lost_bg),
                    ctx.getColor(R.color.type_lost_text));
            tv.setText("LOST");
        }
    }

    /**
     * Applies a rounded pill background + text colour to a TextView
     * based on the item's status ("AVAILABLE" or "RETURNED").
     */
    public static void applyStatusBadge(Context ctx, TextView tv, String status) {
        if ("AVAILABLE".equalsIgnoreCase(status)) {
            applyBadge(ctx, tv,
                    ctx.getColor(R.color.status_available_bg),
                    ctx.getColor(R.color.status_available_text));
            tv.setText("AVAILABLE");
        } else {
            applyBadge(ctx, tv,
                    ctx.getColor(R.color.status_returned_bg),
                    ctx.getColor(R.color.status_returned_text));
            tv.setText("RETURNED");
        }
    }

    private static void applyBadge(Context ctx, TextView tv, int bg, int textColor) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(50f);
        shape.setColor(bg);
        tv.setBackground(shape);
        tv.setTextColor(textColor);
        int px = (int) (6 * ctx.getResources().getDisplayMetrics().density);
        tv.setPadding(px * 2, px, px * 2, px);
    }
}
