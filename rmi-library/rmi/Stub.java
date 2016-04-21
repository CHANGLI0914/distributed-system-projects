package rmi;

import java.lang.reflect.*;
import java.net.*;

/*
    TODO: What's UnknownHostException ?
    TODO: What's Cannot be Dynamically Created ?
 */


public abstract class Stub
{
    private static boolean interfaceCkeck(Class<?> c) {
        // Check whether c is an interface or a class
        if (!Modifier.isInterface(c.getModifiers())) {
            return false;
        }
        // Ckeck if is a Remote Interface
        for (Method m : c.getMethods()) {
            boolean throwRmiException = false;
            for (Class<?> exp : m.getExceptionTypes()) {
                if (exp.equals(RMIException.class)) {
                    throwRmiException = true;
                    break;
                }
            }
            if (!throwRmiException) return false;   // Failed
        }
        return true;    // Success
    }

    //TODO:here is question! when no address was input, what should do. If skeleton is not bound an address as well
    public static <T> T create(Class<T> c, Skeleton<T> skeleton) throws UnknownHostException
    {
        if (c == null || skeleton == null) {
            throw new NullPointerException();
        }
        if (!interfaceCkeck(c)) {
            throw new Error("Illegal interface");
        }
        InetSocketAddress inetSocketAddress = skeleton.getSocketAddress();
        if (inetSocketAddress == null) {
            throw new IllegalStateException();
        }
        // TODO: What's this ?
        if (inetSocketAddress.getAddress().equals("0.0.0.0")) {
            throw new UnknownHostException();
        }
        return Stub.create(c, inetSocketAddress);
    }

    public static <T> T create(Class<T> c, Skeleton<T> skeleton, String hostname)
    {
        if (c == null || skeleton == null || hostname == null) {
            throw new NullPointerException();
        }
        if (!interfaceCkeck(c)) {
            throw new Error("Illegal interface");
        }
        InetSocketAddress inetSocketAddress = skeleton.getSocketAddress();
        if (inetSocketAddress == null) {
            throw new IllegalStateException();
        }
        InetSocketAddress socketAddress = new InetSocketAddress(hostname, inetSocketAddress.getPort());
        return Stub.create(c, skeleton.getSocketAddress());
    }

    public static <T> T create(Class<T> c, InetSocketAddress address)
    {
        if (c == null || address == null) {
            throw new NullPointerException();
        }
        if (!interfaceCkeck(c)) {
            throw new Error("Illegal interface");
        }
        InvocationHandler handler = new StubProxyHandler(c, address);
        return (T) java.lang.reflect.Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[] {c}, handler);
    }
}
