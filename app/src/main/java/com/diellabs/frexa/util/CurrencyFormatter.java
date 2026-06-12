package com.diellabs.frexa.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormatter {
    private static final Locale ID = new Locale("id", "ID");
    private static final DecimalFormatSymbols ID_SYMBOLS;

    static {
        ID_SYMBOLS = new DecimalFormatSymbols(ID);
        ID_SYMBOLS.setGroupingSeparator('.');
        ID_SYMBOLS.setDecimalSeparator(',');
    }

    public static String formatIdr(double value) {
        double abs = Math.abs(value);
        if (abs >= 1_000_000_000_000L) {
            return "Rp" + new DecimalFormat("#,##0.#", ID_SYMBOLS).format(value / 1_000_000_000_000L) + " T";
        } else if (abs >= 1_000_000_000L) {
            return "Rp" + new DecimalFormat("#,##0.#", ID_SYMBOLS).format(value / 1_000_000_000L) + " M";
        } else if (abs >= 1_000_000L) {
            return "Rp" + new DecimalFormat("#,##0.#", ID_SYMBOLS).format(value / 1_000_000L) + " jt";
        } else {
            return "Rp" + new DecimalFormat("#,##0", ID_SYMBOLS).format(value);
        }
    }

    public static String formatIdrFull(double value) {
        return "Rp" + new DecimalFormat("#,##0.##", ID_SYMBOLS).format(value);
    }

    public static String formatPercent(double value) {
        String sign = value >= 0 ? "+" : "\u2212";
        double abs = Math.abs(value);
        return sign + new DecimalFormat("#,##0.##", ID_SYMBOLS).format(abs) + "%";
    }

    public static String formatCoinQty(double qty) {
        DecimalFormat df = new DecimalFormat("#,##0.########", ID_SYMBOLS);
        return df.format(qty);
    }

    public static String formatCompact(double value) {
        double abs = Math.abs(value);
        if (abs >= 1_000_000_000_000_000L) { // >= 1 quadrillion
            return new DecimalFormat("#,##0", ID_SYMBOLS).format(value / 1_000_000_000_000L) + " rb T";
        } else if (abs >= 1_000_000_000_000L) { // >= 1 trillion
            return new DecimalFormat("#,##0.#", ID_SYMBOLS).format(value / 1_000_000_000_000L) + " T";
        } else if (abs >= 1_000_000_000L) { // >= 1 billion (miliar)
            return new DecimalFormat("#,##0.#", ID_SYMBOLS).format(value / 1_000_000_000L) + " M";
        } else if (abs >= 1_000_000L) { // >= 1 million (juta)
            return new DecimalFormat("#,##0.#", ID_SYMBOLS).format(value / 1_000_000L) + " jt";
        } else if (abs >= 1_000L) { // >= 1 thousand (ribu)
            return new DecimalFormat("#,##0.#", ID_SYMBOLS).format(value / 1_000L) + " rb";
        } else {
            return new DecimalFormat("#,##0", ID_SYMBOLS).format(value);
        }
    }

    public static String formatChangeIdr(double change, double changePercent) {
        String sign = change >= 0 ? "+" : "\u2212";
        return sign + formatIdrFull(Math.abs(change)) + " \u00B7 " + formatPercent(changePercent);
    }
}
