package rmi;

import java.net.*;
import java.io.*;

public class Skeleton<T>
{
    RmiListener listener;

    public Skeleton(Class<T> c, T server)
    {
        try {
            listener = new RmiListener(12345);
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
        listener.run();
    }

    public synchronized void stop()
    {
        throw new UnsupportedOperationException("not implemented");
    }
}
