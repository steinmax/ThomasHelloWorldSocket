package at.steinmax.helloworldsocket.logic;

import com.sun.nio.sctp.IllegalReceiveException;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Client {
    final String host;
    final int port;
    Socket serverConnection;
    final byte[] OK = {(byte) 255, (byte) 255, (byte) 255, (byte) 255};

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect(){
        try {
            serverConnection = new Socket(host, port);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String receive() throws IOException {
        byte[] buffer = new byte[10];
        int rec = serverConnection.getInputStream().read(buffer);
        String msg = new String(buffer, 0, rec, StandardCharsets.US_ASCII);

        serverConnection.getOutputStream().write(OK);

        buffer = new byte[Integer.parseInt(msg)];
        rec = serverConnection.getInputStream().read(buffer);
        msg = new String(buffer, 0, rec, StandardCharsets.UTF_8);

        return msg;
    }

    private void send(String message) throws IOException {
        byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
        serverConnection.getOutputStream().write(String.valueOf(buffer.length).getBytes(StandardCharsets.US_ASCII));

        byte[] ackBuffer = new byte[4];
        int rec = serverConnection.getInputStream().read(buffer);
        if(Arrays.equals(ackBuffer, OK)){
            serverConnection.getOutputStream().write(buffer);
        }
        throw new IllegalReceiveException("Protocol violation!");
    }

    public String getResponse(String message) {
        try {
            send(message);
            return receive();
        } catch (IOException e) {
            e.printStackTrace();
            serverConnection = null;
            return null;
        }
    }
}