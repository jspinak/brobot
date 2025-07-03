package io.github.jspinak.brobot.runner.json.validation.resource;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageResourceValidatorTest {
    
    private ImageResourceValidator validator;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        validator = new ImageResourceValidator();
    }
    
    @Test
    void testValidateImageResources_NullProjectJson() {
        // Execute
        ValidationResult result = validator.validateImageResources(null, tempDir);
        
        // Verify
        assertTrue(result.hasErrors());
        List<ValidationError> errors = result.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Invalid project JSON", errors.get(0).errorCode());
        assertEquals(ValidationSeverity.ERROR, errors.get(0).severity());
    }
    
    @Test
    void testValidateImageResources_NullBasePath() {
        // Setup
        JSONObject projectJson = new JSONObject();
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, null);
        
        // Verify
        assertTrue(result.hasErrors());
        List<ValidationError> errors = result.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Invalid image base path", errors.get(0).errorCode());
    }
    
    @Test
    void testValidateImageResources_NonExistentBasePath() {
        // Setup
        JSONObject projectJson = new JSONObject();
        Path nonExistentPath = tempDir.resolve("nonexistent");
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, nonExistentPath);
        
        // Verify
        assertTrue(result.hasErrors());
        List<ValidationError> errors = result.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Invalid image base path", errors.get(0).errorCode());
    }
    
    @Test
    void testValidateImageResources_BasePathIsFile() throws IOException {
        // Setup
        JSONObject projectJson = new JSONObject();
        Path filePath = Files.createFile(tempDir.resolve("file.txt"));
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, filePath);
        
        // Verify
        assertTrue(result.hasErrors());
        List<ValidationError> errors = result.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Invalid image base path", errors.get(0).errorCode());
    }
    
    @Test
    void testValidateImageResources_ValidImage() throws IOException {
        // Setup
        Path imageFile = createValidImageFile("button.png");
        JSONObject projectJson = createProjectJsonWithImage("button.png");
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, tempDir);
        
        // Verify
        assertFalse(result.hasErrors());
    }
    
    @Test
    void testValidateImageResources_MissingImage() {
        // Setup
        JSONObject projectJson = createProjectJsonWithImage("missing.png");
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, tempDir);
        
        // Verify
        assertTrue(result.hasErrors());
        List<ValidationError> errors = result.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Missing image resource", errors.get(0).errorCode());
        assertTrue(errors.get(0).message().contains("missing.png"));
    }
    
    @Test
    void testValidateImageResources_DirectoryInsteadOfFile() throws IOException {
        // Setup
        Path subDir = Files.createDirectory(tempDir.resolve("image.png"));
        JSONObject projectJson = createProjectJsonWithImage("image.png");
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, tempDir);
        
        // Verify
        assertTrue(result.hasErrors());
        List<ValidationError> errors = result.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Invalid image resource", errors.get(0).errorCode());
        assertTrue(errors.get(0).message().contains("not a file"));
    }
    
    @Test
    void testValidateImageResources_NoExtension() throws IOException {
        // Setup
        Path imageFile = Files.createFile(tempDir.resolve("image"));
        JSONObject projectJson = createProjectJsonWithImage("image");
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, tempDir);
        
        // Verify
        assertTrue(result.hasErrors());
        List<ValidationError> errors = result.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Invalid image format", errors.get(0).errorCode());
        assertEquals(ValidationSeverity.WARNING, errors.get(0).severity());
        assertTrue(errors.get(0).message().contains("no extension"));
    }
    
    @Test
    void testValidateImageResources_UnsupportedExtension() throws IOException {
        // Setup
        Path imageFile = Files.createFile(tempDir.resolve("image.xyz"));
        JSONObject projectJson = createProjectJsonWithImage("image.xyz");
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, tempDir);
        
        // Verify
        assertTrue(result.hasErrors());
        List<ValidationError> errors = result.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Unsupported image format", errors.get(0).errorCode());
        assertEquals(ValidationSeverity.WARNING, errors.get(0).severity());
        assertTrue(errors.get(0).message().contains("xyz"));
    }
    
    @Test
    void testValidateImageResources_InvalidImageContent() throws IOException {
        // Setup - create a file with .png extension but invalid content
        Path imageFile = tempDir.resolve("fake.png");
        Files.write(imageFile, "This is not an image".getBytes());
        JSONObject projectJson = createProjectJsonWithImage("fake.png");
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, tempDir);
        
        // Verify
        assertTrue(result.hasErrors());
        List<ValidationError> errors = result.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Invalid image format", errors.get(0).errorCode());
        assertEquals(ValidationSeverity.WARNING, errors.get(0).severity());
    }
    
    @Test
    void testValidateImageResources_SmallImage() throws IOException {
        // Setup - create a 1x1 pixel image
        Path imageFile = createSmallImageFile("tiny.png", 1, 1);
        JSONObject projectJson = createProjectJsonWithImage("tiny.png");
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, tempDir);
        
        // Verify
        assertTrue(result.hasErrors());
        List<ValidationError> errors = result.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Suspicious image dimensions", errors.get(0).errorCode());
        assertEquals(ValidationSeverity.WARNING, errors.get(0).severity());
        assertTrue(errors.get(0).message().contains("1x1"));
    }
    
    @Test
    void testValidateImageResources_AbsolutePath() throws IOException {
        // Setup
        Path imageFile = createValidImageFile("absolute.png");
        JSONObject projectJson = createProjectJsonWithImage(imageFile.toAbsolutePath().toString());
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, tempDir);
        
        // Verify
        assertFalse(result.hasErrors());
    }
    
    @Test
    void testValidateImageResources_MultipleImages() throws IOException {
        // Setup
        Path image1 = createValidImageFile("img1.png");
        Path image2 = createValidImageFile("img2.jpg");
        JSONObject projectJson = createProjectJsonWithMultipleImages("img1.png", "img2.jpg", "missing.png");
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, tempDir);
        
        // Verify
        assertTrue(result.hasErrors());
        List<ValidationError> errors = result.getErrors();
        assertEquals(1, errors.size()); // Only missing.png should error
        assertTrue(errors.get(0).message().contains("missing.png"));
    }
    
    @Test
    void testValidateImageResources_EmptyProject() {
        // Setup
        JSONObject projectJson = new JSONObject();
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, tempDir);
        
        // Verify
        assertFalse(result.hasErrors());
    }
    
    @Test
    void testValidateImageResources_EmptyImagePath() {
        // Setup
        JSONObject pattern = new JSONObject();
        pattern.put("imgPath", "");
        
        JSONArray patterns = new JSONArray();
        patterns.put(pattern);
        
        JSONObject stateImage = new JSONObject();
        stateImage.put("patterns", patterns);
        
        JSONArray stateImages = new JSONArray();
        stateImages.put(stateImage);
        
        JSONObject state = new JSONObject();
        state.put("stateImages", stateImages);
        
        JSONArray states = new JSONArray();
        states.put(state);
        
        JSONObject projectJson = new JSONObject();
        projectJson.put("states", states);
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, tempDir);
        
        // Verify
        assertFalse(result.hasErrors()); // Empty paths are ignored
    }
    
    @Test
    void testValidateImageResources_DuplicateImages() throws IOException {
        // Setup - Same image referenced multiple times
        Path imageFile = createValidImageFile("shared.png");
        
        JSONObject projectJson = new JSONObject();
        JSONArray states = new JSONArray();
        
        // Create two states referencing the same image
        for (int i = 0; i < 2; i++) {
            JSONObject pattern = new JSONObject();
            pattern.put("imgPath", "shared.png");
            
            JSONArray patterns = new JSONArray();
            patterns.put(pattern);
            
            JSONObject stateImage = new JSONObject();
            stateImage.put("patterns", patterns);
            
            JSONArray stateImages = new JSONArray();
            stateImages.put(stateImage);
            
            JSONObject state = new JSONObject();
            state.put("stateImages", stateImages);
            
            states.put(state);
        }
        
        projectJson.put("states", states);
        
        // Execute
        ValidationResult result = validator.validateImageResources(projectJson, tempDir);
        
        // Verify
        assertFalse(result.hasErrors()); // Duplicate references are fine
    }
    
    // Helper methods
    
    private JSONObject createProjectJsonWithImage(String imagePath) {
        JSONObject pattern = new JSONObject();
        pattern.put("imgPath", imagePath);
        
        JSONArray patterns = new JSONArray();
        patterns.put(pattern);
        
        JSONObject stateImage = new JSONObject();
        stateImage.put("patterns", patterns);
        
        JSONArray stateImages = new JSONArray();
        stateImages.put(stateImage);
        
        JSONObject state = new JSONObject();
        state.put("stateImages", stateImages);
        
        JSONArray states = new JSONArray();
        states.put(state);
        
        JSONObject projectJson = new JSONObject();
        projectJson.put("states", states);
        
        return projectJson;
    }
    
    private JSONObject createProjectJsonWithMultipleImages(String... imagePaths) {
        JSONArray patterns = new JSONArray();
        for (String path : imagePaths) {
            JSONObject pattern = new JSONObject();
            pattern.put("imgPath", path);
            patterns.put(pattern);
        }
        
        JSONObject stateImage = new JSONObject();
        stateImage.put("patterns", patterns);
        
        JSONArray stateImages = new JSONArray();
        stateImages.put(stateImage);
        
        JSONObject state = new JSONObject();
        state.put("stateImages", stateImages);
        
        JSONArray states = new JSONArray();
        states.put(state);
        
        JSONObject projectJson = new JSONObject();
        projectJson.put("states", states);
        
        return projectJson;
    }
    
    private Path createValidImageFile(String filename) throws IOException {
        return createSmallImageFile(filename, 10, 10);
    }
    
    private Path createSmallImageFile(String filename, int width, int height) throws IOException {
        Path imagePath = tempDir.resolve(filename);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, getExtension(filename), imagePath.toFile());
        return imagePath;
    }
    
    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "png";
    }
}