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
Hệ thống Truyền File Qua TCP được xây dựng nhằm hỗ trợ người dùng truyền tải file giữa client và server một cách an toàn và đáng tin cậy bằng giao thức TCP. Ứng dụng này minh họa các khái niệm cơ bản trong môn Mạng Máy Tính, chẳng hạn như kết nối socket, luồng dữ liệu (stream), và xử lý file trong môi trường mạng.
Các chức năng chính:

Client:

Kết nối đến server qua TCP Socket.
Gửi file đến server bằng lệnh "send [tên file]".
Nhận file từ server bằng lệnh "receive [tên file]".
Hiển thị thông báo thành công hoặc lỗi (ví dụ: file không tồn tại, kết nối thất bại).


Server:

Lắng nghe kết nối từ client trên port mặc định.
Nhận file từ client và lưu trữ tại thư mục hiện tại.
Gửi file cho client khi nhận yêu cầu.
Xử lý nhiều kết nối cơ bản (có thể mở rộng đa luồng).



Hệ thống hỗ trợ truyền các loại file khác nhau (text, hình ảnh, video, binary), với cơ chế gửi kích thước file trước để kiểm tra toàn vẹn dữ liệu. Dự án có thiết kế modular để dễ dàng phát triển thêm tính năng như hiển thị tiến độ truyền, tiếp tục truyền nếu đứt kết nối (resume), hoặc hỗ trợ truyền nhiều file cùng lúc.

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

JDK: Phiên bản 8 trở lên (khuyến nghị 17 cho hiệu suất tốt hơn).

VS Code: Với extension "Extension Pack for Java" để biên dịch và debug.
Môi trường: Windows/Linux/macOS với quyền đọc/ghi file.

Các bước cài đặt

Bước 1: Cài đặt JDK:

Tải từ Oracle JDK.
Cấu hình biến môi trường: Thêm JAVA_HOME (đường dẫn JDK) và %JAVA_HOME%\bin vào PATH.
Kiểm tra: Chạy java -version trong terminal.


Bước 2: Cài đặt VS Code:

Tải từ Visual Studio Code.
Cài extension: Tìm "Extension Pack for Java" và install.


Bước 3: Tải source code:

Clone repo: git clone https://github.com/maidkalstit/TruyenfileTCP.git.
Mở thư mục src trong VS Code: File > Open Folder > chọn /TruyenfileTCP/src.
Cấu trúc thư mục: src/bin (chứa class files sau compile).


Bước 4: Biên dịch code:

Mở terminal trong VS Code (Ctrl + `).
Chạy: javac -d bin *.java (biên dịch tất cả file .java vào thư mục bin).



Hướng dẫn sử dụng

Khởi động hệ thống:

Mở Terminal 1 (cho Server): Chạy lệnh java -cp bin ServerGUI.
(ServerGUI sẽ khởi động, hiển thị giao diện lắng nghe trên port 8080.)
Mở Terminal 2 (cho Client): Chạy lệnh java -cp bin ClientGUI.
(ClientGUI sẽ hiển thị giao diện chọn file và kết nối đến server.)


Thao tác truyền file:

Trên ClientGUI: Nhấn "Chọn File" > Chọn file > Nhấn "Gửi File" (kết nối tự động đến server).
Trên ServerGUI: Xem log nhận file và lưu tự động.
Để nhận file: Trên Client, chọn "Nhận File" > Nhập tên file trên server > Nhấn "Nhận".
Kết thúc: Đóng GUI hoặc nhấn nút "Exit".


Test và debug:

Test cơ bản: Gửi file nhỏ (text.txt) từ client đến server, kiểm tra file lưu trên server.
Debug trong VS Code: Đặt breakpoint trong ServerGUI.java hoặc ClientGUI.java, nhấn F5 để debug.
Lỗi phổ biến: Port 8080 bị chiếm (thay đổi trong code), firewall chặn (cho phép port 8080).

## 5. Liên hệ

Nếu bạn có câu hỏi, góp ý hoặc muốn đóng góp cho dự án, vui lòng liên hệ qua:

Tác giả: Đặng Bùi Thanh Tùng.
Repository: TruyenfileTCP – Fork và pull request để cải tiến.
Email: tung12342004@gmail.com

Hỗ trợ học phần: Thảo luận trên Issues của repo hoặc diễn đàn lớp học.

Cảm ơn bạn đã quan tâm đến dự án! Dự án này là tài liệu tham khảo cho học phần Lập Trình Mạng, khuyến khích sinh viên mở rộng thêm tính năng như mã hóa AES cho file truyền.

© 2025 - Dự án học thuật, mã nguồn mở dưới giấy phép MIT. Không sử dụng cho mục đích thương mại mà không có sự cho phép.

