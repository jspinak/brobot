package io.github.jspinak.brobot.runner.ui.config.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.scene.image.Image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;

class ImagePreviewServiceTest {

    private ImagePreviewService service;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        service = new ImagePreviewService();

        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    void testLoadImagePreviewWithNull() {
        // When
        Image result = service.loadImagePreview(null, mock(ConfigEntry.class));

        // Then
        assertNull(result);
    }

    @Test
    void testLoadImagePreviewWithEmptyName() {
        // When
        Image result = service.loadImagePreview("", mock(ConfigEntry.class));

        // Then
        assertNull(result);
    }

    @Test
    void testLoadImagePreviewWithNullConfig() {
        // When
        Image result = service.loadImagePreview("test", null);

        // Then
        assertNull(result);
    }

    @Test
    void testLoadImagePreviewSuccess() throws Exception {
        // Given
        ConfigEntry mockConfig = mock(ConfigEntry.class);
        when(mockConfig.getName()).thenReturn("TestConfig");
        when(mockConfig.getImagePath()).thenReturn(tempDir);

        // Create a test image file
        createTestImageFile(tempDir.resolve("test.png"));

        // When
        Image result = service.loadImagePreview("test", mockConfig);

        // Then
        assertNotNull(result);

        // Verify caching
        Image cached = service.loadImagePreview("test", mockConfig);
        assertSame(result, cached);
    }

    @Test
    void testLoadImagePreviewWithExtension() throws Exception {
        // Given
        ConfigEntry mockConfig = mock(ConfigEntry.class);
        when(mockConfig.getName()).thenReturn("TestConfig");
        when(mockConfig.getImagePath()).thenReturn(tempDir);

        // Create a test image file
        createTestImageFile(tempDir.resolve("test.png"));

        // When
        Image result = service.loadImagePreview("test.png", mockConfig);

        // Then
        assertNotNull(result);
    }

    @Test
    void testLoadImagePreviewNotFound() {
        // Given
        ConfigEntry mockConfig = mock(ConfigEntry.class);
        when(mockConfig.getName()).thenReturn("TestConfig");
        when(mockConfig.getImagePath()).thenReturn(tempDir);

        // When
        Image result = service.loadImagePreview("nonexistent", mockConfig);

        // Then
        assertNull(result);
    }

    @Test
    void testCacheManagement() throws Exception {
        // Given
        ConfigEntry mockConfig = mock(ConfigEntry.class);
        when(mockConfig.getName()).thenReturn("TestConfig");
        when(mockConfig.getImagePath()).thenReturn(tempDir);

        // Create test images
        for (int i = 0; i < 60; i++) {
            createTestImageFile(tempDir.resolve("test" + i + ".png"));
        }

        // When - Load more than cache limit
        for (int i = 0; i < 60; i++) {
            service.loadImagePreview("test" + i, mockConfig);
        }

        // Then - Cache should have been cleared at least once
        assertTrue(service.getCacheSize() <= 50);
    }

    @Test
    void testClearCache() throws Exception {
        // Given
        ConfigEntry mockConfig = mock(ConfigEntry.class);
        when(mockConfig.getName()).thenReturn("TestConfig");
        when(mockConfig.getImagePath()).thenReturn(tempDir);

        createTestImageFile(tempDir.resolve("test.png"));
        service.loadImagePreview("test", mockConfig);
        assertTrue(service.getCacheSize() > 0);

        // When
        service.clearCache();

        // Then
        assertEquals(0, service.getCacheSize());
    }

    @Test
    void testClearConfigCache() throws Exception {
        // Given
        ConfigEntry mockConfig1 = mock(ConfigEntry.class);
        when(mockConfig1.getName()).thenReturn("Config1");
        when(mockConfig1.getImagePath()).thenReturn(tempDir);

        ConfigEntry mockConfig2 = mock(ConfigEntry.class);
        when(mockConfig2.getName()).thenReturn("Config2");
        when(mockConfig2.getImagePath()).thenReturn(tempDir);

        createTestImageFile(tempDir.resolve("test.png"));

        service.loadImagePreview("test", mockConfig1);
        service.loadImagePreview("test", mockConfig2);
        assertEquals(2, service.getCacheSize());

        // When
        service.clearConfigCache("Config1");

        // Then
        assertEquals(1, service.getCacheSize());

        // Config2 image should still be cached
        Image cached = service.loadImagePreview("test", mockConfig2);
        assertNotNull(cached);
    }

    private void createTestImageFile(Path path) throws Exception {
        // Copy a small test image from resources or create a minimal PNG
        File file = path.toFile();
        file.getParentFile().mkdirs();

        // Create a minimal 1x1 PNG
        byte[] minimalPng = {
            (byte) 0x89,
            0x50,
            0x4E,
            0x47,
            0x0D,
            0x0A,
            0x1A,
            0x0A,
            0x00,
            0x00,
            0x00,
            0x0D,
            0x49,
            0x48,
            0x44,
            0x52,
            0x00,
            0x00,
            0x00,
            0x01,
            0x00,
            0x00,
            0x00,
            0x01,
            0x08,
            0x06,
            0x00,
            0x00,
            0x00,
            0x1F,
            0x15,
            (byte) 0xC4,
            (byte) 0x89,
            0x00,
            0x00,
            0x00,
            0x0D,
            0x49,
            0x44,
            0x41,
            0x54,
            0x08,
            0x5B,
            0x63,
            (byte) 0xF8,
            0x0F,
            0x00,
            0x01,
            0x01,
            0x01,
            0x00,
            0x1B,
            (byte) 0xB6,
            (byte) 0xEE,
            0x56,
            0x00,
            0x00,
            0x00,
            0x00,
            0x49,
            0x45,
            0x4E,
            0x44,
            (byte) 0xAE,
            0x42,
            0x60,
            (byte) 0x82
        };

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(minimalPng);
        }
    }
}
