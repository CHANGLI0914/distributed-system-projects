package rmi;

import java.io.Console;
import java.net.InetSocketAddress;

class EchoServer implements Server {
    @Override
    public String echo(String s) {
        return s + " +1";
    }
}

public class ServerTest {
    public static void main(String[] args) {
        Server server = new EchoServer();
        InetSocketAddress addr = new InetSocketAddress("localhost", 12345);
        Skeleton<Server> skeleton = new Skeleton<Server>(Server.class, server, addr);
        try {
            skeleton.start();
        } catch (RMIException e) {
            e.printStackTrace();
        }
        Console console = System.console();
        if (console != null) {
            console.readLine("Press any key to stop.");
        }
        skeleton.stop();
    }
}
