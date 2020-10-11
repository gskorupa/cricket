/*
Idea from:
https://stackoverflow.com/users/2876079/stefan
https://stackoverflow.com/questions/43163592/standalone-websocket-server-without-jee-application-server
Thanks a lot!
 */
package org.cricketmsf.in.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import org.cricketmsf.Adapter;
import org.cricketmsf.event.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.WebsocketServer;
import org.cricketmsf.annotation.WebsocketAdapterHook;
import org.cricketmsf.in.InboundAdapter;
import org.cricketmsf.in.InboundAdapterIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class WebsocketAdapter extends InboundAdapter implements InboundAdapterIface, Adapter, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketAdapter.class);

    public static final int DIALOG = 0;
    public static final int INPUT = 1;
    public static final int OUTPUT = 2;

    public int serviceType = DIALOG;
    public boolean sendHello = false;
    public boolean echo = false;
    public String context = null;
    public boolean stopped = false;

    //private Adapter handler = null;
    private Socket socket;
    InputStream inputStream;
    OutputStream outputStream;
    private String dataToSend = null;
    private WebsocketServer server;

    private String serviceHookName = null;

    public WebsocketAdapter() {
        super();
    }

    public WebsocketAdapter(Socket socket, WebsocketServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        loadProperties(properties, adapterName, true);
    }

    public void loadProperties(HashMap<String, String> properties, String adapterName, boolean onStart) {
        super.loadProperties(properties, adapterName);
        setContext(properties.getOrDefault("context", ""));
        echo = Boolean.parseBoolean(properties.getOrDefault("echo", "false"));
        sendHello = Boolean.parseBoolean(properties.getOrDefault("send-hello", "false"));
        String tmpMode = properties.getOrDefault("mode", "input");
        if ("dialog".equalsIgnoreCase(tmpMode)) {
            serviceType = WebsocketAdapter.DIALOG;
        } else if ("output".equalsIgnoreCase(tmpMode)) {
            serviceType = WebsocketAdapter.OUTPUT;
        } else {
            serviceType = WebsocketAdapter.INPUT;
            tmpMode = "input";
        }
        if (!onStart) {
            getServiceHook();
        } else {
            logger.info("context=" + getContext());
            logger.info("echo=" + echo);
            logger.info("send-hello=" + sendHello);
            logger.info("mode=" + tmpMode);
        }
    }

    public void sendMessage(String message) throws IOException {
        try {
            outputStream.write(encode(message));
            outputStream.flush();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new IOException("unsupported encoding");
        }
    }

    private String readInputStream(InputStream inputStream) {
        int len = 0;
        byte[] b = new byte[1024];
        //rawIn is a Socket.getInputStream();
        boolean waiting = true;
        //while (waiting) {
        try {
            len = inputStream.read(b);
            if (len != -1) {

                byte rLength = 0;
                int rMaskIndex = 2;
                int rDataStart = 0;
                //b[0] is always text in my case so no need to check;
                byte data = b[1];
                byte op = (byte) 127;
                rLength = (byte) (data & op);

                if (rLength == (byte) 126) {
                    rMaskIndex = 4;
                }
                if (rLength == (byte) 127) {
                    rMaskIndex = 10;
                }

                byte[] masks = new byte[4];

                int j = 0;
                int i = 0;
                for (i = rMaskIndex; i < (rMaskIndex + 4); i++) {
                    masks[j] = b[i];
                    j++;
                }

                rDataStart = rMaskIndex + 4;

                int messLen = len - rDataStart;

                byte[] message = new byte[messLen];

                for (i = rDataStart, j = 0; i < len; i++, j++) {
                    message[j] = (byte) (b[i] ^ masks[j % 4]);
                }
                return new String(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encode(String mess) throws IOException {
        byte[] rawData = mess.getBytes();

        int frameCount = 0;
        byte[] frame = new byte[10];

        frame[0] = (byte) 129;

        if (rawData.length <= 125) {
            frame[1] = (byte) rawData.length;
            frameCount = 2;
        } else if (rawData.length >= 126 && rawData.length <= 65535) {
            frame[1] = (byte) 126;
            int len = rawData.length;
            frame[2] = (byte) ((len >> 8) & (byte) 255);
            frame[3] = (byte) (len & (byte) 255);
            frameCount = 4;
        } else {
            frame[1] = (byte) 127;
            int len = rawData.length;
            frame[2] = (byte) ((len >> 56) & (byte) 255);
            frame[3] = (byte) ((len >> 48) & (byte) 255);
            frame[4] = (byte) ((len >> 40) & (byte) 255);
            frame[5] = (byte) ((len >> 32) & (byte) 255);
            frame[6] = (byte) ((len >> 24) & (byte) 255);
            frame[7] = (byte) ((len >> 16) & (byte) 255);
            frame[8] = (byte) ((len >> 8) & (byte) 255);
            frame[9] = (byte) (len & (byte) 255);
            frameCount = 10;
        }

        int bLength = frameCount + rawData.length;

        byte[] reply = new byte[bLength];

        int bLim = 0;
        for (int i = 0; i < frameCount; i++) {
            reply[bLim] = frame[i];
            bLim++;
        }
        for (int i = 0; i < rawData.length; i++) {
            reply[bLim] = rawData[i];
            bLim++;
        }

        return reply;
    }

    private String doHandShakeToInitializeWebSocketConnection(InputStream inputStream, OutputStream outputStream) throws UnsupportedEncodingException {
        String data = new Scanner(inputStream, "UTF-8").useDelimiter("\\r\\n\\r\\n").next();
        String path = "";
        Matcher get = Pattern.compile("^GET.*HTTP").matcher(data);

        if (get.find()) {
            String pathWithQuery = get.group();
            String[] parts = pathWithQuery.split(" ");
            if (parts.length == 3) {
                path = parts[1].split("\\?")[0];
            } else {
                path = "/";
            }
            Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            match.find();
            byte[] response = null;
            try {
                response = ("HTTP/1.1 101 Switching Protocols\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Sec-WebSocket-Accept: "
                        + DatatypeConverter.printBase64Binary(
                                MessageDigest
                                        .getInstance("SHA-1")
                                        .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                                .getBytes("UTF-8")))
                        + "\r\n\r\n")
                        .getBytes("UTF-8");
            } catch (NoSuchAlgorithmException e) {
                // TODO
                e.printStackTrace();
            }

            try {
                outputStream.write(response, 0, response.length);
            } catch (IOException e) {
                // TODO
                e.printStackTrace();
            }
        } else {

        }
        return path;
    }

    @Override
    public void run() {
        try {
            inputStream = this.socket.getInputStream();
        } catch (IOException inputStreamException) {
            throw new IllegalStateException("Could not connect to client input stream", inputStreamException);
        }

        try {
            outputStream = this.socket.getOutputStream();
        } catch (IOException inputStreamException) {
            throw new IllegalStateException("Could not connect to client input stream", inputStreamException);
        }

        try {
            setContext(doHandShakeToInitializeWebSocketConnection(inputStream, outputStream));
        } catch (UnsupportedEncodingException handShakeException) {
            throw new IllegalStateException("Could not connect to client input stream", handShakeException);
        }

        if (null == server.getRegisteredContexts().get(getContext())) {
            try {
                sendMessage("context " + getContext() + " is not supported");
            } catch (IOException ex) {
            }
            this.stop();
            return;
        } else {
            loadProperties(server.getRegisteredContexts().get(getContext()).properties, getName(), false);
        }
        boolean ok = true;
        if (sendHello) {
            try {
                sendMessage("hello");
            } catch (IOException ex) {
                ex.printStackTrace();
                ok = false;
            }
        }
        while (ok && !this.socket.isClosed() && this.socket.isConnected()) {
            try {
                if (OUTPUT == serviceType) {
                    sendMessage(getData());
                } else {
                    String message = readInputStream(inputStream);
                    if (null != message && !message.isEmpty()) {
                        setReceivedData(message);
                        if (DIALOG == serviceType) {
                            if (echo) {
                                sendMessage(message);
                            } else {
                                sendMessage(getData());
                            }
                        }
                    } else {
                        ok = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                ok = false;
            }
        }

        try {
            inputStream.close();
        } catch (Exception e) {
        }
        try {
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {

        }
        logger.info("WS client disconnected from " + getContext());
        stop();
    }

    private void setReceivedData(String message) {
        String result = null;
        if (serviceHookName == null) {
            logger.warn("hook method is not defined for context {}", getContext());
            return;
        }
        try {
            Method m = Kernel.getInstance().getClass().getMethod(serviceHookName, String.class);
            result = (String) m.invoke(Kernel.getInstance(), message);
        } catch (NoSuchMethodException e) {
            logger.warn("handler method NoSuchMethodException {} {}", serviceHookName, e.getMessage());
        } catch (IllegalAccessException e) {
            logger.warn("handler method IllegalAccessException {} {}", serviceHookName, e.getMessage());
        } catch (InvocationTargetException e) {
            logger.warn("handler method InvocationTargetException {} {}", serviceHookName, e.getMessage());
        }
        if (null != result) {
            setOutcomingData(result);
        }
    }

    public void setOutcomingData(String message) {
        if (!stopped) {
            dataToSend = message;
        }
    }

    private synchronized String getData() {
        boolean interrupted = false;
        String result = null;
        while (!interrupted && null == dataToSend && !this.socket.isClosed() && this.socket.isConnected()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                interrupted = true;
            }
        }
        if (interrupted) {
            result = "interrupted";
        } else {
            result = dataToSend;
        }
        dataToSend = null;
        return result;
    }

    public String waitContext() {
        boolean interrupted = false;
        String result;
        while (!interrupted && null == context) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                interrupted = true;
            }
        }
        if (interrupted) {
            result = null;
        } else {
            result = context;
        }
        return result;
    }

    public void start() {
        //System.out.println("Starting WS adapter");
        Thread t = new Thread(this);
        t.start();
    }

    public void stop() {
        try {
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        stopped = true;
    }

    /**
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
    }

    private void getServiceHook() {
        WebsocketAdapterHook ah;
        String ctx;
        String defaultMethod = null;
        // for every method of a Kernel instance (our service class extending Kernel)
        for (Method m : Kernel.getInstance().getClass().getMethods()) {
            ah = (WebsocketAdapterHook) m.getAnnotation(WebsocketAdapterHook.class);
            // we search for annotated method
            if (ah != null) {
                ctx = ah.context();
                if (getContext().equalsIgnoreCase(ctx)) {
                    serviceHookName = m.getName();
                } else if ("*".equalsIgnoreCase(ctx)) {
                    defaultMethod = m.getName();
                }
            }
        }
        if (null == serviceHookName && null != defaultMethod) {
            serviceHookName = defaultMethod;
        }
    }
}
