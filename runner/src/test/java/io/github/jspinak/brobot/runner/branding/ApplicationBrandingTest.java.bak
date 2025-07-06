package io.github.jspinak.brobot.runner.branding;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationBrandingTest {

    @TempDir
    Path tempDir;
    
    @Test
    @DisplayName("Should generate all icon types")
    void shouldGenerateAllIconTypes() throws Exception {
        // Change to temp directory for test
        System.setProperty("user.dir", tempDir.toString());
        
        // Generate icons
        ApplicationBranding.generateIcons();
        
        // Verify icons directory created
        Path iconsDir = tempDir.resolve("packaging/icons");
        assertTrue(Files.exists(iconsDir));
        
        // Verify main icons
        assertTrue(Files.exists(iconsDir.resolve("brobot.png")));
        assertTrue(Files.exists(iconsDir.resolve("brobot.svg")));
        
        // Verify Windows icons
        assertTrue(Files.exists(iconsDir.resolve("brobot-16.png")));
        assertTrue(Files.exists(iconsDir.resolve("brobot-32.png")));
        assertTrue(Files.exists(iconsDir.resolve("brobot-48.png")));
        assertTrue(Files.exists(iconsDir.resolve("brobot-256.png")));
        
        // Verify macOS iconset
        assertTrue(Files.exists(iconsDir.resolve("brobot.iconset")));
        assertTrue(Files.exists(iconsDir.resolve("icon_16x16.png")));
        assertTrue(Files.exists(iconsDir.resolve("icon_32x32.png")));
        assertTrue(Files.exists(iconsDir.resolve("icon_128x128.png")));
        assertTrue(Files.exists(iconsDir.resolve("icon_256x256.png")));
        assertTrue(Files.exists(iconsDir.resolve("icon_512x512.png")));
        
        // Verify retina versions
        assertTrue(Files.exists(iconsDir.resolve("icon_16x16@2x.png")));
        assertTrue(Files.exists(iconsDir.resolve("icon_32x32@2x.png")));
        
        // Verify Linux icons
        assertTrue(Files.exists(iconsDir.resolve("brobot-64x64.png")));
        assertTrue(Files.exists(iconsDir.resolve("brobot-128x128.png")));
        assertTrue(Files.exists(iconsDir.resolve("brobot-256x256.png")));
        assertTrue(Files.exists(iconsDir.resolve("brobot-512x512.png")));
        
        // Verify file association icons
        assertTrue(Files.exists(iconsDir.resolve("brobot-config.png")));
        assertTrue(Files.exists(iconsDir.resolve("brobot-config-icon.png")));
        assertTrue(Files.exists(iconsDir.resolve("application-x-brobot.png")));
    }
    
    @Test
    @DisplayName("Should create valid PNG images")
    void shouldCreateValidPNGImages() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        ApplicationBranding.generateIcons();
        
        Path iconsDir = tempDir.resolve("packaging/icons");
        
        // Test main icon
        Path mainIcon = iconsDir.resolve("brobot.png");
        BufferedImage image = ImageIO.read(mainIcon.toFile());
        assertNotNull(image);
        assertEquals(256, image.getWidth());
        assertEquals(256, image.getHeight());
        
        // Test different sizes
        Path smallIcon = iconsDir.resolve("brobot-16.png");
        BufferedImage smallImage = ImageIO.read(smallIcon.toFile());
        assertEquals(16, smallImage.getWidth());
        assertEquals(16, smallImage.getHeight());
        
        Path largeIcon = iconsDir.resolve("brobot-512x512.png");
        BufferedImage largeImage = ImageIO.read(largeIcon.toFile());
        assertEquals(512, largeImage.getWidth());
        assertEquals(512, largeImage.getHeight());
    }
    
    @Test
    @DisplayName("Should create valid SVG file")
    void shouldCreateValidSVGFile() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        ApplicationBranding.generateIcons();
        
        Path svgFile = tempDir.resolve("packaging/icons/brobot.svg");
        assertTrue(Files.exists(svgFile));
        
        String content = Files.readString(svgFile);
        assertTrue(content.contains("<?xml version=\"1.0\""));
        assertTrue(content.contains("<svg"));
        assertTrue(content.contains("width=\"256\""));
        assertTrue(content.contains("height=\"256\""));
        assertTrue(content.contains("<circle"));
        assertTrue(content.contains("<text"));
    }
    
    @Test
    @DisplayName("Should create macOS iconset structure")
    void shouldCreateMacOSIconsetStructure() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        ApplicationBranding.generateIcons();
        
        Path iconsetDir = tempDir.resolve("packaging/icons/brobot.iconset");
        assertTrue(Files.exists(iconsetDir));
        assertTrue(Files.isDirectory(iconsetDir));
        
        // Verify ICNS creation script
        Path scriptFile = tempDir.resolve("packaging/icons/create-icns.sh");
        assertTrue(Files.exists(scriptFile));
        assertTrue(Files.isExecutable(scriptFile));
        
        String scriptContent = Files.readString(scriptFile);
        assertTrue(scriptContent.contains("iconutil"));
        assertTrue(scriptContent.contains("brobot.iconset"));
        assertTrue(scriptContent.contains("brobot.icns"));
    }
    
    @Test
    @DisplayName("Should create metadata files")
    void shouldCreateMetadataFiles() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        ApplicationBranding.createMetadataFiles();
        
        Path readmeFile = tempDir.resolve("packaging/icons/README.md");
        assertTrue(Files.exists(readmeFile));
        
        String content = Files.readString(readmeFile);
        assertTrue(content.contains("# Brobot Runner Icons"));
        assertTrue(content.contains("## Icon Files"));
        assertTrue(content.contains("### Application Icons"));
        assertTrue(content.contains("### File Association Icons"));
        assertTrue(content.contains("## Generating Platform-Specific Icons"));
    }
    
    @Test
    @DisplayName("Should create file association icons")
    void shouldCreateFileAssociationIcons() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        ApplicationBranding.generateIcons();
        
        Path iconsDir = tempDir.resolve("packaging/icons");
        
        // Check file icons exist
        Path configIcon = iconsDir.resolve("brobot-config.png");
        assertTrue(Files.exists(configIcon));
        
        BufferedImage configImage = ImageIO.read(configIcon.toFile());
        assertNotNull(configImage);
        assertEquals(256, configImage.getWidth());
        assertEquals(256, configImage.getHeight());
        
        // Verify other platform file icons
        assertTrue(Files.exists(iconsDir.resolve("brobot-config-icon.png")));
        assertTrue(Files.exists(iconsDir.resolve("application-x-brobot.png")));
    }
    
    @Test
    @DisplayName("Should handle icon generation errors gracefully")
    void shouldHandleIconGenerationErrors() throws Exception {
        // Create read-only directory to trigger error
        Path readOnlyDir = tempDir.resolve("readonly");
        Files.createDirectories(readOnlyDir);
        readOnlyDir.toFile().setReadOnly();
        
        System.setProperty("user.dir", readOnlyDir.toString());
        
        // Should throw IOException due to permissions
        assertThrows(IOException.class, () -> {
            ApplicationBranding.generateIcons();
        });
    }
}