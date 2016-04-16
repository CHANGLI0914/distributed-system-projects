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
        String res = "";
        Socket socket = null;
        try {
            socket = new Socket(address.getAddress(), address.getPort());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            StringBuilder msg = new StringBuilder();
            msg.append(method.getName());
            for (Object obj : objects) {
                msg.append(" - ");
                msg.append(obj.toString());
            }
            msg.append("[client]\n");
            writer.write(msg.toString());
            writer.flush();

            res = reader.readLine();
        } catch (IOException e) {
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
