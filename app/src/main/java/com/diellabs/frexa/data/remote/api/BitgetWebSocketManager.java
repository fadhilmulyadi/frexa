package com.diellabs.frexa.data.remote.api;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.util.concurrent.TimeUnit;

public class BitgetWebSocketManager {
    private static final String TAG = "BitgetWS";
    private static final String WS_URL = "wss://ws.bitget.com/v2/ws/public";

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

    public BitgetWebSocketManager(OkHttpClient httpClient) {
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
                Log.d(TAG, "Connected: " + symbol);
                startHeartbeat(ws);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                if (text.equals("pong")) return;
                try {
                    JsonObject json = gson.fromJson(text, JsonObject.class);
                    if (json.has("data")) {
                        JsonArray dataArray = json.getAsJsonArray("data");
                        if (dataArray.size() > 0) {
                            JsonObject data = dataArray.get(0).getAsJsonObject();
                            if (data.has("lastPr")) {
                                double price = data.get("lastPr").getAsDouble();
                                if (listener != null) listener.onPrice(activeSymbol, price);
                            }
                        }
                    }
                } catch (Exception e) {}
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                connected = false;
                Log.e(TAG, "Failure: " + t.getMessage());
                if (listener != null) listener.onError(t.getMessage());
            }
        });
    }

    private void subscribe(String symbol) {
        if (webSocket == null) return;
        String sub = "{\"op\":\"subscribe\",\"args\":[{\"instType\":\"SPOT\",\"channel\":\"ticker\",\"instId\":\"" + symbol + "\"}]}";
        webSocket.send(sub);
    }

    private void startHeartbeat(WebSocket ws) {
        new Thread(() -> {
            while (connected) {
                try {
                    Thread.sleep(25000);
                    if (connected) ws.send("ping");
                } catch (InterruptedException e) { break; }
            }
        }).start();
    }

    public void switchSymbol(String symbol) {
        if (symbol.equals(activeSymbol) && connected) return;
        if (webSocket != null && connected) {
            String unsub = "{\"op\":\"unsubscribe\",\"args\":[{\"instType\":\"SPOT\",\"channel\":\"ticker\",\"instId\":\"" + activeSymbol + "\"}]}";
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
