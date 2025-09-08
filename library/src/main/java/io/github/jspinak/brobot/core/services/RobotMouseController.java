package io.github.jspinak.brobot.core.services;

import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Pure Java Robot-based implementation of mouse and keyboard control.
 * 
 * This implementation bypasses SikuliX entirely and uses Java's Robot class
 * directly for input control. Since SikuliX itself uses Robot under the hood,
 * this provides the same functionality with less overhead.
 * 
 * Benefits over SikuliX:
 * - Direct control (no abstraction layer)
 * - Smaller memory footprint
 * - No SikuliX dependency needed
 * - Better performance (no wrapper overhead)
 * - Same cross-platform compatibility (Robot is cross-platform)
 * 
 * @since 2.0.0
 */
@Component
public class RobotMouseController implements MouseController {
    
    private final Robot robot;
    private Point currentPosition;
    private static final int CLICK_DELAY = 50; // ms between press and release
    private static final int MOVE_DELAY = 10;  // ms after moving mouse
    
    public RobotMouseController() {
        try {
            this.robot = new Robot();
            this.robot.setAutoDelay(10); // Small delay between events
            this.robot.setAutoWaitForIdle(true); // Wait for events to process
            this.currentPosition = MouseInfo.getPointerInfo().getLocation();
        } catch (AWTException e) {
            throw new RuntimeException("Failed to initialize Robot", e);
        }
    }
    
    @Override
    public synchronized boolean moveTo(int x, int y) {
        try {
            // Smooth mouse movement (optional - can make it instant)
            smoothMove(x, y);
            
            // Update position
            currentPosition = new Point(x, y);
            
            // Small delay to ensure movement completes
            robot.delay(MOVE_DELAY);
            
            // Verify position (optional)
            Point actual = MouseInfo.getPointerInfo().getLocation();
            boolean success = Math.abs(actual.x - x) <= 1 && Math.abs(actual.y - y) <= 1;
            
            if (!success) {
                ConsoleReporter.println("[RobotMouseController] Position mismatch - expected: " + 
                    x + "," + y + " actual: " + actual.x + "," + actual.y);
            }
            
            return success;
            
        } catch (Exception e) {
            ConsoleReporter.println("[RobotMouseController] Error moving mouse: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Smooth mouse movement for more natural appearance.
     * Can be disabled for instant movement.
     */
    private void smoothMove(int targetX, int targetY) {
        Point current = MouseInfo.getPointerInfo().getLocation();
        int steps = 10; // Number of intermediate positions
        
        for (int i = 1; i <= steps; i++) {
            int x = current.x + (targetX - current.x) * i / steps;
            int y = current.y + (targetY - current.y) * i / steps;
            robot.mouseMove(x, y);
            
            if (i < steps) {
                robot.delay(2); // Small delay between steps
            }
        }
    }
    
    /**
     * Instant mouse movement (no smoothing).
     */
    private void instantMove(int x, int y) {
        robot.mouseMove(x, y);
    }
    
    @Override
    public synchronized boolean click(int x, int y, MouseButton button) {
        try {
            // Move to position first
            if (!moveTo(x, y)) {
                return false;
            }
            
            // Perform click
            int buttonMask = getButtonMask(button);
            robot.mousePress(buttonMask);
            robot.delay(CLICK_DELAY);
            robot.mouseRelease(buttonMask);
            
            ConsoleReporter.println("[RobotMouseController] Clicked " + button + " at: " + x + ", " + y);
            return true;
            
        } catch (Exception e) {
            ConsoleReporter.println("[RobotMouseController] Error clicking: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public synchronized boolean doubleClick(int x, int y, MouseButton button) {
        try {
            // Move to location
            if (!moveTo(x, y)) {
                return false;
            }
            
            // Get button mask
            int buttonMask = getButtonMask(button);
            
            // Perform double click
            robot.mousePress(buttonMask);
            robot.mouseRelease(buttonMask);
            robot.delay(50); // Small delay between clicks
            robot.mousePress(buttonMask);
            robot.mouseRelease(buttonMask);
            
            ConsoleReporter.println("[RobotMouseController] Double-clicked at " + x + "," + y + " with " + button + " button");
            return true;
            
        } catch (Exception e) {
            ConsoleReporter.println("[RobotMouseController] Error double-clicking: " + e.getMessage());
            return false;
        }
    }
    
    // Convenience method for left double-click
    public synchronized boolean doubleClick(int x, int y) {
        return doubleClick(x, y, MouseButton.LEFT);
    }
    
    @Override
    public synchronized boolean drag(int fromX, int fromY, int toX, int toY, MouseButton button) {
        try {
            // Move to start position
            if (!moveTo(fromX, fromY)) {
                return false;
            }
            
            // Press mouse button
            int buttonMask = getButtonMask(button);
            robot.mousePress(buttonMask);
            robot.delay(100); // Hold briefly
            
            // Drag to end position (smooth movement)
            smoothMove(toX, toY);
            
            // Release mouse button
            robot.delay(100); // Hold briefly at destination
            robot.mouseRelease(buttonMask);
            
            ConsoleReporter.println("[RobotMouseController] Dragged from " + 
                fromX + "," + fromY + " to " + toX + "," + toY + " with " + button + " button");
            return true;
            
        } catch (Exception e) {
            ConsoleReporter.println("[RobotMouseController] Error dragging: " + e.getMessage());
            return false;
        }
    }
    
    // Convenience method for left-button drag
    public synchronized boolean drag(int fromX, int fromY, int toX, int toY) {
        return drag(fromX, fromY, toX, toY, MouseButton.LEFT);
    }
    
    @Override
    public synchronized boolean mouseDown(MouseButton button) {
        try {
            int buttonMask = getButtonMask(button);
            robot.mousePress(buttonMask);
            
            ConsoleReporter.println("[RobotMouseController] Mouse " + button + " pressed");
            return true;
            
        } catch (Exception e) {
            ConsoleReporter.println("[RobotMouseController] Error pressing mouse: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public synchronized boolean mouseUp(MouseButton button) {
        try {
            int buttonMask = getButtonMask(button);
            robot.mouseRelease(buttonMask);
            
            ConsoleReporter.println("[RobotMouseController] Mouse " + button + " released");
            return true;
            
        } catch (Exception e) {
            ConsoleReporter.println("[RobotMouseController] Error releasing mouse: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public synchronized boolean scroll(int wheelAmt) {
        try {
            robot.mouseWheel(wheelAmt);
            ConsoleReporter.println("[RobotMouseController] Scrolled by " + wheelAmt);
            return true;
        } catch (Exception e) {
            ConsoleReporter.println("[RobotMouseController] Error scrolling: " + e.getMessage());
            return false;
        }
    }
    
    // Legacy method for backward compatibility
    public synchronized boolean scroll(int direction, int amount) {
        try {
            // Positive direction = scroll down, negative = scroll up
            int wheelAmt = direction > 0 ? amount : -amount;
            robot.mouseWheel(wheelAmt);
            
            ConsoleReporter.println("[RobotMouseController] Scrolled " + 
                (direction > 0 ? "down" : "up") + " by " + amount);
            return true;
            
        } catch (Exception e) {
            ConsoleReporter.println("[RobotMouseController] Error scrolling: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public int[] getPosition() {
        Point p = MouseInfo.getPointerInfo().getLocation();
        return new int[] { p.x, p.y };
    }
    
    /**
     * Converts MouseButton enum to Robot button mask.
     */
    private int getButtonMask(MouseButton button) {
        switch (button) {
            case LEFT:
                return InputEvent.BUTTON1_DOWN_MASK;
            case RIGHT:
                return InputEvent.BUTTON3_DOWN_MASK;
            case MIDDLE:
                return InputEvent.BUTTON2_DOWN_MASK;
            default:
                return InputEvent.BUTTON1_DOWN_MASK;
        }
    }
    
    /**
     * Type text using Robot.
     */
    public void type(String text) {
        for (char c : text.toCharArray()) {
            typeChar(c);
        }
    }
    
    /**
     * Type a single character.
     */
    private void typeChar(char c) {
        boolean shift = Character.isUpperCase(c) || isShiftChar(c);
        
        if (shift) {
            robot.keyPress(KeyEvent.VK_SHIFT);
        }
        
        int keyCode = getKeyCode(c);
        if (keyCode != -1) {
            robot.keyPress(keyCode);
            robot.delay(10);
            robot.keyRelease(keyCode);
        }
        
        if (shift) {
            robot.keyRelease(KeyEvent.VK_SHIFT);
        }
    }
    
    /**
     * Check if character requires shift key.
     */
    private boolean isShiftChar(char c) {
        String shiftChars = "!@#$%^&*()_+{}|:\"<>?~";
        return shiftChars.indexOf(c) >= 0;
    }
    
    /**
     * Get KeyEvent code for character.
     */
    private int getKeyCode(char c) {
        // Handle letters and digits
        if (Character.isLetterOrDigit(c)) {
            return KeyEvent.getExtendedKeyCodeForChar(Character.toUpperCase(c));
        }
        
        // Handle special characters
        switch (c) {
            case ' ': return KeyEvent.VK_SPACE;
            case '\t': return KeyEvent.VK_TAB;
            case '\n': return KeyEvent.VK_ENTER;
            case '.': return KeyEvent.VK_PERIOD;
            case ',': return KeyEvent.VK_COMMA;
            case '-': return KeyEvent.VK_MINUS;
            case '=': return KeyEvent.VK_EQUALS;
            case '/': return KeyEvent.VK_SLASH;
            case '\\': return KeyEvent.VK_BACK_SLASH;
            case '[': return KeyEvent.VK_OPEN_BRACKET;
            case ']': return KeyEvent.VK_CLOSE_BRACKET;
            case ';': return KeyEvent.VK_SEMICOLON;
            case '\'': return KeyEvent.VK_QUOTE;
            case '`': return KeyEvent.VK_BACK_QUOTE;
            // Shift characters map to their non-shift keys
            case '!': return KeyEvent.VK_1;
            case '@': return KeyEvent.VK_2;
            case '#': return KeyEvent.VK_3;
            case '$': return KeyEvent.VK_4;
            case '%': return KeyEvent.VK_5;
            case '^': return KeyEvent.VK_6;
            case '&': return KeyEvent.VK_7;
            case '*': return KeyEvent.VK_8;
            case '(': return KeyEvent.VK_9;
            case ')': return KeyEvent.VK_0;
            case '_': return KeyEvent.VK_MINUS;
            case '+': return KeyEvent.VK_EQUALS;
            case '{': return KeyEvent.VK_OPEN_BRACKET;
            case '}': return KeyEvent.VK_CLOSE_BRACKET;
            case '|': return KeyEvent.VK_BACK_SLASH;
            case ':': return KeyEvent.VK_SEMICOLON;
            case '"': return KeyEvent.VK_QUOTE;
            case '<': return KeyEvent.VK_COMMA;
            case '>': return KeyEvent.VK_PERIOD;
            case '?': return KeyEvent.VK_SLASH;
            default: return -1;
        }
    }
    
    /**
     * Press a special key (like ESC, ENTER, etc).
     */
    public void pressKey(int keyCode) {
        robot.keyPress(keyCode);
        robot.delay(10);
        robot.keyRelease(keyCode);
    }
    
    /**
     * Perform key combination (like Ctrl+C).
     */
    public void keyCombo(int... keyCodes) {
        // Press all keys
        for (int keyCode : keyCodes) {
            robot.keyPress(keyCode);
            robot.delay(10);
        }
        
        // Release all keys in reverse order
        for (int i = keyCodes.length - 1; i >= 0; i--) {
            robot.keyRelease(keyCodes[i]);
            robot.delay(10);
        }
    }
    
    @Override
    public boolean isAvailable() {
        return robot != null;
    }
    
    @Override
    public String getImplementationName() {
        return "AWT Robot";
    }
}