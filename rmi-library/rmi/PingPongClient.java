package rmi;

import java.net.InetSocketAddress;

//input format
// 1. java rmi/PingPongClient ip port
//2. java rmi/PingPongClient ip 
public class PingPongClient {
    public static void main(String[] args) {
           ServerInterface stub1 = PingServerFactory.makePingServer(args);         
           System.out.println("Client start");
           try {
        	   for(int i=0;i<3;i++)
               System.out.println("Server replied: " + stub1.ping(i).toString());

           } catch (RMIException re) {
               re.printStackTrace();
           }
    }
}
