package io.github.jspinak.brobot.util.capture;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;

/**
 * Brobot Pattern Capture Tool - Alternative to SikuliX IDE for capturing patterns.
 * This tool captures patterns with metadata about the environment to ensure
 * they work correctly across different DPI settings.
 */
@Component
public class BrobotPatternCapture {
    
    private static final Logger log = LoggerFactory.getLogger(BrobotPatternCapture.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Screen screen;
    private String outputDirectory = "src/test/resources/patterns";
    
    /**
     * Get or initialize the Screen instance.
     */
    private Screen getScreen() {
        if (screen == null) {
            screen = new Screen();
        }
        return screen;
    }
    
    /**
     * Set the Screen instance (for testing).
     */
    public void setScreen(Screen screen) {
        this.screen = screen;
    }
    
    /**
     * Set the output directory for captured patterns.
     * @param outputDirectory the directory path relative to project root
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    /**
     * Get the output directory for captured patterns.
     * @return the output directory path
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }
    
    /**
     * Capture a pattern from screen with metadata.
     */
    public void capturePattern(String name, Rectangle region) {
        try {
            // Capture the region
            Screen screen = getScreen();
            ScreenImage capture = screen.capture(region);
            BufferedImage image = capture.getImage();
            
            // Create metadata
            PatternMetadata metadata = createMetadata();
            
            // Save pattern with metadata
            savePatternWithMetadata(image, name, metadata);
            
            // Create scaled versions for common DPI settings
            createScaledVersions(image, name, metadata);
            
            log.info("Pattern '{}' captured successfully", name);
            log.info("  Original size: {}x{}", image.getWidth(), image.getHeight());
            log.info("  Captured at: {} DPI", metadata.getDpi());
            
        } catch (Exception e) {
            log.error("Failed to capture pattern: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Interactive capture with GUI selection.
     */
    public void interactiveCapture(String name) {
        try {
            // Create transparent overlay for selection
            SelectionOverlay overlay = new SelectionOverlay(name);
            overlay.show();
            
        } catch (Exception e) {
            log.error("Interactive capture failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Create metadata about current environment.
     */
    private PatternMetadata createMetadata() {
        PatternMetadata metadata = new PatternMetadata();
        
        // Get DPI information
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        
        double scaleX = gc.getDefaultTransform().getScaleX();
        double scaleY = gc.getDefaultTransform().getScaleY();
        
        metadata.setDpi((int)(scaleX * 100));
        metadata.setScaleFactorX(scaleX);
        metadata.setScaleFactorY(scaleY);
        
        // Screen information
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        metadata.setPhysicalWidth(screenSize.width);
        metadata.setPhysicalHeight(screenSize.height);
        
        Screen currentScreen = getScreen();
        metadata.setLogicalWidth(currentScreen.w);
        metadata.setLogicalHeight(currentScreen.h);
        
        // Java version
        metadata.setJavaVersion(System.getProperty("java.version"));
        
        // Timestamp
        metadata.setCaptureTimestamp(Instant.now().toString());
        
        // OS information
        metadata.setOs(System.getProperty("os.name"));
        metadata.setOsVersion(System.getProperty("os.version"));
        
        return metadata;
    }
    
    /**
     * Save pattern with embedded metadata.
     */
    private void savePatternWithMetadata(BufferedImage image, String name, 
                                        PatternMetadata metadata) throws IOException {
        File outputDir = new File(outputDirectory);
        outputDir.mkdirs();
        
        // Save the image with metadata in PNG tEXt chunk
        File imageFile = new File(outputDir, name + ".png");
        savePNGWithMetadata(image, imageFile, metadata);
        
        // Also save metadata as JSON for easy access
        File metadataFile = new File(outputDir, name + ".json");
        objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(metadataFile, metadata);
        
        log.info("Saved pattern to: {}", imageFile.getAbsolutePath());
        log.info("Saved metadata to: {}", metadataFile.getAbsolutePath());
    }
    
    /**
     * Save PNG with metadata embedded in tEXt chunks.
     */
    private void savePNGWithMetadata(BufferedImage image, File file, 
                                    PatternMetadata metadata) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        ImageWriter writer = writers.next();
        
        ImageOutputStream ios = ImageIO.createImageOutputStream(file);
        writer.setOutput(ios);
        
        // Create metadata
        IIOMetadata imageMetadata = writer.getDefaultImageMetadata(
            javax.imageio.ImageTypeSpecifier.createFromBufferedImageType(image.getType()),
            writer.getDefaultWriteParam()
        );
        
        // Add custom metadata
        String metadataJson = objectMapper.writeValueAsString(metadata);
        addTextEntry(imageMetadata, "BrobotMetadata", metadataJson);
        addTextEntry(imageMetadata, "BrobotDPI", String.valueOf(metadata.getDpi()));
        addTextEntry(imageMetadata, "BrobotScale", String.valueOf(metadata.getScaleFactorX()));
        
        // Write image with metadata
        writer.write(null, new javax.imageio.IIOImage(image, null, imageMetadata), null);
        
        ios.close();
        writer.dispose();
    }
    
    /**
     * Add text entry to PNG metadata.
     */
    private void addTextEntry(IIOMetadata metadata, String key, String value) {
        try {
            IIOMetadataNode textEntry = new IIOMetadataNode("tEXtEntry");
            textEntry.setAttribute("keyword", key);
            textEntry.setAttribute("value", value);
            
            IIOMetadataNode text = new IIOMetadataNode("tEXt");
            text.appendChild(textEntry);
            
            IIOMetadataNode root = new IIOMetadataNode("javax_imageio_png_1.0");
            root.appendChild(text);
            
            metadata.mergeTree("javax_imageio_png_1.0", root);
        } catch (Exception e) {
            log.warn("Could not add metadata entry {}: {}", key, e.getMessage());
        }
    }
    
    /**
     * Create scaled versions for common DPI settings.
     */
    private void createScaledVersions(BufferedImage original, String name, 
                                     PatternMetadata metadata) {
        double[] scales = {0.75, 0.8, 1.0, 1.25, 1.33, 1.5};
        
        File outputDir = new File(outputDirectory + "/scaled");
        outputDir.mkdirs();
        
        for (double scale : scales) {
            if (scale == 1.0) continue; // Skip original
            
            int newWidth = (int)(original.getWidth() * scale);
            int newHeight = (int)(original.getHeight() * scale);
            
            BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                              RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, 
                              RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(original, 0, 0, newWidth, newHeight, null);
            g.dispose();
            
            try {
                String scaledName = String.format("%s_%.2fx.png", name, scale);
                File scaledFile = new File(outputDir, scaledName);
                ImageIO.write(scaled, "png", scaledFile);
                log.debug("Created scaled version: {} ({}x{})", 
                         scaledName, newWidth, newHeight);
            } catch (IOException e) {
                log.warn("Failed to save scaled version at {}x: {}", scale, e.getMessage());
            }
        }
    }
    
    /**
     * Pattern metadata class.
     */
    public static class PatternMetadata {
        private int dpi;
        private double scaleFactorX;
        private double scaleFactorY;
        private int physicalWidth;
        private int physicalHeight;
        private int logicalWidth;
        private int logicalHeight;
        private String javaVersion;
        private String captureTimestamp;
        private String os;
        private String osVersion;
        
        // Getters and setters
        public int getDpi() { return dpi; }
        public void setDpi(int dpi) { this.dpi = dpi; }
        
        public double getScaleFactorX() { return scaleFactorX; }
        public void setScaleFactorX(double scaleFactorX) { this.scaleFactorX = scaleFactorX; }
        
        public double getScaleFactorY() { return scaleFactorY; }
        public void setScaleFactorY(double scaleFactorY) { this.scaleFactorY = scaleFactorY; }
        
        public int getPhysicalWidth() { return physicalWidth; }
        public void setPhysicalWidth(int physicalWidth) { this.physicalWidth = physicalWidth; }
        
        public int getPhysicalHeight() { return physicalHeight; }
        public void setPhysicalHeight(int physicalHeight) { this.physicalHeight = physicalHeight; }
        
        public int getLogicalWidth() { return logicalWidth; }
        public void setLogicalWidth(int logicalWidth) { this.logicalWidth = logicalWidth; }
        
        public int getLogicalHeight() { return logicalHeight; }
        public void setLogicalHeight(int logicalHeight) { this.logicalHeight = logicalHeight; }
        
        public String getJavaVersion() { return javaVersion; }
        public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }
        
        public String getCaptureTimestamp() { return captureTimestamp; }
        public void setCaptureTimestamp(String captureTimestamp) { 
            this.captureTimestamp = captureTimestamp; 
        }
        
        public String getOs() { return os; }
        public void setOs(String os) { this.os = os; }
        
        public String getOsVersion() { return osVersion; }
        public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    }
    
    /**
     * Interactive selection overlay for capturing patterns.
     */
    private class SelectionOverlay extends JFrame {
        private Point startPoint;
        private Rectangle selection;
        private final String patternName;
        
        public SelectionOverlay(String patternName) {
            this.patternName = patternName;
            setUndecorated(true);
            setBackground(new Color(0, 0, 0, 50));
            setAlwaysOnTop(true);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Full screen
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            gd.setFullScreenWindow(this);
            
            // Mouse listeners
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (selection != null && selection.width > 0 && selection.height > 0) {
                        captureSelection();
                    }
                    dispose();
                }
            });
            
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    updateSelection(e.getPoint());
                }
            });
            
            // ESC to cancel
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        dispose();
                    }
                }
            });
        }
        
        private void updateSelection(Point currentPoint) {
            int x = Math.min(startPoint.x, currentPoint.x);
            int y = Math.min(startPoint.y, currentPoint.y);
            int width = Math.abs(currentPoint.x - startPoint.x);
            int height = Math.abs(currentPoint.y - startPoint.y);
            
            selection = new Rectangle(x, y, width, height);
            repaint();
        }
        
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            
            if (selection != null) {
                Graphics2D g2d = (Graphics2D) g;
                
                // Draw selection rectangle
                g2d.setColor(new Color(0, 255, 0, 128));
                g2d.fillRect(selection.x, selection.y, selection.width, selection.height);
                
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(selection.x, selection.y, selection.width, selection.height);
                
                // Draw size info
                String info = String.format("%dx%d", selection.width, selection.height);
                g2d.setColor(Color.WHITE);
                g2d.drawString(info, selection.x + 5, selection.y - 5);
            }
        }
        
        private void captureSelection() {
            capturePattern(patternName, selection);
        }
        
        public void show() {
            setVisible(true);
        }
    }
}