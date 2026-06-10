package com.diellabs.frexa.util;

import java.util.Locale;

public class CurrencyFormatter {
    public static String formatUsd(double value) {
        if (value >= 1) {
            return String.format(Locale.US, "$%,.2f", value);
        } else {
            return String.format(Locale.US, "$%.6f", value);
        }
    }

    public static String formatPercent(double value) {
        return String.format(Locale.US, "%+.2f%%", value);
    }

    public static String formatBalance(float value) {
        return String.format(Locale.US, "$%,.2f", value);
    }
}
