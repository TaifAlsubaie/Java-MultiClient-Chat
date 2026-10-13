import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(serverAddress, port)) {
            System.out.println("Connected to server successfully!");
        } catch (IOException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}