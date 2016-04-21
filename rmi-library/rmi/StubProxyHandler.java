package rmi;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
        
        if (method.equals(Object.class.getMethod("equals", Object.class))) {
        	Object target =objects[0];
        	if(target==null){
        		return false;
        	}
        	StubProxyHandler s1=(StubProxyHandler)Proxy.getInvocationHandler(target);
        	
        	if(s1.address==null^this.address==null){

        		return false;
        	}
        	if(s1.address!=null)
        		if(s1.address.getPort()!=this.address.getPort()||s1.address.getAddress()!=this.address.getAddress()){
        			return false;
        		}
        	if(!this.remoteClass.equals(s1.remoteClass)){

        		return false;
        	}

        		return true;
            
        }
        else if (method.getName().equals("hashCode")) {
        	int ret= this.address.hashCode() ^this.remoteClass.hashCode();  
        	return ret;
        }
        else  if (method.getName().equals("toString")) {
        	return this.address.toString() +this.remoteClass.toString();
        }
        else{
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
                return res;
            } catch (Exception e) {
                throw new RMIException(e);
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    throw new RMIException(e);
                }
            }
        }

    }
    public int hashCode(){
    	return this.address.hashCode() ^this.remoteClass.hashCode();

    }
    public String toString(){  	
    	System.out.println("Name of remote interface is "+ this.remoteClass.getName());
    	System.out.println("remote address: hostname: " + this.address.getHostName()+" port:" +this.address.getPort()); 
    	return "";
    }

}
