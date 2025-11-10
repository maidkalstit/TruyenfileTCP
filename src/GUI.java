import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

/**
 * Lớp chính của ứng dụng Swing TCP file transfer.
 * Ứng dụng cho phép nhiều client kết nối với nhau qua server để chia sẻ file.
 */
public class GUI {

    /**
     * Hàm main khởi động ứng dụng.
     * Thiết lập Look and Feel của hệ thống và hiển thị cửa sổ launcher.
     * @param args Tham số dòng lệnh (không sử dụng)
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Sử dụng Look and Feel của hệ thống để giao diện đẹp hơn
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Nếu không thể thiết lập, sử dụng Look and Feel mặc định
            }
            new LauncherFrame().setVisible(true);
        });
    }

    /**
     * Cửa sổ launcher chính - khởi động server và cho phép mở các cửa sổ client.
     * Cửa sổ này hiển thị log của server và trạng thái kết nối.
     */
    private static class LauncherFrame extends JFrame {
        private final Server server;                              // Server instance
        private final DefaultListModel<String> logModel = new DefaultListModel<>();  // Model cho danh sách log
        private final List<ClientWindow> openClients = new CopyOnWriteArrayList<>(); // Danh sách client windows đang mở
        private final JLabel statusLabel = new JLabel();          // Label hiển thị trạng thái

        /**
         * Constructor khởi tạo cửa sổ launcher.
         * Tạo server, thiết lập giao diện và khởi động server.
         */
        LauncherFrame() {
            setTitle("AuroraShare • Local TCP File Transfer");
            setSize(500, 380);
            setLocationRelativeTo(null);  // Đặt cửa sổ ở giữa màn hình
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            // Khởi tạo và khởi động server
            server = Server.getInstance(this::appendLog);
            server.start();

            // Thêm các panel vào cửa sổ
            add(createHeaderPanel(), BorderLayout.NORTH);
            add(createLogPanel(), BorderLayout.CENTER);
            add(createFooterPanel(), BorderLayout.SOUTH);

            appendLog("Ứng dụng sẵn sàng! Nhấn \"Mở cửa sổ Client\" để bắt đầu.");
        }

        /**
         * Tạo panel header với tiêu đề và các nút action.
         * @return Panel header
         */
        private JPanel createHeaderPanel() {
            // Panel với gradient background
            JPanel panel = new GradientPanel(new Color(33, 45, 62), new Color(23, 132, 232));
            panel.setBorder(new EmptyBorder(18, 20, 18, 20));
            panel.setLayout(new BorderLayout());

            JLabel title = new JLabel("AuroraShare");
            title.setFont(new Font("Segoe UI", Font.BOLD, 26));
            title.setForeground(Color.WHITE);

            JLabel subtitle = new JLabel("Chia sẻ file TCP oách xà lách");
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            subtitle.setForeground(new Color(220, 235, 255));

            JPanel textPanel = new JPanel();
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.add(title);
            textPanel.add(Box.createVerticalStrut(6));
            textPanel.add(subtitle);

            panel.add(textPanel, BorderLayout.WEST);

            JPanel actions = new JPanel();
            actions.setOpaque(false);
            actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));

            RoundedButton newClient = new RoundedButton("Mở cửa sổ Client");
            newClient.addActionListener(e -> openClientWindow());
            RoundedButton openDownloads = new RoundedButton("Thư mục tải về");
            openDownloads.setButtonColors(new Color(255, 255, 255, 80),
                    new Color(255, 255, 255, 120),
                    new Color(255, 255, 255, 180));
            openDownloads.setForeground(Color.WHITE);
            openDownloads.addActionListener(this::openDownloadFolder);
            actions.add(newClient);
            actions.add(Box.createVerticalStrut(10));
            actions.add(openDownloads);

            panel.add(actions, BorderLayout.EAST);
            return panel;
        }

        /**
         * Tạo panel hiển thị log của server.
         * @return ScrollPane chứa danh sách log
         */
        private JScrollPane createLogPanel() {
            JList<String> logList = new JList<>(logModel);
            logList.setFont(new Font("Consolas", Font.PLAIN, 13));
            logList.setBackground(new Color(245, 247, 250));
            logList.setForeground(new Color(60, 70, 80));
            logList.setSelectionBackground(new Color(210, 225, 255));
            JScrollPane scrollPane = new JScrollPane(logList);
            scrollPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            return scrollPane;
        }

        /**
         * Tạo panel footer hiển thị trạng thái và nút copy port.
         * @return Panel footer
         */
        private JPanel createFooterPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(12, 16, 12, 16));
            statusLabel.setText("Server PORT: " + server.getPort() + " • Clients đang mở: 0");
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            panel.add(statusLabel, BorderLayout.WEST);

            RoundedButton copyPort = new RoundedButton("Copy PORT");
            copyPort.setButtonColors(new Color(235, 240, 255), new Color(215, 225, 255), new Color(195, 210, 255));
            copyPort.setForeground(new Color(40, 70, 140));
            copyPort.addActionListener(e -> {
                Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                cb.setContents(new StringSelection(String.valueOf(server.getPort())), null);
                appendLog("Đã copy port vào clipboard.");
            });
            panel.add(copyPort, BorderLayout.EAST);
            return panel;
        }

        private void openClientWindow() {
            // Hiển thị dialog chọn port để kết nối
            String portInput = JOptionPane.showInputDialog(
                    this,
                    "Nhập PORT để kết nối (mặc định: " + server.getPort() + "):",
                    "Chọn PORT Server",
                    JOptionPane.QUESTION_MESSAGE
            );
            
            int port = server.getPort();
            if (portInput != null && !portInput.trim().isEmpty()) {
                try {
                    port = Integer.parseInt(portInput.trim());
                    if (port < 1 || port > 65535) {
                        JOptionPane.showMessageDialog(this,
                                "PORT phải trong khoảng 1-65535",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "PORT không hợp lệ. Sử dụng port mặc định: " + server.getPort(),
                            "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE);
                    port = server.getPort();
                }
            }
            
            ClientWindow clientWindow = new ClientWindow(port, this::appendLog, this::refreshClientCount);
            openClients.add(clientWindow);
            clientWindow.setVisible(true);
            refreshClientCount();
        }

        /**
         * Cập nhật số lượng client đang mở và hiển thị trong status label.
         */
        private void refreshClientCount() {
            // Xóa các cửa sổ đã đóng khỏi danh sách
            openClients.removeIf(window -> !window.isDisplayable());
            statusLabel.setText("Server PORT: " + server.getPort() + " • Clients đang mở: " + openClients.size());
        }

        /**
         * Mở thư mục download trong file explorer.
         * @param e Action event (không sử dụng)
         */
        private void openDownloadFolder(ActionEvent e) {
            try {
                File dir = new File(ClientWindow.defaultDownloadDirectory());
                // Tạo thư mục nếu chưa có
                if (!dir.exists() && !dir.mkdirs()) {
                    appendLog("Không thể tạo thư mục tải về: " + dir.getAbsolutePath());
                    return;
                }
                // Mở thư mục bằng ứng dụng mặc định
                Desktop.getDesktop().open(dir);
            } catch (IOException ex) {
                appendLog("Không thể mở thư mục tải về: " + ex.getMessage());
            }
        }

        /**
         * Thêm log vào danh sách log với timestamp.
         * @param text Nội dung log
         */
        private void appendLog(String text) {
            SwingUtilities.invokeLater(() -> {
                // Thêm timestamp vào log
                logModel.addElement(String.format("[%s] %s",
                        new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()), text));
            });
        }
    }

    /**
     * A client window representing a single user instance.
     */
    private static class ClientWindow extends JFrame {
        private final DefaultComboBoxModel<String> clientComboModel = new DefaultComboBoxModel<>();
        private final JTextArea previewTextArea = new JTextArea();
        private final JLabel previewImageLabel = new JLabel("", JLabel.CENTER);
        private final CardLayout previewCardLayout = new CardLayout();
        private final JPanel previewContentPanel = new JPanel(previewCardLayout);
        private final JTextArea activityArea = new JTextArea();
        private final JLabel clientIdLabel = new JLabel("Đang kết nối...");
        private final JLabel statusLabel = new JLabel("Chưa chọn file");
        private final JProgressBar progressBar = new JProgressBar(0, 100);
        private final RoundedButton sendButton = new RoundedButton("Gửi Ngay");
        private final RoundedButton selectFileButton = new RoundedButton("Chọn File");
        private final RoundedButton livePreviewButton = new RoundedButton("Xem trước");
        private final Theme[] themes = Theme.values();
        private int themeIndex = 0;
        private File selectedFile;
        private File lastReceivedFile;
        private Client client;
        @SuppressWarnings("unused") // Used in WindowAdapter.windowClosed
        private final Runnable onClose;
        private final Consumer<String> serverLogCallback;
        private GradientPanel rootPanel;
        private RoundedPanel headerCard;
        private RoundedPanel previewCard;
        private RoundedPanel activityCard;
        private RoundedPanel bottomCard;
        private JLabel hintLabel;
        private JLabel previewLabel;
        private JComboBox<String> clientCombo;
        private RoundedButton openFileButton;

        ClientWindow(int serverPort, Consumer<String> serverLogCallback, Runnable onClose) {
            this.onClose = onClose;
            this.serverLogCallback = serverLogCallback;
            setTitle("Client chưa định danh");
            setSize(720, 520);
            setLocationByPlatform(true);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setLayout(new BorderLayout());
            setMinimumSize(new Dimension(640, 480));

            rootPanel = new GradientPanel(new Color(245, 246, 255), new Color(209, 216, 255));
            JPanel content = rootPanel;
            content.setLayout(new BorderLayout(16, 16));
            content.setBorder(new EmptyBorder(20, 20, 20, 20));

            content.add(createTopPanel(), BorderLayout.NORTH);
            content.add(createCenterPanel(), BorderLayout.CENTER);
            content.add(createBottomPanel(), BorderLayout.SOUTH);

            add(content);

            try {
                client = new Client("127.0.0.1",
                        serverPort,
                        this::handleMessage,
                        this::log,
                        progress -> SwingUtilities.invokeLater(() -> updateProgress(progress)),
                        defaultDownloadDirectory());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Không thể kết nối tới server: " + e.getMessage(),
                        "Lỗi kết nối",
                        JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    try {
                        client.close();
                    } catch (IOException ignored) {
                    }
                    onClose.run();
                }
            });

            // Enable drag and drop for the entire window
            setupDragAndDrop(rootPanel);

            applyTheme(themes[themeIndex]);
        }

        private JPanel createTopPanel() {
            headerCard = new RoundedPanel(new Color(255, 255, 255, 230));
            headerCard.setLayout(new BorderLayout(12, 12));
            headerCard.setBorder(new EmptyBorder(16, 16, 16, 16));

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

            clientIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            clientIdLabel.setForeground(new Color(32, 52, 74));
            hintLabel = new JLabel("Chọn client cần gửi ở bên phải, xem trước nội dung trước khi gửi.");
            hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            hintLabel.setForeground(new Color(80, 90, 115));

            left.add(clientIdLabel);
            left.add(Box.createVerticalStrut(6));
            left.add(hintLabel);

            JPanel right = new JPanel();
            right.setOpaque(false);
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.add(new JLabel("Gửi tới client:"));
            clientCombo = new JComboBox<>(clientComboModel);
            clientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            clientCombo.setBackground(Color.WHITE);
            clientCombo.setMaximumSize(new Dimension(220, 36));
            right.add(clientCombo);
            right.add(Box.createVerticalStrut(8));

            RoundedButton themeToggle = new RoundedButton("Đổi Bộ Giao Diện");
            themeToggle.setButtonColors(new Color(245, 245, 245),
                    new Color(225, 225, 225),
                    new Color(205, 205, 205));
            themeToggle.setForeground(new Color(60, 60, 60));
            themeToggle.addActionListener(e -> cycleTheme());
            right.add(themeToggle);

            headerCard.add(left, BorderLayout.CENTER);
            headerCard.add(right, BorderLayout.EAST);

            selectFileButton.addActionListener(e -> selectFile());
            sendButton.addActionListener(e -> sendSelectedFile((String) clientCombo.getSelectedItem()));
            livePreviewButton.addActionListener(e -> showPreviewDialog());

            return headerCard;
        }

        private JPanel createCenterPanel() {
            JPanel panel = new JPanel(new GridLayout(1, 2, 16, 16));
            panel.setOpaque(false);

            previewCard = new RoundedPanel(new Color(255, 255, 255, 230));
            previewCard.setLayout(new BorderLayout(12, 12));
            previewCard.setBorder(new EmptyBorder(16, 16, 16, 16));
            previewLabel = new JLabel("Xem trước nội dung");
            previewLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            previewLabel.setForeground(new Color(32, 52, 74));
            
            // Setup text area
            previewTextArea.setEditable(false);
            previewTextArea.setFont(new Font("Consolas", Font.PLAIN, 13));
            previewTextArea.setLineWrap(true);
            previewTextArea.setWrapStyleWord(true);
            JScrollPane textScroll = new JScrollPane(previewTextArea);
            textScroll.setBorder(BorderFactory.createEmptyBorder());
            
            // Setup image label
            previewImageLabel.setHorizontalAlignment(JLabel.CENTER);
            previewImageLabel.setVerticalAlignment(JLabel.CENTER);
            JScrollPane imageScroll = new JScrollPane(previewImageLabel);
            imageScroll.setBorder(BorderFactory.createEmptyBorder());
            
            // Add both to card layout
            previewContentPanel.add(textScroll, "TEXT");
            previewContentPanel.add(imageScroll, "IMAGE");
            previewCardLayout.show(previewContentPanel, "TEXT");
            
            JPanel previewHeader = new JPanel(new BorderLayout());
            previewHeader.setOpaque(false);
            previewHeader.add(previewLabel, BorderLayout.CENTER);
            previewCard.add(previewHeader, BorderLayout.NORTH);
            previewCard.add(previewContentPanel, BorderLayout.CENTER);
            
            JPanel previewButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            previewButtons.setOpaque(false);
            previewButtons.add(livePreviewButton);
            openFileButton = new RoundedButton("Mở file");
            openFileButton.setButtonColors(new Color(240, 248, 255), new Color(220, 238, 255), new Color(200, 228, 255));
            openFileButton.setForeground(new Color(40, 100, 180));
            openFileButton.addActionListener(e -> openReceivedFile());
            openFileButton.setEnabled(false);
            previewButtons.add(openFileButton);
            previewCard.add(previewButtons, BorderLayout.SOUTH);

            activityCard = new RoundedPanel(new Color(255, 255, 255, 230));
            activityCard.setLayout(new BorderLayout(12, 12));
            activityCard.setBorder(new EmptyBorder(16, 16, 16, 16));
            JLabel activityLabel = new JLabel("Hoạt động");
            activityLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            activityLabel.setForeground(new Color(32, 52, 74));
            activityArea.setEditable(false);
            activityArea.setFont(new Font("Consolas", Font.PLAIN, 12));
            activityArea.setLineWrap(true);
            activityArea.setWrapStyleWord(true);
            JScrollPane activityScroll = new JScrollPane(activityArea);
            activityScroll.setBorder(BorderFactory.createEmptyBorder());

            activityCard.add(activityLabel, BorderLayout.NORTH);
            activityCard.add(activityScroll, BorderLayout.CENTER);

            panel.add(previewCard);
            panel.add(activityCard);
            return panel;
        }

        private JPanel createBottomPanel() {
            bottomCard = new RoundedPanel(new Color(255, 255, 255, 230));
            bottomCard.setBorder(new EmptyBorder(18, 18, 18, 18));
            bottomCard.setLayout(new BoxLayout(bottomCard, BoxLayout.Y_AXIS));

            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            statusLabel.setForeground(new Color(60, 70, 90));
            progressBar.setStringPainted(true);
            progressBar.setFont(new Font("Segoe UI", Font.BOLD, 13));
            progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            JPanel buttonRow = new JPanel();
            buttonRow.setOpaque(false);
            buttonRow.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));
            RoundedButton resetButton = new RoundedButton("Làm mới");
            resetButton.setButtonColors(new Color(250, 250, 250), new Color(230, 230, 230), new Color(210, 210, 210));
            resetButton.setForeground(new Color(60, 60, 60));
            resetButton.addActionListener(e -> resetSelection());

            buttonRow.add(selectFileButton);
            buttonRow.add(sendButton);
            buttonRow.add(resetButton);

            bottomCard.add(statusLabel);
            bottomCard.add(Box.createVerticalStrut(8));
            bottomCard.add(progressBar);
            bottomCard.add(Box.createVerticalStrut(12));
            bottomCard.add(buttonRow);
            return bottomCard;
        }

        /**
         * Xử lý thông điệp nhận được từ server.
         * Cập nhật UI tương ứng với từng loại thông điệp.
         * @param message Thông điệp nhận được
         */
        private void handleMessage(NetworkMessage message) {
            SwingUtilities.invokeLater(() -> {
                switch (message.getType()) {
                    case HELLO -> {
                        // Nhận ID từ server và cập nhật UI
                        clientIdLabel.setText("Client ID: " + message.getSenderId());
                        setTitle("Client • " + message.getSenderId());
                        log("Kết nối thành công đến server trên 127.0.0.1");
                    }
                    case CLIENT_LIST -> updateClientList(message);  // Cập nhật danh sách client
                    case FILE_OFFER -> showIncomingOffer(message);  // Hiển thị thông báo file đến
                    case FILE_DATA -> log("Đang nhận file: " + message.getFileName() + " (" + message.getChunkSequence() + ")");
                    case FILE_COMPLETE -> {
                        // File nhận xong, cập nhật UI và hiển thị preview
                        progressBar.setValue(100);
                        progressBar.setString("Đã nhận xong " + message.getFileName());
                        log("Hoàn tất nhận file: " + message.getFileName());
                        statusLabel.setText("Đã nhận file: " + message.getFileName());
                        displayReceivedFile(message.getFileName());  // Tự động hiển thị file đã nhận
                    }
                    case TEXT -> log(message.getPayloadText());
                    default -> log("Nhận message: " + message.getType());
                }
            });
        }

        /**
         * Cập nhật danh sách client trong combo box.
         * Loại bỏ client hiện tại khỏi danh sách và giữ nguyên selection nếu có thể.
         * @param message Thông điệp CLIENT_LIST chứa danh sách client IDs
         */
        private void updateClientList(NetworkMessage message) {
            Object previous = clientCombo != null ? clientCombo.getSelectedItem() : null;
            // Parse danh sách client IDs từ message
            List<String> ids = Arrays.stream(message.getPayloadText().split("\n"))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .collect(Collectors.toList());
            String currentId = client != null ? client.getClientId() : null;
            // Xóa danh sách cũ
            clientComboModel.removeAllElements();
            // Thêm các client khác (không bao gồm client hiện tại)
            ids.stream()
                    .filter(id -> !Objects.equals(id, currentId))
                    .forEach(clientComboModel::addElement);
            // Nếu không có client nào, hiển thị thông báo
            if (clientComboModel.getSize() == 0) {
                clientComboModel.addElement("Chưa có client khác");
            } else if (previous != null) {
                // Giữ nguyên selection nếu có thể
                for (int i = 0; i < clientComboModel.getSize(); i++) {
                    if (previous.equals(clientComboModel.getElementAt(i))) {
                        clientCombo.setSelectedIndex(i);
                        return;
                    }
                }
                clientCombo.setSelectedIndex(0);
            }
        }

        private void showIncomingOffer(NetworkMessage message) {
            String text = "<html><b>" + message.getSenderId() + "</b> gửi cho bạn file <b>"
                    + message.getFileName() + "</b> (" + readableBytes(message.getFileSize()) + ").</html>";
            log("Nhận đề nghị gửi file từ " + message.getSenderId() + ": " + message.getFileName());
            JOptionPane.showMessageDialog(this, text, "File tới", JOptionPane.INFORMATION_MESSAGE);
        }

        private void updateProgress(double value) {
            int percent = (int) Math.round(value * 100);
            progressBar.setValue(percent);
            progressBar.setString(percent + "%");
        }

        /**
         * Thiết lập chức năng kéo thả file vào component.
         * Cho phép người dùng kéo file từ file explorer vào ứng dụng.
         * @param component Component để kích hoạt drag and drop
         */
        private void setupDragAndDrop(Component component) {
            new DropTarget(component, new DropTargetAdapter() {
                @Override
                public void drop(DropTargetDropEvent dtde) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        Transferable transferable = dtde.getTransferable();
                        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                            @SuppressWarnings("unchecked")
                            List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                            if (files != null && !files.isEmpty()) {
                                File file = files.get(0);
                                if (file.isFile()) {
                                    selectedFile = file;
                                    lastReceivedFile = null;
                                    previewLabel.setText("File đã chọn: " + file.getName());
                                    statusLabel.setText("Đã chọn: " + file.getName() + " (" + readableBytes(file.length()) + ")");
                                    progressBar.setValue(0);
                                    progressBar.setString("");
                                    loadPreview(file);
                                    openFileButton.setEnabled(false);
                                    log("Đã kéo thả file: " + file.getName());
                                }
                            }
                        }
                        dtde.dropComplete(true);
                    } catch (UnsupportedFlavorException | IOException e) {
                        dtde.dropComplete(false);
                        log("Lỗi kéo thả file: " + e.getMessage());
                    }
                }
            });
        }

        /**
         * Enum định nghĩa các loại file.
         */
        private enum FileType {
            IMAGE,      // File hình ảnh (jpg, png, gif, etc.)
            TEXT,       // File text (txt, md, java, etc.)
            BINARY,     // File nhị phân (docx, xlsx, pdf, etc.)
            CSV         // File CSV
        }

        /**
         * Xác định loại file dựa trên extension.
         * @param file File cần kiểm tra
         * @return Loại file
         */
        private FileType getFileType(File file) {
            String fileName = file.getName().toLowerCase();
            String extension = "";
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0) {
                extension = fileName.substring(lastDot + 1);
            }
            
            // Image types
            if (extension.matches("jpg|jpeg|png|gif|bmp|webp|svg|ico")) {
                return FileType.IMAGE;
            }
            // Text types
            if (extension.matches("txt|md|java|json|xml|html|css|js|py|cpp|c|h|hpp|cs|php|rb|sh|bat|log|ini|cfg|conf|yml|yaml")) {
                return FileType.TEXT;
            }
            // CSV
            if (extension.equals("csv")) {
                return FileType.CSV;
            }
            // Binary (docx, xlsx, pdf, etc.)
            return FileType.BINARY;
        }

        /**
         * Load và hiển thị preview file.
         * Tự động xác định loại file và gọi method tương ứng để hiển thị.
         * @param file File cần preview
         */
        private void loadPreview(File file) {
            try {
                if (!file.exists() || !file.isFile()) {
                    showFileInfo(file, "File không tồn tại");
                    return;
                }

                long size = file.length();
                FileType fileType = getFileType(file);

                // Xử lý theo loại file
                switch (fileType) {
                    case IMAGE -> loadImagePreview(file, size);
                    case TEXT -> loadTextPreview(file, size);
                    case CSV -> loadCsvPreview(file, size);
                    case BINARY -> showFileInfo(file, "File nhị phân (docx, xlsx, pdf, etc.). Nhấn 'Mở file' để xem.");
                }
            } catch (Exception e) {
                showFileInfo(file, "Lỗi đọc file: " + e.getMessage());
            }
        }

        /**
         * Load và hiển thị preview hình ảnh.
         * Tự động scale hình ảnh để vừa với vùng preview.
         * @param file File hình ảnh
         * @param size Kích thước file (bytes)
         */
        private void loadImagePreview(File file, long size) {
            try {
                BufferedImage image = ImageIO.read(file);
                if (image == null) {
                    showFileInfo(file, "Không thể đọc hình ảnh");
                    return;
                }
                
                // Scale hình ảnh để vừa với vùng preview
                int maxWidth = 600;
                int maxHeight = 400;
                int imgWidth = image.getWidth();
                int imgHeight = image.getHeight();
                
                // Tính tỷ lệ scale để vừa với kích thước tối đa
                double scale = Math.min(1.0, Math.min((double) maxWidth / imgWidth, (double) maxHeight / imgHeight));
                int scaledWidth = (int) (imgWidth * scale);
                int scaledHeight = (int) (imgHeight * scale);
                
                // Scale hình ảnh
                Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);
                previewImageLabel.setIcon(icon);
                previewImageLabel.setText("");
                // Hiển thị panel hình ảnh
                previewCardLayout.show(previewContentPanel, "IMAGE");
                log("Đã tải hình ảnh: " + file.getName() + " (" + imgWidth + "x" + imgHeight + ", " + readableBytes(size) + ")");
            } catch (IOException e) {
                showFileInfo(file, "Lỗi đọc hình ảnh: " + e.getMessage());
            }
        }

        /**
         * Load và hiển thị preview file text.
         * Thử đọc với UTF-8, nếu thất bại thì thử ISO-8859-1.
         * @param file File text
         * @param size Kích thước file (bytes)
         */
        private void loadTextPreview(File file, long size) {
            try {
                // Giới hạn file lớn hơn 5MB
                if (size > 5 * 1024 * 1024) {
                    showFileInfo(file, "File quá lớn (" + readableBytes(size) + "). Vui lòng mở bằng trình soạn thảo.");
                    return;
                }
                // Thử đọc với UTF-8
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                previewTextArea.setText(String.join("\n", lines));
                previewCardLayout.show(previewContentPanel, "TEXT");
                log("Đã tải file text: " + file.getName() + " (" + lines.size() + " dòng)");
            } catch (IOException e) {
                // Nếu thất bại, thử với ISO-8859-1
                try {
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    String content = new String(bytes, StandardCharsets.ISO_8859_1);
                    previewTextArea.setText(content);
                    previewCardLayout.show(previewContentPanel, "TEXT");
                    log("Đã tải file text (ISO-8859-1): " + file.getName());
                } catch (IOException e2) {
                    showFileInfo(file, "Không thể đọc file text: " + e.getMessage());
                }
            }
        }

        private void loadCsvPreview(File file, long size) {
            try {
                if (size > 2 * 1024 * 1024) {
                    showFileInfo(file, "File CSV quá lớn (" + readableBytes(size) + "). Vui lòng mở bằng Excel.");
                    return;
                }
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                // Show first 100 lines
                int maxLines = Math.min(100, lines.size());
                String preview = String.join("\n", lines.subList(0, maxLines));
                if (lines.size() > maxLines) {
                    preview += "\n\n... (" + (lines.size() - maxLines) + " dòng còn lại)";
                }
                previewTextArea.setText(preview);
                previewCardLayout.show(previewContentPanel, "TEXT");
                log("Đã tải file CSV: " + file.getName() + " (" + lines.size() + " dòng)");
            } catch (IOException e) {
                showFileInfo(file, "Không thể đọc file CSV: " + e.getMessage());
            }
        }

        private void showFileInfo(File file, String message) {
            String info = "Tên file: " + file.getName() + "\n";
            info += "Kích thước: " + readableBytes(file.length()) + "\n";
            info += "Loại: " + getFileType(file) + "\n\n";
            if (message != null && !message.isEmpty()) {
                info += message + "\n\n";
            }
            info += "Nhấn nút 'Mở file' để mở bằng ứng dụng mặc định.";
            previewTextArea.setText(info);
            previewImageLabel.setIcon(null);
            previewImageLabel.setText("");
            previewCardLayout.show(previewContentPanel, "TEXT");
        }

        /**
         * Mở dialog chọn file để gửi.
         * Sau khi chọn file, tự động load preview và cập nhật UI.
         */
        private void selectFile() {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setApproveButtonText("Chọn");
            chooser.setDialogTitle("Chọn file cần gửi");
            // Thêm filter cho các loại file phổ biến (không bắt buộc)
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("Tài liệu (txt, md, java, json)", "txt", "md", "java", "json"));
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
                lastReceivedFile = null;  // Xóa file đã nhận nếu có
                previewLabel.setText("File đã chọn: " + selectedFile.getName());
                statusLabel.setText("Đã chọn: " + selectedFile.getName() + " (" + readableBytes(selectedFile.length()) + ")");
                progressBar.setValue(0);
                progressBar.setString("");
                loadPreview(selectedFile);  // Tự động preview file đã chọn
                openFileButton.setEnabled(false);
            }
        }

        /**
         * Hiển thị file đã nhận trong preview area.
         * Sử dụng retry mechanism để đợi file được ghi hoàn toàn.
         * @param fileName Tên file đã nhận
         */
        private void displayReceivedFile(String fileName) {
            File downloadDir = new File(defaultDownloadDirectory());
            File receivedFile = new File(downloadDir, fileName);
            
            // Cơ chế retry: đợi một chút để file được ghi hoàn toàn
            new Thread(() -> {
                int retries = 10;
                while (retries > 0 && (!receivedFile.exists() || receivedFile.length() == 0)) {
                    try {
                        Thread.sleep(100);
                        retries--;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                SwingUtilities.invokeLater(() -> {
                    if (receivedFile.exists() && receivedFile.isFile() && receivedFile.length() > 0) {
                        lastReceivedFile = receivedFile;
                        selectedFile = null;
                        previewLabel.setText("File đã nhận: " + fileName);
                        loadPreview(receivedFile);
                        openFileButton.setEnabled(true);
                        log("Đã hiển thị file đã nhận: " + fileName + " (" + readableBytes(receivedFile.length()) + ")");
                    } else {
                        previewLabel.setText("Xem trước nội dung");
                        previewTextArea.setText("Đang chờ file: " + fileName + "...\nNếu file không xuất hiện, vui lòng kiểm tra thư mục tải về.");
                        previewCardLayout.show(previewContentPanel, "TEXT");
                        openFileButton.setEnabled(false);
                        log("Chưa tìm thấy file: " + fileName);
                    }
                });
            }, "FileDisplayWaiter").start();
        }

        private void openReceivedFile() {
            if (lastReceivedFile != null && lastReceivedFile.exists()) {
                try {
                    Desktop.getDesktop().open(lastReceivedFile);
                    log("Đã mở file: " + lastReceivedFile.getName());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this,
                            "Không thể mở file: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    log("Lỗi mở file: " + e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy file để mở.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void showPreviewDialog() {
            File fileToPreview = selectedFile != null ? selectedFile : lastReceivedFile;
            if (fileToPreview == null) {
                JOptionPane.showMessageDialog(this, "Không có file để xem trước.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            JPanel panel = new JPanel(new BorderLayout(12, 12));
            String title = selectedFile != null ? "File đã chọn" : "File đã nhận";
            panel.add(new JLabel(title + ": " + fileToPreview.getName()), BorderLayout.NORTH);
            
            FileType fileType = getFileType(fileToPreview);
            if (fileType == FileType.IMAGE) {
                try {
                    BufferedImage image = ImageIO.read(fileToPreview);
                    if (image != null) {
                        JLabel imgLabel = new JLabel(new ImageIcon(image));
                        JScrollPane imgScroll = new JScrollPane(imgLabel);
                        imgScroll.setPreferredSize(new Dimension(800, 600));
                        panel.add(imgScroll, BorderLayout.CENTER);
                    }
                } catch (IOException e) {
                    panel.add(new JLabel("Không thể đọc hình ảnh: " + e.getMessage()), BorderLayout.CENTER);
                }
            } else {
                JTextArea area = new JTextArea(previewTextArea.getText());
                area.setEditable(false);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setFont(new Font("Consolas", Font.PLAIN, 13));
                JScrollPane scrollPane = new JScrollPane(area);
                scrollPane.setPreferredSize(new Dimension(560, 420));
                panel.add(scrollPane, BorderLayout.CENTER);
            }

            JOptionPane.showMessageDialog(this, panel, "Xem trước nội dung", JOptionPane.PLAIN_MESSAGE);
        }

        private void sendSelectedFile(String targetId) {
            if (selectedFile == null || !selectedFile.exists()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn file trước khi gửi.", "Thiếu file", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (targetId == null || targetId.startsWith("Chưa")) {
                JOptionPane.showMessageDialog(this, "Hiện chưa có client khác để gửi file.", "Không tìm thấy người nhận", JOptionPane.WARNING_MESSAGE);
                return;
            }
            client.getClientIdSafe().ifPresent(sender -> {
                log("Đang gửi file tới " + targetId + ": " + selectedFile.getName());
                progressBar.setValue(5);
                progressBar.setString("Đang gửi...");
                client.sendFile(targetId, selectedFile, chunk -> SwingUtilities.invokeLater(() -> {
                    double ratio = (double) chunk / Math.max(1, Math.ceil((double) selectedFile.length() / (1024 * 32)));
                    updateProgress(ratio);
                }));
                statusLabel.setText("Đã gửi: " + selectedFile.getName());
            });
        }

        private void resetSelection() {
            selectedFile = null;
            lastReceivedFile = null;
            previewTextArea.setText("");
            previewImageLabel.setIcon(null);
            previewImageLabel.setText("");
            previewCardLayout.show(previewContentPanel, "TEXT");
            previewLabel.setText("Xem trước nội dung");
            statusLabel.setText("Chưa chọn file");
            progressBar.setValue(0);
            progressBar.setString("");
            openFileButton.setEnabled(false);
        }

        private void cycleTheme() {
            themeIndex = (themeIndex + 1) % themes.length;
            applyTheme(themes[themeIndex]);
        }

        private void applyTheme(Theme theme) {
            Color cardColor = asOpaque(theme.cardBackground);
            if (rootPanel != null) {
                rootPanel.setColors(theme.backgroundStart, theme.backgroundEnd);
            }
            if (headerCard != null) {
                headerCard.setPanelBackground(theme.cardBackground);
            }
            if (previewCard != null) {
                previewCard.setPanelBackground(theme.cardBackground);
            }
            if (activityCard != null) {
                activityCard.setPanelBackground(theme.cardBackground);
            }
            if (bottomCard != null) {
                bottomCard.setPanelBackground(theme.cardBackground);
            }
            sendButton.setButtonColors(theme.primary, theme.primaryHover, theme.primaryPressed);
            selectFileButton.setButtonColors(theme.secondary, theme.secondaryHover, theme.secondaryPressed);
            livePreviewButton.setButtonColors(theme.accent, theme.accentHover, theme.accentPressed);
            sendButton.setForeground(theme.primaryText);
            selectFileButton.setForeground(theme.secondaryText);
            livePreviewButton.setForeground(theme.accentText);
            activityArea.setBackground(cardColor);
            previewTextArea.setBackground(cardColor);
            previewContentPanel.setBackground(cardColor);
            activityArea.setForeground(theme.bodyText);
            previewTextArea.setForeground(theme.bodyText);
            clientIdLabel.setForeground(theme.titleText);
            statusLabel.setForeground(theme.bodyText);
            if (previewLabel != null) {
                previewLabel.setForeground(theme.titleText);
            }
            if (hintLabel != null) {
                hintLabel.setForeground(theme.bodyText);
            }
            if (clientCombo != null) {
                clientCombo.setBackground(cardColor);
                clientCombo.setForeground(theme.bodyText);
            }
            progressBar.setForeground(theme.primary);
            progressBar.setBackground(new Color(240, 240, 240));
        }

        private void log(String message) {
            SwingUtilities.invokeLater(() -> {
                activityArea.append("• " + message + "\n");
                activityArea.setCaretPosition(activityArea.getDocument().getLength());
            });
            serverLogCallback.accept("[" + clientIdLabel.getText() + "] " + message);
        }

        private Color asOpaque(Color color) {
            if (color.getAlpha() == 255) {
                return color;
            }
            return new Color(color.getRed(), color.getGreen(), color.getBlue());
        }

        static String readableBytes(long bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            }
            double kb = bytes / 1024.0;
            double mb = kb / 1024.0;
            DecimalFormat df = new DecimalFormat("#,##0.##");
            if (mb >= 1) {
                return df.format(mb) + " MB";
            }
            return df.format(kb) + " KB";
        }

        static String defaultDownloadDirectory() {
            return System.getProperty("user.home") + File.separator + "AuroraShareDownloads";
        }
    }

    /**
     * Theme definitions for modern look.
     */
    private enum Theme {
        AURORA(
                new Color(79, 127, 255), new Color(69, 117, 245), new Color(59, 107, 235), Color.WHITE,
                new Color(241, 244, 255), new Color(225, 234, 255), new Color(210, 224, 255), new Color(35, 65, 120),
                new Color(46, 196, 182), new Color(56, 206, 192), new Color(36, 176, 162), Color.WHITE,
                new Color(255, 255, 255, 235), new Color(33, 45, 62), new Color(60, 70, 90),
                new Color(245, 246, 255), new Color(209, 216, 255)),
        MIDNIGHT(
                new Color(52, 201, 235), new Color(42, 191, 225), new Color(32, 181, 215), new Color(12, 24, 38),
                new Color(34, 48, 68), new Color(44, 58, 78), new Color(24, 38, 58), Color.WHITE,
                new Color(255, 179, 71), new Color(255, 189, 91), new Color(235, 159, 51), new Color(35, 25, 15),
                new Color(24, 32, 48, 235), new Color(230, 238, 255), new Color(190, 199, 220),
                new Color(8, 12, 24), new Color(14, 31, 54)),
        SUNSET(
                new Color(255, 136, 102), new Color(245, 126, 92), new Color(235, 116, 82), Color.WHITE,
                new Color(255, 241, 230), new Color(255, 231, 220), new Color(255, 221, 210), new Color(120, 65, 45),
                new Color(255, 208, 0), new Color(255, 218, 30), new Color(245, 198, 0), new Color(90, 60, 0),
                new Color(255, 245, 235, 235), new Color(120, 60, 40), new Color(110, 70, 60),
                new Color(255, 235, 220), new Color(255, 200, 180));

        final Color primary;
        final Color primaryHover;
        final Color primaryPressed;
        final Color primaryText;
        final Color secondary;
        final Color secondaryHover;
        final Color secondaryPressed;
        final Color secondaryText;
        final Color accent;
        final Color accentHover;
        final Color accentPressed;
        final Color accentText;
        final Color cardBackground;
        final Color titleText;
        final Color bodyText;
        final Color backgroundStart;
        final Color backgroundEnd;

        Theme(Color primary, Color primaryHover, Color primaryPressed, Color primaryText,
              Color secondary, Color secondaryHover, Color secondaryPressed, Color secondaryText,
              Color accent, Color accentHover, Color accentPressed, Color accentText,
              Color cardBackground, Color titleText, Color bodyText,
              Color backgroundStart, Color backgroundEnd) {
            this.primary = primary;
            this.primaryHover = primaryHover;
            this.primaryPressed = primaryPressed;
            this.primaryText = primaryText;
            this.secondary = secondary;
            this.secondaryHover = secondaryHover;
            this.secondaryPressed = secondaryPressed;
            this.secondaryText = secondaryText;
            this.accent = accent;
            this.accentHover = accentHover;
            this.accentPressed = accentPressed;
            this.accentText = accentText;
            this.cardBackground = cardBackground;
            this.titleText = titleText;
            this.bodyText = bodyText;
            this.backgroundStart = backgroundStart;
            this.backgroundEnd = backgroundEnd;
        }
    }

    /**
     * Simple gradient background panel.
     */
    private static class GradientPanel extends JPanel {
        private Color start;
        private Color end;

        GradientPanel(Color start, Color end) {
            this.start = start;
            this.end = end;
            setOpaque(false);
        }

        void setColors(Color start, Color end) {
            this.start = start;
            this.end = end;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, start, 0, getHeight(), end));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Rounded panel used for cards.
     */
    private static class RoundedPanel extends JPanel {
        RoundedPanel(Color background) {
            setOpaque(false);
            setBackground(background);
        }

        void setPanelBackground(Color background) {
            setBackground(background);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
