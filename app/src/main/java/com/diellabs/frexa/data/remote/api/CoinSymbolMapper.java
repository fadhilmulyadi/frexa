package com.diellabs.frexa.data.remote.api;

import java.util.HashMap;
import java.util.Map;

public final class CoinSymbolMapper {
    private static final Map<String, String> COIN_TO_BASE = new HashMap<>();

    static {
        COIN_TO_BASE.put("bitcoin",            "BTC");
        COIN_TO_BASE.put("ethereum",           "ETH");
        COIN_TO_BASE.put("binancecoin",        "BNB");
        COIN_TO_BASE.put("solana",             "SOL");
        COIN_TO_BASE.put("ripple",             "XRP");
        COIN_TO_BASE.put("cardano",            "ADA");
        COIN_TO_BASE.put("dogecoin",           "DOGE");
        COIN_TO_BASE.put("polkadot",           "DOT");
        COIN_TO_BASE.put("avalanche-2",        "AVAX");
        COIN_TO_BASE.put("tron",               "TRX");
        COIN_TO_BASE.put("chainlink",          "LINK");
        COIN_TO_BASE.put("polygon",            "MATIC");
        COIN_TO_BASE.put("litecoin",           "LTC");
        COIN_TO_BASE.put("uniswap",            "UNI");
        COIN_TO_BASE.put("near",               "NEAR");
        COIN_TO_BASE.put("stellar",            "XLM");
        COIN_TO_BASE.put("cosmos",             "ATOM");
        COIN_TO_BASE.put("filecoin",           "FIL");
        COIN_TO_BASE.put("aptos",              "APT");
        COIN_TO_BASE.put("arbitrum",           "ARB");
        COIN_TO_BASE.put("optimism",           "OP");
        COIN_TO_BASE.put("shiba-inu",          "SHIB");
        COIN_TO_BASE.put("sui",                "SUI");
        COIN_TO_BASE.put("pepe",               "PEPE");
        COIN_TO_BASE.put("toncoin",            "TON");
        COIN_TO_BASE.put("internet-computer",  "ICP");
    }

    public static String toBitgetSymbol(String coinId) {
        String base = COIN_TO_BASE.get(coinId);
        if (base == null) base = coinId.replace("-", "").toUpperCase();
        return base + "USDT";
    }

    public static String toBitgetGranularity(int seconds) {
        if (seconds >= 86400) return "1day";
        if (seconds >= 14400) return "4h";
        if (seconds >= 3600)  return "1h";
        if (seconds >= 900)   return "15min";
        if (seconds >= 300)   return "5min";
        return "1min";
    }
}
