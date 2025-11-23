<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
     Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
    TRUYỀN FILE QUA GIAO THỨC TCP
</h2>
<div align="center">
    <p align="center">
        <img alt="AIoTLab Logo" width="170" src="https://github.com/user-attachments/assets/711a2cd8-7eb4-4dae-9d90-12c0a0a208a2" />
        <img alt="AIoTLab Logo" width="180" src="https://github.com/user-attachments/assets/dc2ef2b8-9a70-4cfa-9b4b-f6c2f25f1660" />
        <img alt="DaiNam University Logo" width="200" src="https://github.com/user-attachments/assets/77fe0fd1-2e55-4032-be3c-b1a705a1b574" />
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

---
## 1. Giới thiệu hệ thống
Hệ thống AuroraShare được xây dựng nhằm hỗ trợ người dùng chia sẻ file và thông điệp text giữa các client qua server trung tâm một cách an toàn và đáng tin cậy bằng giao thức TCP. Ứng dụng này minh họa các khái niệm cơ bản trong môn Lập Trình Mạng, chẳng hạn như kết nối socket, luồng dữ liệu (stream), chunking dữ liệu, và xử lý file trong môi trường mạng cục bộ.

Các chức năng chính:

Client:

Kết nối đến server qua TCP Socket và nhận ID duy nhất.

Gửi đề nghị file (FILE_OFFER), truyền file theo chunk (32KB/chunk) với sequence number.

Nhận file tự động và lưu vào thư mục download (mặc định ~/AuroraShareDownloads).

Hiển thị preview file (text/image), progress bar, và activity log cho thành công hoặc lỗi (ví dụ: kết nối thất bại, checksum mismatch).

Gửi/nhận thông điệp text (TEXT) cho giao tiếp cơ bản.

Server:
Lắng nghe kết nối từ nhiều client trên port mặc định 5055.

Quản lý và route thông điệp/file giữa các client (không lưu trữ dữ liệu).

Broadcast danh sách client đang kết nối (CLIENT_LIST).

Xử lý nhiều kết nối đồng thời qua multi-threading (ExecutorService).


Hệ thống hỗ trợ truyền các loại file khác nhau (text, hình ảnh, video, binary), với cơ chế gửi metadata (tên, kích thước) trước để kiểm tra toàn vẹn dữ liệu. Dự án có thiết kế modular với GUI Swing hiện đại (rounded buttons, theme switching) để dễ dàng phát triển thêm tính năng như hiển thị tiến độ truyền chi tiết, tiếp tục truyền nếu đứt kết nối (resume), hỗ trợ chat realtime, hoặc truyền nhiều file cùng lúc.

## 2. Ngôn ngữ & công nghệ chính
<div align="center">
    
[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![TCP Socket](https://img.shields.io/badge/TCP%20Socket-1572B6?style=for-the-badge&logo=network&logoColor=white)](https://docs.oracle.com/javase/8/docs/api/java/net/Socket.html) 
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/)
[![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)](https://git-scm.com/)


 </div>
Java: Ngôn ngữ lập trình chính, sử dụng gói java.net để xử lý Socket TCP và java.io để đọc/ghi file qua stream. Phiên bản Java 8 trở lên được khuyến nghị để đảm bảo tương thích.

TCP Socket: Giao thức cốt lõi, đảm bảo truyền dữ liệu đáng tin cậy, có kết nối (connection-oriented), thứ tự gói tin, và phát hiện lỗi tự động.

JDK (Java Development Kit): Phiên bản 8 trở lên để biên dịch và chạy code Java. Hỗ trợ các tính năng mạng và IO cơ bản.

VS Code: Môi trường phát triển tích hợp (IDE) chính, với extension "Extension Pack for Java" để hỗ trợ biên dịch, chạy, debug code Java một cách dễ dàng. VS Code cho phép mở terminal nội bộ để compile và run mà không cần công cụ bên ngoài.

GitHub: Nền tảng lưu trữ và chia sẻ repo Git online để hợp tác làm việc nhóm.

Git: Công cụ quản lý phiên bản phân tán, giúp theo dõi và quay lại lịch sử code.
 ### Môi trường chạy
<div align="center">


 [![JDK](https://img.shields.io/badge/JDK-4479A1?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/technologies/downloads/)
 [![VS Code](https://img.shields.io/badge/VS%20Code-007ACC?style=for-the-badge&logo=visualstudiocode&logoColor=white)](https://code.visualstudio.com/)

  </div>
 JDK (Java Development Kit): Phiên bản 8 trở lên để biên dịch và chạy code Java. Hỗ trợ các tính năng mạng và IO cơ bản.
VS Code: Môi trường phát triển tích hợp (IDE) chính, với extension "Extension Pack for Java" để hỗ trợ biên dịch, chạy, debug code Java một cách dễ dàng. VS Code cho phép mở terminal nội bộ để compile và run mà không cần công cụ bên ngoài.

## 3. Hình ảnh các chức năng

<div align = "center">

<img alt="AIoTLab Logo" width="500" src="https://github.com/maidkalstit/TruyenfileTCP/blob/main/img/sv.png" />

hình ảnh Server kết nối đến Client


<img alt="AIoTLab Logo" width="500" src="https://github.com/maidkalstit/TruyenfileTCP/blob/main/img/cli.png" />

Hình ảnh Client sau khi được kết nối

<img alt="AIoTLab Logo" width="500" src="https://github.com/maidkalstit/TruyenfileTCP/blob/main/img/clisend.png" />

Hình ảnh Client sau khi ấn chọn file để chuẩn bị gửi

<img alt="AIoTLab Logo" width="500" src="https://github.com/maidkalstit/TruyenfileTCP/blob/main/img/clipick.png" />

Hình ảnh Client sau khi chọn file và hiển thị file để gửi

<img alt="AIoTLab Logo" width="500" src="https://github.com/maidkalstit/TruyenfileTCP/blob/main/img/clidone.png" />

Hình ảnh Client thông báo thông tin sau khi gửi

<img alt="AIoTLab Logo" width="500" src="https://github.com/maidkalstit/TruyenfileTCP/blob/main/img/svdone.png" />

Hình ảnh Server sau khi thực hiện các lệnh 

</div>

## 4. Hướng dẫn cài đặt và sử dụng

Yêu cầu hệ thống

JDK: Phiên bản 8 trở lên (khuyến nghị 17 cho hiệu suất tốt hơn và hỗ trợ Swing hiện đại).

VS Code: Khuyến nghị với extension "Extension Pack for Java" để biên dịch, debug và quản lý dự án.

Môi trường: Windows/Linux/macOS với quyền đọc/ghi file (để lưu file nhận được vào thư mục download).

RAM: Ít nhất 512MB (có thể cần nhiều hơn cho file lớn do đọc file vào memory).

Port: Port 5055 phải mở (không bị firewall chặn).

Các bước cài đặt

Bước 1: Cài đặt JDK:

Tải từ Oracle JDK hoặc OpenJDK (ví dụ: https://www.oracle.com/java/technologies/downloads/).

Cấu hình biến môi trường: Thêm JAVA_HOME (đường dẫn đến thư mục JDK) và %JAVA_HOME%\bin vào PATH.

Kiểm tra: Chạy lệnh java -version trong terminal để xác nhận.

Bước 2: Cài đặt VS Code (tùy chọn nhưng khuyến nghị):

Tải từ https://code.visualstudio.com/.

Cài extension: Mở VS Code, tìm "Extension Pack for Java" (của Microsoft) và install để hỗ trợ biên dịch, run và debug Java.

Bước 3: Tải source code:

Clone repo: git clone https://github.com/maidkalstit/TruyenfileTCP.git (thay thế bằng URL repo thực tế nếu có; hiện tại giả định).

Mở thư mục dự án trong VS Code: File > Open Folder > chọn thư mục chứa các file .java (ví dụ: /TruyenfileTCP/src).

Cấu trúc thư mục:

src/ (chứa các file như GUI.java, Server.java, Client.java, NetworkMessage.java, RoundedButton.java).

bin/ (tạo mới để chứa class files sau biên dịch).

Không có thư viện bên ngoài (lib/), chỉ dùng Java chuẩn.


Bước 4: Biên dịch code:

Mở terminal trong VS Code (Ctrl + `) hoặc Command Prompt/Terminal.

Chạy lệnh: javac -d bin *.java (biên dịch tất cả file .java trong thư mục hiện tại vào thư mục bin).

Nếu dùng VS Code với extension Java, có thể biên dịch tự động qua Run > Run Without Debugging.

Hướng dẫn sử dụng

Khởi động hệ thống:

Mở terminal và di chuyển đến thư mục bin (nơi chứa các .class).

Chạy lệnh: java -cp bin GUI để khởi động ứng dụng chính (LauncherFrame sẽ mở, tự động khởi động server trên port 5055 và hiển thị log).

Launcher sẽ hiển thị giao diện với nút "Mở cửa sổ Client" để mở các instance client mới (hỗ trợ nhiều client trên cùng máy).

Thao tác truyền file:

Trong Launcher: Nhấn "Mở cửa sổ Client" để mở một client window (nhập port nếu khác 5055, mặc định localhost).

Trên Client Window:

Chờ nhận client ID từ server (hiển thị ở header).

Chọn target client từ combo box (danh sách tự động cập nhật từ server).

Nhấn "Chọn File" (hoặc drag-and-drop vào preview area) để chọn file (hỗ trợ text, image, PDF, v.v.; preview tự động hiển thị nếu là text/image).

Nhấn "Gửi Ngay" để gửi file (file được chia chunk 32KB, tiến trình hiển thị trên progress bar).

Để nhận file: File sẽ tự động nhận và lưu vào thư mục download mặc định (~AuroraShareDownloads); theo dõi activity log và progress.

Thêm tính năng: Nhấn nút theme để thay đổi giao diện (Aurora/Midnight/Sunset), hoặc "Xem trước" để xem full preview trong dialog.

Kết thúc: Đóng window client (gửi DISCONNECT), hoặc đóng Launcher để dừng server.

Test và debug:

Test cơ bản:

Mở hai client window từ Launcher.

Từ client 1: Chọn file nhỏ (ví dụ: text.txt), chọn target là client 2, gửi.

Kiểm tra: Client 2 nhận file trong thư mục download, log hiển thị "File received".

Debug trong VS Code:

Đặt breakpoint trong file như GUI.java hoặc Client.java (ví dụ: tại phương thức sendFile).

Chạy debug: Run > Start Debugging (F5), chọn "Java" configuration.

Lỗi phổ biến:

Port 5055 bị chiếm: Thay đổi port trong Server.java (static final int PORT = 5055) và biên dịch lại.

Firewall chặn: Cho phép port 5055 qua firewall (Windows: Settings > Update & Security > Firewall > Allow an app).

OutOfMemoryError: Với file lớn, do đọc toàn bộ vào memory; giải quyết tạm bằng tăng heap size (java -Xmx1024m -cp bin GUI).

Không kết nối: Kiểm tra localhost hoặc IP server đúng, và server đang chạy.

## 5. Liên hệ

Nếu bạn có câu hỏi, góp ý, gặp lỗi, hoặc muốn đóng góp cho dự án (ví dụ: thêm tính năng chat, mã hóa file, hoặc cải thiện hiệu suất), vui lòng liên hệ qua:

Tác giả: Đặng Bùi Thanh Tùng.
Repository: TruyenfileTCP – Fork và tạo pull request để cải tiến (ví dụ: thêm tính năng chat realtime hoặc mã hóa AES cho file truyền).

Email: tung12342004@gmail.com.

Hỗ trợ học phần: Thảo luận trên Issues của repo, hoặc diễn đàn lớp học (Lập Trình Mạng).

Cảm ơn bạn đã quan tâm đến dự án! Dự án này là tài liệu tham khảo cho học phần Lập Trình Mạng, khuyến khích sinh viên mở rộng thêm tính năng như tích hợp chat, mã hóa AES cho file truyền, hoặc hỗ trợ resume transfer.

© 2025 - Dự án học thuật, mã nguồn mở dưới giấy phép MIT. Không sử dụng cho mục đích thương mại mà không có sự cho phép.

