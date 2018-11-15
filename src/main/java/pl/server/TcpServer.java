package pl.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

   private ServerSocket server;

   public TcpServer() {
       try {
           server = new ServerSocket(8085);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

   public void listen() throws Exception {
       String data = null;

       Socket client = server.accept();
       String clientAddress = client.getInetAddress().getHostAddress();
       System.out.println("Połączenie: " + clientAddress);

       BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));

       while ((data = input.readLine()) != null) {
           System.out.println(data);
       }
   }

   public void start() {
       try {
           listen();
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
}
