package com.diellabs.frexa.data.remote.api;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import okio.ByteString;

public class MexcWebSocketManager {
    private static final String TAG = "MexcWS";
    private static final String WS_URL = "wss://wspush.mexc.com/ws";

    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();
    private WebSocket webSocket;
    private PriceListener listener;
    private String activeSymbol;
    private volatile boolean connected = false;

    public interface PriceListener {
        void onPrice(String symbol, double price);
        void onError(String message);
    }

    public MexcWebSocketManager(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setListener(PriceListener listener) {
        this.listener = listener;
    }

    public void connect(String symbol) {
        activeSymbol = symbol;
        if (connected && webSocket != null) return;

        Request request = new Request.Builder().url(WS_URL).build();
        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                connected = true;
                subscribe(symbol);
                Log.d(TAG, "Connected & Subscribed: " + symbol);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                try {
                    JsonObject json = gson.fromJson(text, JsonObject.class);
                    if (json.has("c") && json.get("c").getAsString().contains("deals")) {
                        JsonObject data = json.getAsJsonObject("d");
                        if (data != null && data.has("p")) {
                            double price = data.get("p").getAsDouble();
                            if (listener != null) listener.onPrice(activeSymbol, price);
                        }
                    }
                } catch (Exception e) {
                    // Log.e(TAG, "Parse error: " + e.getMessage());
                }
            }

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

    private void subscribe(String symbol) {
        if (webSocket == null) return;
        String sub = "{\"method\":\"SUBSCRIPTION\",\"params\":[\"spot@public.deals.v3.api@" + symbol + "\"]}";
        webSocket.send(sub);
    }

    public void switchSymbol(String symbol) {
        if (symbol.equals(activeSymbol) && connected) return;
        if (webSocket != null && connected) {
            String unsub = "{\"method\":\"UNSUBSCRIPTION\",\"params\":[\"spot@public.deals.v3.api@" + activeSymbol + "\"]}";
            webSocket.send(unsub);
        }
        activeSymbol = symbol;
        if (connected) {
            subscribe(symbol);
        } else {
            connect(symbol);
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnect");
            webSocket = null;
            connected = false;
        }
    }

    public boolean isConnected() { return connected; }
}
