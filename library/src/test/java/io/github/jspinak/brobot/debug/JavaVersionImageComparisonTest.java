package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.debug.DebugTestBase;

import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Test to identify Java version differences in image processing
 * Java 8 vs Java 21 image handling comparison
 */
@DisabledInCI
public class JavaVersionImageComparisonTest extends DebugTestBase {
    
    @Test
    public void compareJavaImageHandling() {
        System.out.println("=== JAVA VERSION IMAGE COMPARISON TEST ===\n");
        
        // Check Java version
        System.out.println("JAVA VERSION INFO:");
        System.out.println("  Version: " + System.getProperty("java.version"));
        System.out.println("  Vendor: " + System.getProperty("java.vendor"));
        System.out.println("  Runtime: " + System.getProperty("java.runtime.version"));
        System.out.println("  VM: " + System.getProperty("java.vm.version"));
        
        // Check Graphics Environment
        System.out.println("\nGRAPHICS ENVIRONMENT:");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        System.out.println("  Headless: " + GraphicsEnvironment.isHeadless());
        System.out.println("  Available fonts: " + ge.getAvailableFontFamilyNames().length);
        
        // Check Color Management
        System.out.println("\nCOLOR MANAGEMENT:");
        System.out.println("  sRGB ColorSpace: " + ColorSpace.getInstance(ColorSpace.CS_sRGB));
        System.out.println("  Default ColorModel: " + ColorModel.getRGBdefault());
        
        // Test pattern loading
        String patternPath = "images/prompt/claude-prompt-1.png";
        File patternFile = new File(patternPath);
        
        if (!patternFile.exists()) {
            System.out.println("\nPattern file not found: " + patternPath);
            return;
        }
        
        try {
            System.out.println("\nIMAGE LOADING TEST:");
            System.out.println("  File: " + patternPath);
            
            // Load with ImageIO
            BufferedImage img = ImageIO.read(patternFile);
            System.out.println("  Loaded: " + img.getWidth() + "x" + img.getHeight());
            System.out.println("  Type: " + getImageTypeName(img.getType()));
            System.out.println("  ColorModel: " + img.getColorModel().getClass().getName());
            System.out.println("  ColorSpace: " + img.getColorModel().getColorSpace());
            System.out.println("  Transparency: " + img.getColorModel().getTransparency());
            System.out.println("  Alpha: " + img.getColorModel().hasAlpha());
            System.out.println("  Premultiplied: " + img.getColorModel().isAlphaPremultiplied());
            
            // Check pixel format
            System.out.println("\nPIXEL FORMAT:");
            System.out.println("  Num components: " + img.getColorModel().getNumComponents());
            System.out.println("  Num color components: " + img.getColorModel().getNumColorComponents());
            System.out.println("  Pixel size: " + img.getColorModel().getPixelSize() + " bits");
            
            // Sample pixels
            System.out.println("\nPIXEL SAMPLES:");
            samplePixels(img);
            
            // Test different loading methods
            System.out.println("\nALTERNATIVE LOADING METHODS:");
            
            // Method 1: Force specific type
            BufferedImage imgRGB = new BufferedImage(
                img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = imgRGB.createGraphics();
            
            // Test different rendering hints (Java 8 vs 21 differences)
            testRenderingHints(g);
            
            g.drawImage(img, 0, 0, null);
            g.dispose();
            
            System.out.println("  Converted to TYPE_INT_RGB");
            System.out.println("    Type: " + getImageTypeName(imgRGB.getType()));
            
            // Compare pixels
            comparePixels(img, imgRGB);
            
            // Test color conversion
            System.out.println("\nCOLOR CONVERSION TEST:");
            testColorConversion(img);
            
            // Test antialiasing differences
            System.out.println("\nANTIALIASING TEST:");
            testAntialiasing();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("\n=== JAVA VERSION SPECIFIC ISSUES ===");
        System.out.println("\nKnown differences between Java 8 and Java 21:");
        System.out.println("1. Color Management: Java 9+ has improved color space handling");
        System.out.println("2. HiDPI Support: Java 9+ has better DPI scaling support");
        System.out.println("3. Image I/O: Changes in PNG/JPEG decoders between versions");
        System.out.println("4. Font Rendering: Different antialiasing and hinting");
        System.out.println("5. Graphics2D: New rendering pipeline in Java 9+");
        System.out.println("\nThese differences can cause slight pixel variations in image processing!");
    }
    
    private void testRenderingHints(Graphics2D g) {
        System.out.println("\nRENDERING HINTS:");
        
        // These hints may behave differently between Java versions
        RenderingHints hints = new RenderingHints(null);
        
        // Interpolation
        hints.put(RenderingHints.KEY_INTERPOLATION, 
                  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        System.out.println("  Interpolation: BILINEAR");
        
        // Rendering
        hints.put(RenderingHints.KEY_RENDERING, 
                  RenderingHints.VALUE_RENDER_QUALITY);
        System.out.println("  Rendering: QUALITY");
        
        // Antialiasing
        hints.put(RenderingHints.KEY_ANTIALIASING, 
                  RenderingHints.VALUE_ANTIALIAS_ON);
        System.out.println("  Antialiasing: ON");
        
        // Color rendering
        hints.put(RenderingHints.KEY_COLOR_RENDERING, 
                  RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        System.out.println("  Color Rendering: QUALITY");
        
        // Dithering
        hints.put(RenderingHints.KEY_DITHERING, 
                  RenderingHints.VALUE_DITHER_ENABLE);
        System.out.println("  Dithering: ENABLED");
        
        g.setRenderingHints(hints);
    }
    
    private void testColorConversion(BufferedImage img) {
        // Test if color values are converted differently
        int testPixel = img.getRGB(0, 0);
        
        Color c1 = new Color(testPixel, true);
        System.out.println("  Original pixel (0,0): " + c1);
        
        // Convert through different color spaces (may differ between Java versions)
        float[] hsb = Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), null);
        int rgbFromHSB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        Color c2 = new Color(rgbFromHSB);
        
        System.out.println("  After HSB conversion: " + c2);
        System.out.println("  Difference: R=" + (c1.getRed() - c2.getRed()) +
                          " G=" + (c1.getGreen() - c2.getGreen()) +
                          " B=" + (c1.getBlue() - c2.getBlue()));
    }
    
    private void testAntialiasing() {
        // Create a small test image with antialiasing
        BufferedImage testImg = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImg.createGraphics();
        
        // With antialiasing (may render differently in Java 8 vs 21)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                          RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.drawLine(0, 0, 9, 9);
        
        // Check pixels along the line
        System.out.println("  Antialiased line pixels:");
        for (int i = 0; i < 5; i++) {
            int rgb = testImg.getRGB(i, i);
            Color c = new Color(rgb);
            System.out.println("    (" + i + "," + i + "): " + c);
        }
        
        g.dispose();
    }
    
    private void samplePixels(BufferedImage img) {
        // Sample corner and center pixels
        int[][] points = {{0, 0}, {img.getWidth()-1, 0}, 
                         {img.getWidth()/2, img.getHeight()/2}};
        String[] labels = {"Top-left", "Top-right", "Center"};
        
        for (int i = 0; i < points.length; i++) {
            int x = points[i][0];
            int y = points[i][1];
            int rgb = img.getRGB(x, y);
            
            Color c = new Color(rgb, true);
            System.out.printf("  %s (%d,%d): RGB(%d,%d,%d) A=%d\n",
                labels[i], x, y, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        }
    }
    
    private void comparePixels(BufferedImage img1, BufferedImage img2) {
        System.out.println("\nPIXEL COMPARISON:");
        
        int differences = 0;
        int maxDiff = 0;
        
        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                
                if (rgb1 != rgb2) {
                    differences++;
                    
                    Color c1 = new Color(rgb1, true);
                    Color c2 = new Color(rgb2, true);
                    
                    int diff = Math.abs(c1.getRed() - c2.getRed()) +
                              Math.abs(c1.getGreen() - c2.getGreen()) +
                              Math.abs(c1.getBlue() - c2.getBlue());
                    
                    maxDiff = Math.max(maxDiff, diff);
                    
                    if (differences <= 3) {
                        System.out.printf("    Diff at (%d,%d): RGB1(%d,%d,%d) vs RGB2(%d,%d,%d)\n",
                            x, y, c1.getRed(), c1.getGreen(), c1.getBlue(),
                            c2.getRed(), c2.getGreen(), c2.getBlue());
                    }
                }
            }
        }
        
        System.out.println("  Total differences: " + differences);
        System.out.println("  Max difference: " + maxDiff);
    }
    
    private String getImageTypeName(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR: return "TYPE_INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "TYPE_4BYTE_ABGR_PRE";
            default: return "Type " + type;
        }
    }
}