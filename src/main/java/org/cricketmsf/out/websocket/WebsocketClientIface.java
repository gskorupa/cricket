package org.cricketmsf.out.websocket;

/**
 *
 * @author greg
 */
public interface WebsocketClientIface {
    public void sendMessage(String message);
    public void onClose(int statusCode, String reason);
    public void onText(String message);
    public void onOpen();
    public void sendMessage(Object data);
    public void start();
    public void stop();
    public int getStatusCode();
}
