import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.*;
import java.awt.*;
import java.util.Locale;//+

public class ChatServer {

    private static final CopyOnWriteArrayList<PrintWriter> clients = new CopyOnWriteArrayList<>();

    private static JTextArea logArea;
    private static JLabel clientsLabel;

    public static void main(String[] args) {

       SwingUtilities.invokeLater(() -> createGUI());

       int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            showLog("Server started...");

            while (true) {

                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }

        } catch (IOException e) {
            showLog("Server error: " + e.getMessage());
        }
    }

    private static void createGUI() {
        JFrame frame = new JFrame("Chat Server Dashboard");
        frame.setSize(650, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        clientsLabel = new JLabel("Connected Clients: 0");
        clientsLabel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(logArea);

        JButton stopButton = new JButton("Stop Server");
        stopButton.addActionListener(e -> System.exit(0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(stopButton);

        frame.add(clientsLabel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static void showLog(String message) {
    System.out.println(message);

    SwingUtilities.invokeLater(() -> {
        if (logArea != null) {
            logArea.append(message + "\n");
            logArea.setCaretPosition(
                    logArea.getDocument().getLength());
        }
    });
    }

    private static void updateClientCount() {
        int count = clients.size();

        SwingUtilities.invokeLater(() -> {
            if (clientsLabel != null) {
                clientsLabel.setText("Connected Clients: " + count);
            }
        });
    }

    public static void addClient(PrintWriter clientOut) {
        clients.add(clientOut);
        updateClientCount();
    }

    public static void removeClient(PrintWriter clientOut) {
        clients.remove(clientOut);
        updateClientCount();
    }

    public static void processMessage(String message, PrintWriter sender) {//++
        String modifiedMessage = message.toUpperCase(Locale.ROOT);

    showLog("Received: " + message);

    sender.println(modifiedMessage);

    showLog("Sent: " + modifiedMessage);
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter writer;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // Receive username from client
                username = reader.readLine();

                if (username == null || username.trim().isEmpty()) {
                    return;
                }
                username = username.trim();
                addClient(writer);
                showLog(username + " connected.");

                String message;
                while ((message = reader.readLine()) != null) {
                    processMessage(message, writer);//+
                }
            } catch (IOException e) {
                showLog("Client error: " + e.getMessage());
            } finally {
                if (writer != null) {
                    removeClient(writer);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                showLog("Client disconnected.");
            }
        }
    }
}