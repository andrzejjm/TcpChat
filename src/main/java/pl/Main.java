package pl;

import pl.server.TcpServer;

public class Main {
    public static void main(String[] args) {
        TcpServer tcpServer = null;
        try {
            tcpServer = new TcpServer();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tcpServer.start();
    }
}
