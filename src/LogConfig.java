import java.util.logging.*;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Utility class for configuring Logger with file, console, and GUI handlers.
 */
public class LogConfig {
    /**
     * Get a logger with optional GUI text area for logging.
     * @param name Logger name
     * @param logArea JTextArea for GUI logging (null for console only)
     * @return Configured Logger
     */
    public static Logger getLogger(String name, JTextArea logArea) {
        Logger logger = Logger.getLogger(name);
        try {
            // File handler for persistent logging
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

            // Console handler for realtime output
            ConsoleHandler ch = new ConsoleHandler();
            ch.setFormatter(formatter);
            logger.addHandler(ch);

            // GUI handler if provided
            if (logArea != null) {
                GUIHandler guiHandler = new GUIHandler(logArea);
                guiHandler.setFormatter(formatter);
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
            String message = (fmt != null) ? fmt.format(record) : record.getLevel() + ": " + record.getMessage() + "\n";
            SwingUtilities.invokeLater(() -> {
                if (logArea != null) {
                    logArea.append(message);
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                }
            });
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}
    }
}