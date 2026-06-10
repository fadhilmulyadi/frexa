package com.diellabs.frexa.ui.terminal;

import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DurationBottomSheetFragment extends BottomSheetDialogFragment {
    private static final String[] LABELS = {"1 mnt", "5 mnt", "15 mnt", "30 mnt", "1 jam"};
    private static final int[] SECONDS = {60, 300, 900, 1800, 3600};

    private DurationCallback callback;

    public interface DurationCallback { void onSelected(int seconds, String label); }
    public void setCallback(DurationCallback cb) { callback = cb; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup parent, Bundle saved) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 32, 48, 48);
        for (int i = 0; i < LABELS.length; i++) {
            TextView tv = new TextView(requireContext());
            tv.setText(LABELS[i]);
            tv.setTextSize(18);
            tv.setPadding(0, 24, 0, 24);
            final int sec = SECONDS[i];
            final String label = LABELS[i];
            tv.setOnClickListener(v -> {
                if (callback != null) callback.onSelected(sec, label);
                dismiss();
            });
            root.addView(tv);
        }
        return root;
    }
}
