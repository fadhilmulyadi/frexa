package com.diellabs.frexa.data.remote.api;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import okio.ByteString;
import java.lang.reflect.Type;
import java.util.Map;

public class CoinCapWebSocketManager {

    private static final String TAG = "CoinCapWS";
    private static final String WS_BASE = "wss://ws.coincap.io/prices?assets=";

    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();
    private final Type priceMapType = new TypeToken<Map<String, String>>(){}.getType();
    private WebSocket webSocket;
    private volatile PriceListener listener;
    private volatile String activeAssetId;
    private volatile boolean connected = false;

    public interface PriceListener {
        void onPrice(String assetId, double price);
        void onError(String message);
    }

    public CoinCapWebSocketManager(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setListener(PriceListener listener) {
        this.listener = listener;
    }

    public void connect(String assetId) {
        activeAssetId = assetId;
        if (connected && webSocket != null) return;
        final String capturedAssetId = assetId;
        Request request = new Request.Builder()
                .url(WS_BASE + capturedAssetId)
                .build();
        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                connected = true;
                Log.d(TAG, "Connected: " + capturedAssetId);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                try {
                    Map<String, String> prices = gson.fromJson(text, priceMapType);
                    if (prices != null && prices.containsKey(capturedAssetId) && listener != null) {
                        double price = Double.parseDouble(prices.get(capturedAssetId));
                        listener.onPrice(capturedAssetId, price);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse error: " + e.getMessage());
                }
            }

            @Override public void onMessage(WebSocket ws, ByteString bytes) {}

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                connected = false;
                Log.e(TAG, "Failure: " + t.getMessage());
                if (listener != null) listener.onError(t.getMessage());
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                connected = false;
                Log.d(TAG, "Closed: " + reason);
            }
        });
    }

    public void switchAsset(String assetId) {
        if (assetId.equals(activeAssetId) && connected) return;
        disconnect();
        connect(assetId);
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnect");
            webSocket = null;
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
