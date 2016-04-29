package rmi;

import java.io.Console;
import java.net.InetSocketAddress;

class PingServer implements ServerInterface {
    @Override
    public String ping(int idNumber) {
    	return "Pong"+ idNumber;
    }
}

public class PingPongServer {
    public static void main(String[] args) {
	  ServerInterface server = new PingServer();
	  InetSocketAddress addr=new InetSocketAddress(7000);
	  if(args.length==1){
		   addr= new InetSocketAddress(Integer.parseInt(args[0]));
	  }
      Skeleton<ServerInterface> skeleton = new Skeleton<ServerInterface>(ServerInterface.class, server, addr);
      System.out.println("Server start");
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
