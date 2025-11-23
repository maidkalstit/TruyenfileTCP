import java.io.Serializable;

/**
 * Lớp đại diện cho các thông điệp được trao đổi giữa client và server.
 * Tất cả các thông điệp mạng đều được serialize để truyền qua TCP.
 */
public class NetworkMessage implements Serializable {

    /**
     * Enum định nghĩa các loại thông điệp trong hệ thống.
     */
    public enum MessageType {
        HELLO,              // Thông điệp chào từ server khi client kết nối
        CLIENT_LIST,        // Danh sách các client đang kết nối
        FILE_OFFER,         // Đề nghị gửi file
        FILE_DATA,          // Dữ liệu file (chunk)
        FILE_COMPLETE,      // Hoàn tất gửi file
        FILE_REJECTED,      // Từ chối nhận file
        FILE_ACCEPTED,      // Chấp nhận nhận file
        TEXT,               // Thông điệp text
        DISCONNECT          // Ngắt kết nối
    }

    private static final long serialVersionUID = 1L;

    // Các trường dữ liệu của thông điệp
    private MessageType type;           // Loại thông điệp
    private String senderId;            // ID của người gửi
    private String targetId;            // ID của người nhận
    private String payloadText;         // Nội dung text (cho TEXT, CLIENT_LIST, etc.)
    private String fileName;            // Tên file (cho FILE_OFFER, FILE_DATA, etc.)
    private long fileSize;              // Kích thước file
    private byte[] fileChunk;           // Dữ liệu file (chunk)
    private int chunkSequence;          // Thứ tự chunk (0, 1, 2, ...)
    private boolean finalChunk;         // Chunk cuối cùng hay không

    /**
     * Tạo thông điệp HELLO từ server gửi cho client mới kết nối.
     * @param clientId ID được gán cho client
     * @return Thông điệp HELLO
     */
    public static NetworkMessage hello(String clientId) {
        NetworkMessage message = new NetworkMessage();
        message.type = MessageType.HELLO;
        message.senderId = clientId;
        return message;
    }

    /**
     * Tạo thông điệp danh sách client.
     * @param payload Danh sách client IDs, mỗi ID trên một dòng
     * @return Thông điệp CLIENT_LIST
     */
    public static NetworkMessage clientList(String payload) {
        NetworkMessage message = new NetworkMessage();
        message.type = MessageType.CLIENT_LIST;
        message.payloadText = payload;
        return message;
    }

    /**
     * Tạo thông điệp text.
     * @param senderId ID người gửi
     * @param targetId ID người nhận
     * @param text Nội dung text
     * @return Thông điệp TEXT
     */
    public static NetworkMessage text(String senderId, String targetId, String text) {
        NetworkMessage message = new NetworkMessage();
        message.type = MessageType.TEXT;
        message.senderId = senderId;
        message.targetId = targetId;
        message.payloadText = text;
        return message;
    }

    /**
     * Tạo thông điệp đề nghị gửi file.
     * @param senderId ID người gửi
     * @param targetId ID người nhận
     * @param fileName Tên file
     * @param fileSize Kích thước file (bytes)
     * @return Thông điệp FILE_OFFER
     */
    public static NetworkMessage fileOffer(String senderId, String targetId, String fileName, long fileSize) {
        NetworkMessage message = new NetworkMessage();
        message.type = MessageType.FILE_OFFER;
        message.senderId = senderId;
        message.targetId = targetId;
        message.fileName = fileName;
        message.fileSize = fileSize;
        return message;
    }

    /**
     * Tạo thông điệp chấp nhận nhận file.
     * @param senderId ID người gửi
     * @param targetId ID người nhận
     * @param fileName Tên file
     * @return Thông điệp FILE_ACCEPTED
     */
    public static NetworkMessage fileAccepted(String senderId, String targetId, String fileName) {
        NetworkMessage message = new NetworkMessage();
        message.type = MessageType.FILE_ACCEPTED;
        message.senderId = senderId;
        message.targetId = targetId;
        message.fileName = fileName;
        return message;
    }

    /**
     * Tạo thông điệp từ chối nhận file.
     * @param senderId ID người gửi
     * @param targetId ID người nhận
     * @param reason Lý do từ chối
     * @return Thông điệp FILE_REJECTED
     */
    public static NetworkMessage fileRejected(String senderId, String targetId, String reason) {
        NetworkMessage message = new NetworkMessage();
        message.type = MessageType.FILE_REJECTED;
        message.senderId = senderId;
        message.targetId = targetId;
        message.payloadText = reason;
        return message;
    }

    /**
     * Tạo thông điệp dữ liệu file (một chunk).
     * @param senderId ID người gửi
     * @param targetId ID người nhận
     * @param fileName Tên file
     * @param fileSize Kích thước file tổng (bytes)
     * @param data Dữ liệu chunk (bytes)
     * @param sequence Thứ tự chunk (bắt đầu từ 0)
     * @param finalChunk Có phải chunk cuối cùng không
     * @return Thông điệp FILE_DATA
     */
    public static NetworkMessage fileData(String senderId, String targetId, String fileName,
                                          long fileSize, byte[] data, int sequence, boolean finalChunk) {
        NetworkMessage message = new NetworkMessage();
        message.type = MessageType.FILE_DATA;
        message.senderId = senderId;
        message.targetId = targetId;
        message.fileName = fileName;
        message.fileSize = fileSize;
        message.fileChunk = data;
        message.chunkSequence = sequence;
        message.finalChunk = finalChunk;
        return message;
    }

    /**
     * Tạo thông điệp hoàn tất gửi file.
     * @param senderId ID người gửi
     * @param targetId ID người nhận
     * @param fileName Tên file
     * @return Thông điệp FILE_COMPLETE
     */
    public static NetworkMessage fileComplete(String senderId, String targetId, String fileName) {
        NetworkMessage message = new NetworkMessage();
        message.type = MessageType.FILE_COMPLETE;
        message.senderId = senderId;
        message.targetId = targetId;
        message.fileName = fileName;
        message.finalChunk = true;
        return message;
    }

    /**
     * Tạo thông điệp ngắt kết nối.
     * @param clientId ID client đang ngắt kết nối
     * @return Thông điệp DISCONNECT
     */
    public static NetworkMessage disconnect(String clientId) {
        NetworkMessage message = new NetworkMessage();
        message.type = MessageType.DISCONNECT;
        message.senderId = clientId;
        return message;
    }

    // Các getter methods

    /**
     * Lấy loại thông điệp.
     * @return Loại thông điệp
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Lấy ID người gửi.
     * @return ID người gửi
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Lấy ID người nhận.
     * @return ID người nhận
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * Lấy nội dung text.
     * @return Nội dung text
     */
    public String getPayloadText() {
        return payloadText;
    }

    /**
     * Lấy tên file.
     * @return Tên file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Lấy kích thước file.
     * @return Kích thước file (bytes)
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Lấy dữ liệu file chunk.
     * @return Dữ liệu chunk (bytes)
     */
    public byte[] getFileChunk() {
        return fileChunk;
    }

    /**
     * Lấy thứ tự chunk.
     * @return Thứ tự chunk (0, 1, 2, ...)
     */
    public int getChunkSequence() {
        return chunkSequence;
    }

    /**
     * Kiểm tra có phải chunk cuối cùng không.
     * @return true nếu là chunk cuối cùng
     */
    public boolean isFinalChunk() {
        return finalChunk;
    }
}
