package io.github.jspinak.brobot.tools.diagnostics;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Quick comparison tool to test the hypothesis that SikuliX loads patterns
 * differently when using Pattern(String) vs Pattern(BufferedImage).
 * 
 * Run this with: java -cp <classpath> QuickMatchComparison <pattern-path>
 */
public class QuickMatchComparison {
    
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java QuickMatchComparison <pattern-path>");
            System.exit(1);
        }
        
        String patternPath = args[0];
        File patternFile = new File(patternPath);
        
        if (!patternFile.exists()) {
            System.err.println("Pattern file not found: " + patternPath);
            System.exit(1);
        }
        
        System.out.println("=== QUICK MATCH COMPARISON ===");
        System.out.println("Pattern: " + patternPath);
        System.out.println("Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println();
        
        // Capture screen once
        Screen screen = new Screen();
        ScreenImage screenImage = screen.capture();
        
        // Method 1: Load pattern as SikuliX IDE would (String path)
        System.out.println("Method 1: Pattern(String) - as SikuliX IDE");
        Pattern pattern1 = new Pattern(patternPath);
        Finder finder1 = new Finder(screenImage);
        finder1.find(pattern1);
        
        double score1 = 0;
        if (finder1.hasNext()) {
            Match match = finder1.next();
            score1 = match.getScore();
            System.out.println("  Found at: " + match.getTarget());
            System.out.println("  Similarity: " + score1);
        } else {
            System.out.println("  Not found");
        }
        
        // Method 2: Load pattern through BufferedImage (as Brobot might)
        System.out.println("\nMethod 2: Pattern(BufferedImage) - as Brobot");
        BufferedImage bufferedImage = ImageIO.read(patternFile);
        Pattern pattern2 = new Pattern(bufferedImage);
        Finder finder2 = new Finder(screenImage);
        finder2.find(pattern2);
        
        double score2 = 0;
        if (finder2.hasNext()) {
            Match match = finder2.next();
            score2 = match.getScore();
            System.out.println("  Found at: " + match.getTarget());
            System.out.println("  Similarity: " + score2);
        } else {
            System.out.println("  Not found");
        }
        
        // Method 3: Use Image.create() (another Brobot possibility)
        System.out.println("\nMethod 3: Image.create(BufferedImage)");
        org.sikuli.script.Image sikuliImage = new org.sikuli.script.Image(bufferedImage);
        Pattern pattern3 = new Pattern(sikuliImage);
        Finder finder3 = new Finder(screenImage);
        finder3.find(pattern3);
        
        double score3 = 0;
        if (finder3.hasNext()) {
            Match match = finder3.next();
            score3 = match.getScore();
            System.out.println("  Found at: " + match.getTarget());
            System.out.println("  Similarity: " + score3);
        } else {
            System.out.println("  Not found");
        }
        
        // Method 4: Direct file path with similarity threshold
        System.out.println("\nMethod 4: Pattern with similar()");
        Pattern pattern4 = new Pattern(patternPath).similar(0.5);
        Finder finder4 = new Finder(screenImage);
        finder4.find(pattern4);
        
        double score4 = 0;
        if (finder4.hasNext()) {
            Match match = finder4.next();
            score4 = match.getScore();
            System.out.println("  Found at: " + match.getTarget());
            System.out.println("  Similarity: " + score4);
        } else {
            System.out.println("  Not found");
        }
        
        // Compare internal image properties
        System.out.println("\n=== PATTERN PROPERTIES ===");
        
        System.out.println("\nPattern1 (String path):");
        Image img1 = pattern1.getImage();
        System.out.println("  Size: " + img1.getSize());
        System.out.println("  URL: " + img1.getURL());
        System.out.println("  IsValid: " + img1.isValid());
        
        System.out.println("\nPattern2 (BufferedImage):");
        Image img2 = pattern2.getImage();
        System.out.println("  Size: " + img2.getSize());
        System.out.println("  URL: " + img2.getURL());
        System.out.println("  IsValid: " + img2.isValid());
        
        System.out.println("\nBufferedImage properties:");
        System.out.println("  Type: " + bufferedImage.getType());
        System.out.println("  Size: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
        System.out.println("  HasAlpha: " + bufferedImage.getColorModel().hasAlpha());
        
        // Summary
        System.out.println("\n=== SUMMARY ===");
        System.out.println("Pattern(String):        " + String.format("%.4f", score1));
        System.out.println("Pattern(BufferedImage): " + String.format("%.4f", score2));
        System.out.println("Image.create():         " + String.format("%.4f", score3));
        System.out.println("Pattern.similar(0.5):   " + String.format("%.4f", score4));
        
        double difference = Math.abs(score1 - score2);
        if (difference > 0.1) {
            System.out.println("\n*** SIGNIFICANT DIFFERENCE DETECTED: " + String.format("%.4f", difference) + " ***");
            System.out.println("This confirms the loading method affects matching!");
        }
    }
}