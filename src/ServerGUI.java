import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private JProgressBar progressBar;
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 8192;
    private Logger logger;
    private ExecutorService executor = Executors.newFixedThreadPool(5);
    private DecimalFormat df = new DecimalFormat("#.2");

    public ServerGUI() {
        setTitle("TCP File Transfer Server");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        add(logScroll, BorderLayout.CENTER);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        add(progressBar, BorderLayout.SOUTH);

        JPanel controlPanel = new JPanel();
        JButton startBtn = new JButton("Khởi Động Server");
        startBtn.addActionListener(_ -> startServer());
        controlPanel.add(startBtn);
        add(controlPanel, BorderLayout.NORTH);

        logger = LogConfig.getLogger("Server", logArea);
        logger.info("Server GUI sẵn sàng. Nhấn 'Khởi Động Server' để bắt đầu.");

        setVisible(true);
    }

    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                logger.info("Server đang lắng nghe trên port " + PORT);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Kết nối từ client: " + clientSocket.getInetAddress());
                    executor.submit(() -> handleClient(clientSocket));
                }
            } catch (IOException e) {
                logger.severe("Lỗi server: " + e.getMessage());
            }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        try {
            progressBar.setVisible(true);
            progressBar.setValue(0);

            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            int numItems = dis.readInt();
            logger.info("Sẵn sàng nhận " + numItems + " item(s)");

            for (int i = 0; i < numItems; i++) {
                String itemName = dis.readUTF();
                long itemSize = dis.readLong();
                boolean isFolder = dis.readBoolean();

                double sizeMB = itemSize / 1024.0 / 1024.0;
                String type = isFolder ? "Folder" : "File";
                logger.info("Nhận " + type + " " + (i + 1) + ": " + itemName + " (" + df.format(sizeMB) + " MB)");

                String receivedPath = "received_" + itemName;
                if (isFolder) {
                    new File(receivedPath).mkdirs();  // Tạo folder
                    receiveFolder(dis, receivedPath, itemSize);  // Nhận recursion
                } else {
                    receiveFile(dis, receivedPath, itemSize);
                }
                logger.info("Hoàn thành nhận " + type + " " + (i + 1));
            }
            dis.close();
            clientSocket.close();
            logger.info("Phiên kết nối kết thúc");
            progressBar.setVisible(false);
        } catch (IOException e) {
            logger.severe("Lỗi xử lý client: " + e.getMessage());
            progressBar.setVisible(false);
        }
    }

    private void receiveFolder(DataInputStream dis, String basePath, long folderSize) throws IOException {
        int numFiles = dis.readInt();
        long totalRead = 0;
        for (int j = 0; j < numFiles; j++) {
            String subName = dis.readUTF();
            long subSize = dis.readLong();
            boolean isSubFolder = dis.readBoolean();

            String subPath = basePath + "/" + subName;
            if (isSubFolder) {
                new File(subPath).mkdirs();
                receiveFolder(dis, subPath, subSize);
            } else {
                receiveFile(dis, subPath, subSize);
            }
            totalRead += subSize;
            int progress = (int) ((totalRead * 100) / folderSize);
            progressBar.setValue(progress);
            progressBar.setString("Nhận folder: " + progress + "%");
        }
    }

    private void receiveFile(DataInputStream dis, String path, long fileSize) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalRead = 0;
        int bytesRead;
        while (totalRead < fileSize) {
            bytesRead = dis.read(buffer, 0, (int) Math.min(BUFFER_SIZE, fileSize - totalRead));
            if (bytesRead == -1) break;
            fos.write(buffer, 0, bytesRead);
            totalRead += bytesRead;
            int progress = (int) ((totalRead * 100) / fileSize);
            progressBar.setValue(progress);
            progressBar.setString("Nhận file: " + progress + "%");
        }
        fos.close();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerGUI());
    }
}