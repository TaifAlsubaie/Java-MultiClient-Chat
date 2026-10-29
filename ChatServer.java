import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ChatServer {

    private static final CopyOnWriteArrayList<PrintWriter> clients =
        new CopyOnWriteArrayList<>();

    
    private static JTextArea logArea;
    private static JLabel statusLabel;
    private static JLabel clientsLabel;
    private static int connectedClients = 0;

    public static void main(String[] args) {

        
        SwingUtilities.invokeLater(() -> createGUI());

        
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            private final StringBuilder line = new StringBuilder();

            @Override
            public synchronized void write(int b) throws IOException {
                originalOut.write(b);

                if (b == '\n') {
                    String message = line.toString();
                    line.setLength(0);
                    showLog(message);
                } else if (b != '\r') {
                    line.append((char) b);
                }
            }

            @Override
            public void flush() throws IOException {
                originalOut.flush();
            }
        }, true, StandardCharsets.UTF_8));

        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is running on port " + port);
            System.out.println("Waiting for clients...");

            while (true) {

                Socket clientSocket = serverSocket.accept();

                System.out.println("New client connected: "
                        + clientSocket.getInetAddress());

                new Thread(new ClientHandler(clientSocket)).start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    
    private static void createGUI() {
        JFrame frame = new JFrame("Chat Server Dashboard");
        frame.setSize(650, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        statusLabel = new JLabel("Server Status: Starting...");
        clientsLabel = new JLabel("Connected Clients: 0");

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        infoPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(statusLabel);
        infoPanel.add(clientsLabel);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(logArea);

        JButton stopButton = new JButton("Stop Server");
        stopButton.addActionListener(e -> System.exit(0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(stopButton);

        frame.add(infoPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    
    private static void showLog(String message) {
        SwingUtilities.invokeLater(() -> {
            if (logArea != null) {
                logArea.append(message + "\n");
                logArea.setCaretPosition(
                        logArea.getDocument().getLength());
            }

            if (statusLabel != null) {
                if (message.contains("Server is running")) {
                    statusLabel.setText("Server Status: Running");
                } else if (message.contains("Server error")) {
                    statusLabel.setText("Server Status: Error");
                }
            }

            if (message.contains("New client connected")) {
                connectedClients++;
                updateClientCount();
            } else if (message.contains("Client disconnected")) {
                connectedClients = Math.max(0, connectedClients - 1);
                updateClientCount();
            }
        });
    }

    private static void updateClientCount() {
        if (clientsLabel != null) {
            clientsLabel.setText(
                    "Connected Clients: " + connectedClients);
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

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
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