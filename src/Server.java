import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Server TCP chạy trên localhost để chuyển tiếp thông điệp giữa các client.
 * Server này sử dụng mô hình Singleton và chạy trên port cố định.
 */
public class Server {

    // Port mặc định của server
    private static final int PORT = 5055;
    
    // Instance duy nhất của server (Singleton pattern)
    private static Server instance;

    // Executor service để xử lý các client connections
    private final ExecutorService executor = Executors.newCachedThreadPool();
    
    // Map lưu các client đang kết nối (key = clientId, value = ClientHandler)
    private final Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());
    
    // Callback để ghi log
    private final Consumer<String> logConsumer;
    
    // Trạng thái server (đang chạy hay không)
    private volatile boolean running;

    /**
     * Constructor private (Singleton pattern).
     * @param logConsumer Callback để ghi log
     */
    private Server(Consumer<String> logConsumer) {
        this.logConsumer = logConsumer;
    }

    /**
     * Lấy instance duy nhất của server (Singleton pattern).
     * @param logConsumer Callback để ghi log
     * @return Instance của server
     */
    public static synchronized Server getInstance(Consumer<String> logConsumer) {
        if (instance == null) {
            instance = new Server(logConsumer);
        }
        return instance;
    }

    /**
     * Khởi động server trên port mặc định.
     * Server sẽ lắng nghe các kết nối từ client và tạo ClientHandler cho mỗi client.
     */
    public void start() {
        if (running) {
            log("Server already running on port " + PORT);
            return;
        }
        running = true;
        executor.execute(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                log("Server started on port " + PORT);
                while (running) {
                    // Chờ client kết nối
                    Socket socket = serverSocket.accept();
                    // Xử lý client trong thread riêng
                    executor.execute(() -> registerClient(socket));
                }
            } catch (IOException e) {
                log("Server stopped: " + e.getMessage());
            }
        });
    }

    /**
     * Dừng server và đóng tất cả kết nối.
     */
    public void stop() {
        running = false;
        executor.shutdownNow();
    }

    /**
     * Lấy port mà server đang chạy.
     * @return Port của server
     */
    public int getPort() {
        return PORT;
    }

    /**
     * Đăng ký client mới kết nối.
     * Tạo ClientHandler và thêm vào danh sách clients.
     * @param socket Socket của client
     */
    private void registerClient(Socket socket) {
        // Tạo ID ngẫu nhiên cho client
        String clientId = UUID.randomUUID().toString();
        try {
            ClientHandler handler = new ClientHandler(clientId, socket);
            clients.put(clientId, handler);
            handler.start();
            // Gửi danh sách client mới cho tất cả clients
            broadcastClientList();
            log("Client connected: " + clientId + " [" + socket.getInetAddress() + "]");
        } catch (IOException e) {
            log("Failed to register client: " + e.getMessage());
        }
    }

    /**
     * Gửi danh sách client hiện tại cho tất cả clients.
     * Danh sách được gửi dưới dạng CLIENT_LIST message.
     */
    private void broadcastClientList() {
        StringBuilder builder = new StringBuilder();
        clients.keySet().forEach(id -> builder.append(id).append("\n"));
        NetworkMessage listMessage = NetworkMessage.clientList(builder.toString());
        sendToAll(listMessage);
    }

    /**
     * Gửi thông điệp cho tất cả clients đang kết nối.
     * @param message Thông điệp cần gửi
     */
    private void sendToAll(NetworkMessage message) {
        synchronized (clients) {
            clients.values().forEach(handler -> handler.send(message));
        }
    }

    /**
     * Chuyển tiếp thông điệp từ client này đến client khác.
     * @param message Thông điệp cần chuyển tiếp
     */
    private void route(NetworkMessage message) {
        if (message.getTargetId() == null || message.getTargetId().isBlank()) {
            return;
        }
        // Tìm client nhận
        ClientHandler recipient = clients.get(message.getTargetId());
        if (recipient != null) {
            recipient.send(message);
        }
    }

    /**
     * Xóa client khỏi danh sách khi ngắt kết nối.
     * @param clientId ID của client ngắt kết nối
     */
    private void disconnect(String clientId) {
        clients.remove(clientId);
        // Gửi danh sách client mới cho tất cả clients
        broadcastClientList();
        log("Client disconnected: " + clientId);
    }

    /**
     * Ghi log (gọi callback nếu có, nếu không in ra console).
     * @param text Nội dung log
     */
    private void log(String text) {
        if (logConsumer != null) {
            logConsumer.accept(text);
        } else {
            System.out.println(text);
        }
    }

    /**
     * Lớp xử lý kết nối với một client cụ thể.
     * Mỗi client có một ClientHandler riêng chạy trong thread riêng.
     */
    private class ClientHandler extends Thread {

        private final String clientId;          // ID của client
        private final Socket socket;            // Socket kết nối
        private final ObjectOutputStream output; // Stream để gửi dữ liệu
        private final ObjectInputStream input;   // Stream để nhận dữ liệu

        /**
         * Constructor tạo ClientHandler.
         * @param clientId ID của client
         * @param socket Socket kết nối
         * @throws IOException Nếu không thể tạo streams
         */
        private ClientHandler(String clientId, Socket socket) throws IOException {
            this.clientId = clientId;
            this.socket = socket;
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.input = new ObjectInputStream(socket.getInputStream());
        }

        /**
         * Thread chính xử lý thông điệp từ client.
         * Gửi HELLO message khi kết nối, sau đó lắng nghe và chuyển tiếp thông điệp.
         */
        @Override
        public void run() {
            try {
                // Gửi HELLO message với client ID
                send(NetworkMessage.hello(clientId));
                
                // Lắng nghe thông điệp từ client
                while (!interrupted()) {
                    Object incoming = input.readObject();
                    if (incoming instanceof NetworkMessage message) {
                        switch (message.getType()) {
                            // Chuyển tiếp các thông điệp này đến client khác
                            case TEXT, FILE_OFFER, FILE_ACCEPTED, FILE_REJECTED, FILE_DATA, FILE_COMPLETE -> route(message);
                            // Xử lý ngắt kết nối
                            case DISCONNECT -> {
                                disconnect(clientId);
                                return;
                            }
                            default -> log("Unhandled message type: " + message.getType());
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                log("Connection error with client " + clientId + ": " + e.getMessage());
            } finally {
                // Dọn dẹp khi ngắt kết nối
                disconnect(clientId);
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }

        /**
         * Gửi thông điệp đến client (thread-safe).
         * @param message Thông điệp cần gửi
         */
        private synchronized void send(NetworkMessage message) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                log("Failed to send message to " + clientId + ": " + e.getMessage());
            }
        }
    }
}
