
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.SocketHandler;


public class Server {

    private int port;


    public Server() {
        this.port = 12345;
    }

    public Server(int port) {
        this.port = port;
    }


    public void start() throws IOException {

        AlunoResource recurso = new AlunoResource();

        try (ServerSocket server = new ServerSocket(this.port, 2048, InetAddress.getByName("127.0.0.1"))) {

            System.out.println("Servidor iniciado na porta " + this.port);

            while (true) {
                new Thread(new alunoServices(server.accept(), recurso)).start();
            }

        } // server.close;

    }

}
