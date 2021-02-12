package at.steinmax.helloworldsocket.logic;

import com.sun.nio.sctp.IllegalReceiveException;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MultiServer {
    final int port;
    ServerSocket serverSocket;
    final List<Socket> clients = new LinkedList<>();
    Thread acceptThread;
    volatile boolean isAccepting = false;
    byte[] OK = {(byte) 255, (byte) 255, (byte) 255, (byte) 255};

    public MultiServer(int port){
        this.port = port;
    }

    public synchronized boolean start() {
        try {
            if(!isAccepting){
                serverSocket = new ServerSocket(port, 5);
                isAccepting = true;
                acceptThread = createAcceptThread();
                acceptThread.setDaemon(true);
                acceptThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            stop();
            return false;
        }
        return true;
    }

    public void stop(){
        isAccepting = false;
        acceptThread = null;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Thread createAcceptThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                while(isAccepting){
                    try {
                        Socket newClient = serverSocket.accept();
                        clients.add(newClient);
                        Thread thread = createClientThread(newClient);
                        thread.setDaemon(true);
                        thread.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private Thread createClientThread(final Socket client){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                boolean exit = false;

                while (!exit){
                    try {
                        String message = recieve(client);

                        if(message.startsWith("gettime")){
                            send(client, LocalDateTime.now().toString());
                        }else {
                            send(client, "Weird Command ?!");
                        }

                    }catch (Exception exception){
                        exit = true;
                        clients.remove(client);
                        try {
                            client.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private String recieve(Socket socket) throws IOException {
        byte[] buffer = new byte[10];
        int rec = socket.getInputStream().read(buffer);
        String msg = new String(buffer, 0, rec, StandardCharsets.US_ASCII);

        socket.getOutputStream().write(OK);

        buffer = new byte[Integer.parseInt(msg)];
        rec = socket.getInputStream().read(buffer);
        msg = new String(buffer, 0, rec, StandardCharsets.UTF_8);

        return msg;
    }

    private void send(Socket socket, String message) throws IOException {
        byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
        socket.getOutputStream().write(String.valueOf(buffer.length).getBytes(StandardCharsets.US_ASCII));

        byte[] ackBuffer = new byte[4];
        int rec = socket.getInputStream().read(buffer);
        if(Arrays.equals(ackBuffer, OK)){
            socket.getOutputStream().write(buffer);
        }
        throw new IllegalReceiveException("Protocol violation!");
    }
}
