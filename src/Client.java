import java.io.*;
import java.net.*;  // Thêm cho Socket và exceptions
import java.util.logging.Logger;  // Thêm cho Logger

public class Client {
    private static final String HOST = "localhost"; // Thay bằng IP nếu test LAN
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 1024;
    private static final Logger logger = LogConfig.getLogger("Client");

    public static void main(String[] args) {
        // Hardcode danh sách file để test; sau có thể dùng args hoặc GUI
        String[] filePaths = {"test1.txt", "test2.txt"}; // Tạo file này trước khi chạy
        logger.info("Client khởi động, chuẩn bị gửi " + filePaths.length + " file(s) đến " + HOST + ":" + PORT);

        try (Socket socket = new Socket(HOST, PORT)) {
            logger.info("Kết nối thành công đến server");
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(filePaths.length); // Gửi số lượng file

            for (int i = 0; i < filePaths.length; i++) {
                String filePath = filePaths[i];
                File file = new File(filePath);  // Bây giờ File resolve OK
                if (!file.exists()) {
                    logger.severe("Lỗi: File không tồn tại - " + filePath);
                    continue; // Bỏ qua file lỗi, gửi file khác
                }

                dos.writeUTF(file.getName());
                dos.writeLong(file.length());
                logger.info("Bắt đầu gửi file " + (i + 1) + ": " + filePath + " (kích thước: " + file.length() + " bytes)");

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                    dos.flush();
                    logger.info("Hoàn thành gửi file " + (i + 1) + ": " + filePath);
                } catch (IOException e) {
                    logger.severe("Lỗi gửi file " + (i + 1) + ": " + e.getMessage());
                }
            }
            dos.close();
            socket.close();
            logger.info("Tất cả file đã gửi (có thể có lỗi ở một số file)");
        } catch (UnknownHostException e) {
            logger.severe("Lỗi: Không tìm thấy host " + HOST + " - Kiểm tra mạng");
        } catch (ConnectException e) {
            logger.severe("Lỗi: Không kết nối được đến server - Chạy server trước!");
        } catch (IOException e) {
            logger.severe("Lỗi kết nối chung: " + e.getMessage());
            e.printStackTrace();
        }
    }
}