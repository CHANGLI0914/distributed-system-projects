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
                int i =0;
        	    for(i=0;i<4;i++) {
                    String reply = stub1.ping(i).toString();
                    System.out.println("Server replied: " + reply);
                    if (!reply.equals("Pong"+i)) { break; }
                }
                System.out.println(i+" Tests Completed, "+
                                    (4-i)+" Tests Failed");                        

           } catch (RMIException re) {
               re.printStackTrace();
           }
    }
}
