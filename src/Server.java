

import server.ServerBusinesses;

import java.io.IOException;


public class Server {
    public static void main(String[] args) throws IOException {
        ServerBusinesses server = new ServerBusinesses();
        server.start();
    }
}
