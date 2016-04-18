package rmi;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;

public class RmiHandler<T> implements Runnable {

    protected final Class<T> serverClass;
    protected final T server;
    protected final Socket socket;
    protected final Skeleton<T> skeleton;

    public RmiHandler(Class<T> serverClass, T server, Socket socket, Skeleton<T> skeleton) {
        this.serverClass = serverClass;
        this.server = server;
        this.socket = socket;
        this.skeleton = skeleton;
    }

    @Override
    public void run() {
        System.out.print("[Server] New handler for " + socket.toString() + "starts!\n");
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream((socket.getOutputStream()));
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            // Get call info (this part is not so safe, need to add more check)
            String methodName = (String) inputStream.readObject();
            int parameterNum = (Integer) inputStream.readObject();
            ArrayList<Class<?>> parameterTypeList = new ArrayList<>();
            ArrayList<Object> parameterValueList = new ArrayList<>();
            for (int i=0; i<parameterNum; i++) {
                parameterTypeList.add((Class<?>) inputStream.readObject());
                parameterValueList.add(inputStream.readObject());
            }

            // Do method call
            Method method = serverClass.getMethod(methodName, parameterTypeList.toArray(new Class<?>[parameterNum]));
            Object res = method.invoke(server, parameterValueList.toArray());

            // Write Back
            outputStream.writeObject(res);
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
            skeleton.service_error(new RMIException("Handler Exception", e));
        } /*finally {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/
    }
}
