package com.diellabs.frexa.data.remote.api;

import android.os.Handler;
import android.os.Looper;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class BitfinexWebSocketManager {
    private static final String WSS_URL = "wss://api-pub.bitfinex.com/ws/2";

    public interface PriceListener {
        void onPrice(double price);
    }

    private final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .pingInterval(30, TimeUnit.SECONDS)
            .build();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private WebSocket webSocket;
    private volatile String currentSymbol;
    private volatile PriceListener listener;
    private volatile int channelId = -1;
    private volatile boolean stopped = false;

    public void connect(String symbol, PriceListener priceListener) {
        stopped = false;
        currentSymbol = symbol;
        listener = priceListener;
        openSocket();
    }

    private void openSocket() {
        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }
        channelId = -1;

        Request req = new Request.Builder().url(WSS_URL).build();
        webSocket = client.newWebSocket(req, new WebSocketListener() {

            @Override public void onOpen(WebSocket ws, Response response) {
                String sym = currentSymbol;
                if (sym == null) return;
                try {
                    JSONObject sub = new JSONObject();
                    sub.put("event", "subscribe");
                    sub.put("channel", "ticker");
                    sub.put("symbol", sym);
                    ws.send(sub.toString());
                } catch (Exception ignored) {}
            }

            @Override public void onMessage(WebSocket ws, String text) {
                try {
                    if (text.startsWith("{")) {
                        JSONObject obj = new JSONObject(text);
                        if ("subscribed".equals(obj.optString("event"))) {
                            channelId = obj.getInt("chanId");
                        }
                    } else if (text.startsWith("[") && channelId >= 0) {
                        JSONArray arr = new JSONArray(text);
                        if (arr.length() >= 2 && arr.getInt(0) == channelId) {
                            Object second = arr.get(1);
                            if (second instanceof JSONArray) {
                                JSONArray ticker = (JSONArray) second;
                                // Bitfinex ticker: [BID,BID_SIZE,ASK,ASK_SIZE,DAILY_CHANGE,
                                //                   DAILY_CHANGE_REL,LAST_PRICE,VOLUME,HIGH,LOW]
                                if (ticker.length() > 6) {
                                    double price = ticker.getDouble(6);
                                    PriceListener cb = listener;
                                    if (price > 0 && cb != null) {
                                        mainHandler.post(() -> cb.onPrice(price));
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            @Override public void onFailure(WebSocket ws, Throwable t, Response response) {
                if (stopped) return;
                // Reconnect after 3 seconds
                mainHandler.postDelayed(() -> {
                    if (!stopped && currentSymbol != null) openSocket();
                }, 3000);
            }

            @Override public void onClosing(WebSocket ws, int code, String reason) {
                ws.close(1000, null);
            }
        });
    }

    public void disconnect() {
        stopped = true;
        listener = null;
        currentSymbol = null;
        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }
    }
}
