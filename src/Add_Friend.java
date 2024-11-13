import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class Add_Friend extends JDialog implements ActionListener {

    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    JTextField inputField, ipField, portField;
    JButton sendButton, clearButton;
    DefaultListModel<String> friendListModel;
    Map<String, FriendInfo> friendsMap;

    public Add_Friend(DefaultListModel<String> friendListModel, Map<String, FriendInfo> friendsMap) {
        this.friendListModel = friendListModel;
        this.friendsMap = friendsMap;

        setSize((int) (d.getWidth() / 4), (int) (d.getHeight() / 5));
        setTitle("Add Friend");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);

        initComponent();
    }

    private void initComponent() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        JLabel nameLabel = new JLabel("Friend's Name");
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setFont(new Font("Segoe Print", Font.BOLD, 14));
        mainPanel.add(nameLabel, BorderLayout.NORTH);

        inputField = new JTextField();
        inputField.setBackground(Color.WHITE);
        inputField.setFont(new Font("Segoe Print", Font.BOLD, 14));
        mainPanel.add(inputField, BorderLayout.CENTER);

        JLabel ipLabel = new JLabel("Friend's IP");
        ipField = new JTextField();
        ipField.setBackground(Color.WHITE);
        ipField.setFont(new Font("Segoe Print", Font.BOLD, 14));

        JLabel portLabel = new JLabel("Friend's Port");
        portField = new JTextField();
        portField.setBackground(Color.WHITE);
        portField.setFont(new Font("Segoe Print", Font.BOLD, 14));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(nameLabel);
        inputPanel.add(inputField);
        inputPanel.add(ipLabel);
        inputPanel.add(ipField);
        inputPanel.add(portLabel);
        inputPanel.add(portField);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        sendButton = new JButton("Add");
        sendButton.setBackground(new Color(0, 122, 204));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Segoe Print", Font.BOLD, 14));
        sendButton.setActionCommand("add");
        sendButton.addActionListener(this);
        buttonPanel.add(sendButton);

        clearButton = new JButton("Clear");
        clearButton.setBackground(new Color(0, 122, 204));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setFont(new Font("Segoe Print", Font.BOLD, 14));
        clearButton.setActionCommand("clear");
        clearButton.addActionListener(this);
        buttonPanel.add(clearButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equalsIgnoreCase("add")) {
            String friendName = inputField.getText().trim();
            String friendIP = ipField.getText().trim();
            int friendPort = Integer.parseInt(portField.getText().trim());
            if (!friendName.isEmpty() && !friendIP.isEmpty() && friendPort > 0) {
                friendListModel.addElement(friendName);
                friendsMap.put(friendName, new FriendInfo(friendIP, friendPort));
                inputField.setText("");
                ipField.setText("");
                portField.setText("");
            }
        } else if (cmd.equalsIgnoreCase("clear")) {
            inputField.setText("");
            ipField.setText("");
            portField.setText("");
        }
    }
}
