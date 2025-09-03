package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.MessageDigest;

/**
 * Check if pattern images are being modified during loading
 */
public class PatternIntegrityCheckTest extends BrobotTestBase {
    
    @Test
    public void checkPatternIntegrity() {
        System.out.println("=== PATTERN INTEGRITY CHECK ===\n");
        
        try {
            String[] patterns = {
                "images/prompt/claude-prompt-1.png",
                "images/working/claude-icon-1.png"
            };
            
            for (String patternPath : patterns) {
                System.out.println("Checking: " + patternPath);
                System.out.println("=" + "=".repeat(50));
                
                File file = new File(patternPath);
                if (!file.exists()) {
                    System.out.println("  ERROR: File not found!");
                    continue;
                }
                
                // File system info
                System.out.println("\nFile System Info:");
                System.out.println("  Absolute path: " + file.getAbsolutePath());
                System.out.println("  File size: " + file.length() + " bytes");
                System.out.println("  Last modified: " + new java.util.Date(file.lastModified()));
                System.out.println("  Can read: " + file.canRead());
                
                // Calculate MD5 hash of file
                String md5 = calculateMD5(file);
                System.out.println("  MD5 hash: " + md5);
                
                // Load with ImageIO
                BufferedImage img = ImageIO.read(file);
                System.out.println("\nImage Properties (loaded with ImageIO):");
                System.out.println("  Dimensions: " + img.getWidth() + "x" + img.getHeight());
                System.out.println("  Type: " + getImageTypeDetails(img.getType()));
                System.out.println("  Color model: " + img.getColorModel().getClass().getSimpleName());
                System.out.println("  Has alpha: " + img.getColorModel().hasAlpha());
                System.out.println("  Num components: " + img.getColorModel().getNumComponents());
                System.out.println("  Pixel size: " + img.getColorModel().getPixelSize() + " bits");
                System.out.println("  Transparency: " + img.getColorModel().getTransparency());
                
                // Sample some pixels
                System.out.println("\nPixel Samples:");
                samplePixels(img);
                
                // Save a copy and compare
                File testDir = new File("pattern-integrity-test");
                testDir.mkdirs();
                
                String baseName = file.getName().replace(".png", "");
                File copyFile = new File(testDir, baseName + "_copy.png");
                ImageIO.write(img, "png", copyFile);
                System.out.println("\nSaved copy to: " + copyFile.getName());
                System.out.println("  Copy size: " + copyFile.length() + " bytes");
                System.out.println("  Size difference: " + (copyFile.length() - file.length()) + " bytes");
                
                // Reload and compare
                BufferedImage reloaded = ImageIO.read(copyFile);
                System.out.println("\nReloaded copy:");
                System.out.println("  Dimensions: " + reloaded.getWidth() + "x" + reloaded.getHeight());
                System.out.println("  Type: " + getImageTypeDetails(reloaded.getType()));
                
                // Compare pixels
                boolean identical = compareImages(img, reloaded);
                System.out.println("  Pixels identical to original: " + identical);
                
                // Try different loading methods
                System.out.println("\nAlternative Loading Methods:");
                testAlternativeLoading(patternPath);
                
                System.out.println("\n");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void testAlternativeLoading(String path) {
        try {
            // Method 1: Using org.sikuli.script.Pattern
            org.sikuli.script.Pattern sikuliPattern = new org.sikuli.script.Pattern(path);
            BufferedImage sikuliImage = sikuliPattern.getBImage();
            if (sikuliImage != null) {
                System.out.println("  SikuliX Pattern loading:");
                System.out.println("    Dimensions: " + sikuliImage.getWidth() + "x" + sikuliImage.getHeight());
                System.out.println("    Type: " + getImageTypeDetails(sikuliImage.getType()));
            } else {
                System.out.println("  SikuliX Pattern.getBImage() returned null");
            }
            
            // Method 2: Using org.sikuli.script.Image
            org.sikuli.script.Image sikuliImg = org.sikuli.script.Image.create(path);
            if (sikuliImg != null && sikuliImg.isValid()) {
                System.out.println("  SikuliX Image.create():");
                System.out.println("    Size: " + sikuliImg.getSize());
                System.out.println("    URL: " + sikuliImg.getURL());
            } else {
                System.out.println("  SikuliX Image.create() failed");
            }
            
        } catch (Exception e) {
            System.out.println("  Error with alternative loading: " + e.getMessage());
        }
    }
    
    private void samplePixels(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        
        // Sample corners and center
        int[][] points = {
            {0, 0}, {w-1, 0}, {w/2, h/2}, {0, h-1}, {w-1, h-1}
        };
        String[] labels = {"Top-left", "Top-right", "Center", "Bottom-left", "Bottom-right"};
        
        for (int i = 0; i < points.length; i++) {
            int x = points[i][0];
            int y = points[i][1];
            int rgb = img.getRGB(x, y);
            
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            int a = (rgb >> 24) & 0xFF;
            
            System.out.printf("  %s (%d,%d): RGBA(%d,%d,%d,%d) hex=0x%08X\n",
                labels[i], x, y, r, g, b, a, rgb);
        }
    }
    
    private boolean compareImages(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return false;
        }
        
        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private String calculateMD5(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                while ((read = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, read);
                }
            }
            
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private String getImageTypeDetails(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: 
                return "TYPE_INT_RGB (1) - 24-bit RGB";
            case BufferedImage.TYPE_INT_ARGB: 
                return "TYPE_INT_ARGB (2) - 32-bit ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE: 
                return "TYPE_INT_ARGB_PRE (3) - 32-bit ARGB premultiplied";
            case BufferedImage.TYPE_INT_BGR: 
                return "TYPE_INT_BGR (4) - 24-bit BGR";
            case BufferedImage.TYPE_3BYTE_BGR: 
                return "TYPE_3BYTE_BGR (5) - 24-bit BGR";
            case BufferedImage.TYPE_4BYTE_ABGR: 
                return "TYPE_4BYTE_ABGR (6) - 32-bit ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: 
                return "TYPE_4BYTE_ABGR_PRE (7) - 32-bit ABGR premultiplied";
            default: 
                return "Type " + type;
        }
    }
}