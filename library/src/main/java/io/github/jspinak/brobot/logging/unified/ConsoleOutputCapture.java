package io.github.jspinak.brobot.logging.unified;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Captures all System.out and System.err output and routes it through BrobotLogger.
 * 
 * <p>This component ensures that all console output, including from third-party
 * libraries, is captured and properly logged through Brobot's unified logging
 * system. It prevents direct console output and maintains logging consistency.</p>
 * 
 * @since 2.0
 */
@Component
public class ConsoleOutputCapture {
    
    private BrobotLogger brobotLogger;
    private static PrintStream originalOut;
    private static PrintStream originalErr;
    private boolean captureEnabled = true;
    private static final ThreadLocal<Boolean> isLogging = ThreadLocal.withInitial(() -> false);
    
    public ConsoleOutputCapture() {
        // Empty constructor to break circular dependency
    }
    
    @Autowired
    public void setBrobotLogger(BrobotLogger brobotLogger) {
        this.brobotLogger = brobotLogger;
    }
    
    @PostConstruct
    public void startCapture() {
        if (!captureEnabled) return;
        
        // Save original streams
        originalOut = System.out;
        originalErr = System.err;
        
        // Create intercepting streams
        System.setOut(createLoggingPrintStream(originalOut, false));
        System.setErr(createLoggingPrintStream(originalErr, true));
    }
    
    @PreDestroy
    public void stopCapture() {
        // Restore original streams
        if (originalOut != null) {
            System.setOut(originalOut);
        }
        if (originalErr != null) {
            System.setErr(originalErr);
        }
    }
    
    private PrintStream createLoggingPrintStream(PrintStream original, boolean isError) {
        return new PrintStream(new LoggingOutputStream(original, isError));
    }
    
    /**
     * Custom OutputStream that intercepts console output and routes to BrobotLogger.
     */
    private class LoggingOutputStream extends OutputStream {
        private final PrintStream original;
        private final boolean isError;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        LoggingOutputStream(PrintStream original, boolean isError) {
            this.original = original;
            this.isError = isError;
        }
        
        @Override
        public void write(int b) throws IOException {
            // Buffer until we hit a newline
            if (b == '\n') {
                String line = buffer.toString().trim();
                buffer.reset();
                
                if (!line.isEmpty() && shouldLog(line)) {
                    logLine(line);
                }
            } else if (b != '\r') {
                buffer.write(b);
            }
        }
        
        private boolean shouldLog(String line) {
            // Filter out known verbose/redundant logs
            if (line.startsWith("[log]")) return false; // SikuliX logs
            if (line.startsWith("in HighlightRegion:")) return false;
            if (line.contains("SikuliX")) return false;
            if (line.startsWith("Click on L")) return false; // SikuliX click logs
            if (line.startsWith("highlight ")) return false; // SikuliX highlight logs
            if (line.contains("TRACE")) return false; // Filter trace logs
            
            return true;
        }
        
        private void logLine(String line) {
            // Prevent recursive logging
            if (isLogging.get()) {
                // Write directly to original stream to avoid infinite recursion
                if (isError && originalErr != null) {
                    originalErr.println(line);
                } else if (!isError && originalOut != null) {
                    originalOut.println(line);
                }
                return;
            }
            
            try {
                isLogging.set(true);
                
                if (isError) {
                    brobotLogger.log()
                        .observation(line)
                        .level(LogEvent.Level.ERROR)
                        .metadata("source", "console.err")
                        .log();
                } else {
                    // Determine appropriate log level based on content
                    LogEvent.Level level = determineLogLevel(line);
                    
                    brobotLogger.log()
                        .observation(line)
                        .level(level)
                        .metadata("source", "console.out")
                        .log();
                }
            } finally {
                isLogging.remove();
            }
        }
        
        private LogEvent.Level determineLogLevel(String line) {
            String lower = line.toLowerCase();
            
            if (lower.contains("error") || lower.contains("exception")) {
                return LogEvent.Level.ERROR;
            } else if (lower.contains("warn")) {
                return LogEvent.Level.WARNING;
            } else if (lower.contains("debug") || lower.contains("trace")) {
                return LogEvent.Level.DEBUG;
            }
            
            return LogEvent.Level.INFO;
        }
        
        @Override
        public void flush() throws IOException {
            // Flush any remaining content
            if (buffer.size() > 0) {
                String line = buffer.toString().trim();
                buffer.reset();
                if (!line.isEmpty() && shouldLog(line)) {
                    logLine(line);
                }
            }
        }
    }
    
    /**
     * Temporarily disables console capture (useful for debugging).
     */
    public void disableCapture() {
        stopCapture();
        captureEnabled = false;
    }
    
    /**
     * Re-enables console capture.
     */
    public void enableCapture() {
        captureEnabled = true;
        startCapture();
    }
    
    /**
     * Gets the original stdout PrintStream.
     * Used by MessageRouter to avoid recursive logging.
     */
    public static PrintStream getOriginalOut() {
        return originalOut;
    }
    
    /**
     * Gets the original stderr PrintStream.
     * Used by MessageRouter to avoid recursive logging.
     */
    public static PrintStream getOriginalErr() {
        return originalErr;
    }
}