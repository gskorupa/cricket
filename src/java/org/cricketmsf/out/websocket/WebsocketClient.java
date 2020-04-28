package org.cricketmsf.out.websocket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Builder;
import java.net.http.WebSocket.Listener;
import java.util.HashMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.OutboundAdapterIface;

/**
 *
 * @author greg
 */
public class WebsocketClient extends OutboundAdapter implements OutboundAdapterIface, Adapter, WebsocketClientIface {

    public static int NOT_INITIALIZED = 0;
    public static int CONNECTED = 1;
    
    private ExecutorService executor;
    private WebSocket webSocket;
    private String endpoint;

    private int statusCode = -1;
    private String reason = "";
    private WebsocketClient self;

    public WebsocketClient() {
        super();
        self = this;
        statusCode = WebsocketClient.NOT_INITIALIZED;
        
        //endpoint = "wss://echo.websocket.org";
    }

    public void stop() {
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "ok")
                .thenRun(() -> System.out.println("Sent close"));
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void start() {
        executor = Executors.newFixedThreadPool(6);
        HttpClient httpClient = HttpClient.newBuilder().executor(executor).build();
        Builder webSocketBuilder = httpClient.newWebSocketBuilder();
        webSocket = webSocketBuilder.buildAsync(URI.create(endpoint), new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                self.onOpen();
                Listener.super.onOpen(webSocket);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                self.onText("" + data);
                return Listener.super.onText(webSocket, data, last);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                self.onClose(statusCode, reason);
                executor.shutdown();
                return Listener.super.onClose(webSocket, statusCode, reason);
            }
        }).join();
    }

    @Override
    public void sendMessage(String message) {
        if (null == webSocket) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(name, "not connected"));
            return;
        }
        webSocket.sendText(message, true);
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        getStatus(adapterName); //required if we need to overwrite updateStatusItem() method
        endpoint = properties.get("url");
        properties.put("url", endpoint);
        Kernel.getInstance().getLogger().print("\turl: " + endpoint);
        if (null != endpoint) {
            start();
        }
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode the statusCode to set
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public void onClose(int statusCode, String reason) {
        setStatusCode(statusCode);
        setReason(reason);
    }

    @Override
    public void onText(String message) {
        System.out.println("Received message: " + message);
    }

    @Override
    public void onOpen() {
        setStatusCode(CONNECTED);
    }

    @Override
    public void sendMessage(Object data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
