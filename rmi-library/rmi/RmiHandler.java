package rmi;

import java.io.*;
import java.net.Socket;

public class RmiHandler implements Runnable {

    protected Socket socket;

    public RmiHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.print("New handler for " + socket.toString() + "starts!\n");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String data = reader.readLine();
            System.out.print("[Server] " + data);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(data + "[server]\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
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
