import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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

                // Pass clientSocket to ClientHandler
                new Thread(new ClientHandler(clientSocket)).start();
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
        System.out.println("Broadcasting: " + message);
        for (PrintWriter client : clients) {
            if (client != sender) {
                client.println(message);
                client.flush();
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter writer;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                writer = new PrintWriter(socket.getOutputStream(), true);
                addClient(writer);

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = reader.readLine()) != null) {
                    broadcastMessage(message, writer);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            } finally {
                if (writer != null) {
                    removeClient(writer);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}