package rmi;

import java.net.*;
import java.io.*;


public class RmiListener<T> extends Thread{

    protected T server;
    protected ServerSocket serverSocket;

    public RmiListener(T server, int port) throws IOException {
        this.server = server;
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("[Server] New Conn:" + socket.toString());
                (new Thread(new RmiHandler<T>(server, socket))).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
