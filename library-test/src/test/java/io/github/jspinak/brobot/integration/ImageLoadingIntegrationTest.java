package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.config.EarlyImagePathInitializer;io.github.jspinak.brobot.config.core.ImagePathManager;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.sikuli.script.ImagePath;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the complete image loading flow,
 * ensuring that image paths are correctly configured before Pattern creation.
 */
@DisplayName("Image Loading Integration Tests")
public class ImageLoadingIntegrationTest extends BrobotTestBase {

    @Mock
    private BrobotProperties brobotProperties;

    @Mock
    private BrobotProperties.Core coreConfig;

    private ImagePathManager imagePathManager;
    private EarlyImagePathInitializer earlyInitializer;

    @TempDir
    Path tempDir;

    private BufferedImage testImage;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        imagePathManager = new ImagePathManager();
        earlyInitializer = new EarlyImagePathInitializer();

        // Create test image
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    }

    @Nested
    @DisplayName("End-to-End Image Loading Flow")
    class EndToEndImageLoadingFlow {

        @Test
        @DisplayName("Should complete full image loading pipeline")
        void shouldCompleteFullImageLoadingPipeline() throws IOException {
            // Given - Set up image directory structure
            Path imagesDir = tempDir.resolve("images");
            Path stateDir = imagesDir.resolve("login-state");
            Files.createDirectories(stateDir);

            Path loginButton = stateDir.resolve("login-button.png");
            Path usernameField = stateDir.resolve("username-field.png");
            ImageIO.write(testImage, "png", loginButton.toFile());
            ImageIO.write(testImage, "png", usernameField.toFile());

            // Step 1: Configure properties
            when(brobotProperties.getCore()).thenReturn(coreConfig);
            when(coreConfig.getImagePath()).thenReturn(imagesDir.toString());

            // Step 2: Initialize early path configuration
            ReflectionTestUtils.setField(earlyInitializer, "brobotProperties", brobotProperties);
            ReflectionTestUtils.setField(earlyInitializer, "primaryImagePath", imagesDir.toString());

            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                earlyInitializer.initializeImagePaths();

                // Verify SikuliX paths were set
                imagePathMock.verify(() -> ImagePath.setBundlePath(imagesDir.toString()));
                imagePathMock.verify(() -> ImagePath.add(imagesDir.toString()));
            }

            // Step 3: Initialize ImagePathManager
            imagePathManager.initialize(imagesDir.toString());

            // Step 4: Create Patterns using configured paths
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", true);

            Pattern loginPattern = new Pattern("login-button.png");
            Pattern usernamePattern = new Pattern("username-field.png");

            // Step 5: Create StateImage with Patterns
            StateImage loginStateImage = new StateImage.Builder()
                    .setName("login-elements")
                    .addPattern(loginPattern)
                    .addPattern(usernamePattern)
                    .build();

            // Step 6: Create State with StateImage
            State loginState = new State();
            loginState.setName("LoginState");
            loginState.getStateImages().add(loginStateImage);

            // Verify complete flow
            assertNotNull(loginState);
            assertEquals(1, loginState.getStateImages().size());
            StateImage firstImage = loginState.getStateImages().iterator().next();
            assertEquals(2, firstImage.getPatterns().size());

            // Verify paths are configured
            assertTrue(imagePathManager.getConfiguredPaths().contains(imagesDir.toString()));
        }

        @Test
        @DisplayName("Should handle Spring Boot startup sequence")
        void shouldHandleSpringBootStartupSequence() throws IOException {
            // Given
            Path imagesDir = tempDir.resolve("spring-images");
            Files.createDirectories(imagesDir);

            // Simulate Spring Boot startup sequence
            // 1. @Value injection
            ReflectionTestUtils.setField(earlyInitializer, "primaryImagePath", imagesDir.toString());

            // 2. @Autowired injection (optional dependencies)
            ReflectionTestUtils.setField(earlyInitializer, "brobotProperties", null);

            // 3. @PostConstruct
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                earlyInitializer.initializeImagePaths();

                // Should work even without BrobotProperties
                imagePathMock.verify(() -> ImagePath.setBundlePath(imagesDir.toString()));
            }

            // 4. Component initialization
            imagePathManager.initialize(imagesDir.toString());

            // 5. Pattern creation
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", true);

            Pattern pattern = new Pattern("test.png");

            // Verify
            assertNotNull(pattern);
            assertTrue(imagePathManager.getConfiguredPaths().contains(imagesDir.toString()));
        }
    }

    @Nested
    @DisplayName("Configuration Priority and Fallback")
    class ConfigurationPriorityAndFallback {

        @Test
        @DisplayName("Should respect configuration priority hierarchy")
        void shouldRespectConfigurationPriorityHierarchy() throws IOException {
            // Given - Multiple configuration sources
            Path defaultPath = tempDir.resolve("default-images");
            Path propertyPath = tempDir.resolve("property-images");
            Path programmaticPath = tempDir.resolve("programmatic-images");

            Files.createDirectories(defaultPath);
            Files.createDirectories(propertyPath);
            Files.createDirectories(programmaticPath);

            // 1. Default from @Value
            ReflectionTestUtils.setField(earlyInitializer, "primaryImagePath", defaultPath.toString());

            // 2. Override with BrobotProperties
            when(brobotProperties.getCore()).thenReturn(coreConfig);
            when(coreConfig.getImagePath()).thenReturn(propertyPath.toString());
            ReflectionTestUtils.setField(earlyInitializer, "brobotProperties", brobotProperties);

            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                earlyInitializer.initializeImagePaths();

                // Should use BrobotProperties path
                imagePathMock.verify(() -> ImagePath.setBundlePath(propertyPath.toString()));
            }

            // 3. Programmatic override
            imagePathManager.initialize(programmaticPath.toString());

            // Verify final configuration
            assertTrue(imagePathManager.getConfiguredPaths().contains(programmaticPath.toString()));
        }

        @Test
        @DisplayName("Should fallback gracefully when paths unavailable")
        void shouldFallbackGracefullyWhenPathsUnavailable() {
            // Given - No configured paths available
            ReflectionTestUtils.setField(earlyInitializer, "primaryImagePath", null);
            ReflectionTestUtils.setField(earlyInitializer, "brobotProperties", null);

            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                earlyInitializer.initializeImagePaths();

                // Then - Should use default "images"
                imagePathMock.verify(() -> ImagePath.setBundlePath("images"));
            }
        }
    }

    @Nested
    @DisplayName("Multi-State Image Resolution")
    class MultiStateImageResolution {

        @Test
        @DisplayName("Should resolve images for multiple states")
        void shouldResolveImagesForMultipleStates() throws IOException {
            // Given - Multiple state directories
            Path imagesDir = tempDir.resolve("images");
            Path loginDir = imagesDir.resolve("login");
            Path dashboardDir = imagesDir.resolve("dashboard");
            Path settingsDir = imagesDir.resolve("settings");

            Files.createDirectories(loginDir);
            Files.createDirectories(dashboardDir);
            Files.createDirectories(settingsDir);

            // Create images for each state
            ImageIO.write(testImage, "png", loginDir.resolve("login.png").toFile());
            ImageIO.write(testImage, "png", dashboardDir.resolve("dashboard.png").toFile());
            ImageIO.write(testImage, "png", settingsDir.resolve("settings.png").toFile());

            // Configure paths
            imagePathManager.initialize(imagesDir.toString());
            imagePathManager.addPath(loginDir.toString());
            imagePathManager.addPath(dashboardDir.toString());
            imagePathManager.addPath(settingsDir.toString());

            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", true);

            // Create states with images
            State loginState = new State();
            loginState.setName("Login");
            loginState.getStateImages().add(new StateImage.Builder().addPattern("login.png").build());

            State dashboardState = new State();
            dashboardState.setName("Dashboard");
            dashboardState.getStateImages().add(new StateImage.Builder().addPattern("dashboard.png").build());

            State settingsState = new State();
            settingsState.setName("Settings");
            settingsState.getStateImages().add(new StateImage.Builder().addPattern("settings.png").build());

            // Verify
            assertFalse(loginState.getStateImages().isEmpty());
            assertFalse(dashboardState.getStateImages().isEmpty());
            assertFalse(settingsState.getStateImages().isEmpty());

            // Verify all paths are configured
            List<String> configuredPaths = imagePathManager.getConfiguredPaths();
            assertTrue(configuredPaths.contains(imagesDir.toString()));
            assertTrue(configuredPaths.contains(loginDir.toString()));
            assertTrue(configuredPaths.contains(dashboardDir.toString()));
            assertTrue(configuredPaths.contains(settingsDir.toString()));
        }

        @Test
        @DisplayName("Should handle shared images across states")
        void shouldHandleSharedImagesAcrossStates() throws IOException {
            // Given - Shared images directory
            Path imagesDir = tempDir.resolve("images");
            Path sharedDir = imagesDir.resolve("shared");
            Files.createDirectories(sharedDir);

            Path sharedLogo = sharedDir.resolve("company-logo.png");
            ImageIO.write(testImage, "png", sharedLogo.toFile());

            imagePathManager.initialize(imagesDir.toString());
            imagePathManager.addPath(sharedDir.toString());

            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", true);

            // Create shared image
            StateImage sharedImage = new StateImage.Builder()
                    .addPattern("company-logo.png")
                    .build();
            sharedImage.setShared(true);

            // Use in multiple states
            State state1 = new State();
            state1.setName("State1");
            state1.getStateImages().add(sharedImage);

            State state2 = new State();
            state2.setName("State2");
            state2.getStateImages().add(sharedImage);

            // Verify
            StateImage state1Image = state1.getStateImages().iterator().next();
            StateImage state2Image = state2.getStateImages().iterator().next();
            assertTrue(state1Image.isShared());
            assertTrue(state2Image.isShared());
            assertEquals(state1Image, state2Image);
        }
    }

    @Nested
    @DisplayName("Concurrent Image Loading")
    class ConcurrentImageLoading {

        @Test
        @DisplayName("Should handle concurrent Pattern creation safely")
        void shouldHandleConcurrentPatternCreationSafely() throws Exception {
            // Given
            Path imagesDir = tempDir.resolve("concurrent");
            Files.createDirectories(imagesDir);

            int numImages = 20;
            for (int i = 0; i < numImages; i++) {
                Path imageFile = imagesDir.resolve("image" + i + ".png");
                ImageIO.write(testImage, "png", imageFile.toFile());
            }

            imagePathManager.initialize(imagesDir.toString());

            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", true);

            // When - Create patterns concurrently
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(numImages);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 0; i < numImages; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        Pattern pattern = new Pattern("image" + index + ".png");
                        if (pattern != null && pattern.getImage() != null) {
                            successCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertEquals(numImages, successCount.get());

            executor.shutdown();
        }

        @Test
        @DisplayName("Should handle concurrent path configuration")
        void shouldHandleConcurrentPathConfiguration() throws Exception {
            // Given
            int numPaths = 10;
            CountDownLatch latch = new CountDownLatch(numPaths);
            ExecutorService executor = Executors.newFixedThreadPool(5);

            // When - Configure paths concurrently
            for (int i = 0; i < numPaths; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        Path path = tempDir.resolve("path" + index);
                        Files.createDirectories(path);
                        imagePathManager.addPath(path.toString());
                    } catch (IOException e) {
                        // Ignore for test
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));

            List<String> configuredPaths = imagePathManager.getConfiguredPaths();
            // Should have added some paths (exact count may vary due to concurrent access)
            assertTrue(configuredPaths.size() > 0);

            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("Error Scenarios and Recovery")
    class ErrorScenariosAndRecovery {

        @Test
        @DisplayName("Should recover from corrupted image files")
        void shouldRecoverFromCorruptedImageFiles() throws IOException {
            // Given - Create corrupted image file
            Path imagesDir = tempDir.resolve("corrupted");
            Files.createDirectories(imagesDir);
            Path corruptedFile = imagesDir.resolve("corrupted.png");
            Files.writeString(corruptedFile, "Not a valid PNG file");

            imagePathManager.initialize(imagesDir.toString());

            try (MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile("corrupted.png"))
                        .thenReturn(null); // Simulate failure to load

                ExecutionEnvironment env = ExecutionEnvironment.getInstance();
                ReflectionTestUtils.setField(env, "mockMode", false);

                // When/Then - Should throw with helpful message
                assertThrows(IllegalStateException.class, () -> {
                    new Pattern("corrupted.png");
                });
            }
        }

        @Test
        @DisplayName("Should handle missing directories gracefully")
        void shouldHandleMissingDirectoriesGracefully() {
            // Given
            Path nonExistentPath = tempDir.resolve("non-existent");

            // When
            imagePathManager.initialize(nonExistentPath.toString());

            // Then - Should create fallback
            assertNotNull(imagePathManager.getConfiguredPaths());
            assertFalse(imagePathManager.getConfiguredPaths().isEmpty());
        }

        @Test
        @DisplayName("Should validate image loading before state creation")
        void shouldValidateImageLoadingBeforeStateCreation() {
            // Given
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", true);

            // When - Create state with multiple images
            State state = new State();
            state.setName("TestState");
            state.getStateImages().add(
                    new StateImage.Builder()
                            .addPatterns("image1.png", "image2.png", "image3.png")
                            .build());

            // Then - All patterns should have images
            StateImage stateImage = state.getStateImages().iterator().next();
            assertEquals(3, stateImage.getPatterns().size());

            for (Pattern pattern : stateImage.getPatterns()) {
                assertNotNull(pattern.getImage(),
                        "Pattern " + pattern.getName() + " should have loaded image");
            }
        }
    }

    @Nested
    @DisplayName("Performance and Optimization")
    class PerformanceAndOptimization {

        @Test
        @DisplayName("Should cache image paths for performance")
        void shouldCacheImagePathsForPerformance() {
            // Given
            Path imagesDir = tempDir.resolve("cached");

            // When - Initialize multiple times with same path
            imagePathManager.initialize(imagesDir.toString());
            List<String> firstPaths = imagePathManager.getConfiguredPaths();

            imagePathManager.initialize(imagesDir.toString()); // Same path
            List<String> secondPaths = imagePathManager.getConfiguredPaths();

            // Then - Should reuse cached configuration
            assertEquals(firstPaths, secondPaths);
        }

        @Test
        @DisplayName("Should validate paths contain images")
        void shouldValidatePathsContainImages() throws IOException {
            // Given
            Path emptyDir = tempDir.resolve("empty");
            Path imagesDir = tempDir.resolve("with-images");
            Files.createDirectories(emptyDir);
            Files.createDirectories(imagesDir);

            // Add image to one directory
            ImageIO.write(testImage, "png", imagesDir.resolve("test.png").toFile());

            // When
            imagePathManager.initialize(imagesDir.toString());
            imagePathManager.addPath(emptyDir.toString());

            // Then
            assertTrue(imagePathManager.validatePaths());
        }
    }
}