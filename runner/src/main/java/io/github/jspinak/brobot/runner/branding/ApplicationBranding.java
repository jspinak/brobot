package io.github.jspinak.brobot.runner.branding;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages application branding resources including icons and metadata.
 * Generates icons in various formats and sizes for different platforms.
 */
@Slf4j
public class ApplicationBranding {

    private static final String ICONS_DIR = "packaging/icons";
    private static final Color PRIMARY_COLOR = new Color(0, 123, 255); // Brobot Blue
    private static final Color SECONDARY_COLOR = new Color(40, 167, 69); // Success Green
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    
    /**
     * Generate all required icon sizes and formats.
     */
    public static void generateIcons() throws IOException {
        Path iconsDir = Paths.get(System.getProperty("user.dir"), ICONS_DIR);
        Files.createDirectories(iconsDir);
        
        // Generate base icon
        BufferedImage baseIcon = createBaseIcon(512);
        
        // Generate Windows icons
        generateWindowsIcons(baseIcon, iconsDir);
        
        // Generate macOS icons
        generateMacOSIcons(baseIcon, iconsDir);
        
        // Generate Linux icons
        generateLinuxIcons(baseIcon, iconsDir);
        
        // Generate file association icons
        generateFileAssociationIcons(iconsDir);
        
        log.info("All icons generated in: {}", iconsDir);
    }
    
    /**
     * Create the base application icon design.
     */
    private static BufferedImage createBaseIcon(int size) {
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Background
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, size, size);
        
        // Outer circle
        int margin = size / 8;
        g2d.setColor(PRIMARY_COLOR);
        g2d.fillOval(margin, margin, size - 2 * margin, size - 2 * margin);
        
        // Inner circle
        int innerMargin = size / 4;
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillOval(innerMargin, innerMargin, size - 2 * innerMargin, size - 2 * innerMargin);
        
        // "B" letter
        g2d.setColor(PRIMARY_COLOR);
        Font font = new Font("Arial", Font.BOLD, size / 3);
        g2d.setFont(font);
        
        FontMetrics fm = g2d.getFontMetrics();
        String text = "B";
        int textX = (size - fm.stringWidth(text)) / 2;
        int textY = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);
        
        // Robot indicator (small circle)
        int robotSize = size / 8;
        int robotX = size - innerMargin - robotSize / 2;
        int robotY = size - innerMargin - robotSize / 2;
        g2d.setColor(SECONDARY_COLOR);
        g2d.fillOval(robotX - robotSize / 2, robotY - robotSize / 2, robotSize, robotSize);
        
        g2d.dispose();
        return icon;
    }
    
    /**
     * Generate Windows ICO files.
     */
    private static void generateWindowsIcons(BufferedImage baseIcon, Path iconsDir) throws IOException {
        // Windows requires multiple sizes in ICO format
        List<Integer> sizes = List.of(16, 32, 48, 256);
        List<BufferedImage> images = new ArrayList<>();
        
        for (int size : sizes) {
            BufferedImage scaled = scaleImage(baseIcon, size);
            images.add(scaled);
        }
        
        // Save as PNG for now (ICO conversion would require additional library)
        Path iconPath = iconsDir.resolve("brobot.png");
        ImageIO.write(images.get(images.size() - 1), "PNG", iconPath.toFile());
        
        // Save individual sizes
        for (int i = 0; i < sizes.size(); i++) {
            Path sizePath = iconsDir.resolve("brobot-" + sizes.get(i) + ".png");
            ImageIO.write(images.get(i), "PNG", sizePath.toFile());
        }
        
        log.info("Windows icons generated (PNG format - convert to ICO with external tool)");
    }
    
    /**
     * Generate macOS ICNS files.
     */
    private static void generateMacOSIcons(BufferedImage baseIcon, Path iconsDir) throws IOException {
        // macOS requires specific sizes for ICNS
        List<Integer> sizes = List.of(16, 32, 128, 256, 512);
        
        for (int size : sizes) {
            BufferedImage scaled = scaleImage(baseIcon, size);
            
            // Save regular size
            Path iconPath = iconsDir.resolve("icon_" + size + "x" + size + ".png");
            ImageIO.write(scaled, "PNG", iconPath.toFile());
            
            // Save @2x retina version
            if (size <= 256) {
                BufferedImage retina = scaleImage(baseIcon, size * 2);
                Path retinaPath = iconsDir.resolve("icon_" + size + "x" + size + "@2x.png");
                ImageIO.write(retina, "PNG", retinaPath.toFile());
            }
        }
        
        // Create iconset directory structure
        createIconsetStructure(iconsDir);
        
        log.info("macOS iconset generated - use iconutil to create ICNS");
    }
    
    /**
     * Generate Linux PNG icons.
     */
    private static void generateLinuxIcons(BufferedImage baseIcon, Path iconsDir) throws IOException {
        // Linux uses PNG icons in various sizes
        List<Integer> sizes = List.of(16, 24, 32, 48, 64, 128, 256, 512);
        
        for (int size : sizes) {
            BufferedImage scaled = scaleImage(baseIcon, size);
            Path iconPath = iconsDir.resolve("brobot-" + size + "x" + size + ".png");
            ImageIO.write(scaled, "PNG", iconPath.toFile());
        }
        
        // Save main icon
        Path mainIcon = iconsDir.resolve("brobot.png");
        ImageIO.write(scaleImage(baseIcon, 256), "PNG", mainIcon.toFile());
        
        // Create scalable SVG (simplified representation)
        createSVGIcon(iconsDir);
        
        log.info("Linux icons generated");
    }
    
    /**
     * Generate file association icons.
     */
    private static void generateFileAssociationIcons(Path iconsDir) throws IOException {
        BufferedImage fileIcon = createFileAssociationIcon(256);
        
        // Windows
        Path winFileIcon = iconsDir.resolve("brobot-config.png");
        ImageIO.write(fileIcon, "PNG", winFileIcon.toFile());
        
        // macOS
        Path macFileIcon = iconsDir.resolve("brobot-config-icon.png");
        ImageIO.write(fileIcon, "PNG", macFileIcon.toFile());
        
        // Linux
        Path linuxFileIcon = iconsDir.resolve("application-x-brobot.png");
        ImageIO.write(fileIcon, "PNG", linuxFileIcon.toFile());
        
        log.info("File association icons generated");
    }
    
    /**
     * Create file association icon design.
     */
    private static BufferedImage createFileAssociationIcon(int size) {
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Document shape
        int margin = size / 8;
        int width = size - 2 * margin;
        int height = size - 2 * margin;
        int foldSize = width / 5;
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(margin, margin, width, height);
        
        // Document fold
        int[] xPoints = {margin + width - foldSize, margin + width, margin + width};
        int[] yPoints = {margin, margin, margin + foldSize};
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Document outline
        g2d.setColor(PRIMARY_COLOR);
        g2d.setStroke(new BasicStroke(size / 32f));
        g2d.drawRect(margin, margin, width - foldSize, height);
        g2d.drawLine(margin + width - foldSize, margin, margin + width - foldSize, margin + foldSize);
        g2d.drawLine(margin + width - foldSize, margin + foldSize, margin + width, margin + foldSize);
        
        // JSON brackets
        g2d.setColor(PRIMARY_COLOR);
        Font font = new Font("Courier New", Font.BOLD, size / 4);
        g2d.setFont(font);
        
        String jsonText = "{ }";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (size - fm.stringWidth(jsonText)) / 2;
        int textY = size / 2 + fm.getHeight() / 4;
        g2d.drawString(jsonText, textX, textY);
        
        // Small "B" badge
        int badgeSize = size / 4;
        int badgeX = margin + width - badgeSize - size / 16;
        int badgeY = margin + height - badgeSize - size / 16;
        
        g2d.setColor(SECONDARY_COLOR);
        g2d.fillOval(badgeX, badgeY, badgeSize, badgeSize);
        
        g2d.setColor(Color.WHITE);
        Font badgeFont = new Font("Arial", Font.BOLD, badgeSize / 2);
        g2d.setFont(badgeFont);
        fm = g2d.getFontMetrics();
        String badgeText = "B";
        int badgeTextX = badgeX + (badgeSize - fm.stringWidth(badgeText)) / 2;
        int badgeTextY = badgeY + (badgeSize - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(badgeText, badgeTextX, badgeTextY);
        
        g2d.dispose();
        return icon;
    }
    
    /**
     * Scale image to specified size.
     */
    private static BufferedImage scaleImage(BufferedImage original, int size) {
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(original, 0, 0, size, size, null);
        g2d.dispose();
        
        return scaled;
    }
    
    /**
     * Create iconset directory structure for macOS.
     */
    private static void createIconsetStructure(Path iconsDir) throws IOException {
        Path iconsetDir = iconsDir.resolve("brobot.iconset");
        Files.createDirectories(iconsetDir);
        
        // Create script to generate ICNS
        String script = """
            #!/bin/bash
            # Generate ICNS from iconset
            cd "$(dirname "$0")"
            iconutil -c icns brobot.iconset -o brobot.icns
            echo "Created brobot.icns"
            """;
        
        Path scriptPath = iconsDir.resolve("create-icns.sh");
        Files.writeString(scriptPath, script);
        scriptPath.toFile().setExecutable(true);
    }
    
    /**
     * Create simplified SVG icon.
     */
    private static void createSVGIcon(Path iconsDir) throws IOException {
        String svg = """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="256" height="256" viewBox="0 0 256 256" xmlns="http://www.w3.org/2000/svg">
              <!-- Brobot Runner Icon -->
              <circle cx="128" cy="128" r="96" fill="#007BFF"/>
              <circle cx="128" cy="128" r="64" fill="#FFFFFF"/>
              <text x="128" y="148" font-family="Arial" font-size="80" font-weight="bold" 
                    text-anchor="middle" fill="#007BFF">B</text>
              <circle cx="192" cy="192" r="16" fill="#28A745"/>
            </svg>
            """;
        
        Path svgPath = iconsDir.resolve("brobot.svg");
        Files.writeString(svgPath, svg);
    }
    
    /**
     * Create application metadata files.
     */
    public static void createMetadataFiles() throws IOException {
        Path packagingDir = Paths.get(System.getProperty("user.dir"), "packaging");
        Files.createDirectories(packagingDir);
        
        // Create README for icons
        String iconReadme = """
            # Brobot Runner Icons
            
            This directory contains application icons for all platforms.
            
            ## Icon Files
            
            ### Application Icons
            - `brobot.png` - Main application icon (256x256)
            - `brobot.svg` - Scalable vector icon
            - `brobot.ico` - Windows icon (convert from PNG)
            - `brobot.icns` - macOS icon (generate with iconutil)
            
            ### File Association Icons
            - `brobot-config.png` - JSON configuration file icon
            - `brobot-config.ico` - Windows file icon
            - `brobot-config.icns` - macOS file icon
            
            ## Generating Platform-Specific Icons
            
            ### Windows ICO
            Use a tool like ImageMagick:
            ```
            magick convert brobot-16.png brobot-32.png brobot-48.png brobot-256.png brobot.ico
            ```
            
            ### macOS ICNS
            Run the provided script:
            ```
            ./create-icns.sh
            ```
            
            ### Linux
            PNG files are ready to use. Install to appropriate directories:
            - `/usr/share/icons/hicolor/SIZE/apps/brobot-runner.png`
            """;
        
        Path readmePath = packagingDir.resolve("icons/README.md");
        Files.createDirectories(readmePath.getParent());
        Files.writeString(readmePath, iconReadme);
        
        log.info("Metadata files created");
    }
}