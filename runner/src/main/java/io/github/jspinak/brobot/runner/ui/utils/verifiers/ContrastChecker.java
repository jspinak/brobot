package io.github.jspinak.brobot.runner.ui.utils.verifiers;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ContrastChecker {
    private static final Logger logger = LoggerFactory.getLogger(ContrastChecker.class);
    
    // Minimum contrast ratios for WCAG compliance
    private static final double MIN_CONTRAST_RATIO_NORMAL = 4.5; // WCAG AA for normal text
    private static final double MIN_CONTRAST_RATIO_LARGE = 3.0;  // WCAG AA for large text (18pt+)
    
    public static class TextVisibilityIssue {
        public final Node node;
        public final String text;
        public final Color textColor;
        public final Color backgroundColor;
        public final double contrastRatio;
        public final String description;
        
        public TextVisibilityIssue(Node node, String text, Color textColor, 
                                  Color backgroundColor, double contrastRatio) {
            this.node = node;
            this.text = text;
            this.textColor = textColor;
            this.backgroundColor = backgroundColor;
            this.contrastRatio = contrastRatio;
            this.description = String.format(
                "Text '%s' has poor contrast (%.2f:1) - %s on %s",
                text.length() > 30 ? text.substring(0, 30) + "..." : text,
                contrastRatio,
                formatColor(textColor),
                formatColor(backgroundColor)
            );
        }
        
        private static String formatColor(Color color) {
            if (color == null) return "transparent";
            return String.format("rgb(%d,%d,%d)", 
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
        }
    }
    
    public List<TextVisibilityIssue> checkTextVisibility(List<Node> nodes) {
        List<TextVisibilityIssue> issues = new ArrayList<>();
        
        for (Node node : nodes) {
            if (!NodeVisibilityUtil.isNodeActuallyVisible(node)) continue;
            
            String text = NodeDescriptionUtil.extractText(node);
            if (text == null || text.trim().isEmpty()) continue;
            
            // Get text color
            Color textColor = getTextColor(node);
            if (textColor == null) continue;
            
            // Get effective background color
            Color backgroundColor = getEffectiveBackgroundColor(node);
            if (backgroundColor == null) {
                // If we can't determine background, assume white for light theme, dark for dark theme
                backgroundColor = Color.WHITE; // Default assumption
            }
            
            // Calculate contrast ratio
            double contrastRatio = calculateContrastRatio(textColor, backgroundColor);
            
            // Check if contrast meets WCAG standards
            boolean isLargeText = isLargeText(node);
            double minRatio = isLargeText ? MIN_CONTRAST_RATIO_LARGE : MIN_CONTRAST_RATIO_NORMAL;
            
            if (contrastRatio < minRatio) {
                issues.add(new TextVisibilityIssue(node, text, textColor, backgroundColor, contrastRatio));
            }
        }
        
        return issues;
    }
    
    private Color getTextColor(Node node) {
        if (node instanceof Text) {
            return (Color) ((Text) node).getFill();
        } else if (node instanceof Label) {
            return (Color) ((Label) node).getTextFill();
        } else if (node instanceof Button) {
            return (Color) ((Button) node).getTextFill();
        }
        
        // Try to get from style
        String style = node.getStyle();
        if (style != null && style.contains("-fx-text-fill:")) {
            String colorStr = extractStyleValue(style, "-fx-text-fill");
            return parseColor(colorStr);
        }
        
        return null;
    }
    
    private Color getEffectiveBackgroundColor(Node node) {
        Node current = node;
        
        while (current != null) {
            Color color = getBackgroundColor(current);
            if (color != null && color.getOpacity() > 0) {
                return color;
            }
            current = current.getParent();
        }
        
        return null;
    }
    
    private Color getBackgroundColor(Node node) {
        if (node instanceof Region) {
            Region region = (Region) node;
            if (region.getBackground() != null && !region.getBackground().getFills().isEmpty()) {
                var fill = region.getBackground().getFills().get(0).getFill();
                if (fill instanceof Color) {
                    return (Color) fill;
                }
            }
        }
        
        // Try to get from style
        String style = node.getStyle();
        if (style != null && style.contains("-fx-background-color:")) {
            String colorStr = extractStyleValue(style, "-fx-background-color");
            return parseColor(colorStr);
        }
        
        return null;
    }
    
    private boolean isLargeText(Node node) {
        double fontSize = 12; // Default
        boolean isBold = false;
        
        // Get font size from style
        String style = node.getStyle();
        if (style != null && style.contains("-fx-font-size:")) {
            String sizeStr = extractStyleValue(style, "-fx-font-size");
            if (sizeStr != null) {
                sizeStr = sizeStr.replace("px", "").replace("pt", "").trim();
                try {
                    fontSize = Double.parseDouble(sizeStr);
                } catch (NumberFormatException e) {
                    // Use default
                }
            }
        }
        
        // Check if bold
        if (style != null && (style.contains("-fx-font-weight: bold") || style.contains("-fx-font-weight:bold"))) {
            isBold = true;
        }
        
        // WCAG large text: 18pt+ or 14pt+ bold
        return fontSize >= 18 || (fontSize >= 14 && isBold);
    }
    
    private double calculateContrastRatio(Color color1, Color color2) {
        double l1 = getRelativeLuminance(color1);
        double l2 = getRelativeLuminance(color2);
        
        double lighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);
        
        return (lighter + 0.05) / (darker + 0.05);
    }
    
    private double getRelativeLuminance(Color color) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        
        // Apply gamma correction
        r = r <= 0.03928 ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = g <= 0.03928 ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = b <= 0.03928 ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
        
        // Calculate luminance
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }
    
    private String extractStyleValue(String style, String property) {
        int index = style.indexOf(property);
        if (index == -1) return null;
        
        int start = index + property.length();
        while (start < style.length() && (style.charAt(start) == ':' || style.charAt(start) == ' ')) {
            start++;
        }
        
        int end = style.indexOf(';', start);
        if (end == -1) end = style.length();
        
        return style.substring(start, end).trim();
    }
    
    private Color parseColor(String colorStr) {
        if (colorStr == null) return null;
        
        try {
            // Remove any quotes
            colorStr = colorStr.replace("'", "").replace("\"", "").trim();
            
            // Handle hex colors
            if (colorStr.startsWith("#")) {
                return Color.web(colorStr);
            }
            
            // Handle rgb/rgba
            if (colorStr.startsWith("rgb")) {
                return Color.web(colorStr);
            }
            
            // Handle named colors
            return Color.web(colorStr);
        } catch (Exception e) {
            return null;
        }
    }
    
    public void logResults(List<TextVisibilityIssue> issues) {
        if (!issues.isEmpty()) {
            logger.error("TEXT VISIBILITY ISSUES: {}", issues.size());
            for (TextVisibilityIssue issue : issues) {
                logger.error("  - {}", issue.description);
            }
        }
    }
}