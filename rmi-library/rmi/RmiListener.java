package rmi;

import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RmiListener<T> extends Thread{

    protected final Skeleton<T> skeleton;
    protected final Class<T> serverClass;
    protected final T server;
    protected final ServerSocket serverSocket;
    protected final ExecutorService pool;

    public RmiListener(Skeleton<T> skeleton, Class<T> serverClass, T server, ServerSocket socket) {
        this.skeleton = skeleton;
        this.serverClass = serverClass;
        this.server = server;
        this.serverSocket = socket;
        this.pool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        while (serverSocket!= null && !serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                pool.execute(new RmiHandler<T>(serverClass, server, socket, skeleton));
            } catch (SocketException se) {
                // Should only appear when Skeleton close the server socket
                System.out.println("Catched SocketException in Listener.");
                se.printStackTrace();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                skeleton.listen_error(e);
            }
        }
        pool.shutdown();
    }
}
