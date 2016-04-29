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

        try {
            ObjectOutputStream outputStream = new ObjectOutputStream((socket.getOutputStream()));
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            // Get call info (this part is not so safe, need to add more check)
            String methodName = (String) inputStream.readObject();
            int parameterNum = (Integer) inputStream.readObject();
            ArrayList<Class<?>> parameterTypeList = new ArrayList<Class<?>>();
            ArrayList<Object> parameterValueList = new ArrayList<Object>();
            for (int i=0; i<parameterNum; i++) {
                parameterTypeList.add((Class<?>) inputStream.readObject());
                parameterValueList.add(inputStream.readObject());
            }

            // Do method call
            Object res = null;
            boolean callSuccess = true;
            try {
                Method method = serverClass.getMethod(methodName,
                        parameterTypeList.toArray(new Class<?>[parameterNum]));
                res = method.invoke(server, parameterValueList.toArray());
            } catch (NoSuchMethodException noe) {
                callSuccess = false;
                res = new Exception(new RMIException(noe));
            } catch (Exception call_e) {
                callSuccess = false;
                res = call_e;
            }

            // Send back result/exception
            outputStream.writeObject(callSuccess);
            outputStream.writeObject(res);
            outputStream.flush();

        } catch (Exception e) {
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
