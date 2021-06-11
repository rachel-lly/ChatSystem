import server.control.ServerController;
import java.io.IOException;

public class Server {
    public static void main(String[] args) throws IOException {
      new ServerController().start();
    }
}
