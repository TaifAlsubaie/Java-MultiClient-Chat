import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;


public class ChatClient extends JFrame {

    JTextArea chatArea = new JTextArea();
    JTextField messageField = new JTextField();
    JButton sendButton = new JButton("Send");
    PrintWriter out;
    BufferedReader in;
    String username;
    public ChatClient() {
        setTitle("Chat Client");
        setSize(450, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        connectToServer();
        setVisible(true);
    }

    void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 5000);

in = new BufferedReader(
    new InputStreamReader(socket.getInputStream())
);

out = new PrintWriter(socket.getOutputStream(), true);

// Ask for username
username = javax.swing.JOptionPane.showInputDialog(
    this,
    "Enter your name:"
);

if (username == null || username.trim().isEmpty()) {
    chatArea.append("Username is required.\n");
    socket.close();
    messageField.setEnabled(false);
    sendButton.setEnabled(false);
    return;
}

// Send username to server
username = username.trim();
out.println(username);

chatArea.append("Connected as " + username + "\n");

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String text = message;
                        SwingUtilities.invokeLater(() -> chatArea.append(text + "\n"));
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> chatArea.append("Disconnected from server.\n"));
                }
            }).start();

        } catch (IOException e) {
            chatArea.append("Connection failed: " + e.getMessage() + "\n");
            messageField.setEnabled(false);
            sendButton.setEnabled(false);
        }
    }

    void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        out.println(text);
        chatArea.append("You: " + text + "\n");
        messageField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }
}