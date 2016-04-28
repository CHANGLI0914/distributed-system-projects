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
    protected boolean toStop;
    protected Exception exception;

    public RmiListener(Skeleton<T> skeleton, Class<T> serverClass, T server, ServerSocket socket) {
        this.skeleton = skeleton;
        this.serverClass = serverClass;
        this.server = server;
        this.serverSocket = socket;
        this.pool = Executors.newCachedThreadPool();
        this.toStop = false;
        this.exception = null;
    }

    @Override
    public void run() {
        while (!toStop) {
            try {
                serverSocket.setSoTimeout(9);
                Socket socket = serverSocket.accept();
                pool.execute(new RmiHandler<T>(serverClass, server, socket, skeleton));
            } catch (SocketTimeoutException se) {
                // Do nothing. Just enable the loop to check the "toStop" var
            } catch (IOException e) {
                if (!skeleton.listen_error(e)) {
                    exception = e;
                    toStop = true;
                    break;
                }
            }
        }

        // All code below is to stop the listener and skeleton
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pool.shutdown();
        try {
            pool.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        skeleton.serverSocket = null;
        skeleton.listener = null;
        skeleton.stopped(exception);    //TODO: Here may be a problem
    }
}
