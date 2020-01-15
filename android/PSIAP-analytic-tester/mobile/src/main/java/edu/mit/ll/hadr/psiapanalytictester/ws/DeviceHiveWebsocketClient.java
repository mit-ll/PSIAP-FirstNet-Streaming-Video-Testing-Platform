package edu.mit.ll.hadr.psiapanalytictester.ws;


import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class DeviceHiveWebsocketClient extends WebSocketListener
{
    private static final String TAG = "PSIAP_WS";
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private volatile boolean isOpen = false;

    private OkHttpClient client = null;
    private WebSocket ws = null;

    public void init(final String wsAddress)
    {
        if (client == null)
            client = new OkHttpClient();

        Request request = new Request.Builder().url(wsAddress).build();
        ws = client.newWebSocket(request, this);
    }

    public void closeAndShutdown()
    {
        try
        {
            if (client != null) {
                client.dispatcher().executorService().shutdown();
            }

            this.shutdown();
            Log.d(TAG, "Shutdown DhWsClient!");
        } catch (Exception e) {
            Log.e("Caught exception shutting down", e.getMessage(), e);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.i(TAG, "Opened WS");
        isOpen = true;
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        isOpen = false;
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        isOpen = false;
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
//        super.onFailure(webSocket, t, response);
        isOpen = false;
    }

    public void shutdown() {

    }


}
