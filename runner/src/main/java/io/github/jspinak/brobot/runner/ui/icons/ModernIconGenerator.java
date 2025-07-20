package io.github.jspinak.brobot.runner.ui.icons;

import lombok.Data;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates modern icons programmatically for the UI.
 */
@Component
@Data
public class ModernIconGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ModernIconGenerator.class);
    
    private final Map<String, Image> iconCache = new HashMap<>();
    
    /**
     * Gets or generates an icon.
     */
    public Image getIcon(String iconName, int size) {
        String key = iconName + "_" + size;
        
        // Check cache first
        Image cached = iconCache.get(key);
        if (cached != null) {
            return cached;
        }
        
        // Generate icon
        if (javafx.application.Platform.isFxApplicationThread()) {
            logger.debug("Generating icon '{}' on FX thread", iconName);
            Image icon = generateIcon(iconName, size);
            if (icon != null) {
                iconCache.put(key, icon);
            }
            return icon;
        } else {
            // Must generate on FX thread
            logger.warn("Icon generation requested off FX thread for '{}'", iconName);
            final Image[] result = new Image[1];
            try {
                javafx.application.Platform.runLater(() -> {
                    result[0] = generateIcon(iconName, size);
                    if (result[0] != null) {
                        iconCache.put(key, result[0]);
                    }
                });
                // Return placeholder for now
                return createPlaceholderIcon(size);
            } catch (Exception e) {
                logger.error("Error generating icon: {}", iconName, e);
                return createPlaceholderIcon(size);
            }
        }
    }
    
    private Image createPlaceholderIcon(int size) {
        // Create a simple placeholder icon that doesn't require JavaFX thread
        WritableImage image = new WritableImage(size, size);
        // Return empty transparent image as placeholder
        return image;
    }
    
    private Image generateIcon(String iconName, int size) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Clear background
        gc.clearRect(0, 0, size, size);
        
        // Set common properties
        gc.setLineWidth(2);
        gc.setStroke(Color.web("#007ACC"));
        gc.setFill(Color.web("#007ACC"));
        
        switch (iconName.toLowerCase()) {
            case "settings":
            case "configuration":
                drawSettingsIcon(gc, size);
                break;
            case "play":
            case "automation":
                drawPlayIcon(gc, size);
                break;
            case "chart":
            case "resources":
                drawChartIcon(gc, size);
                break;
            case "list":
            case "logs":
                drawListIcon(gc, size);
                break;
            case "grid":
            case "showcase":
                drawGridIcon(gc, size);
                break;
            case "chevron-left":
                drawChevronLeft(gc, size);
                break;
            case "chevron-right":
                drawChevronRight(gc, size);
                break;
            case "theme":
                drawThemeIcon(gc, size);
                break;
            case "home":
                drawHomeIcon(gc, size);
                break;
            case "add":
                drawAddIcon(gc, size);
                break;
            case "edit":
                drawEditIcon(gc, size);
                break;
            case "delete":
                drawDeleteIcon(gc, size);
                break;
            case "save":
                drawSaveIcon(gc, size);
                break;
            case "refresh":
                drawRefreshIcon(gc, size);
                break;
            case "search":
                drawSearchIcon(gc, size);
                break;
            case "info":
                drawInfoIcon(gc, size);
                break;
            case "warning":
                drawWarningIcon(gc, size);
                break;
            case "error":
                drawErrorIcon(gc, size);
                break;
            case "success":
                drawSuccessIcon(gc, size);
                break;
            case "pause":
                drawPauseIcon(gc, size);
                break;
            case "stop":
                drawStopIcon(gc, size);
                break;
            case "folder":
                drawFolderIcon(gc, size);
                break;
            case "folder-open":
                drawFolderOpenIcon(gc, size);
                break;
            case "window":
                drawWindowIcon(gc, size);
                break;
            case "keyboard":
                drawKeyboardIcon(gc, size);
                break;
            case "moon":
                drawMoonIcon(gc, size);
                break;
            case "sun":
                drawSunIcon(gc, size);
                break;
            case "bug":
                drawBugIcon(gc, size);
                break;
            default:
                drawDefaultIcon(gc, size);
                break;
        }
        
        // Create snapshot
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage image = canvas.snapshot(params, null);
        
        return image;
    }
    
    private void drawSettingsIcon(GraphicsContext gc, int size) {
        double centerX = size / 2.0;
        double centerY = size / 2.0;
        double radius = size * 0.3;
        double innerRadius = size * 0.15;
        
        // Draw gear wheel
        gc.save();
        gc.translate(centerX, centerY);
        
        // Outer circle with teeth
        int teeth = 8;
        gc.beginPath();
        for (int i = 0; i < teeth; i++) {
            double angle = i * 2 * Math.PI / teeth;
            double x1 = Math.cos(angle) * radius;
            double y1 = Math.sin(angle) * radius;
            double x2 = Math.cos(angle + Math.PI / teeth) * (radius - 3);
            double y2 = Math.sin(angle + Math.PI / teeth) * (radius - 3);
            
            if (i == 0) {
                gc.moveTo(x1, y1);
            } else {
                gc.lineTo(x1, y1);
            }
            gc.lineTo(x2, y2);
        }
        gc.closePath();
        gc.fill();
        
        // Inner circle (hole)
        gc.setFill(Color.WHITE);
        gc.fillOval(-innerRadius, -innerRadius, innerRadius * 2, innerRadius * 2);
        
        gc.restore();
    }
    
    private void drawPlayIcon(GraphicsContext gc, int size) {
        double margin = size * 0.25;
        double[] xPoints = {margin, size - margin, margin};
        double[] yPoints = {margin, size / 2.0, size - margin};
        gc.fillPolygon(xPoints, yPoints, 3);
    }
    
    private void drawChartIcon(GraphicsContext gc, int size) {
        double margin = size * 0.2;
        double barWidth = (size - 2 * margin) / 4;
        double gap = barWidth / 3;
        
        // Draw bars
        double[] heights = {0.3, 0.6, 0.4, 0.8};
        for (int i = 0; i < 4; i++) {
            double x = margin + i * (barWidth + gap);
            double height = heights[i] * (size - 2 * margin);
            double y = size - margin - height;
            gc.fillRect(x, y, barWidth * 0.8, height);
        }
    }
    
    private void drawListIcon(GraphicsContext gc, int size) {
        double margin = size * 0.2;
        double lineHeight = (size - 2 * margin) / 4;
        
        for (int i = 0; i < 3; i++) {
            double y = margin + i * lineHeight * 1.5;
            
            // Draw bullet
            gc.fillOval(margin, y, 3, 3);
            
            // Draw line
            gc.strokeLine(margin + 8, y + 1.5, size - margin, y + 1.5);
        }
    }
    
    private void drawGridIcon(GraphicsContext gc, int size) {
        double margin = size * 0.2;
        double cellSize = (size - 2 * margin) / 3;
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                double x = margin + col * cellSize;
                double y = margin + row * cellSize;
                gc.fillRect(x + 2, y + 2, cellSize - 4, cellSize - 4);
            }
        }
    }
    
    private void drawChevronLeft(GraphicsContext gc, int size) {
        double margin = size * 0.3;
        gc.strokeLine(size - margin, margin, margin, size / 2.0);
        gc.strokeLine(margin, size / 2.0, size - margin, size - margin);
    }
    
    private void drawChevronRight(GraphicsContext gc, int size) {
        double margin = size * 0.3;
        gc.strokeLine(margin, margin, size - margin, size / 2.0);
        gc.strokeLine(size - margin, size / 2.0, margin, size - margin);
    }
    
    private void drawThemeIcon(GraphicsContext gc, int size) {
        double centerX = size / 2.0;
        double centerY = size / 2.0;
        double radius = size * 0.35;
        
        // Draw half moon
        gc.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, 180, javafx.scene.shape.ArcType.ROUND);
    }
    
    private void drawHomeIcon(GraphicsContext gc, int size) {
        double margin = size * 0.2;
        double roofHeight = size * 0.4;
        
        // Draw roof
        double[] xPoints = {size / 2.0, margin, size - margin};
        double[] yPoints = {margin, roofHeight, roofHeight};
        gc.fillPolygon(xPoints, yPoints, 3);
        
        // Draw house
        gc.fillRect(margin + size * 0.1, roofHeight, size * 0.6, size * 0.4);
    }
    
    private void drawAddIcon(GraphicsContext gc, int size) {
        double centerX = size / 2.0;
        double centerY = size / 2.0;
        double barSize = size * 0.6;
        double barWidth = size * 0.15;
        
        // Horizontal bar
        gc.fillRect(centerX - barSize / 2, centerY - barWidth / 2, barSize, barWidth);
        
        // Vertical bar
        gc.fillRect(centerX - barWidth / 2, centerY - barSize / 2, barWidth, barSize);
    }
    
    private void drawEditIcon(GraphicsContext gc, int size) {
        double margin = size * 0.2;
        
        // Draw pencil
        gc.save();
        gc.translate(margin, margin);
        gc.rotate(45);
        
        // Pencil body
        gc.fillRect(0, 0, size * 0.1, size * 0.5);
        
        // Pencil tip
        double[] xPoints = {0, size * 0.05, size * 0.1};
        double[] yPoints = {size * 0.5, size * 0.6, size * 0.5};
        gc.fillPolygon(xPoints, yPoints, 3);
        
        gc.restore();
    }
    
    private void drawDeleteIcon(GraphicsContext gc, int size) {
        double margin = size * 0.25;
        double width = size * 0.5;
        
        // Draw trash can
        gc.fillRect(margin, size * 0.3, width, size * 0.5);
        
        // Draw lid
        gc.fillRect(margin - size * 0.1, size * 0.2, width + size * 0.2, size * 0.1);
        
        // Draw handle
        gc.strokeArc(size * 0.35, size * 0.1, size * 0.3, size * 0.2, 0, 180, javafx.scene.shape.ArcType.OPEN);
    }
    
    private void drawSaveIcon(GraphicsContext gc, int size) {
        double margin = size * 0.15;
        
        // Draw floppy disk
        gc.fillRect(margin, margin, size - 2 * margin, size - 2 * margin);
        
        // Draw label area
        gc.setFill(Color.WHITE);
        gc.fillRect(margin + size * 0.2, margin + size * 0.1, size * 0.5, size * 0.3);
        
        // Draw metal slider
        gc.setFill(Color.web("#007ACC"));
        gc.fillRect(size * 0.6, margin, size * 0.25, size * 0.4);
    }
    
    private void drawRefreshIcon(GraphicsContext gc, int size) {
        double centerX = size / 2.0;
        double centerY = size / 2.0;
        double radius = size * 0.35;
        
        gc.setLineWidth(3);
        
        // Draw circular arrow
        gc.strokeArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 45, 270, javafx.scene.shape.ArcType.OPEN);
        
        // Draw arrow head
        double endX = centerX + radius * Math.cos(Math.toRadians(45));
        double endY = centerY - radius * Math.sin(Math.toRadians(45));
        
        gc.strokeLine(endX, endY, endX - 5, endY);
        gc.strokeLine(endX, endY, endX, endY + 5);
    }
    
    private void drawSearchIcon(GraphicsContext gc, int size) {
        double centerX = size * 0.4;
        double centerY = size * 0.4;
        double radius = size * 0.25;
        
        // Draw magnifying glass circle
        gc.setLineWidth(3);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Draw handle
        double handleStart = centerX + radius * 0.7;
        gc.strokeLine(handleStart, handleStart, size * 0.8, size * 0.8);
    }
    
    private void drawInfoIcon(GraphicsContext gc, int size) {
        double centerX = size / 2.0;
        double centerY = size / 2.0;
        double radius = size * 0.4;
        
        // Draw circle
        gc.setLineWidth(2);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Draw "i"
        gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.5));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("i", centerX, centerY + size * 0.15);
    }
    
    private void drawWarningIcon(GraphicsContext gc, int size) {
        double margin = size * 0.1;
        
        // Draw triangle
        double[] xPoints = {size / 2.0, margin, size - margin};
        double[] yPoints = {margin, size - margin, size - margin};
        gc.fillPolygon(xPoints, yPoints, 3);
        
        // Draw exclamation mark
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.5));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("!", size / 2.0, size * 0.75);
    }
    
    private void drawErrorIcon(GraphicsContext gc, int size) {
        double centerX = size / 2.0;
        double centerY = size / 2.0;
        double radius = size * 0.4;
        
        // Draw circle
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Draw X
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        double offset = radius * 0.5;
        gc.strokeLine(centerX - offset, centerY - offset, centerX + offset, centerY + offset);
        gc.strokeLine(centerX - offset, centerY + offset, centerX + offset, centerY - offset);
    }
    
    private void drawSuccessIcon(GraphicsContext gc, int size) {
        double centerX = size / 2.0;
        double centerY = size / 2.0;
        double radius = size * 0.4;
        
        // Draw circle
        gc.setFill(Color.web("#28A745"));
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Draw checkmark
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeLine(size * 0.3, centerY, size * 0.45, size * 0.65);
        gc.strokeLine(size * 0.45, size * 0.65, size * 0.7, size * 0.35);
    }
    
    private void drawDefaultIcon(GraphicsContext gc, int size) {
        double margin = size * 0.2;
        gc.strokeRect(margin, margin, size - 2 * margin, size - 2 * margin);
    }
    
    private void drawPauseIcon(GraphicsContext gc, int size) {
        double width = size * 0.25;
        double height = size * 0.6;
        double y = size * 0.2;
        
        // Draw two vertical bars
        gc.fillRect(size * 0.25, y, width * 0.4, height);
        gc.fillRect(size * 0.6, y, width * 0.4, height);
    }
    
    private void drawStopIcon(GraphicsContext gc, int size) {
        double margin = size * 0.25;
        gc.fillRect(margin, margin, size - 2 * margin, size - 2 * margin);
    }
    
    private void drawFolderIcon(GraphicsContext gc, int size) {
        double margin = size * 0.15;
        double folderHeight = size * 0.6;
        double tabWidth = size * 0.3;
        double tabHeight = size * 0.1;
        
        // Draw folder tab
        gc.fillRect(margin, margin + tabHeight, tabWidth, tabHeight);
        
        // Draw folder body
        gc.fillRect(margin, margin + tabHeight, size - 2 * margin, folderHeight);
    }
    
    private void drawFolderOpenIcon(GraphicsContext gc, int size) {
        double margin = size * 0.15;
        double folderHeight = size * 0.5;
        
        // Draw folder back
        gc.setFill(Color.web("#007ACC").darker());
        gc.fillRect(margin, margin + size * 0.2, size - 2 * margin, folderHeight);
        
        // Draw folder front (slightly offset)
        gc.setFill(Color.web("#007ACC"));
        gc.fillRect(margin, margin + size * 0.3, size - 2 * margin, folderHeight);
    }
    
    private void drawWindowIcon(GraphicsContext gc, int size) {
        double margin = size * 0.15;
        double windowSize = size - 2 * margin;
        double titleBarHeight = size * 0.15;
        
        // Draw window frame
        gc.strokeRect(margin, margin, windowSize, windowSize);
        
        // Draw title bar
        gc.fillRect(margin, margin, windowSize, titleBarHeight);
        
        // Draw window buttons
        gc.setFill(Color.WHITE);
        double buttonSize = titleBarHeight * 0.5;
        double buttonY = margin + (titleBarHeight - buttonSize) / 2;
        gc.fillOval(size - margin - buttonSize * 3, buttonY, buttonSize, buttonSize);
        gc.fillOval(size - margin - buttonSize * 2, buttonY, buttonSize, buttonSize);
        gc.fillOval(size - margin - buttonSize, buttonY, buttonSize, buttonSize);
    }
    
    private void drawKeyboardIcon(GraphicsContext gc, int size) {
        double margin = size * 0.1;
        double keyboardHeight = size * 0.4;
        double keySize = size * 0.08;
        double keySpacing = size * 0.02;
        
        // Draw keyboard base
        gc.strokeRect(margin, size - margin - keyboardHeight, size - 2 * margin, keyboardHeight);
        
        // Draw keys
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                double x = margin + keySpacing + col * (keySize + keySpacing);
                double y = size - margin - keyboardHeight + keySpacing + row * (keySize + keySpacing);
                gc.fillRect(x, y, keySize, keySize);
            }
        }
    }
    
    private void drawMoonIcon(GraphicsContext gc, int size) {
        double centerX = size / 2.0;
        double centerY = size / 2.0;
        double radius = size * 0.35;
        
        // Draw moon (circle with cutout)
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Cut out crescent
        gc.setFill(Color.WHITE);
        gc.fillOval(centerX - radius * 0.5, centerY - radius, radius * 2, radius * 2);
    }
    
    private void drawSunIcon(GraphicsContext gc, int size) {
        double centerX = size / 2.0;
        double centerY = size / 2.0;
        double radius = size * 0.25;
        
        // Draw sun circle
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Draw rays
        gc.setLineWidth(2);
        int rays = 8;
        for (int i = 0; i < rays; i++) {
            double angle = i * 2 * Math.PI / rays;
            double innerRadius = radius * 1.5;
            double outerRadius = radius * 2.2;
            double x1 = centerX + Math.cos(angle) * innerRadius;
            double y1 = centerY + Math.sin(angle) * innerRadius;
            double x2 = centerX + Math.cos(angle) * outerRadius;
            double y2 = centerY + Math.sin(angle) * outerRadius;
            gc.strokeLine(x1, y1, x2, y2);
        }
    }
    
    private void drawBugIcon(GraphicsContext gc, int size) {
        double centerX = size / 2.0;
        double centerY = size / 2.0;
        double bodyRadius = size * 0.2;
        
        // Draw body
        gc.fillOval(centerX - bodyRadius, centerY - bodyRadius, bodyRadius * 2, bodyRadius * 2);
        
        // Draw head
        double headRadius = bodyRadius * 0.6;
        gc.fillOval(centerX - headRadius, centerY - bodyRadius * 2, headRadius * 2, headRadius * 2);
        
        // Draw legs
        gc.setLineWidth(1.5);
        for (int i = -1; i <= 1; i++) {
            double legY = centerY + i * bodyRadius * 0.5;
            gc.strokeLine(centerX - bodyRadius, legY, centerX - bodyRadius * 1.5, legY + bodyRadius * 0.3);
            gc.strokeLine(centerX + bodyRadius, legY, centerX + bodyRadius * 1.5, legY + bodyRadius * 0.3);
        }
    }
}