import java.io.*;
import java.net.*;  // Cho ServerSocket, Socket
import java.util.logging.Logger;  // Cho Logger

public class Server {
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 1024;
    private static final Logger logger = LogConfig.getLogger("Server");

    public static void main(String[] args) {
        logger.info("Server khởi động, lắng nghe trên port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Socket clientSocket = serverSocket.accept();
            logger.info("Kết nối từ client: " + clientSocket.getInetAddress());

            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            int numFiles = dis.readInt();
            logger.info("Sẵn sàng nhận " + numFiles + " file(s)");

            for (int i = 0; i < numFiles; i++) {
                try {
                    String fileName = dis.readUTF();
                    long fileSize = dis.readLong();
                    logger.info("Bắt đầu nhận file " + (i + 1) + ": " + fileName + " (kích thước: " + fileSize + " bytes)");

                    FileOutputStream fos = new FileOutputStream("received_" + fileName); // Prefix để tránh ghi đè
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
                } catch (IOException e) {
                    logger.severe("Lỗi nhận file " + (i + 1) + ": " + e.getMessage());
                    // Vẫn tiếp tục file tiếp theo
                }
            }
            dis.close();
            clientSocket.close();
            logger.info("Phiên kết nối kết thúc thành công");
        } catch (IOException e) {
            logger.severe("Lỗi server chính: " + e.getMessage());
            e.printStackTrace();
        }
    }
}