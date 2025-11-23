import java.awt.*;
import javax.swing.JButton;

/**
 * Nút với góc bo tròn và màu sắc tùy chỉnh để tạo giao diện hiện đại hơn.
 * Nút này mở rộng JButton và tự vẽ lại với hiệu ứng hover và pressed.
 */
public class RoundedButton extends JButton {

    // Độ bo tròn của góc (width và height)
    private int arcWidth = 20;
    private int arcHeight = 20;
    
    // Màu nền khi hover
    private Color hoverBackground;
    
    // Màu nền bình thường
    private Color normalBackground;
    
    // Màu nền khi nhấn
    private Color pressedBackground;
    
    // Màu nền hiện tại (thay đổi theo trạng thái)
    private Color currentBackground;

    /**
     * Constructor tạo nút với text.
     * @param text Text hiển thị trên nút
     */
    public RoundedButton(String text) {
        super(text);
        // Tắt vẽ border và background mặc định
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        
        // Đặt font đậm, cỡ 14
        setFont(getFont().deriveFont(Font.BOLD, 14f));
        
        // Màu mặc định (xanh dương)
        normalBackground = new Color(72, 118, 255);
        hoverBackground = new Color(52, 98, 235);
        pressedBackground = new Color(32, 78, 215);
        currentBackground = normalBackground;
        
        // Màu chữ trắng
        setForeground(Color.WHITE);

        // Con trỏ tay khi hover
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Lắng nghe thay đổi trạng thái để cập nhật màu
        addChangeListener(e -> {
            if (getModel().isPressed()) {
                currentBackground = pressedBackground;
            } else if (getModel().isRollover()) {
                currentBackground = hoverBackground;
            } else {
                currentBackground = normalBackground;
            }
            repaint();
        });
    }

    /**
     * Đặt độ bo tròn của góc.
     * @param arcWidth Độ bo tròn theo chiều ngang
     * @param arcHeight Độ bo tròn theo chiều dọc
     */
    public void setArc(int arcWidth, int arcHeight) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        repaint();
    }

    /**
     * Đặt màu sắc cho nút (normal, hover, pressed).
     * @param normal Màu nền bình thường
     * @param hover Màu nền khi hover
     * @param pressed Màu nền khi nhấn
     */
    public void setButtonColors(Color normal, Color hover, Color pressed) {
        this.normalBackground = normal;
        this.hoverBackground = hover;
        this.pressedBackground = pressed;
        this.currentBackground = normal;
        repaint();
    }

    /**
     * Vẽ nền nút với góc bo tròn.
     * @param g Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Bật khử răng cưa để vẽ mượt hơn
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Vẽ nền với góc bo tròn
        g2.setColor(currentBackground);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
        // Vẽ text và icon lên trên
        super.paintComponent(g2);
        g2.dispose();
    }

    /**
     * Vẽ toàn bộ component (override để đảm bảo khử răng cưa).
     * @param g Graphics context
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Bật khử răng cưa
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(g2);
        g2.dispose();
    }

    /**
     * Lấy kích thước ưa thích của nút.
     * Đảm bảo nút có chiều cao tối thiểu 44px và padding ngang 20px.
     * @return Kích thước ưa thích
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.height = Math.max(size.height, 44);
        size.width += 20;
        return size;
    }
}
