import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClientGUI extends JFrame {

    private final JTextArea chatArea = new JTextArea();
    private final JTextField messageField = new JTextField();
    private final JButton sendButton = new JButton("Send");

    private PrintWriter out;
    private BufferedReader in;

    public ChatClientGUI() {
        setTitle("Chat Client");
        setSize(450, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // chat messages area
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // bottom panel: text field + send button
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // send on button click or Enter key
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        connectToServer();
        setVisible(true);
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            chatArea.append("Connected to server!\n");

            // thread that receives messages from the server
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String text = message;
                        SwingUtilities.invokeLater(() -> chatArea.append(text + "\n"));
                    }
                } catch (IOException e) {
                    // connection closed
                }
                SwingUtilities.invokeLater(() -> chatArea.append("Disconnected from server.\n"));
            }).start();

        } catch (IOException e) {
            chatArea.append("Could not connect to server.\n");
            messageField.setEnabled(false);
            sendButton.setEnabled(false);
        }
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty() || out == null) {
            return;
        }
        out.println(text);
        chatArea.append("Me: " + text + "\n");
        messageField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}