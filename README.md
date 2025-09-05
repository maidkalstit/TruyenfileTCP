<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
     Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
    Lập Trình Mạng
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

 [![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/) [![TCP Socket](https://img.shields.io/badge/TCP%20Socket-1572B6?style=for-the-badge&logo=network&logoColor=white)](https://docs.oracle.com/javase/8/docs/api/java/net/Socket.html) ### Môi trường chạy [![JDK](https://img.shields.io/badge/JDK-4479A1?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/technologies/downloads/) [![VS Code](https://img.shields.io/badge/VS%20Code-007ACC?style=for-the-badge&logo=visualstudiocode&logoColor=white)](https://code.visualstudio.com/)


