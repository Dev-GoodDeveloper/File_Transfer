import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Chat extends JFrame implements ActionListener {

    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    JTextArea chatArea;
    JTextField inputField;
    DefaultListModel<String> friendListModel;
    JList<String> friendList;
    String selectedFriend;

    Map<String, FriendInfo> friendsMap = new HashMap<>();

    public Chat() {
        setSize((int) (d.getWidth() / 2), (int) (d.getHeight() / 2));
        setTitle("ChatApp");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initComponent();
        startServer();
    }

    private void initComponent() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(0, 51, 51));
        add(mainPanel);

        JPanel topBar = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("CHAT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe Print", Font.BOLD, 14));
        JButton friendButton = new JButton("ADD Friends");
        friendButton.setFont(new Font("Segoe Print", Font.BOLD, 14));
        friendButton.setActionCommand("add friends");
        friendButton.addActionListener(this);
        topBar.add(titleLabel, BorderLayout.CENTER);
        topBar.add(friendButton, BorderLayout.EAST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        friendListModel = new DefaultListModel<>();
        friendList = new JList<>(friendListModel);
        friendList.setFont(new Font("Segoe Print", Font.BOLD, 14));
        friendList.addListSelectionListener(e -> loadChat());
        JScrollPane friendScrollPanel = new JScrollPane(friendList);
        friendScrollPanel.setPreferredSize(new Dimension(150, getHeight()));
        friendScrollPanel.setBackground(new Color(204, 204, 204));

        JPanel friendListPanel = new JPanel(new BorderLayout());
        friendListPanel.setBackground(new Color(204, 204, 204));
        friendListPanel.add(friendScrollPanel, BorderLayout.CENTER);
        JLabel label = new JLabel("Friend List", SwingConstants.CENTER);
        label.setFont(new Font("Segoe Print", Font.BOLD, 14));
        friendListPanel.add(label, BorderLayout.NORTH);
        mainPanel.add(friendListPanel, BorderLayout.EAST);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe Print", Font.BOLD, 14));
        inputPanel.add(inputField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe Print", Font.BOLD, 14));
        sendButton.setActionCommand("send");
        sendButton.addActionListener(this);

        JButton fileButton = new JButton("Send File");
        fileButton.setFont(new Font("Segoe Print", Font.BOLD, 14));
        fileButton.setActionCommand("sendFile");
        fileButton.addActionListener(this);

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe Print", Font.BOLD, 14));
        closeButton.setActionCommand("close");
        closeButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendButton);
        buttonPanel.add(fileButton);
        buttonPanel.add(closeButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        Chat f = new Chat();
        f.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equalsIgnoreCase("send")) {
            sendMessage();
        } else if (cmd.equalsIgnoreCase("sendFile")) {
            sendFile();
        } else if (cmd.equalsIgnoreCase("close")) {
            System.exit(0);
        } else if (cmd.equalsIgnoreCase("add friends")) {
            Add_Friend addFriendFrame = new Add_Friend(friendListModel, friendsMap);
            addFriendFrame.setVisible(true);
        }
    }

    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(2080)) {
                while (true) {
                    System.out.println("Server is waiting for incoming messages...");
                    try (Socket connectionSocket = serverSocket.accept();
                         DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream())) {

                        String messageType = inFromClient.readUTF();
                        if (messageType.equals("TEXT")) {
                            String sentence = inFromClient.readUTF();
                            chatArea.append("Friend: " + sentence + "\n");
                        } else if (messageType.equals("FILE")) {
                            String fileName = inFromClient.readUTF();
                            long fileSize = inFromClient.readLong();
                            File file = new File(fileName);
                            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                long bytesReceived = 0;
                                while (bytesReceived < fileSize) {
                                    bytesRead = inFromClient.read(buffer);
                                    fileOut.write(buffer, 0, bytesRead);
                                    bytesReceived += bytesRead;
                                }
                                chatArea.append("Friend sent a file: " + fileName + "\n");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You cannot send an empty message!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedFriend == null) {
            JOptionPane.showMessageDialog(this, "No friend selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FriendInfo friendInfo = friendsMap.get(selectedFriend);
        if (friendInfo == null) {
            JOptionPane.showMessageDialog(this, "Friend information not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String friendIP = friendInfo.getIpAddress();
        int friendPort = friendInfo.getPort();

        try (Socket clientSocket = new Socket(friendIP, friendPort);
             DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream())) {

            outToServer.writeUTF("TEXT");
            outToServer.writeUTF(message);
            chatArea.append("You: " + message + "\n");
            saveChat(message);
            inputField.setText("");

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to send message!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName();
            long fileSize = selectedFile.length();

            if (selectedFriend == null) {
                JOptionPane.showMessageDialog(this, "No friend selected!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            FriendInfo friendInfo = friendsMap.get(selectedFriend);
            if (friendInfo == null) {
                JOptionPane.showMessageDialog(this, "Friend information not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String friendIP = friendInfo.getIpAddress();
            int friendPort = friendInfo.getPort();

            try (Socket clientSocket = new Socket(friendIP, friendPort);
                 DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                 FileInputStream fileInputStream = new FileInputStream(selectedFile)) {

                outToServer.writeUTF("FILE");
                outToServer.writeUTF(fileName);
                outToServer.writeLong(fileSize);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outToServer.write(buffer, 0, bytesRead);
                }

                chatArea.append("You sent a file: " + fileName + "\n");

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to send file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadChat() {
        selectedFriend = friendList.getSelectedValue();
        if (selectedFriend != null) {
            chatArea.setText("");
            Path chatFilePath = Path.of(selectedFriend + ".txt");
            if (Files.exists(chatFilePath)) {
                try (BufferedReader reader = new BufferedReader(new FileReader(chatFilePath.toFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        chatArea.append(line + "\n");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void saveChat(String message) {
        Path chatFilePath = Path.of(selectedFriend + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chatFilePath.toFile(), true))) {
            writer.write("You: " + message);
            writer.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

class FriendInfo {
    private String ipAddress;
    private int port;

    public FriendInfo(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }
}
