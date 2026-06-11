package com.diellabs.frexa.data.remote.api;

import java.util.HashMap;
import java.util.Map;

public final class CoinSymbolMapper {

    private static final Map<String, String> COIN_TO_FSYM = new HashMap<>();

    static {
        COIN_TO_FSYM.put("bitcoin",            "BTC");
        COIN_TO_FSYM.put("ethereum",           "ETH");
        COIN_TO_FSYM.put("binancecoin",        "BNB");
        COIN_TO_FSYM.put("solana",             "SOL");
        COIN_TO_FSYM.put("ripple",             "XRP");
        COIN_TO_FSYM.put("cardano",            "ADA");
        COIN_TO_FSYM.put("dogecoin",           "DOGE");
        COIN_TO_FSYM.put("polkadot",           "DOT");
        COIN_TO_FSYM.put("avalanche-2",        "AVAX");
        COIN_TO_FSYM.put("tron",               "TRX");
        COIN_TO_FSYM.put("chainlink",          "LINK");
        COIN_TO_FSYM.put("polygon",            "MATIC");
        COIN_TO_FSYM.put("litecoin",           "LTC");
        COIN_TO_FSYM.put("uniswap",            "UNI");
        COIN_TO_FSYM.put("near",               "NEAR");
        COIN_TO_FSYM.put("stellar",            "XLM");
        COIN_TO_FSYM.put("cosmos",             "ATOM");
        COIN_TO_FSYM.put("filecoin",           "FIL");
        COIN_TO_FSYM.put("aptos",              "APT");
        COIN_TO_FSYM.put("arbitrum",           "ARB");
        COIN_TO_FSYM.put("optimism",           "OP");
        COIN_TO_FSYM.put("shiba-inu",          "SHIB");
        COIN_TO_FSYM.put("sui",                "SUI");
        COIN_TO_FSYM.put("pepe",               "PEPE");
        COIN_TO_FSYM.put("toncoin",            "TON");
        COIN_TO_FSYM.put("internet-computer",  "ICP");
        COIN_TO_FSYM.put("aave",               "AAVE");
        COIN_TO_FSYM.put("maker",              "MKR");
        COIN_TO_FSYM.put("the-graph",          "GRT");
        COIN_TO_FSYM.put("hedera-hashgraph",   "HBAR");
        COIN_TO_FSYM.put("vechain",            "VET");
        COIN_TO_FSYM.put("algorand",           "ALGO");
        COIN_TO_FSYM.put("quant-network",      "QNT");
        COIN_TO_FSYM.put("fantom",             "FTM");
        COIN_TO_FSYM.put("the-sandbox",        "SAND");
        COIN_TO_FSYM.put("decentraland",       "MANA");
        COIN_TO_FSYM.put("tezos",              "XTZ");
        COIN_TO_FSYM.put("eos",                "EOS");
        COIN_TO_FSYM.put("theta-token",        "THETA");
        COIN_TO_FSYM.put("injective-protocol", "INJ");
        COIN_TO_FSYM.put("render-token",       "RNDR");
        COIN_TO_FSYM.put("fetch-ai",           "FET");
        COIN_TO_FSYM.put("stacks",             "STX");
        COIN_TO_FSYM.put("immutable-x",        "IMX");
        COIN_TO_FSYM.put("worldcoin-wld",      "WLD");
        COIN_TO_FSYM.put("bonk",               "BONK");
        COIN_TO_FSYM.put("celestia",           "TIA");
        COIN_TO_FSYM.put("sei-network",        "SEI");
        COIN_TO_FSYM.put("kaspa",              "KAS");
    }

    public static String toCryptoCompareFsym(String coinId) {
        String fsym = COIN_TO_FSYM.get(coinId);
        if (fsym != null) return fsym;
        return coinId.replace("-", "").toUpperCase();
    }

    /** Returns aggregate value for CryptoCompare histominute. For 1h use isHourly() instead. */
    public static int toAggregate(int seconds) {
        switch (seconds) {
            case 300:  return 5;
            case 900:  return 15;
            case 1800: return 30;
            default:   return 1;
        }
    }

    public static boolean isHourly(int seconds) {
        return seconds >= 3600;
    }
}
