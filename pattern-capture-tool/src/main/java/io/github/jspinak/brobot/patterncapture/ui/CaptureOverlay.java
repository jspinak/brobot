package io.github.jspinak.brobot.patterncapture.ui;

import io.github.jspinak.brobot.capture.BrobotCaptureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Full-screen transparent overlay for capturing screen regions.
 * Similar to SikuliX IDE's capture functionality.
 */
@Component
public class CaptureOverlay extends JFrame {
    
    private static final float DARKEN_FACTOR = 0.6f;
    private static final Color SELECTION_BORDER = new Color(255, 0, 0, 200);
    private static final Color SELECTION_FILL = new Color(255, 255, 255, 30);
    
    @Autowired
    private BrobotCaptureService captureService;
    
    private BufferedImage screenshot;
    private BufferedImage originalScreenshot;  // Original unscaled screenshot
    private BufferedImage darkenedScreenshot;
    private double captureScale = 1.0;  // Scale factor for coordinate conversion
    private Point startPoint;
    private Point currentPoint;
    private Rectangle selectionBounds;
    private boolean selecting;
    
    private List<CaptureListener> listeners = new ArrayList<>();
    
    public interface CaptureListener {
        void onCaptureComplete(BufferedImage image, Rectangle bounds);
        void onCaptureCancelled();
    }
    
    public CaptureOverlay() {
        initializeOverlay();
        setupListeners();
    }
    
    private void initializeOverlay() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        // Full screen
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Custom panel for drawing
        JPanel capturePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintOverlay(g);
            }
        };
        
        capturePanel.setOpaque(false);
        setContentPane(capturePanel);
        
        // Make frame transparent
        setOpacity(1.0f);
    }
    
    private void setupListeners() {
        // Mouse listeners
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startSelection(e.getPoint());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                endSelection();
            }
        };
        
        MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updateSelection(e.getPoint());
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }
        };
        
        getContentPane().addMouseListener(mouseAdapter);
        getContentPane().addMouseMotionListener(motionAdapter);
        
        // Keyboard listener for ESC
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelCapture();
                }
            }
        });
    }
    
    private void paintOverlay(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw darkened screenshot
        if (darkenedScreenshot != null) {
            g2d.drawImage(darkenedScreenshot, 0, 0, null);
        }
        
        // Draw selection area
        if (selecting && selectionBounds != null) {
            // Draw original brightness in selection area
            if (screenshot != null) {
                g2d.setClip(selectionBounds);
                g2d.drawImage(screenshot, 0, 0, null);
                g2d.setClip(null);
            }
            
            // Draw selection border
            g2d.setColor(SELECTION_BORDER);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(selectionBounds.x, selectionBounds.y,
                        selectionBounds.width, selectionBounds.height);
            
            // Draw selection fill
            g2d.setColor(SELECTION_FILL);
            g2d.fillRect(selectionBounds.x, selectionBounds.y,
                        selectionBounds.width, selectionBounds.height);
            
            // Draw dimensions
            drawDimensions(g2d);
        }
    }
    
    private void drawDimensions(Graphics2D g2d) {
        if (selectionBounds == null) return;
        
        String dimensions = String.format("%d x %d", 
                                         selectionBounds.width, 
                                         selectionBounds.height);
        
        Font font = new Font("Arial", Font.BOLD, 14);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        
        int textWidth = fm.stringWidth(dimensions);
        int textHeight = fm.getHeight();
        
        // Position below selection
        int textX = selectionBounds.x + (selectionBounds.width - textWidth) / 2;
        int textY = selectionBounds.y + selectionBounds.height + textHeight + 5;
        
        // If too low, position above
        if (textY > getHeight() - 20) {
            textY = selectionBounds.y - 10;
        }
        
        // Draw background for text
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(textX - 5, textY - textHeight + 3, 
                         textWidth + 10, textHeight + 4, 5, 5);
        
        // Draw text
        g2d.setColor(Color.WHITE);
        g2d.drawString(dimensions, textX, textY);
    }
    
    private void startSelection(Point point) {
        startPoint = point;
        currentPoint = point;
        selecting = true;
        updateSelectionBounds();
    }
    
    private void updateSelection(Point point) {
        currentPoint = point;
        updateSelectionBounds();
        repaint();
    }
    
    private void updateSelectionBounds() {
        if (startPoint != null && currentPoint != null) {
            int x = Math.min(startPoint.x, currentPoint.x);
            int y = Math.min(startPoint.y, currentPoint.y);
            // Add 1 to include both start and end pixels
            int width = Math.abs(currentPoint.x - startPoint.x) + 1;
            int height = Math.abs(currentPoint.y - startPoint.y) + 1;
            
            selectionBounds = new Rectangle(x, y, width, height);
        }
    }
    
    private void endSelection() {
        if (selecting && selectionBounds != null && 
            selectionBounds.width > 0 && selectionBounds.height > 0) {
            captureSelection();
        }
        selecting = false;
    }
    
    private void captureSelection() {
        if (screenshot != null && selectionBounds != null) {
            BufferedImage selectedImage;
            Rectangle actualBounds;
            
            // Use original screenshot if we had to scale for display
            if (originalScreenshot != null && captureScale != 1.0) {
                // Convert logical coordinates to physical coordinates
                int x = (int) (selectionBounds.x * captureScale);
                int y = (int) (selectionBounds.y * captureScale);
                int width = (int) (selectionBounds.width * captureScale);
                int height = (int) (selectionBounds.height * captureScale);
                
                // Ensure bounds are within the original image
                x = Math.min(x, originalScreenshot.getWidth() - 1);
                y = Math.min(y, originalScreenshot.getHeight() - 1);
                width = Math.min(width, originalScreenshot.getWidth() - x);
                height = Math.min(height, originalScreenshot.getHeight() - y);
                
                actualBounds = new Rectangle(x, y, width, height);
                selectedImage = originalScreenshot.getSubimage(x, y, width, height);
            } else {
                // No scaling needed, use the screenshot directly
                actualBounds = selectionBounds;
                selectedImage = screenshot.getSubimage(
                    selectionBounds.x, selectionBounds.y,
                    selectionBounds.width, selectionBounds.height);
            }
            
            // Notify listeners with actual captured image
            for (CaptureListener listener : listeners) {
                listener.onCaptureComplete(selectedImage, actualBounds);
            }
            
            // Hide overlay
            setVisible(false);
        }
    }
    
    private void cancelCapture() {
        // Notify listeners
        for (CaptureListener listener : listeners) {
            listener.onCaptureCancelled();
        }
        
        // Hide overlay
        setVisible(false);
    }
    
    public void startCapture() {
        // Take screenshot
        try {
            screenshot = captureService.captureScreen();
            
            // Check if we need to scale the image to fit the logical screen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            if (screenshot.getWidth() > screenSize.width || screenshot.getHeight() > screenSize.height) {
                // Physical resolution capture needs to be scaled to logical size
                double scaleX = (double) screenSize.width / screenshot.getWidth();
                double scaleY = (double) screenSize.height / screenshot.getHeight();
                double scale = Math.min(scaleX, scaleY);
                
                // Scale the screenshot to fit the logical display
                int scaledWidth = (int) (screenshot.getWidth() * scale);
                int scaledHeight = (int) (screenshot.getHeight() * scale);
                
                BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = scaledImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(screenshot, 0, 0, scaledWidth, scaledHeight, null);
                g2d.dispose();
                
                // Store original for capture, use scaled for display
                this.originalScreenshot = screenshot;
                screenshot = scaledImage;
                
                // Store scaling factor for coordinate conversion
                this.captureScale = 1.0 / scale;
            } else {
                this.originalScreenshot = null;
                this.captureScale = 1.0;
            }
            
            darkenedScreenshot = createDarkenedImage(screenshot);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Failed to capture screen: " + e.getMessage(),
                "Capture Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Reset state
        startPoint = null;
        currentPoint = null;
        selectionBounds = null;
        selecting = false;
        
        // Show overlay
        setVisible(true);
        requestFocus();
        repaint();
    }
    
    private BufferedImage createDarkenedImage(BufferedImage original) {
        BufferedImage darkened = new BufferedImage(
            original.getWidth(), original.getHeight(), 
            BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2d = darkened.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, DARKEN_FACTOR));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, original.getWidth(), original.getHeight());
        g2d.dispose();
        
        return darkened;
    }
    
    public void addCaptureListener(CaptureListener listener) {
        listeners.add(listener);
    }
    
    public void removeCaptureListener(CaptureListener listener) {
        listeners.remove(listener);
    }
}