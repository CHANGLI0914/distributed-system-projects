package rmi;

import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
                break;
            } catch (IOException e) {
                if (!skeleton.listen_error(e)) {
                    // How to stop the skeleton here ?
                }
            }
        }
        pool.shutdown();
        try {
            // Wait until all Handler threads exit
            pool.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ie) {
            skeleton.listen_error(new RMIException("Interrupted when " +
                    "waiting all handler threads exit.", ie));
        }
    }
}
