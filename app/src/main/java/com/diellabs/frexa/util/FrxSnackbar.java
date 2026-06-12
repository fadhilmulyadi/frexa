package com.diellabs.frexa.util;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.diellabs.frexa.R;
import com.google.android.material.snackbar.Snackbar;

public class FrxSnackbar {

    public static void showSuccess(View anchor, String title, String detail,
            String actionLabel, Runnable onAction) {
        show(anchor, title, detail, actionLabel, onAction, true);
    }

    public static void showError(View anchor, String title, String detail,
            String actionLabel, Runnable onAction) {
        show(anchor, title, detail, actionLabel, onAction, false);
    }

    @android.annotation.SuppressLint("RestrictedApi")
    private static void show(View anchor, String title, String detail, String actionLabel,
            Runnable onAction, boolean isSuccess) {
        Snackbar snackbar = Snackbar.make(anchor, "", 3000);

        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        layout.setPadding(12, 0, 12, 12);
        layout.setBackgroundColor(0x00000000);

        View custom = LayoutInflater.from(anchor.getContext())
            .inflate(R.layout.snackbar_order_result, layout, false);
        custom.setClipToOutline(true);

        custom.findViewById(R.id.snack_border).setBackgroundColor(
            anchor.getContext().getColor(isSuccess ? R.color.frx_up : R.color.frx_down));
        custom.findViewById(R.id.snack_icon_bg).setBackgroundResource(
            isSuccess ? R.drawable.bg_badge_success : R.drawable.bg_badge_failed);
        ((ImageView) custom.findViewById(R.id.snack_icon)).setImageResource(
            isSuccess ? R.drawable.ic_check_order : R.drawable.ic_x_order);

        ((TextView) custom.findViewById(R.id.snack_title)).setText(title);
        ((TextView) custom.findViewById(R.id.snack_detail)).setText(detail);

        TextView tvAction = custom.findViewById(R.id.snack_action);
        if (actionLabel != null) {
            tvAction.setText(actionLabel);
            tvAction.setOnClickListener(v -> {
                snackbar.dismiss();
                if (onAction != null) onAction.run();
            });
        } else {
            tvAction.setVisibility(View.GONE);
        }

        layout.addView(custom, 0);
        snackbar.show();
    }
}
