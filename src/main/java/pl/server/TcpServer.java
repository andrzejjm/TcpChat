package pl.server;

import pwr_msg.PwrMsg;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class TcpServer {
    private PwrMsg temp;

   private ServerSocket serverSocket;
   private Socket socket;

   private BufferedReader keyReader;

   private PrintWriter writer;
   private BufferedReader reader;

   private String reciveMessage;
   private String sendMessage;

   public TcpServer() throws  Exception {
        serverSocket = new ServerSocket(8085);
   }

   public void listen() throws Exception {
	    temp = new PwrMsg();
        socket = serverSocket.accept();
        System.out.println("Połączenie: " + socket.getInetAddress().getHostName());

        keyReader = new BufferedReader(new InputStreamReader(System.in));

        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        while (true) {
            if((reciveMessage = reader.readLine()) != null) {
                System.out.println(reciveMessage);
            }
            sendMessage = keyReader.readLine();
            writer.println(sendMessage);
            writer.flush();
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
