package rmi;

import java.lang.reflect.Method;
import java.net.*;
import java.io.*;

public class Skeleton<T>
{
    protected Class<T> c;
    protected T server;
    protected InetSocketAddress address;
    protected RmiListener<T> listener;

    public Skeleton(Class<T> c, T server)
    {
        this(c, server, null);
    }

    public Skeleton(Class<T> c, T server, InetSocketAddress address)
    {
        // Input null pointer check
        if (c == null || server == null) {
            throw new NullPointerException();
        }

        // Remote Interface c check
        for (Method m : c.getMethods()) {
            boolean throwRmiException = false;
            for (Class<?> exp : m.getExceptionTypes()) {
                if (exp.equals(RMIException.class)) {
                    throwRmiException = true;
                    break;
                }
            }
            if (!throwRmiException) throw new Error("Illegal interface");
        }

        this.c = c;
        this.server = server;
        this.address = address;
    }

    protected void stopped(Throwable cause)
    {
    }

    protected boolean listen_error(Exception exception)
    {
        return false;
    }

    protected void service_error(RMIException exception)
    {
    }

    public synchronized void start() throws RMIException
    {
        // TODO: test if has already started. Should only call start once before stop() called
        // Create new ServerSocket and bind
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(address);
        }catch (IOException ioe) {
            throw new RMIException(ioe);
        }

        listener = new RmiListener<T>(this, server, serverSocket);
        listener.start();
    }

    public synchronized void stop()
    {
        // TODO: Stop listener, then wait all handler stop, finally call stopped and exit
        if (listener != null && listener.isAlive()) {
            try {
                listener.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
