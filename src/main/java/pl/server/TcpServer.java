package pl.server;

// import org.korz.ant.ProtocTask;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import pwr_msg.*;

public class TcpServer {
	public PwrMsg tepm;
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
