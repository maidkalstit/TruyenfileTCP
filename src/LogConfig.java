import java.util.logging.*;

public class LogConfig {
    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        try {
            // Ghi log vào file app.log (append mode)
            FileHandler fh = new FileHandler("app.log", true);
            fh.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord lr) {
                    return java.time.LocalDateTime.now() + " [" + lr.getLevel() + "] " + lr.getMessage() + "\n";
                }
            });
            logger.addHandler(fh);
            logger.setLevel(Level.ALL); // Ghi tất cả level
            ConsoleHandler ch = new ConsoleHandler();
            ch.setFormatter(fh.getFormatter()); // Format giống file
            logger.addHandler(ch);
        } catch (Exception e) {
            System.err.println("Lỗi setup log: " + e.getMessage());
        }
        return logger;
    }
}