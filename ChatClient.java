import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    public static void main(String[] args) {

        try (
            Socket socket = new Socket("localhost", 5000);

            BufferedReader inFromServer = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

            PrintWriter outToServer = new PrintWriter(
                socket.getOutputStream(), true);

            BufferedReader inFromUser = new BufferedReader(
                new InputStreamReader(System.in))
        ) {

            // Enter username
            System.out.print("Enter your name: ");
            String username = inFromUser.readLine();

            if (username == null || username.trim().isEmpty()) {
                System.out.println("Username is required.");
                return;
            }

            outToServer.println(username.trim());

            System.out.println("Connected to Server!");
            System.out.println("Type your message (type exit to quit):");

            // Receive messages from server
            Thread receiver = new Thread(() -> {
                try {
                    String response;

                    while ((response = inFromServer.readLine()) != null) {
                        System.out.println("Server reply: " + response);
                    }

                } catch (IOException e) {
                    System.out.println("Disconnected from Server.");
                }
            });

            receiver.start();

            // Read from standard input and send to server
            String sentence;

            while ((sentence = inFromUser.readLine()) != null) {

                if (sentence.equalsIgnoreCase("exit")) {
                    break;
                }

                outToServer.println(sentence);
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }
}