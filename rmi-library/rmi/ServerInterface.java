package rmi;

public interface ServerInterface {
    String ping(int  idNumber) throws RMIException;
}