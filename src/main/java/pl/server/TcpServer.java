package pl.server;

import pwr_msg.PwrMsg;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class TcpServer {
    private ServerSocket serverSocket;
    private Socket socket;

    private DataInputStream inputStream;
    private DataOutputStream outputStream;


    private Map<String, String> ipMap;

   public TcpServer() throws  Exception {
        serverSocket = new ServerSocket(8085);
        ipMap = new HashMap<String, String>();
   }

   public void listen() throws IOException {
       while (true) {
           System.out.println("Nasłuchuję...");
           socket = serverSocket.accept();
           inputStream = new DataInputStream(socket.getInputStream());
           outputStream = new DataOutputStream(socket.getOutputStream());
           System.out.println("Połączenie: " + socket.getInetAddress().getHostName());

           Thread deamon = new Thread(new DeamonThread(inputStream, outputStream, socket, ipMap));
           deamon.start();

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

class DeamonThread implements Runnable {
    DataInputStream inputStream;
    DataOutputStream outputStream;
    Map<String, String> ipMap;
    Socket socket;

    public DeamonThread(DataInputStream inputStream, DataOutputStream outputStream, Socket socket, Map<String, String> ipMap) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.ipMap = ipMap;
        this.socket = socket;
    }


    @Override
    public void run() {
        PwrMsg.server_to_clinet toSend = null;
        PwrMsg.clinet_to_server toGet;

        try {
            DbController dbController = new DbController();
            int length = inputStream.readInt();
            byte[] message = new byte[length];

            if (length > 0) {
                inputStream.read(message, 0, message.length); // read the message
                toGet = PwrMsg.clinet_to_server.parseFrom(message);

                System.out.println("Parametry zapytania: " + toGet.getTypeValue() + " " + toGet.getPasswordString() + " " + toGet.getLoginString());

                switch (toGet.getTypeValue()) {
                    case 0: { //rejestrujemy
                        boolean dbResponse = register(dbController, toGet);
                        toSend = PwrMsg.server_to_clinet.newBuilder().setTypeValue(0).setIsSuccesful(dbResponse).build();
                        break;
                    }
                    case 1: { //logujemy
                        boolean dbResponse = login(dbController, toGet);
                        toSend = PwrMsg.server_to_clinet.newBuilder().setTypeValue(1).setIsSuccesful(dbResponse).build();
                        break;
                    }
                    case 2: { //getIp
                        toSend = getIP(toGet);
                        break;
                    }
                    case 3: { //wyloguj
                        toSend = logout(toGet);
                        break;
                    }
                    default: {
                        break;
                    }
                }

                byte[] toClientResponse = toSend.toByteArray();

                outputStream.flush();
                outputStream.writeInt(toClientResponse.length);
                outputStream.write(toClientResponse);
            }
        } catch (Exception e) {
            System.out.println("Rozłączono klienta");
        }
    }

    private boolean register(DbController dbController, PwrMsg.clinet_to_server toGet) {
        boolean dbResponse;
        try {
            dbResponse = dbController.register(toGet.getLoginString(), toGet.getPasswordString());
        } catch (SQLException e) {
            dbResponse = false;
            System.out.println("Prawdopodobnie użytkownik istnieje. Albo inny problem z bazą xD");
        }

        return dbResponse;
    }

    private boolean login(DbController dbController, PwrMsg.clinet_to_server toGet) {
        boolean dbResponse;

        try {
            dbResponse = dbController.login(toGet.getLoginString(), toGet.getPasswordString());
            synchronized (ipMap) {
                ipMap.put(toGet.getLoginString(), socket.getInetAddress().getHostName());
                System.out.println("Mapa IP: " + ipMap);
            }
        } catch (SQLException e) {
            dbResponse = false;
            System.out.println("Jakiś błąd przy logowaniu");
        }

        return dbResponse;
    }

    private PwrMsg.server_to_clinet getIP(PwrMsg.clinet_to_server toGet) {
        PwrMsg.server_to_clinet toSend = null;
        String ip = null;

        try {
            synchronized (ipMap) {
                System.out.println("Pobieram IP " + toGet.getLoginString());
                ip = ipMap.get(toGet.getLoginString());
            }
            System.out.println(ip);

            toSend = PwrMsg.server_to_clinet.newBuilder().setTypeValue(2).setIsSuccesful(true).setSecondClinetIp(ip).build();
        } catch (Exception e) {
            toSend = PwrMsg.server_to_clinet.newBuilder().setTypeValue(2).setIsSuccesful(false).build();
        }

        return toSend;
    }

    private PwrMsg.server_to_clinet logout(PwrMsg.clinet_to_server toGet) {
        PwrMsg.server_to_clinet toSend = null;

        try {
            synchronized (ipMap) {
                System.out.println("Usuwam IP " + toGet.getLoginString());
                ipMap.remove(toGet.getLoginString());
                System.out.println("Mapa IP: " + ipMap);
            }
            toSend = PwrMsg.server_to_clinet.newBuilder().setTypeValue(3).setIsSuccesful(true).build();
        } catch (Exception e) {
            toSend = PwrMsg.server_to_clinet.newBuilder().setTypeValue(3).setIsSuccesful(false).build();
        }

        return toSend;
    }
}