import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ClientGUI extends JFrame {
    private JTextField hostField, portField;
    private JTextArea logArea;
    private List<File> selectedFiles = new ArrayList<>();
    private static final int BUFFER_SIZE = 1024;
    private Logger logger;

    public ClientGUI() {
        setTitle("TCP File Transfer Client");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout());
        hostField = new JTextField("localhost", 15);
        portField = new JTextField("8080", 8);
        JButton selectBtn = new JButton("Chọn File(s)");
        JButton sendBtn = new JButton("Gửi File");

        inputPanel.add(new JLabel("Host:"));
        inputPanel.add(hostField);
        inputPanel.add(new JLabel("Port:"));
        inputPanel.add(portField);
        inputPanel.add(selectBtn);
        inputPanel.add(sendBtn);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(inputPanel, BorderLayout.NORTH);

        logger = LogConfig.getLogger("Client", logArea);
        logger.info("Client GUI sẵn sàng. Chọn file và gửi đến server.");

        selectBtn.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFiles.clear();
                File[] files = chooser.getSelectedFiles();
                if (files.length > 0) {
                    for (File f : files) selectedFiles.add(f);
                } else if (chooser.getSelectedFile() != null) {
                    selectedFiles.add(chooser.getSelectedFile());
                }
                logger.info("Đã chọn " + selectedFiles.size() + " file(s)");
            }
        });

        sendBtn.addActionListener(_ -> {
            if (selectedFiles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chưa chọn file!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Thread(() -> sendFiles()).start();
        });

        setVisible(true);
    }

    private void sendFiles() {
        String host = hostField.getText();
        int port = Integer.parseInt(portField.getText());
        try (Socket socket = new Socket(host, port)) {
            logger.info("Kết nối đến server: " + host + ":" + port);

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(selectedFiles.size());

            for (int i = 0; i < selectedFiles.size(); i++) {
                File file = selectedFiles.get(i);
                dos.writeUTF(file.getName());
                dos.writeLong(file.length());

                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                fis.close();
                logger.info("Hoàn thành gửi file " + (i + 1));
            }
            dos.close();
            socket.close();
        } catch (IOException e) {
            logger.severe("Lỗi gửi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI());
    }
}