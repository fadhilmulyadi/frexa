package com.diellabs.frexa.data.remote.api;

import android.util.Log;
import com.diellabs.frexa.data.remote.model.BinanceKlineEvent;
import com.google.gson.Gson;
import okhttp3.*;
import okio.ByteString;

import java.util.concurrent.atomic.AtomicInteger;

public class BinanceWebSocketManager {

    private static final String TAG = "BinanceWS";
    private static final String WS_URL = "wss://stream.binance.com:9443/ws";

    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();
    private WebSocket webSocket;
    private KlineListener listener;
    private String subscribedStream;
    private final AtomicInteger requestId = new AtomicInteger(1);
    private volatile boolean connected = false;

    public interface KlineListener {
        void onKline(BinanceKlineEvent event);
        void onError(String message);
    }

    public BinanceWebSocketManager(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setListener(KlineListener listener) {
        this.listener = listener;
    }

    public void connect() {
        if (connected && webSocket != null) return;
        Request request = new Request.Builder().url(WS_URL).build();
        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                connected = true;
                Log.d(TAG, "WebSocket connected");
                if (subscribedStream != null) {
                    subscribe(subscribedStream);
                }
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                try {
                    BinanceKlineEvent event = gson.fromJson(text, BinanceKlineEvent.class);
                    if ("kline".equals(event.eventType) && listener != null) {
                        listener.onKline(event);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse error: " + e.getMessage());
                }
            }

            @Override
            public void onMessage(WebSocket ws, ByteString bytes) {}

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                connected = false;
                Log.e(TAG, "WebSocket failure: " + t.getMessage());
                if (listener != null) listener.onError(t.getMessage());
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                connected = false;
                Log.d(TAG, "WebSocket closed: " + reason);
            }
        });
    }

    public void subscribe(String streamName) {
        subscribedStream = streamName;
        if (webSocket == null || !connected) {
            connect();
            return;
        }
        String json = String.format("{\"method\":\"SUBSCRIBE\",\"params\":[\"%s\"],\"id\":%d}",
                streamName, requestId.getAndIncrement());
        webSocket.send(json);
        Log.d(TAG, "Subscribed: " + streamName);
    }

    public void unsubscribe(String streamName) {
        if (webSocket == null || !connected) return;
        String json = String.format("{\"method\":\"UNSUBSCRIBE\",\"params\":[\"%s\"],\"id\":%d}",
                streamName, requestId.getAndIncrement());
        webSocket.send(json);
        Log.d(TAG, "Unsubscribed: " + streamName);
    }

    public void switchStream(String newStreamName) {
        if (subscribedStream != null && !subscribedStream.equals(newStreamName)) {
            unsubscribe(subscribedStream);
        }
        subscribe(newStreamName);
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
