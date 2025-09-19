import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ClientGUI extends JFrame {
    private JTextField hostField, portField;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JList<String> fileList;
    private List<Object> selectedItems = new ArrayList<>();  // Lưu file hoặc folder (File object)
    private Set<String> selectedPaths = new HashSet<>();
    private static final int BUFFER_SIZE = 8192;
    private Logger logger;
    private DecimalFormat df = new DecimalFormat("#.2");

    public ClientGUI() {
        setTitle("TCP File Transfer Client");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout());
        hostField = new JTextField("localhost", 15);
        portField = new JTextField("8080", 8);
        JButton selectFileBtn = new JButton("Thêm File(s)");
        JButton selectDirBtn = new JButton("Thêm Folder");
        JButton sendBtn = new JButton("Gửi");
        JButton clearBtn = new JButton("Xóa List");

        inputPanel.add(new JLabel("Host:"));
        inputPanel.add(hostField);
        inputPanel.add(new JLabel("Port:"));
        inputPanel.add(portField);
        inputPanel.add(selectFileBtn);
        inputPanel.add(selectDirBtn);
        inputPanel.add(sendBtn);
        inputPanel.add(clearBtn);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        add(logScroll, BorderLayout.CENTER);

        fileList = new JList<>();
        fileList.setVisibleRowCount(5);
        JScrollPane fileScroll = new JScrollPane(fileList);
        add(fileScroll, BorderLayout.EAST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        add(progressBar, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.NORTH);

        logger = LogConfig.getLogger("Client", logArea);
        logger.info("Client GUI sẵn sàng. Thêm file/folder và gửi đến server.");

        selectFileBtn.addActionListener(_ -> selectItems(false, false));
        selectDirBtn.addActionListener(_ -> selectItems(true, true));
        sendBtn.addActionListener(_ -> {
            if (selectedItems.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chưa chọn item!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            progressBar.setVisible(true);
            progressBar.setValue(0);
            new Thread(this::sendItems).start();
        });
        clearBtn.addActionListener(_ -> {
            selectedItems.clear();
            selectedPaths.clear();
            updateFileList();
            logger.info("List đã xóa");
        });

        // Double-click để xem contents nếu folder
        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = fileList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Object item = selectedItems.get(index);
                        if (item instanceof File && ((File) item).isDirectory()) {
                            showFolderContents((File) item);
                        }
                    }
                }
            }
        });

        setVisible(true);
    }

    private void selectItems(boolean isDirectory, boolean isFolder) {
        JFileChooser chooser = new JFileChooser();
        if (isDirectory) {
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else {
            chooser.setMultiSelectionEnabled(true);
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            if (isDirectory) {
                File dir = chooser.getSelectedFile();
                addUniqueItem(dir);
            } else {
                File[] files = chooser.getSelectedFiles();
                if (files.length > 0) {
                    for (File f : files) addUniqueItem(f);
                } else if (chooser.getSelectedFile() != null) {
                    addUniqueItem(chooser.getSelectedFile());
                }
            }
            updateFileList();
            logger.info("Đã thêm item");
        }
    }

    private void addUniqueItem(File item) {
        String path = item.getAbsolutePath();
        if (!selectedPaths.contains(path)) {
            selectedItems.add(item);
            selectedPaths.add(path);
        }
    }

    private void updateFileList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (Object item : selectedItems) {
            File f = (File) item;
            long size = getSize(f);
            double sizeMB = size / 1024.0 / 1024.0;
            String type = f.isDirectory() ? " (Folder)" : " (File)";
            model.addElement(f.getName() + type + " (" + df.format(sizeMB) + " MB)");
        }
        fileList.setModel(model);
    }

    private long getSize(File file) {
        if (file.isFile()) {
            return file.length();
        } else {
            long size = 0;
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    size += getSize(f);
                }
            }
            return size;
        }
    }

    private void showFolderContents(File folder) {
        DefaultListModel<String> model = new DefaultListModel<>();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                model.addElement(f.getName());
            }
        }
        JList<String> contentList = new JList<>(model);
        JScrollPane scroll = new JScrollPane(contentList);
        JOptionPane.showMessageDialog(this, scroll, "Files in " + folder.getName(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void sendItems() {
        String host = hostField.getText();
        int port = Integer.parseInt(portField.getText());
        try (Socket socket = new Socket(host, port)) {
            logger.info("Kết nối đến server: " + host + ":" + port);

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(selectedItems.size());

            for (int i = 0; i < selectedItems.size(); i++) {
                File item = (File) selectedItems.get(i);
                boolean isFolder = item.isDirectory();
                String name = item.getName();
                long size = getSize(item);
                dos.writeUTF(name);
                dos.writeLong(size);
                dos.writeBoolean(isFolder);  // Gửi flag là folder hay file

                logger.info("Gửi item " + (i + 1) + ": " + name);

                if (isFolder) {
                    sendFolder(item, dos);
                } else {
                    sendFile(item, dos);
                }
                dos.flush();
                logger.info("Hoàn thành gửi item " + (i + 1));
            }
            dos.close();
            socket.close();
            logger.info("Gửi hoàn tất thành công");
            progressBar.setVisible(false);
        } catch (IOException e) {
            logger.severe("Lỗi gửi: " + e.getMessage());
            progressBar.setVisible(false);
        }
    }

    private void sendFolder(File folder, DataOutputStream dos) throws IOException {
        File[] files = folder.listFiles();
        if (files != null) {
            dos.writeInt(files.length);  // Gửi số file trong folder
            for (File f : files) {
                if (f.isDirectory()) {
                    sendFolder(f, dos);  // Recursion
                } else {
                    sendFile(f, dos);
                }
            }
        }
    }

    private void sendFile(File file, DataOutputStream dos) throws IOException {
        long totalSent = 0;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
                totalSent += bytesRead;
                int progress = (int) ((totalSent * 100) / file.length());
                progressBar.setValue(progress);
                progressBar.setString("Gửi file: " + progress + "%");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI());
    }
}