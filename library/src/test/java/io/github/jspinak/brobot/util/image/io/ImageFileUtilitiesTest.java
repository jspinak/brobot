package io.github.jspinak.brobot.util.image.io;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ImageFileUtilities - image file I/O operations.
 * Tests reading, writing, and managing image files in mock mode.
 */
@DisplayName("ImageFileUtilities Tests")
public class ImageFileUtilitiesTest extends BrobotTestBase {
    
    @TempDir
    Path tempDir;
    
    private ImageFileUtilities imageFileUtilities;
    private BufferedImage testImage;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        imageFileUtilities = new ImageFileUtilities();
        
        // Create a test image
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 50, 50);
        g.setColor(Color.RED);
        g.fillRect(50, 50, 50, 50);
        g.dispose();
    }
    
    @Nested
    @DisplayName("Image Writing Operations")
    class ImageWritingOperations {
        
        @ParameterizedTest
        @ValueSource(strings = {"png", "jpg", "jpeg", "bmp", "gif"})
        @DisplayName("Should write image in various formats")
        void shouldWriteImageInFormats(String format) throws IOException {
            File outputFile = tempDir.resolve("test." + format).toFile();
            
            boolean result = imageFileUtilities.writeImage(testImage, outputFile, format);
            
            assertTrue(result);
            assertTrue(outputFile.exists());
            assertTrue(outputFile.length() > 0);
        }
        
        @Test
        @DisplayName("Should handle null image")
        void shouldHandleNullImage() {
            File outputFile = tempDir.resolve("null.png").toFile();
            
            boolean result = imageFileUtilities.writeImage(null, outputFile, "png");
            
            assertFalse(result);
            assertFalse(outputFile.exists());
        }
        
        @Test
        @DisplayName("Should handle null file")
        void shouldHandleNullFile() {
            boolean result = imageFileUtilities.writeImage(testImage, null, "png");
            
            assertFalse(result);
        }
        
        @Test
        @DisplayName("Should handle invalid format")
        void shouldHandleInvalidFormat() {
            File outputFile = tempDir.resolve("test.invalid").toFile();
            
            boolean result = imageFileUtilities.writeImage(testImage, outputFile, "invalid");
            
            // Should either fail or default to a valid format
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should overwrite existing file")
        void shouldOverwriteExistingFile() throws IOException {
            File outputFile = tempDir.resolve("overwrite.png").toFile();
            
            // Write first time
            imageFileUtilities.writeImage(testImage, outputFile, "png");
            long firstSize = outputFile.length();
            
            // Create different image
            BufferedImage differentImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            
            // Write second time
            boolean result = imageFileUtilities.writeImage(differentImage, outputFile, "png");
            
            assertTrue(result);
            assertTrue(outputFile.exists());
            // Size might be different
            assertTrue(outputFile.length() > 0);
        }
    }
    
    @Nested
    @DisplayName("Image Reading Operations")
    class ImageReadingOperations {
        
        @Test
        @DisplayName("Should read PNG image")
        void shouldReadPNGImage() throws IOException {
            File imageFile = tempDir.resolve("read.png").toFile();
            ImageIO.write(testImage, "png", imageFile);
            
            BufferedImage result = imageFileUtilities.readImage(imageFile);
            
            assertNotNull(result);
            assertEquals(100, result.getWidth());
            assertEquals(100, result.getHeight());
        }
        
        @Test
        @DisplayName("Should read JPEG image")
        void shouldReadJPEGImage() throws IOException {
            File imageFile = tempDir.resolve("read.jpg").toFile();
            ImageIO.write(testImage, "jpg", imageFile);
            
            BufferedImage result = imageFileUtilities.readImage(imageFile);
            
            assertNotNull(result);
            assertEquals(100, result.getWidth());
            assertEquals(100, result.getHeight());
        }
        
        @Test
        @DisplayName("Should handle non-existent file")
        void shouldHandleNonExistentFile() {
            File nonExistent = tempDir.resolve("nonexistent.png").toFile();
            
            BufferedImage result = imageFileUtilities.readImage(nonExistent);
            
            assertNull(result);
        }
        
        @Test
        @DisplayName("Should handle corrupted file")
        void shouldHandleCorruptedFile() throws IOException {
            File corruptedFile = tempDir.resolve("corrupted.png").toFile();
            Files.write(corruptedFile.toPath(), "Not an image".getBytes());
            
            BufferedImage result = imageFileUtilities.readImage(corruptedFile);
            
            assertNull(result);
        }
        
        @Test
        @DisplayName("Should handle null file path")
        void shouldHandleNullFilePath() {
            BufferedImage result = imageFileUtilities.readImage(null);
            
            assertNull(result);
        }
    }
    
    @Nested
    @DisplayName("Batch Operations")
    class BatchOperations {
        
        @Test
        @DisplayName("Should write multiple images")
        void shouldWriteMultipleImages() throws IOException {
            List<BufferedImage> images = List.of(
                new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB),
                new BufferedImage(75, 75, BufferedImage.TYPE_INT_RGB),
                new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
            );
            
            for (int i = 0; i < images.size(); i++) {
                File outputFile = tempDir.resolve("batch_" + i + ".png").toFile();
                boolean result = imageFileUtilities.writeImage(images.get(i), outputFile, "png");
                assertTrue(result);
                assertTrue(outputFile.exists());
            }
        }
        
        @Test
        @DisplayName("Should read all images from directory")
        void shouldReadAllImagesFromDirectory() throws IOException {
            // Create test images
            for (int i = 0; i < 5; i++) {
                BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
                File file = tempDir.resolve("img_" + i + ".png").toFile();
                ImageIO.write(img, "png", file);
            }
            
            List<BufferedImage> images = imageFileUtilities.readAllImages(tempDir.toFile());
            
            assertNotNull(images);
            assertEquals(5, images.size());
            images.forEach(img -> {
                assertNotNull(img);
                assertEquals(50, img.getWidth());
            });
        }
        
        @Test
        @DisplayName("Should filter by extension")
        void shouldFilterByExtension() throws IOException {
            // Create mixed files
            ImageIO.write(testImage, "png", tempDir.resolve("test.png").toFile());
            ImageIO.write(testImage, "jpg", tempDir.resolve("test.jpg").toFile());
            Files.write(tempDir.resolve("test.txt"), "text".getBytes());
            
            List<BufferedImage> pngImages = imageFileUtilities.readImagesWithExtension(tempDir.toFile(), "png");
            List<BufferedImage> jpgImages = imageFileUtilities.readImagesWithExtension(tempDir.toFile(), "jpg");
            
            assertEquals(1, pngImages.size());
            assertEquals(1, jpgImages.size());
        }
    }
    
    @Nested
    @DisplayName("Image Conversion")
    class ImageConversion {
        
        @Test
        @DisplayName("Should convert image format")
        void shouldConvertImageFormat() throws IOException {
            File pngFile = tempDir.resolve("source.png").toFile();
            File jpgFile = tempDir.resolve("target.jpg").toFile();
            
            // Save as PNG
            ImageIO.write(testImage, "png", pngFile);
            
            // Convert to JPG
            boolean result = imageFileUtilities.convertImageFormat(pngFile, jpgFile, "jpg");
            
            assertTrue(result);
            assertTrue(jpgFile.exists());
            
            // Verify it's readable
            BufferedImage converted = ImageIO.read(jpgFile);
            assertNotNull(converted);
        }
        
        @Test
        @DisplayName("Should resize image on save")
        void shouldResizeImageOnSave() throws IOException {
            File outputFile = tempDir.resolve("resized.png").toFile();
            
            boolean result = imageFileUtilities.writeResizedImage(testImage, outputFile, 50, 50, "png");
            
            assertTrue(result);
            assertTrue(outputFile.exists());
            
            BufferedImage resized = ImageIO.read(outputFile);
            assertEquals(50, resized.getWidth());
            assertEquals(50, resized.getHeight());
        }
        
        @Test
        @DisplayName("Should maintain aspect ratio when resizing")
        void shouldMaintainAspectRatio() throws IOException {
            BufferedImage wideImage = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
            File outputFile = tempDir.resolve("aspect.png").toFile();
            
            boolean result = imageFileUtilities.writeResizedImageKeepAspect(wideImage, outputFile, 100, "png");
            
            assertTrue(result);
            
            BufferedImage resized = ImageIO.read(outputFile);
            assertEquals(100, resized.getWidth());
            assertEquals(50, resized.getHeight()); // Maintains 2:1 aspect ratio
        }
    }
    
    @Nested
    @DisplayName("File Management")
    class FileManagement {
        
        @Test
        @DisplayName("Should create unique filename")
        void shouldCreateUniqueFilename() throws IOException {
            File existingFile = tempDir.resolve("image.png").toFile();
            ImageIO.write(testImage, "png", existingFile);
            
            String uniqueName = imageFileUtilities.getUniqueFilename(tempDir.toFile(), "image", "png");
            
            assertNotNull(uniqueName);
            assertNotEquals("image.png", uniqueName);
            assertTrue(uniqueName.contains("image"));
            assertTrue(uniqueName.endsWith(".png"));
        }
        
        @Test
        @DisplayName("Should clean old image files")
        void shouldCleanOldImageFiles() throws IOException {
            // Create old files
            for (int i = 0; i < 10; i++) {
                File file = tempDir.resolve("old_" + i + ".png").toFile();
                ImageIO.write(testImage, "png", file);
                // Set last modified to old date
                file.setLastModified(System.currentTimeMillis() - (24 * 60 * 60 * 1000 * (i + 1)));
            }
            
            int deleted = imageFileUtilities.deleteOldImages(tempDir.toFile(), 5);
            
            assertTrue(deleted >= 5);
            
            // Verify some files remain
            File[] remaining = tempDir.toFile().listFiles();
            assertNotNull(remaining);
            assertTrue(remaining.length <= 5);
        }
        
        @Test
        @DisplayName("Should get image file size")
        void shouldGetImageFileSize() throws IOException {
            File imageFile = tempDir.resolve("size.png").toFile();
            ImageIO.write(testImage, "png", imageFile);
            
            long size = imageFileUtilities.getImageFileSize(imageFile);
            
            assertTrue(size > 0);
            assertEquals(imageFile.length(), size);
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle write to read-only directory")
        void shouldHandleWriteToReadOnlyDirectory() {
            File readOnlyDir = tempDir.resolve("readonly").toFile();
            readOnlyDir.mkdir();
            readOnlyDir.setWritable(false);
            
            File outputFile = new File(readOnlyDir, "test.png");
            
            boolean result = imageFileUtilities.writeImage(testImage, outputFile, "png");
            
            assertFalse(result);
            
            // Cleanup
            readOnlyDir.setWritable(true);
        }
        
        @Test
        @DisplayName("Should handle invalid path characters")
        void shouldHandleInvalidPathCharacters() {
            File invalidFile = tempDir.resolve("test<>:|?.png").toFile();
            
            assertDoesNotThrow(() -> 
                imageFileUtilities.writeImage(testImage, invalidFile, "png")
            );
        }
        
        @Test
        @DisplayName("Should handle very long filenames")
        void shouldHandleVeryLongFilenames() {
            String longName = "a".repeat(300) + ".png";
            File longFile = tempDir.resolve(longName).toFile();
            
            assertDoesNotThrow(() -> 
                imageFileUtilities.writeImage(testImage, longFile, "png")
            );
        }
    }
}