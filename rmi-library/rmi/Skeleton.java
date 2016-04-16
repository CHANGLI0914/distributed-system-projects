package rmi;

import java.net.*;
import java.io.*;

public class Skeleton<T>
{
    RmiListener<T> listener;

    public Skeleton(Class<T> c, T server)
    {
        try {
            listener = new RmiListener<T>(server, 12345);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Skeleton(Class<T> c, T server, InetSocketAddress address)
    {
        throw new UnsupportedOperationException("not implemented");
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
        listener.start();
    }

    public synchronized void stop()
    {
        if (listener != null && listener.isAlive()) {
            try {
                listener.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
