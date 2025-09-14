
import java.util.logging.*;
import java.io.*;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class LogConfig {
    public static Logger getLogger(String name, JTextArea logArea) {
        Logger logger = Logger.getLogger(name);
        try {
            FileHandler fh = new FileHandler("app.log", true);
            Formatter formatter = new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord lr) {
                    return new java.util.Date().toString() + " [" + lr.getLevel() + "] " + lr.getMessage() + "\n";
                }
            };
            fh.setFormatter(formatter);
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);

            ConsoleHandler ch = new ConsoleHandler();
            ch.setFormatter(formatter);
            logger.addHandler(ch);

            if (logArea != null) {
                GUIHandler guiHandler = new GUIHandler(logArea);
                guiHandler.setFormatter(formatter);  // Fix: Set formatter cho GUIHandler để tránh null
                logger.addHandler(guiHandler);
            }
        } catch (Exception e) {
            System.err.println("Lỗi setup log: " + e.getMessage());
        }
        return logger;
    }

    public static Logger getLogger(String name) {
        return getLogger(name, null);
    }

    private static class GUIHandler extends Handler {
        private JTextArea logArea;

        public GUIHandler(JTextArea logArea) {
            this.logArea = logArea;
        }

        @Override
        public void publish(LogRecord record) {
            if (!isLoggable(record)) return;
            Formatter fmt = getFormatter();
            String message;
            if (fmt != null) {
                message = fmt.format(record);
            } else {
                message = record.getLevel() + ": " + record.getMessage() + "\n";  // Fallback nếu formatter null (an toàn)
            }
            SwingUtilities.invokeLater(() -> {
                logArea.append(message);
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}
    }
}