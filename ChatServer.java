import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {

    private static final CopyOnWriteArrayList<PrintWriter> clients =
        new CopyOnWriteArrayList<>();

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
    public static void addClient(PrintWriter clientOut) {
        clients.add(clientOut);
    }

    public static void removeClient(PrintWriter clientOut) {
        clients.remove(clientOut);
    }
    public static void broadcastMessage(String message, PrintWriter sender) {
        for (PrintWriter client : clients) {
            if (client != sender) {
                client.println(message);
                client.flush();
            }
        }
    }
}