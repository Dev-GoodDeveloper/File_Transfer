import java.io.*;
import java.net.Socket;

public class FileTransfer {

    public static void sendFile(String filePath, String friendIP, int friendPort) {
        try (Socket socket = new Socket(friendIP, friendPort);
             FileInputStream fileInput = new FileInputStream(filePath);
             OutputStream out = socket.getOutputStream()) {

            File file = new File(filePath);
            byte[] fileNameBytes = file.getName().getBytes();
            byte[] fileContentBytes = fileInput.readAllBytes();

            out.write(fileNameBytes.length);
            out.write(fileNameBytes);

            out.write(fileContentBytes.length);
            out.write(fileContentBytes);

            System.out.println("File sent successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void receiveFile(Socket socket) {
        try (InputStream in = socket.getInputStream()) {

            int fileNameLength = in.read();
            byte[] fileNameBytes = new byte[fileNameLength];
            in.read(fileNameBytes);
            String fileName = new String(fileNameBytes);

            int fileContentLength = in.read();
            byte[] fileContentBytes = new byte[fileContentLength];
            in.read(fileContentBytes);

            // Save received file
            File receivedFile = new File("received_" + fileName);
            try (FileOutputStream fileOut = new FileOutputStream(receivedFile)) {
                fileOut.write(fileContentBytes);
            }

            System.out.println("File received: " + receivedFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
