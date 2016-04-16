package rmi;

class EchoServer implements Server {
    @Override
    public String echo(String s) {
        return s + " +1";
    }
}

public class ServerTest {
    public static void main(String[] args) {
        Server server = new EchoServer();
        Skeleton<Server> skeleton = new Skeleton<Server>(Server.class, server);
        try {
            skeleton.start();
        } catch (RMIException e) {
            e.printStackTrace();
        }
        skeleton.stop();
    }
}
