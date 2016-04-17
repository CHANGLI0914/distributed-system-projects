package rmi;

import java.net.*;
import java.io.*;

/* For any exception catched here, should use functions in Skeleton to report. */

public class RmiListener<T> extends Thread{

    protected Skeleton<T> skeleton;
    protected T server;
    protected ServerSocket serverSocket;
    protected boolean toStop;

    public RmiListener(Skeleton<T> skeleton, T server, ServerSocket socket) {
        this.skeleton = skeleton;
        this.server = server;
        this.serverSocket = socket;
    }

    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
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
