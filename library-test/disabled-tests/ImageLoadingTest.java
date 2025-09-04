package io.github.jspinak.brobot.integration.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Screen;
import org.sikuli.script.Match;
import org.sikuli.script.FindFailed;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;

/**
 * Test to debug why images aren't being found even with 0.7 similarity.
 */
@SpringBootTest
public class ImageLoadingTest extends BrobotTestBase {
    
    @Test
    public void testImagePaths() {
        System.out.println("\n================================================================================");
        System.out.println("IMAGE LOADING AND PATH TEST");
        System.out.println("================================================================================\n");
        
        // Check current working directory
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        
        // Check SikuliX ImagePath
        System.out.println("\n--- SikuliX ImagePath ---");
        java.util.List<ImagePath.PathEntry> paths = ImagePath.getPaths();
        System.out.println("Number of paths: " + paths.size());
        for (ImagePath.PathEntry entry : paths) {
            System.out.println("  " + entry.getPath());
        }
        
        // Check if images directory exists
        System.out.println("\n--- Images Directory Check ---");
        File imagesDir = new File("images");
        System.out.println("images/ exists: " + imagesDir.exists());
        System.out.println("images/ absolute path: " + imagesDir.getAbsolutePath());
        
        if (imagesDir.exists()) {
            // List subdirectories
            File[] subdirs = imagesDir.listFiles(File::isDirectory);
            if (subdirs != null) {
                System.out.println("Subdirectories:");
                for (File dir : subdirs) {
                    System.out.println("  " + dir.getName() + "/");
                    
                    // List images in each subdirectory
                    File[] images = dir.listFiles((d, name) -> 
                        name.endsWith(".png") || name.endsWith(".jpg"));
                    if (images != null) {
                        for (File img : images) {
                            System.out.println("    " + img.getName());
                        }
                    }
                }
            }
        }
        
        // Test loading specific images
        System.out.println("\n--- Testing Specific Image Loading ---");
        testImageFile("images/prompt/claude-prompt-1.png");
        testImageFile("images/prompt/claude-prompt-2.png");
        testImageFile("images/prompt/claude-prompt-3.png");
        testImageFile("images/working/claude-icon-1.png");
        testImageFile("images/working/claude-icon-2.png");
        testImageFile("images/working/claude-icon-3.png");
        testImageFile("images/working/claude-icon-4.png");
        
        // Test StateImage pattern loading
        System.out.println("\n--- Testing StateImage Pattern Loading ---");
        PromptState promptState = new PromptState();
        StateImage claudePrompt = promptState.getClaudePrompt();
        
        System.out.println("ClaudePrompt StateImage:");
        System.out.println("  Name: " + claudePrompt.getName());
        System.out.println("  Patterns: " + claudePrompt.getPatterns().size());
        
        for (Pattern pattern : claudePrompt.getPatterns()) {
            System.out.println("\n  Pattern: " + pattern.getName());
            System.out.println("    URL: " + pattern.getUrl());
            System.out.println("    ImgPath: " + pattern.getImgpath());
            
            if (pattern.getImage() != null) {
                System.out.println("    Image loaded: YES");
                System.out.println("    Image size: " + pattern.getImage().w() + "x" + pattern.getImage().h());
            } else {
                System.out.println("    Image loaded: NO");
            }
            
            // Check search regions
            if (pattern.getSearchRegions() != null && 
                pattern.getSearchRegions().getFixedRegion() != null) {
                System.out.println("    Search region: " + pattern.getSearchRegions().getFixedRegion());
            }
        }
        
        // Test direct SikuliX pattern matching
        System.out.println("\n--- Direct SikuliX Pattern Test ---");
        testDirectSikuliX();
        
        System.out.println("\n================================================================================");
        System.out.println("TEST COMPLETE");
        System.out.println("================================================================================\n");
    }
    
    private void testImageFile(String path) {
        File file = new File(path);
        System.out.print(path + ": ");
        
        if (file.exists()) {
            System.out.print("EXISTS");
            
            // Try to load as image
            try {
                BufferedImage img = ImageIO.read(file);
                System.out.print(", Size: " + img.getWidth() + "x" + img.getHeight());
            } catch (Exception e) {
                System.out.print(", ERROR loading: " + e.getMessage());
            }
        } else {
            System.out.print("NOT FOUND");
        }
        System.out.println();
    }
    
    private void testDirectSikuliX() {
        try {
            Screen screen = new Screen();
            
            // Test with absolute path
            File testFile = new File("images/prompt/claude-prompt-1.png");
            if (testFile.exists()) {
                String absolutePath = testFile.getAbsolutePath();
                System.out.println("Testing with: " + absolutePath);
                
                org.sikuli.script.Pattern pattern = new org.sikuli.script.Pattern(absolutePath);
                pattern.similar(0.7);
                
                System.out.println("Searching for pattern with 0.7 similarity...");
                
                try {
                    Match match = screen.find(pattern);
                    System.out.println("FOUND! Match: " + match);
                    System.out.println("  Location: (" + match.x + "," + match.y + ")");
                    System.out.println("  Score: " + match.getScore());
                } catch (FindFailed e) {
                    System.out.println("NOT FOUND via direct SikuliX");
                    
                    // Try with exists() which is more lenient
                    Match exists = screen.exists(pattern, 1.0);
                    if (exists != null) {
                        System.out.println("But exists() found it: " + exists);
                    } else {
                        System.out.println("exists() also failed");
                    }
                }
            } else {
                System.out.println("Test image not found: " + testFile.getAbsolutePath());
            }
            
        } catch (Exception e) {
            System.err.println("Error in direct SikuliX test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}