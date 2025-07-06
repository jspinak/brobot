package io.github.jspinak.brobot.runner.resources;

import lombok.Data;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.testutil.TestMat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Data
public class ImageResourceManagerTest {

    @Mock
    private ResourceManager resourceManager;

    @Mock
    private EventBus eventBus;

    @Mock
    private BrobotRunnerProperties properties;

    private ImageResourceManager imageResourceManager;

    @BeforeEach
    public void setup() {
        lenient().when(properties.getImagePath()).thenReturn("./images");
        imageResourceManager = new ImageResourceManager(resourceManager, eventBus, properties);
        // We need to register a shutdown hook for the scheduler
        verify(resourceManager).registerResource(eq(imageResourceManager), any(String.class));
    }

    @Test
    public void testCacheImage() throws Exception {
        // Create a test image
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        // Cache the image
        imageResourceManager.cacheImage("test", bufferedImage);

        // Verify cachedMemoryUsed is updated
        long expectedSize = 100L * 100L * 4L; // width * height * 4 bytes per pixel

        // Use reflection to access private field
        Field cachedMemoryUsedField = ImageResourceManager.class.getDeclaredField("cachedMemoryUsed");
        cachedMemoryUsedField.setAccessible(true);
        long actualCachedMemory = ((java.util.concurrent.atomic.AtomicLong) cachedMemoryUsedField.get(imageResourceManager)).get();

        assertEquals(expectedSize, actualCachedMemory);

        // Verify event was published
        verify(eventBus, atLeastOnce()).publish(any(LogEvent.class));
    }

    @Test
    public void testRegisterAndReleaseMat() {
        // Create test Mat object
        TestMat mat = new TestMat();

        // Register Mat
        imageResourceManager.registerMat(mat);

        // Verify active mat count
        assertEquals(1, imageResourceManager.getActiveMatCount());

        // Release Mat
        imageResourceManager.releaseMat(mat);

        // Verify Mat was released
        assertTrue(mat.isReleased());

        // Verify active mat count
        assertEquals(0, imageResourceManager.getActiveMatCount());
    }

    @Test
    public void testUpdateStateImageCache() {
        // Create a test StateImage with Patterns
        StateImage stateImage = new StateImage();

        // Create mock Pattern objects
        Pattern pattern1 = mock(Pattern.class);
        Pattern pattern2 = mock(Pattern.class);

        // Mock BufferedImage
        BufferedImage buffImg1 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        BufferedImage buffImg2 = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);

        // Configure patterns
        when(pattern1.isEmpty()).thenReturn(false);
        when(pattern1.getBImage()).thenReturn(buffImg1);
        when(pattern1.getName()).thenReturn("pattern1");

        when(pattern2.isEmpty()).thenReturn(false);
        when(pattern2.getBImage()).thenReturn(buffImg2);
        when(pattern2.getName()).thenReturn("pattern2");

        // Add patterns to StateImage
        stateImage.setPatterns(Arrays.asList(pattern1, pattern2));

        // Update cache
        imageResourceManager.updateStateImageCache(stateImage);

        // Verify image count
        assertEquals(2, imageResourceManager.getCachedImageCount());

        // Verify memory usage
        long expectedMemory = (50L * 50L * 4L) + (30L * 30L * 4L);
        assertEquals(expectedMemory, imageResourceManager.getCachedMemoryUsage());
    }

    @Test
    public void testTriggerCleanup() throws Exception {
        // Set up a large cached memory to trigger cleanup
        Field cachedMemoryUsedField = ImageResourceManager.class.getDeclaredField("cachedMemoryUsed");
        cachedMemoryUsedField.setAccessible(true);
        ((java.util.concurrent.atomic.AtomicLong) cachedMemoryUsedField.get(imageResourceManager)).set(1000 * 1024 * 1024); // 1GB

        // Set up imageSizes
        Field imageSizesField = ImageResourceManager.class.getDeclaredField("imageSizes");
        imageSizesField.setAccessible(true);
        Map<String, Long> imageSizes = (Map<String, Long>) imageSizesField.get(imageResourceManager);
        imageSizes.put("large1", 500L * 1024L * 1024L);
        imageSizes.put("large2", 300L * 1024L * 1024L);
        imageSizes.put("small1", 1L * 1024L * 1024L);

        // Set up imageCache with mock references
        Field imageCacheField = ImageResourceManager.class.getDeclaredField("imageCache");
        imageCacheField.setAccessible(true);
        Map<String, java.lang.ref.SoftReference<BufferedImage>> imageCache =
                (Map<String, java.lang.ref.SoftReference<BufferedImage>>) imageCacheField.get(imageResourceManager);

        // Add mock references
        imageCache.put("large1", new java.lang.ref.SoftReference<>(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)));
        imageCache.put("large2", new java.lang.ref.SoftReference<>(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)));
        imageCache.put("small1", new java.lang.ref.SoftReference<>(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)));

        // Access private method using reflection
        Field memoryThresholdField = ImageResourceManager.class.getDeclaredField("memoryThreshold");
        memoryThresholdField.setAccessible(true);
        long memoryThreshold = (long) memoryThresholdField.get(imageResourceManager);

        // Call checkMemoryUsage which should trigger cleanup
        java.lang.reflect.Method checkMemoryUsage = ImageResourceManager.class.getDeclaredMethod("checkMemoryUsage");
        checkMemoryUsage.setAccessible(true);
        java.lang.reflect.Method triggerCleanup = ImageResourceManager.class.getDeclaredMethod("triggerCleanup");
        triggerCleanup.setAccessible(true);
        triggerCleanup.invoke(imageResourceManager);

        // Verify warning event was published
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus, atLeastOnce()).publish(eventCaptor.capture());

        boolean foundWarningEvent = false;
        for (LogEvent event : eventCaptor.getAllValues()) {
            if (event.getLevel() == LogEvent.LogLevel.WARNING &&
                    event.getMessage().contains("Memory usage high")) {
                foundWarningEvent = true;
                break;
            }
        }
        assertTrue(foundWarningEvent, "Expected a warning about high memory usage");

        // After cleanup, the largest images should be removed
        assertFalse(imageCache.containsKey("large1"), "Largest image should be removed in cleanup");
    }

    @Test
    public void testClose() {
        // Use a test class instead of mocking Mat
        TestMat mat1 = new TestMat();
        TestMat mat2 = new TestMat();

        // Register Mats
        imageResourceManager.registerMat(mat1);
        imageResourceManager.registerMat(mat2);

        // Cache a test image
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        imageResourceManager.cacheImage("test", bufferedImage);

        // Close the manager
        imageResourceManager.close();

        // Verify all Mats were released
        assertTrue(mat1.isReleased());
        assertTrue(mat2.isReleased());

        // Verify cache is cleared
        assertEquals(0, imageResourceManager.getCachedImageCount());
        assertEquals(0, imageResourceManager.getCachedMemoryUsage());
        assertEquals(0, imageResourceManager.getActiveMatCount());
    }
}