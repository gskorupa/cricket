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
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.WebsocketServer;
import org.cricketmsf.in.InboundAdapter;
import org.cricketmsf.in.InboundAdapterIface;

/**
 *
 * @author greg
 */
public class WebsocketAdapter extends InboundAdapter implements InboundAdapterIface, Runnable {

    public static final int DIALOG = 0;
    public static final int INPUT = 1;
    public static final int OUTPUT = 2;

    public int serviceType = DIALOG;
    public boolean sendHello = true;
    
    private Adapter handler=null;

    private Socket socket;
    InputStream inputStream;
    OutputStream outputStream;
    private String context;
    private String dataToSend = null;
    private WebsocketServer server;
    private boolean running=false;
    
    public WebsocketAdapter(){
        super();
    }

    public WebsocketAdapter(Socket socket, WebsocketServer server) {
        this.socket = socket;
        this.server = server;
        
    }
    
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        setContext(properties.getOrDefault("context",""));
        if(!context.isBlank()){
            Kernel.getLogger().printIndented("context="+getContext());
        }else{
            //
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

    private void readInputStream(InputStream inputStream) {
        int len = 0;
        byte[] b = new byte[1024];
        //rawIn is a Socket.getInputStream();
        boolean waiting = true;
        while (waiting) {
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

                    setReceivedData(new String(message));
                    b = new byte[1024];

                    if (DIALOG == serviceType) {
                        setOutcomingData(new String(message));
                        sendMessage(getData());
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
                waiting = false;
            }
        }
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
        String path = null;
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
        if(null==server.getRegisteredContexts().get(getContext())){
            try {
                sendMessage("context "+getContext()+" is not supported");
            } catch (IOException ex) {
            }
            this.stop();
            return;
        }
        boolean ok = true;

        if (sendHello) {
            try {
                sendMessage("hello");
            } catch (IOException ex) {
                ok = false;
            }
        }
        setRunning(true);
        while (ok && !this.socket.isClosed()) {
            try {
                if (DIALOG == serviceType || INPUT == serviceType) {
                    readInputStream(inputStream);
                } else {
                    //setOutcomingData("" + System.currentTimeMillis());
                    sendMessage(getData());
                }
                Thread.sleep(10);
            } catch (InterruptedException | IOException e) {
                ok = false;
            }
        }

        try {
            inputStream.readAllBytes();
            inputStream.close();
        } catch (IOException e) {

        }
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {

        }
        System.out.println("WS disconnected:" + getContext());
    }

    private void setReceivedData(String message) {
        System.out.println("INCOMING WS MESSAGE: " + message);
    }

    private void setOutcomingData(String message) {
        dataToSend = message;
    }

    private String getData() {
        boolean interrupted = false;
        String result;
        while (!interrupted && null == dataToSend) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                interrupted = true;
                return "";
            }
        }
        result = dataToSend;
        dataToSend = null;
        return result;
    }

    public void start() {
        System.out.println("Starting WS adapter");
        Thread t = new Thread(this);
        t.start();
    }

    public void stop() {
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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

    /**
     * @return the running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }
}
