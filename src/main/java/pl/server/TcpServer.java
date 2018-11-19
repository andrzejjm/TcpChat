package pl.server;

import pwr_msg.PwrMsg;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class TcpServer {
    private PwrMsg.server_to_clinet toSend;
    private PwrMsg.clinet_to_server toGet;

    private ServerSocket serverSocket;
    private Socket socket;

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private DbController dbController;

    private Map<String, String> ipMap;

   public TcpServer() throws  Exception {
        serverSocket = new ServerSocket(8085);
        ipMap = new HashMap<String, String>();
        dbController = new DbController();
   }

   public void listen() throws IOException {
        socket = serverSocket.accept();
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("Połączenie: " + socket.getInetAddress().getHostName());

        int length = inputStream.readInt();
        System.out.println(length);
        byte[] message = new byte[length];

        if(length>0) {
            inputStream.read(message, 0, message.length); // read the message
            for(int i=0; i < length; i++) {
                System.out.print(message[i] + " ");
            }
            toGet = PwrMsg.clinet_to_server.parseFrom(message);

            System.out.println(toGet.getTypeValue() + " " + toGet.getPasswordString() + " " + toGet.getLoginString());

            switch (toGet.getTypeValue()) {
                case 0: {
                    try {
                        dbController.register(toGet.getLoginString(), toGet.getLoginString());
                        toSend = PwrMsg.server_to_clinet.newBuilder().setTypeValue(0).setIsSuccesful(true).build();
                    } catch (SQLException e) {
                        System.out.println("ERRR"); //tu coś nie gra bo nie tworzy się toSend
                        toSend = PwrMsg.server_to_clinet.newBuilder().setTypeValue(0).setIsSuccesful(false).build();
                        System.out.println("ERR2");
                        e.toString();
                    }
                    break;
                }
                case 1:
                    break;
                case 3:
                    break;
                default:
                    break;
            }

            byte[] toClientResponse = toSend.toByteArray();
            for(int i=0; i < toClientResponse.length; i++) {
                System.out.print(toClientResponse[i] + " ");
            }

            outputStream.flush();
            outputStream.writeInt(toClientResponse.length);
            outputStream.write(toClientResponse);
        }
    }

   public void start() {
       try {
           listen();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }
}
