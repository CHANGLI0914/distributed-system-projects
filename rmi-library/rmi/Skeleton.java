package rmi;

import java.lang.reflect.Method;
import java.net.*;
import java.io.*;

public class Skeleton<T>
{
    protected final Class<T> serverClass;
    protected final T serverObject;
    protected final InetSocketAddress socketAddress;
    protected ServerSocket serverSocket;
    protected RmiListener<T> listener;

    public Skeleton(Class<T> c, T server)
    {
        this(c, server, null);
    }

    public Skeleton(Class<T> c, T server, InetSocketAddress socketAddress)
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

        this.serverClass = c;
        this.serverObject = server;
        this.socketAddress = socketAddress;
    }

    protected void stopped(Throwable cause)
    {
        System.out.println("Stopped");
    }

    protected boolean listen_error(Exception exception)
    {
        System.out.println("listen_error");
        return false;
    }

    protected void service_error(RMIException exception)
    {
        System.out.println("server error");
    }

    protected InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public synchronized void start() throws RMIException
    {
        // Test if has already started
        if (serverSocket != null) {
            throw new RMIException("Skeleton already started.");
        }
        assert listener == null;

        // Create new ServerSocket and bind
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(socketAddress);
        } catch (IOException ioe) {
            serverSocket = null;
            throw new RMIException("ServerSocket binding failed.", ioe);
        }

        listener = new RmiListener<T>(this, serverClass, serverObject, serverSocket);
        listener.start();
    }

    public synchronized void stop()
    {
        // TODO: Stop listener, then wait all handler stop, finally call stopped and exit
        // Is exception handling here is right?
        if (serverSocket != null) {
            assert listener == null;
            try {
                serverSocket.close();
                listener.join();
            } catch (IOException ioe) {
                System.out.println("NOTE: Shouldn't be able to catch IOexception here.");
                ioe.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                serverSocket = null;
                listener = null;
                stopped(null);
            }
        }
    }
}
