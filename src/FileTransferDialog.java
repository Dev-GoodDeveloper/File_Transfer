import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class FileTransferDialog extends JDialog implements ActionListener {
    JTextField filePathField;
    JComboBox<String> friendComboBox;
    Map<String, FriendInfo> friendsMap;

    public FileTransferDialog(Map<String, FriendInfo> friendsMap) {
        this.friendsMap = friendsMap;

        setTitle("Send File");
        setSize(300, 150);
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new GridLayout(3, 1));

        JPanel filePanel = new JPanel();
        filePathField = new JTextField(20);
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> chooseFile());
        filePanel.add(new JLabel("File:"));
        filePanel.add(filePathField);
        filePanel.add(browseButton);

        JPanel friendPanel = new JPanel();
        friendComboBox = new JComboBox<>(friendsMap.keySet().toArray(new String[0]));
        friendPanel.add(new JLabel("Friend:"));
        friendPanel.add(friendComboBox);

        JPanel buttonPanel = new JPanel();
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(this);
        buttonPanel.add(sendButton);

        add(filePanel);
        add(friendPanel);
        add(buttonPanel);
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            filePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String filePath = filePathField.getText().trim();
        String friendName = (String) friendComboBox.getSelectedItem();

        if (!filePath.isEmpty() && friendName != null) {
            FriendInfo friendInfo = friendsMap.get(friendName);
            if (friendInfo != null) {
                FileTransfer.sendFile(filePath, friendInfo.getIpAddress(), friendInfo.getPort());
                JOptionPane.showMessageDialog(this, "File sent to " + friendName);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a file and a friend.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
