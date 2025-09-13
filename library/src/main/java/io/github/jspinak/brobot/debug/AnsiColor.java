package io.github.jspinak.brobot.debug;

/**
 * ANSI color codes for colorful console output.
 * Provides easy-to-use color formatting for debug messages.
 */
public class AnsiColor {
    
    // Reset
    public static final String RESET = "\u001B[0m";
    
    // Regular Colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    // Bright Colors
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";
    
    // Background Colors
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_MAGENTA = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";
    
    // Text Styles
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String BLINK = "\u001B[5m";
    public static final String REVERSE = "\u001B[7m";
    public static final String HIDDEN = "\u001B[8m";
    
    // Semantic Colors for debugging
    public static final String SUCCESS = BRIGHT_GREEN;
    public static final String ERROR = BRIGHT_RED;
    public static final String WARNING = BRIGHT_YELLOW;
    public static final String INFO = BRIGHT_CYAN;
    public static final String DEBUG = BRIGHT_MAGENTA;
    public static final String HEADER = BOLD + BRIGHT_BLUE;
    public static final String DIMMED = BRIGHT_BLACK;
    
    // Box Drawing Characters
    public static final String BOX_TOP_LEFT = "╔";
    public static final String BOX_TOP_RIGHT = "╗";
    public static final String BOX_BOTTOM_LEFT = "╚";
    public static final String BOX_BOTTOM_RIGHT = "╝";
    public static final String BOX_HORIZONTAL = "═";
    public static final String BOX_VERTICAL = "║";
    public static final String BOX_CROSS = "╬";
    public static final String BOX_T_DOWN = "╦";
    public static final String BOX_T_UP = "╩";
    public static final String BOX_T_RIGHT = "╠";
    public static final String BOX_T_LEFT = "╣";
    public static final String BOX_HORIZONTAL_LIGHT = "─";
    
    // Symbols
    public static final String CHECK = "✓";
    public static final String CROSS = "✗";
    public static final String ARROW_RIGHT = "→";
    public static final String ARROW_LEFT = "←";
    public static final String ARROW_UP = "↑";
    public static final String ARROW_DOWN = "↓";
    public static final String BULLET = "•";
    public static final String STAR = "★";
    public static final String WARNING_SIGN = "⚠";
    public static final String INFO_SIGN = "ℹ";
    public static final String HOURGLASS = "⏳";
    public static final String CLOCK = "🕐";
    
    /**
     * Apply color to text.
     */
    public static String colorize(String text, String color) {
        return color + text + RESET;
    }
    
    /**
     * Apply success color (green).
     */
    public static String success(String text) {
        return SUCCESS + text + RESET;
    }
    
    /**
     * Apply error color (red).
     */
    public static String error(String text) {
        return ERROR + text + RESET;
    }
    
    /**
     * Apply warning color (yellow).
     */
    public static String warning(String text) {
        return WARNING + text + RESET;
    }
    
    /**
     * Apply info color (cyan).
     */
    public static String info(String text) {
        return INFO + text + RESET;
    }
    
    /**
     * Apply debug color (magenta).
     */
    public static String debug(String text) {
        return DEBUG + text + RESET;
    }
    
    /**
     * Apply header style (bold blue).
     */
    public static String header(String text) {
        return HEADER + text + RESET;
    }
    
    /**
     * Apply dimmed style (bright black).
     */
    public static String dim(String text) {
        return DIMMED + text + RESET;
    }
    
    /**
     * Create a colored box around text.
     */
    public static String box(String title, String content, String color) {
        int width = Math.max(title.length(), getMaxLineLength(content)) + 4;
        StringBuilder sb = new StringBuilder();
        
        // Top border
        sb.append(color).append(BOX_TOP_LEFT);
        for (int i = 0; i < width - 2; i++) {
            sb.append(BOX_HORIZONTAL);
        }
        sb.append(BOX_TOP_RIGHT).append(RESET).append("\n");
        
        // Title
        if (!title.isEmpty()) {
            sb.append(color).append(BOX_VERTICAL).append(" ").append(RESET);
            sb.append(BOLD).append(title).append(RESET);
            sb.append(padRight(title.length(), width - 3));
            sb.append(color).append(BOX_VERTICAL).append(RESET).append("\n");
            
            // Separator
            sb.append(color).append(BOX_T_RIGHT);
            for (int i = 0; i < width - 2; i++) {
                sb.append(BOX_HORIZONTAL_LIGHT);
            }
            sb.append(BOX_T_LEFT).append(RESET).append("\n");
        }
        
        // Content
        for (String line : content.split("\n")) {
            sb.append(color).append(BOX_VERTICAL).append(" ").append(RESET);
            sb.append(line);
            sb.append(padRight(line.length(), width - 3));
            sb.append(color).append(BOX_VERTICAL).append(RESET).append("\n");
        }
        
        // Bottom border
        sb.append(color).append(BOX_BOTTOM_LEFT);
        for (int i = 0; i < width - 2; i++) {
            sb.append(BOX_HORIZONTAL);
        }
        sb.append(BOX_BOTTOM_RIGHT).append(RESET);
        
        return sb.toString();
    }
    
    private static int getMaxLineLength(String text) {
        int max = 0;
        for (String line : text.split("\n")) {
            max = Math.max(max, removeAnsiCodes(line).length());
        }
        return max;
    }
    
    private static String padRight(int currentLength, int targetLength) {
        StringBuilder sb = new StringBuilder();
        int padding = targetLength - currentLength;
        for (int i = 0; i < padding; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
    
    private static String removeAnsiCodes(String text) {
        return text.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}