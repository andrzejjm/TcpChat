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
           socket = serverSocket.accept();
           inputStream = new DataInputStream(socket.getInputStream());
           outputStream = new DataOutputStream(socket.getOutputStream());
           System.out.println("Połączenie: " + socket.getInetAddress().getHostName());

           threadFunc(inputStream, outputStream, socket);
       }
   }

    public void threadFunc(final DataInputStream inputStream, final DataOutputStream outputStream, final Socket socket) {
        new Thread() {
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
                                boolean dbResponse;
                                try {
                                    dbResponse = dbController.register(toGet.getLoginString(), toGet.getPasswordString());
                                } catch (SQLException e) {
                                    dbResponse = false;
                                    System.out.println("Prawdopodobnie użytkownik istnieje. Albo inny problem z bazą xD");
                                }

                                toSend = PwrMsg.server_to_clinet.newBuilder().setTypeValue(0).setIsSuccesful(dbResponse).build(); //to nie działa
                            }
                            break;
                            case 1: { //logujemy
                                boolean dbResponse;

                                try {
                                    dbResponse = dbController.login(toGet.getLoginString(), toGet.getPasswordString());
                                    synchronized (ipMap) {
                                        ipMap.put(toGet.getLoginString(), socket.getInetAddress().getHostName());
                                        System.out.println(ipMap);
                                    }
                                } catch (SQLException e) {
                                    dbResponse = false;
                                    System.out.println("Jakiś błąd przy logowaniu");
                                }

                                toSend = PwrMsg.server_to_clinet.newBuilder().setTypeValue(1).setIsSuccesful(dbResponse).build(); // to działa
                            }
                            break;
                            case 2: { //getIp
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
                            }
                            break;
                            case 3: { //wyloguj

                            }
                            default:
                                break;
                        }

                        byte[] toClientResponse = toSend.toByteArray();

                        outputStream.flush();
                        outputStream.writeInt(toClientResponse.length);
                        outputStream.write(toClientResponse);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.run();
    }

    public void start() {
        try {
            listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
