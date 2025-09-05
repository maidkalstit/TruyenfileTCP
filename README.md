## Mạng Máy Tính
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

DaiNam University Logo

 [![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab) [![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin) [![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn) . --- ## 1. Giới thiệu hệ thống Hệ thống Truyền File qua TCP được xây dựng nhằm hỗ trợ người dùng truyền file giữa client và server một cách an toàn, đáng tin cậy sử dụng giao thức TCP. Ứng dụng này minh họa các khái niệm cơ bản trong mạng máy tính như kết nối socket, stream dữ liệu, và xử lý file. **Các chức năng chính:** - Client: - Kết nối đến server - Chọn file để gửi - Nhận file từ server - Server: - Lắng nghe kết nối - Nhận file từ client và lưu trữ - Gửi file cho client khi yêu cầu --- ### 2. Ngôn ngữ & Công nghệ chính

 [![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/) [![TCP Socket](https://img.shields.io/badge/TCP%20Socket-1572B6?style=for-the-badge&logo=network&logoColor=white)](https://docs.oracle.com/javase/8/docs/api/java/net/Socket.html) ### Môi trường chạy [![JDK](https://img.shields.io/badge/JDK-4479A1?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/technologies/downloads/) [![VS Code](https://img.shields.io/badge/VS%20Code-007ACC?style=for-the-badge&logo=visualstudiocode&logoColor=white)](https://code.visualstudio.com/)

 --- ## 3. Hình ảnh các chức năng --- Giao diện Client (Command Line) ---

image

 --- Giao diện Server (Command Line) ---

image

 --- Quá trình truyền file ---

image

 ## 🚀 4. Các project đã thực hiện dựa trên Platform Một số project sinh viên đã thực hiện: - #### [Khoá 15](./docs/projects/K15/README.md) - #### [Khoá 16]() (Coming soon) ## 5. Các bước cài đặt 1. **Cài đặt JDK** - Tải và cài JDK (phiên bản 8 trở lên): [https://www.oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/) - Cấu hình biến môi trường JAVA_HOME và PATH. 2. **Clone hoặc tải source code** - Clone repo: `git clone https://github.com/your-repo/TruyenFileTCP.git` - Hoặc tải ZIP từ GitHub. 3. **Compile và chạy** - Mở VS Code, mở thư mục project. - Compile: `javac Server.java` và `javac Client.java` - Chạy Server: `java Server` - Chạy Client: `java Client` 4. **Cấu hình kết nối** - Mở file `Client.java`, thay "localhost" bằng IP server nếu chạy trên mạng LAN. - Port mặc định: 8080 (thay đổi nếu cần). 5. **Test hệ thống** - Chạy Server trước. - Trên Client, nhập lệnh để gửi/nhận file (ví dụ: send file.txt). --- 📌 *Lưu ý: Project hỗ trợ truyền file cơ bản. Để mở rộng, thêm đa luồng cho server hoặc resume transmission. Đảm bảo firewall cho phép port 8080.
