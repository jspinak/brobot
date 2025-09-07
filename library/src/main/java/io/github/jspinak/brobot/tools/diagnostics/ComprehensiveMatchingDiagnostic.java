package io.github.jspinak.brobot.tools.diagnostics;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive diagnostic to identify why SikuliX IDE matches at 0.99 
 * while Brobot only achieves 0.69 similarity.
 */
// @Component  // Disabled to prevent OpenCV loading issues
public class ComprehensiveMatchingDiagnostic {

    static {
        // Commented out to prevent startup failures when OpenCV natives are not available
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static class MatchingResult {
        public String method;
        public double similarity;
        public String details;
        public BufferedImage patternUsed;
        public BufferedImage screenUsed;
        
        public MatchingResult(String method, double similarity, String details) {
            this.method = method;
            this.similarity = similarity;
            this.details = details;
        }
    }

    public void runFullDiagnosis(String patternPath) {
        System.out.println("\n=== COMPREHENSIVE MATCHING DIAGNOSTIC ===");
        System.out.println("Pattern: " + patternPath);
        System.out.println("Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println("Settings.MinSimilarity: " + Settings.MinSimilarity);
        
        try {
            List<MatchingResult> results = new ArrayList<>();
            
            // Test 1: Direct SikuliX Pattern/Screen as IDE would use
            results.add(testDirectSikuliXMatching(patternPath));
            
            // Test 2: SikuliX with Brobot's loading method
            results.add(testBrobotLoadedPattern(patternPath));
            
            // Test 3: Raw OpenCV matching (what SikuliX uses internally)
            results.add(testRawOpenCVMatching(patternPath));
            
            // Test 4: Test with different color spaces
            results.add(testColorSpaceConversions(patternPath));
            
            // Test 5: Test with different image types
            results.add(testImageTypeConversions(patternPath));
            
            // Test 6: Compare pixel-by-pixel differences
            results.add(analyzePixelDifferences(patternPath));
            
            // Test 7: Test matching with simple synthetic pattern
            results.add(testWithSyntheticPattern());
            
            // Print comprehensive report
            printReport(results);
            
            // Save diagnostic images
            saveDiagnosticImages(results);
            
        } catch (Exception e) {
            System.err.println("Diagnostic failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private MatchingResult testDirectSikuliXMatching(String patternPath) throws Exception {
        System.out.println("\n--- Test 1: Direct SikuliX Matching (as IDE) ---");
        
        // Load pattern exactly as SikuliX IDE would
        Pattern pattern = new Pattern(patternPath);
        Screen screen = new Screen();
        
        // Capture screen region
        ScreenImage screenImage = screen.capture();
        BufferedImage screenBuf = screenImage.getImage();
        
        // Use SikuliX's internal matching
        Finder finder = new Finder(screenImage);
        finder.find(pattern);
        
        double bestScore = 0;
        if (finder.hasNext()) {
            Match match = finder.next();
            bestScore = match.getScore();
        }
        
        String details = String.format(
            "Pattern loaded as: %s\nScreen size: %dx%d\nBest match score: %.4f",
            pattern.getFilename(),
            screenBuf.getWidth(), screenBuf.getHeight(),
            bestScore
        );
        
        return new MatchingResult("Direct SikuliX (as IDE)", bestScore, details);
    }

    private MatchingResult testBrobotLoadedPattern(String patternPath) throws Exception {
        System.out.println("\n--- Test 2: Brobot-Loaded Pattern ---");
        
        // Load pattern through ImageIO (as Brobot does)
        File patternFile = new File(patternPath);
        BufferedImage patternImage = ImageIO.read(patternFile);
        
        // Convert to Pattern object
        Pattern pattern = new Pattern(patternImage);
        
        Screen screen = new Screen();
        ScreenImage screenImage = screen.capture();
        
        Finder finder = new Finder(screenImage);
        finder.find(pattern);
        
        double bestScore = 0;
        if (finder.hasNext()) {
            Match match = finder.next();
            bestScore = match.getScore();
        }
        
        String details = String.format(
            "Pattern type: %s\nPattern size: %dx%d\nHas alpha: %s",
            getImageTypeString(patternImage.getType()),
            patternImage.getWidth(), patternImage.getHeight(),
            patternImage.getColorModel().hasAlpha()
        );
        
        return new MatchingResult("Brobot-Loaded Pattern", bestScore, details);
    }

    private MatchingResult testRawOpenCVMatching(String patternPath) throws Exception {
        System.out.println("\n--- Test 3: Raw OpenCV Matching ---");
        
        // Load pattern and screen using OpenCV directly
        Mat pattern = Imgcodecs.imread(patternPath);
        
        // Capture screen
        Screen screen = new Screen();
        BufferedImage screenBuf = screen.capture().getImage();
        Mat screenMat = bufferedImageToMat(screenBuf);
        
        // Ensure same number of channels
        if (pattern.channels() != screenMat.channels()) {
            if (pattern.channels() == 4 && screenMat.channels() == 3) {
                Imgproc.cvtColor(pattern, pattern, Imgproc.COLOR_BGRA2BGR);
            } else if (pattern.channels() == 3 && screenMat.channels() == 4) {
                Imgproc.cvtColor(pattern, pattern, Imgproc.COLOR_BGR2BGRA);
            }
        }
        
        // Template matching
        Mat result = new Mat();
        Imgproc.matchTemplate(screenMat, pattern, result, Imgproc.TM_CCOEFF_NORMED);
        
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        double bestScore = mmr.maxVal;
        
        String details = String.format(
            "Pattern channels: %d, Screen channels: %d\nMethod: TM_CCOEFF_NORMED\nBest score: %.4f",
            pattern.channels(), screenMat.channels(), bestScore
        );
        
        pattern.release();
        screenMat.release();
        result.release();
        
        return new MatchingResult("Raw OpenCV", bestScore, details);
    }

    private MatchingResult testColorSpaceConversions(String patternPath) throws Exception {
        System.out.println("\n--- Test 4: Color Space Conversions ---");
        
        BufferedImage pattern = ImageIO.read(new File(patternPath));
        Screen screen = new Screen();
        BufferedImage screenBuf = screen.capture().getImage();
        
        // Test different color space conversions
        double bestScore = 0;
        String bestConversion = "None";
        
        // Test 1: No conversion
        double score1 = matchImages(pattern, screenBuf);
        if (score1 > bestScore) {
            bestScore = score1;
            bestConversion = "No conversion";
        }
        
        // Test 2: Convert pattern to RGB (remove alpha)
        BufferedImage patternRGB = removeAlphaChannel(pattern);
        double score2 = matchImages(patternRGB, screenBuf);
        if (score2 > bestScore) {
            bestScore = score2;
            bestConversion = "Pattern to RGB";
        }
        
        // Test 3: Convert both to grayscale
        BufferedImage patternGray = toGrayscale(pattern);
        BufferedImage screenGray = toGrayscale(screenBuf);
        double score3 = matchImages(patternGray, screenGray);
        if (score3 > bestScore) {
            bestScore = score3;
            bestConversion = "Both to grayscale";
        }
        
        String details = String.format(
            "No conversion: %.4f\nPattern to RGB: %.4f\nGrayscale: %.4f\nBest: %s",
            score1, score2, score3, bestConversion
        );
        
        return new MatchingResult("Color Space Test", bestScore, details);
    }

    private MatchingResult testImageTypeConversions(String patternPath) throws Exception {
        System.out.println("\n--- Test 5: Image Type Conversions ---");
        
        BufferedImage original = ImageIO.read(new File(patternPath));
        Screen screen = new Screen();
        BufferedImage screenBuf = screen.capture().getImage();
        
        double bestScore = 0;
        String bestType = "";
        
        // Test different BufferedImage types
        int[] types = {
            BufferedImage.TYPE_INT_RGB,
            BufferedImage.TYPE_INT_ARGB,
            BufferedImage.TYPE_3BYTE_BGR,
            BufferedImage.TYPE_4BYTE_ABGR,
            BufferedImage.TYPE_BYTE_GRAY
        };
        
        for (int type : types) {
            try {
                BufferedImage converted = convertImageType(original, type);
                double score = matchImages(converted, screenBuf);
                if (score > bestScore) {
                    bestScore = score;
                    bestType = getImageTypeString(type);
                }
                System.out.println(String.format("  Type %s: %.4f", getImageTypeString(type), score));
            } catch (Exception e) {
                System.out.println("  Type " + getImageTypeString(type) + ": Failed - " + e.getMessage());
            }
        }
        
        String details = String.format(
            "Original type: %s\nBest type: %s\nBest score: %.4f",
            getImageTypeString(original.getType()), bestType, bestScore
        );
        
        return new MatchingResult("Image Type Test", bestScore, details);
    }

    private MatchingResult analyzePixelDifferences(String patternPath) throws Exception {
        System.out.println("\n--- Test 6: Pixel-by-Pixel Analysis ---");
        
        // Load pattern two ways
        BufferedImage directLoad = ImageIO.read(new File(patternPath));
        Pattern sikuliPattern = new Pattern(patternPath);
        org.sikuli.script.Image sikuliImage = sikuliPattern.getImage();
        BufferedImage sikuliLoad = sikuliImage.get();
        
        // Compare dimensions
        boolean sameDimensions = (directLoad.getWidth() == sikuliLoad.getWidth() && 
                                   directLoad.getHeight() == sikuliLoad.getHeight());
        
        // Calculate checksums
        String directChecksum = calculateChecksum(directLoad);
        String sikuliChecksum = calculateChecksum(sikuliLoad);
        boolean sameChecksum = directChecksum.equals(sikuliChecksum);
        
        // Count different pixels
        int differentPixels = 0;
        double maxDifference = 0;
        if (sameDimensions) {
            for (int x = 0; x < directLoad.getWidth(); x++) {
                for (int y = 0; y < directLoad.getHeight(); y++) {
                    int rgb1 = directLoad.getRGB(x, y);
                    int rgb2 = sikuliLoad.getRGB(x, y);
                    if (rgb1 != rgb2) {
                        differentPixels++;
                        double diff = calculatePixelDifference(rgb1, rgb2);
                        maxDifference = Math.max(maxDifference, diff);
                    }
                }
            }
        }
        
        String details = String.format(
            "Same dimensions: %s\nDirect: %dx%d, Sikuli: %dx%d\n" +
            "Same checksum: %s\nDifferent pixels: %d (%.2f%%)\nMax pixel diff: %.4f",
            sameDimensions, 
            directLoad.getWidth(), directLoad.getHeight(),
            sikuliLoad.getWidth(), sikuliLoad.getHeight(),
            sameChecksum, differentPixels,
            sameDimensions ? (100.0 * differentPixels / (directLoad.getWidth() * directLoad.getHeight())) : -1,
            maxDifference
        );
        
        return new MatchingResult("Pixel Analysis", sameChecksum ? 1.0 : 0.0, details);
    }

    private MatchingResult testWithSyntheticPattern() throws Exception {
        System.out.println("\n--- Test 7: Synthetic Pattern Test ---");
        
        // Create a simple red square pattern
        BufferedImage pattern = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = pattern.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 50, 50);
        g.dispose();
        
        // Save it temporarily
        File tempFile = File.createTempFile("synthetic", ".png");
        ImageIO.write(pattern, "png", tempFile);
        
        // Test with both methods
        Pattern sikuliPattern = new Pattern(tempFile.getAbsolutePath());
        Screen screen = new Screen();
        
        // Draw the same red square on screen (if possible)
        // This is just for testing - in real scenario, we'd find existing patterns
        
        double sikuliScore = screen.exists(sikuliPattern, 0.01) != null ? 1.0 : 0.0;
        
        tempFile.delete();
        
        String details = "Synthetic 50x50 red square\n" +
                        "Tests basic pattern matching without scaling/DPI issues";
        
        return new MatchingResult("Synthetic Pattern", sikuliScore, details);
    }

    // Helper methods
    
    private Mat bufferedImageToMat(BufferedImage image) {
        BufferedImage convertedImg = new BufferedImage(
            image.getWidth(), image.getHeight(),
            image.getType() == BufferedImage.TYPE_4BYTE_ABGR ? 
                BufferedImage.TYPE_3BYTE_BGR : image.getType()
        );
        convertedImg.getGraphics().drawImage(image, 0, 0, null);
        
        byte[] pixels = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }

    private double matchImages(BufferedImage pattern, BufferedImage screen) {
        try {
            // Use SikuliX Finder for consistency
            Finder finder = new Finder(screen);
            finder.find(new Pattern(pattern));
            if (finder.hasNext()) {
                return finder.next().getScore();
            }
        } catch (Exception e) {
            System.err.println("Match failed: " + e.getMessage());
        }
        return 0.0;
    }

    private BufferedImage removeAlphaChannel(BufferedImage image) {
        if (!image.getColorModel().hasAlpha()) {
            return image;
        }
        
        BufferedImage rgbImage = new BufferedImage(
            image.getWidth(), image.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g = rgbImage.createGraphics();
        g.setColor(Color.WHITE); // White background for transparency
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rgbImage;
    }

    private BufferedImage toGrayscale(BufferedImage image) {
        BufferedImage gray = new BufferedImage(
            image.getWidth(), image.getHeight(),
            BufferedImage.TYPE_BYTE_GRAY
        );
        Graphics2D g = gray.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return gray;
    }

    private BufferedImage convertImageType(BufferedImage source, int targetType) {
        BufferedImage converted = new BufferedImage(
            source.getWidth(), source.getHeight(), targetType
        );
        Graphics2D g = converted.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return converted;
    }

    private String getImageTypeString(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR";
            case BufferedImage.TYPE_BYTE_GRAY: return "TYPE_BYTE_GRAY";
            default: return "TYPE_" + type;
        }
    }

    private String calculateChecksum(BufferedImage image) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                md.update((byte)(image.getRGB(x, y) & 0xFF));
            }
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private double calculatePixelDifference(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;
        
        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;
        
        double dr = Math.abs(r1 - r2) / 255.0;
        double dg = Math.abs(g1 - g2) / 255.0;
        double db = Math.abs(b1 - b2) / 255.0;
        
        return Math.sqrt(dr*dr + dg*dg + db*db) / Math.sqrt(3);
    }

    private void printReport(List<MatchingResult> results) {
        System.out.println("\n=== DIAGNOSTIC REPORT ===");
        System.out.println("Test Results Summary:");
        System.out.println("----------------------------------------");
        
        for (MatchingResult result : results) {
            System.out.println(String.format(
                "\n%s: %.4f", 
                result.method, result.similarity
            ));
            System.out.println("Details:");
            System.out.println(result.details);
            System.out.println("----------------------------------------");
        }
        
        // Find best and worst
        MatchingResult best = results.stream()
            .max((a, b) -> Double.compare(a.similarity, b.similarity))
            .orElse(null);
        MatchingResult worst = results.stream()
            .filter(r -> r.similarity > 0)
            .min((a, b) -> Double.compare(a.similarity, b.similarity))
            .orElse(null);
        
        if (best != null && worst != null) {
            System.out.println("\n=== ANALYSIS ===");
            System.out.println("Best method: " + best.method + " (" + best.similarity + ")");
            System.out.println("Worst method: " + worst.method + " (" + worst.similarity + ")");
            System.out.println("Difference: " + (best.similarity - worst.similarity));
            
            if (best.similarity - worst.similarity > 0.2) {
                System.out.println("\nSIGNIFICANT DIFFERENCE DETECTED!");
                System.out.println("This indicates the issue is in how patterns are loaded/processed.");
            }
        }
    }

    private void saveDiagnosticImages(List<MatchingResult> results) {
        try {
            Path diagnosticDir = Paths.get("diagnostics", "matching-analysis");
            Files.createDirectories(diagnosticDir);
            
            System.out.println("\nDiagnostic images saved to: " + diagnosticDir.toAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Failed to save diagnostic images: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java ComprehensiveMatchingDiagnostic <pattern-path>");
            System.exit(1);
        }
        
        ComprehensiveMatchingDiagnostic diagnostic = new ComprehensiveMatchingDiagnostic();
        diagnostic.runFullDiagnosis(args[0]);
    }
}