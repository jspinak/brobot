package io.github.jspinak.brobot.tools.logging;

import io.github.jspinak.brobot.tools.logging.ConsoleReporter.OutputLevel;
import org.springframework.stereotype.Component;

import java.io.PrintStream;

/**
 * Enhanced console output control with level-based filtering.
 * Provides methods for controlled console output with different verbosity levels.
 */
@Component
public class ConsoleOutputManager {
    
    private OutputLevel currentLevel = OutputLevel.HIGH;
    private PrintStream outputStream = System.out;
    private boolean enabled = true;
    
    /**
     * Print a string to console if the output level matches
     */
    public void print(String message) {
        if (enabled && currentLevel != OutputLevel.NONE) {
            outputStream.print(message);
        }
    }
    
    /**
     * Print a string to console with specific output level requirement
     */
    public void print(String message, OutputLevel requiredLevel) {
        if (enabled && shouldOutput(requiredLevel)) {
            outputStream.print(message);
        }
    }
    
    /**
     * Print a line to console
     */
    public void println() {
        if (enabled && currentLevel != OutputLevel.NONE) {
            outputStream.println();
        }
    }
    
    /**
     * Print a line to console with message
     */
    public void println(String message) {
        if (enabled && currentLevel != OutputLevel.NONE) {
            outputStream.println(message);
        }
    }
    
    /**
     * Print a line to console with specific output level requirement
     */
    public void println(String message, OutputLevel requiredLevel) {
        if (enabled && shouldOutput(requiredLevel)) {
            outputStream.println(message);
        }
    }
    
    /**
     * Print formatted output
     */
    public void printf(String format, Object... args) {
        if (enabled && currentLevel != OutputLevel.NONE) {
            outputStream.printf(format, args);
        }
    }
    
    /**
     * Print formatted output with specific output level
     */
    public void printf(OutputLevel requiredLevel, String format, Object... args) {
        if (enabled && shouldOutput(requiredLevel)) {
            outputStream.printf(format, args);
        }
    }
    
    /**
     * Check if output should be displayed for the given level
     */
    private boolean shouldOutput(OutputLevel requiredLevel) {
        if (currentLevel == OutputLevel.NONE) {
            return false;
        }
        
        // Output if current level is >= required level
        return currentLevel.ordinal() >= requiredLevel.ordinal();
    }
    
    /**
     * Set the current output level
     */
    public void setOutputLevel(OutputLevel level) {
        this.currentLevel = level;
    }
    
    /**
     * Get the current output level
     */
    public OutputLevel getOutputLevel() {
        return currentLevel;
    }
    
    /**
     * Set the output stream (useful for testing)
     */
    public void setOutputStream(PrintStream stream) {
        this.outputStream = stream;
    }
    
    /**
     * Enable or disable all output
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Check if output is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Print debug information (only shown at LOW level)
     */
    public void debug(String message) {
        println(message, OutputLevel.LOW);
    }
    
    /**
     * Print info information (shown at MED and above)
     */
    public void info(String message) {
        println(message, OutputLevel.LOW);
    }
    
    /**
     * Print important information (shown at HIGH)
     */
    public void important(String message) {
        println(message, OutputLevel.HIGH);
    }
    
    /**
     * Print error information (always shown unless OFF)
     */
    public void error(String message) {
        if (enabled && currentLevel != OutputLevel.NONE) {
            outputStream.println("ERROR: " + message);
        }
    }
    
    /**
     * Print a separator line
     */
    public void printSeparator() {
        println("----------------------------------------");
    }
    
    /**
     * Print a separator line with specific level
     */
    public void printSeparator(OutputLevel requiredLevel) {
        println("----------------------------------------", requiredLevel);
    }
    
    /**
     * Print a header with formatting
     */
    public void printHeader(String header) {
        printSeparator();
        println(header);
        printSeparator();
    }
    
    /**
     * Print a header with specific level
     */
    public void printHeader(String header, OutputLevel requiredLevel) {
        printSeparator(requiredLevel);
        println(header, requiredLevel);
        printSeparator(requiredLevel);
    }
}