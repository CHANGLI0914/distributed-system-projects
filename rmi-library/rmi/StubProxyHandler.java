package rmi;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;

public class StubProxyHandler implements InvocationHandler {

    protected InetSocketAddress address;

    public StubProxyHandler(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        Object res = null;
        Socket socket = null;
        try {
            socket = new Socket(address.getAddress(), address.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream((socket.getOutputStream()));
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            // Send call info
            outputStream.writeObject(method.getName());
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
