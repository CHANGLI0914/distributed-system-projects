package rmi;

import java.net.*;
import java.io.*;


public class RmiListener extends Thread{

    protected ServerSocket serverSocket;

    public RmiListener(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("New Conn:" + socket.toString());
                (new Thread(new RmiHandler(socket))).run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
