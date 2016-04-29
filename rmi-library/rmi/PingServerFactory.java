package rmi;

import java.net.InetSocketAddress;

public class PingServerFactory {
	public static ServerInterface makePingServer(String[] args){
		 InetSocketAddress addr=new InetSocketAddress(7000);
		  if(args.length==1){
			   addr= new InetSocketAddress(args[0], 7000);
			   System.out.println(args[0]);
		  }
		  else if(args.length==2){
			  addr= new InetSocketAddress(args[0],Integer.parseInt(args[1]));
		  }
		  return Stub.create(ServerInterface.class, addr);
	}
}
