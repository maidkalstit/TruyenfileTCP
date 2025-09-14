import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.logging.Logger;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 1024;
    private Logger logger;

    public ServerGUI() {
        setTitle("TCP File Transfer Server");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton startBtn = new JButton("Khởi Động Server");
        startBtn.addActionListener(_ -> {
            startServer();
        });
        controlPanel.add(startBtn);
        add(controlPanel, BorderLayout.SOUTH);

        logger = LogConfig.getLogger("Server", logArea);
        logger.info("Server GUI sẵn sàng. Nhấn 'Khởi Động Server' để bắt đầu.");

        setVisible(true);
    }

    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                logger.info("Server đang lắng nghe trên port " + PORT);
                Socket clientSocket = serverSocket.accept();
                logger.info("Kết nối từ client: " + clientSocket.getInetAddress());

                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                int numFiles = dis.readInt();
                logger.info("Sẵn sàng nhận " + numFiles + " file(s)");

                for (int i = 0; i < numFiles; i++) {
                    String fileName = dis.readUTF();
                    long fileSize = dis.readLong();
                    logger.info("Bắt đầu nhận file " + (i + 1) + ": " + fileName + " (kích thước: " + fileSize + " bytes)");

                    FileOutputStream fos = new FileOutputStream("received_" + fileName);
                    byte[] buffer = new byte[BUFFER_SIZE];
                    long totalRead = 0;
                    int bytesRead;
                    while (totalRead < fileSize) {
                        bytesRead = dis.read(buffer, 0, (int) Math.min(BUFFER_SIZE, fileSize - totalRead));
                        if (bytesRead == -1) break;
                        fos.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;
                    }
                    fos.close();
                    logger.info("Hoàn thành file " + (i + 1) + ": " + fileName + " (nhận " + totalRead + " bytes)");
                }
                dis.close();
                clientSocket.close();
                logger.info("Phiên kết nối kết thúc thành công");
            } catch (IOException e) {
                logger.severe("Lỗi server: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerGUI());
    }
}