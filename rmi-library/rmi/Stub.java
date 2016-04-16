package rmi;

import java.lang.reflect.*;
import java.net.*;

public abstract class Stub
{

    public static <T> T create(Class<T> c, Skeleton<T> skeleton)
        throws UnknownHostException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    public static <T> T create(Class<T> c, Skeleton<T> skeleton,
                               String hostname)
    {
        throw new UnsupportedOperationException("not implemented");
    }

    public static <T> T create(Class<T> c, InetSocketAddress address)
    {
        InvocationHandler handler = new StubProxyHandler(address);
        return (T) java.lang.reflect.Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[] {c}, handler);
    }
}
