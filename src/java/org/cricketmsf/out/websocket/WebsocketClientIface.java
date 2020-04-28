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
}
