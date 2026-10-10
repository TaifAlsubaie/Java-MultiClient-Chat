import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

    public static void main(String[] args) {

        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is running on port " + port);
            System.out.println("Waiting for clients...");

            while (true) {

                Socket clientSocket = serverSocket.accept();

                System.out.println("New client connected: "
                        + clientSocket.getInetAddress());

                // TODO: Pass clientSocket to ClientHandler
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}