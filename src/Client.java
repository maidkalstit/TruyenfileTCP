import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Lớp Client xử lý kết nối và giao tiếp với server cho một instance UI.
 * Client này quản lý việc gửi/nhận file, nhận thông điệp và cập nhật UI.
 */
public class Client implements Closeable {

    // Executor service để xử lý gửi file trong thread riêng
    private final ExecutorService sendExecutor = Executors.newSingleThreadExecutor();
    
    // Callback để xử lý thông điệp nhận được
    private final Consumer<NetworkMessage> messageConsumer;
    
    // Callback để ghi log
    private final Consumer<String> logConsumer;
    
    // Callback để cập nhật tiến trình (0.0 - 1.0)
    private final Consumer<Double> progressConsumer;
    
    // Thư mục lưu file nhận được
    private final String downloadDirectory;

    // Socket kết nối với server
    private Socket socket;
    
    // Stream để gửi dữ liệu đến server
    private ObjectOutputStream outputStream;
    
    // Stream để nhận dữ liệu từ server
    private ObjectInputStream inputStream;
    
    // ID của client (được server gán khi kết nối)
    private String clientId;
    
    // Map lưu kích thước file mong đợi (key = senderId|fileName)
    private final Map<String, Long> expectedBytes = new ConcurrentHashMap<>();
    
    // Map lưu số bytes đã nhận (key = senderId|fileName)
    private final Map<String, Long> receivedBytes = new ConcurrentHashMap<>();
    
    // Thread lắng nghe thông điệp từ server
    private Thread listenerThread;

    /**
     * Constructor tạo client và kết nối với server.
     * @param host Địa chỉ server (thường là "127.0.0.1" cho localhost)
     * @param port Port của server
     * @param messageConsumer Callback xử lý thông điệp nhận được
     * @param logConsumer Callback ghi log
     * @param progressConsumer Callback cập nhật tiến trình
     * @param downloadDirectory Thư mục lưu file nhận được
     * @throws IOException Nếu không thể kết nối với server
     */
    public Client(String host,
                  int port,
                  Consumer<NetworkMessage> messageConsumer,
                  Consumer<String> logConsumer,
                  Consumer<Double> progressConsumer,
                  String downloadDirectory) throws IOException {
        this.messageConsumer = messageConsumer;
        this.logConsumer = logConsumer;
        this.progressConsumer = progressConsumer;
        this.downloadDirectory = downloadDirectory;
        connect(host, port);
    }

    /**
     * Lấy ID của client (có thể null nếu chưa nhận HELLO từ server).
     * @return ID của client
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Kết nối với server và khởi động thread lắng nghe.
     * @param host Địa chỉ server
     * @param port Port của server
     * @throws IOException Nếu không thể kết nối
     */
    private void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
        listenerThread = new Thread(this::listen, "AuroraShare-Listener-" + hashCode());
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Thread lắng nghe thông điệp từ server.
     * Xử lý các loại thông điệp: HELLO, FILE_OFFER, FILE_DATA, và các thông điệp khác.
     */
    private void listen() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Object payload = inputStream.readObject();
                if (payload instanceof NetworkMessage message) {
                    if (message.getType() == NetworkMessage.MessageType.HELLO) {
                        // Nhận ID từ server
                        clientId = message.getSenderId();
                        log("Assigned client id: " + clientId);
                    } else if (message.getType() == NetworkMessage.MessageType.FILE_OFFER) {
                        // Khởi tạo tracking cho file sắp nhận
                        String key = createFileKey(message);
                        expectedBytes.put(key, message.getFileSize());
                        receivedBytes.put(key, 0L);
                        messageConsumer.accept(message);
                    } else if (message.getType() == NetworkMessage.MessageType.FILE_DATA) {
                        // Xử lý chunk file
                        handleIncomingFileChunk(message);
                    } else {
                        // Chuyển tiếp các thông điệp khác đến UI
                        messageConsumer.accept(message);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            log("Connection lost: " + e.getMessage());
            messageConsumer.accept(NetworkMessage.text("SYSTEM", clientId, "Disconnected: " + e.getMessage()));
        }
    }

    /**
     * Gửi thông điệp đến server (thread-safe).
     * @param message Thông điệp cần gửi
     */
    public synchronized void send(NetworkMessage message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            log("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Gửi file đến client khác qua server.
     * File được chia thành các chunk 32KB và gửi tuần tự.
     * @param targetId ID của client nhận
     * @param file File cần gửi
     * @param onProgressChunk Callback được gọi mỗi khi gửi xong một chunk
     */
    public void sendFile(String targetId, File file, Consumer<Integer> onProgressChunk) {
        Objects.requireNonNull(file, "File must not be null");
        sendExecutor.submit(() -> {
            try {
                long fileSize = file.length();
                // Gửi FILE_OFFER để thông báo sắp gửi file
                send(NetworkMessage.fileOffer(clientId, targetId, file.getName(), fileSize));
                
                // Đọc toàn bộ file vào memory
                byte[] allBytes = Files.readAllBytes(file.toPath());
                
                // Chia file thành các chunk 32KB
                int chunkSize = 1024 * 32;
                int totalChunks = (int) Math.ceil((double) allBytes.length / chunkSize);
                
                // Gửi từng chunk
                for (int i = 0; i < totalChunks; i++) {
                    int start = i * chunkSize;
                    int end = Math.min(start + chunkSize, allBytes.length);
                    byte[] chunk = new byte[end - start];
                    System.arraycopy(allBytes, start, chunk, 0, chunk.length);
                    boolean finalChunk = i == totalChunks - 1;
                    
                    // Gửi chunk
                    send(NetworkMessage.fileData(clientId, targetId, file.getName(), fileSize, chunk, i, finalChunk));
                    
                    // Cập nhật tiến trình
                    if (onProgressChunk != null) {
                        onProgressChunk.accept(i + 1);
                    }
                }
                
                // Gửi thông báo hoàn tất
                send(NetworkMessage.fileComplete(clientId, targetId, file.getName()));
                log("File sent: " + file.getName());
            } catch (IOException e) {
                log("Failed to send file: " + e.getMessage());
            }
        });
    }

    /**
     * Lấy ID của client một cách an toàn (trả về Optional).
     * @return Optional chứa client ID nếu có
     */
    public Optional<String> getClientIdSafe() {
        return Optional.ofNullable(clientId);
    }

    /**
     * Xử lý một chunk file nhận được.
     * Ghi chunk vào file và cập nhật tiến trình.
     * @param message Thông điệp FILE_DATA chứa chunk
     */
    private void handleIncomingFileChunk(NetworkMessage message) {
        // Tạo thư mục download nếu chưa có
        File directory = new File(downloadDirectory);
        if (!directory.exists() && !directory.mkdirs()) {
            log("Cannot create download directory: " + downloadDirectory);
            return;
        }
        
        File targetFile = new File(directory, message.getFileName());
        String key = createFileKey(message);
        
        // Nếu là chunk đầu tiên và file đã tồn tại, xóa file cũ
        if (message.getChunkSequence() == 0 && targetFile.exists()) {
            if (!targetFile.delete()) {
                log("Cannot overwrite file: " + targetFile.getAbsolutePath());
            }
        }
        
        // Ghi chunk vào file (append mode)
        try (FileOutputStream fos = new FileOutputStream(targetFile, true)) {
            fos.write(message.getFileChunk());
            
            // Cập nhật tiến trình
            if (progressConsumer != null) {
                long received = receivedBytes.compute(key, (k, v) -> 
                    v == null ? message.getFileChunk().length : v + message.getFileChunk().length);
                long expected = expectedBytes.getOrDefault(key, message.getFileSize());
                if (expected > 0) {
                    double progress = Math.min(1d, (double) received / expected);
                    progressConsumer.accept(progress);
                }
            }
            
            // Nếu là chunk cuối cùng, dọn dẹp tracking
            if (message.isFinalChunk()) {
                log("File received: " + targetFile.getAbsolutePath());
                expectedBytes.remove(key);
                receivedBytes.remove(key);
            }
        } catch (IOException e) {
            log("Error writing file: " + e.getMessage());
        }
        
        // Chuyển tiếp thông điệp đến UI
        messageConsumer.accept(message);
    }

    /**
     * Đóng kết nối với server và dọn dẹp resources.
     * @throws IOException Nếu có lỗi khi đóng socket
     */
    @Override
    public void close() throws IOException {
        sendExecutor.shutdownNow();
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        if (socket != null && !socket.isClosed()) {
            if (clientId != null) {
                send(NetworkMessage.disconnect(clientId));
            }
            socket.close();
        }
    }

    /**
     * Ghi log (gọi callback nếu có).
     * @param text Nội dung log
     */
    private void log(String text) {
        if (logConsumer != null) {
            logConsumer.accept(text);
        }
    }

    /**
     * Tạo key để tracking file (senderId|fileName).
     * @param message Thông điệp chứa thông tin file
     * @return Key để tracking
     */
    private String createFileKey(NetworkMessage message) {
        return message.getSenderId() + "|" + message.getFileName();
    }
}
