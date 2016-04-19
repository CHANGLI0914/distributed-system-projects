package rmi;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;

public class StubProxyHandler implements InvocationHandler {

    protected Class<?> remoteClass;
    protected InetSocketAddress address;

    public StubProxyHandler(Class<?> remoteClass, InetSocketAddress address) {
        this.remoteClass = remoteClass;
        this.address = address;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        Object res = null;
        Socket socket = null;

        // Implement equals() TODO: NO finished && IS THIS CORRECT ?
        if (method.equals(Object.class.getMethod("equals", Object.class))) {
            Object target = objects[0];
            //if (target instanceof )
        }

        System.out.println("#This#" + o.getClass().toString());
        try {
            socket = new Socket(address.getAddress(), address.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream((socket.getOutputStream()));
            outputStream.flush();
            System.out.println("#This#" + o.getClass().toString());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("#This#" + o.getClass().toString());
            // Send call info
            outputStream.writeObject(method.getName());
            System.out.println("#This#" + o.getClass().toString());
            int parameterNum = objects.length;
            outputStream.writeObject(parameterNum);
            Class<?>[] parameterTypeArray = method.getParameterTypes();
            for (int i=0; i<parameterNum; i++) {
                outputStream.writeObject(parameterTypeArray[i]);
                outputStream.writeObject(objects[i]);
            }
            outputStream.flush();

            // Receive result
            res = inputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return res;
        }
    }
}
