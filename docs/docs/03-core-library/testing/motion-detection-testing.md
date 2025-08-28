# Motion Detection Testing Guide

## Overview
This guide covers testing strategies for motion detection components in Brobot, including proper use of the OpenCV mock system and best practices for writing reliable tests.

## Prerequisites
- Understanding of Brobot's mock mode (FrameworkSettings.mock)
- Familiarity with OpenCV concepts (Mat, pixel analysis)
- Knowledge of Spring dependency injection

## Test Setup

### Extending BrobotTestBase
All motion detection tests must extend `BrobotTestBase`:
```java
import io.github.jspinak.brobot.test.BrobotTestBase;

public class MotionDetectionTest extends BrobotTestBase {
    
    @Autowired
    private MotionAnalyzer motionAnalyzer;
    
    @Test
    public void testMotionDetection() {
        // Mock mode is automatically enabled
        List<BufferedImage> images = loadTestImages();
        MotionResult result = motionAnalyzer.analyzeMotion(images);
        
        assertTrue(result.isMotionDetected());
    }
}
```

### Configuration for Tests
Configure mock behavior in test resources:
```properties
# src/test/resources/application-test.properties
brobot.mock=true
brobot.opencv.mock.motion.default-confidence=0.75
brobot.opencv.mock.motion.region-count=2
brobot.opencv.mock.replay.enabled=true
brobot.opencv.mock.replay.directory=src/test/resources/motion-recordings
```

## Writing Motion Detection Tests

### Basic Motion Detection Test
```java
@Test
@DisplayName("Should detect motion between different images")
public void shouldDetectMotionBetweenDifferentImages() {
    // Arrange
    List<BufferedImage> images = Arrays.asList(
        loadImage("frame1.png"),
        loadImage("frame2.png"),
        loadImage("frame3.png")
    );
    
    // Act
    MotionResult result = motionAnalyzer.analyzeMotion(images);
    
    // Assert
    assertTrue(result.isMotionDetected());
    assertFalse(result.getMotionRegions().isEmpty());
    assertTrue(result.getConfidenceScore() > 0.5);
}
```

### Testing with Custom Options
```java
@Test
@DisplayName("Should apply custom threshold for motion detection")
public void shouldApplyCustomThreshold() {
    // Arrange
    MotionOptions options = MotionOptions.builder()
        .threshold(100)  // High threshold
        .useGrayscale(true)
        .blurRadius(5)
        .build();
    
    List<BufferedImage> images = loadSequentialFrames();
    
    // Act
    MotionResult result = motionAnalyzer.analyzeMotionWithOptions(images, options);
    
    // Assert
    // High threshold should reduce detected motion
    assertTrue(result.getMotionRegions().size() < 5);
}
```

### Testing No Motion Scenarios
```java
@Test
@DisplayName("Should detect no motion for identical images")
public void shouldDetectNoMotionForIdenticalImages() {
    // Arrange
    BufferedImage sameImage = loadImage("static.png");
    List<BufferedImage> images = Collections.nCopies(5, sameImage);
    
    // Act
    MotionResult result = motionAnalyzer.analyzeMotion(images);
    
    // Assert
    assertFalse(result.isMotionDetected());
    assertTrue(result.getMotionRegions().isEmpty());
    assertEquals(0.0, result.getConfidenceScore(), 0.01);
}
```

## Mock Data Configuration

### Configuring Mock Responses
```java
@TestConfiguration
public class MotionTestConfig {
    
    @Bean
    @Primary
    public MockConfiguration customMockConfig() {
        return MockConfiguration.builder()
            .motionDetectionBehavior(MotionBehavior.REALISTIC)
            .defaultConfidence(0.85)
            .minRegionSize(100)
            .maxRegionSize(500)
            .noiseLevel(0.1)
            .build();
    }
}
```

### Using Record and Replay
```java
@Test
@EnableRecording  // Custom annotation to enable recording
public void recordRealMotionData() {
    // This test will record real OpenCV outputs when run with mock=false
    FrameworkSettings.mock = false;
    
    List<BufferedImage> images = loadRealWorldSequence();
    MotionResult result = motionAnalyzer.analyzeMotion(images);
    
    // Results are automatically recorded for replay in mock mode
    assertTrue(result.isMotionDetected());
}

@Test
@UseRecording("recordRealMotionData")  // Replay recorded data
public void testWithRecordedData() {
    FrameworkSettings.mock = true;
    
    List<BufferedImage> images = loadRealWorldSequence();
    MotionResult result = motionAnalyzer.analyzeMotion(images);
    
    // Uses recorded data for consistent testing
    assertTrue(result.isMotionDetected());
}
```

## Testing Patterns

### Parameterized Testing
```java
@ParameterizedTest
@CsvSource({
    "2, true, 0.5",   // 2 images, motion expected, min confidence
    "3, true, 0.7",   // 3 images, motion expected, higher confidence
    "5, false, 0.0"   // 5 identical images, no motion
})
void testMotionDetectionScenarios(int imageCount, boolean expectedMotion, double minConfidence) {
    List<BufferedImage> images = generateImageSequence(imageCount, expectedMotion);
    
    MotionResult result = motionAnalyzer.analyzeMotion(images);
    
    assertEquals(expectedMotion, result.isMotionDetected());
    if (expectedMotion) {
        assertTrue(result.getConfidenceScore() >= minConfidence);
    }
}
```

### Contract Testing
```java
@Test
public void testMockRealContract() {
    List<BufferedImage> testImages = loadStandardTestSequence();
    
    // Test with mock
    FrameworkSettings.mock = true;
    MotionResult mockResult = motionAnalyzer.analyzeMotion(testImages);
    
    // Test with real (if available)
    if (isOpenCVAvailable()) {
        FrameworkSettings.mock = false;
        MotionResult realResult = motionAnalyzer.analyzeMotion(testImages);
        
        // Results should be structurally similar
        assertEquals(mockResult.isMotionDetected(), realResult.isMotionDetected());
        assertEquals(mockResult.getMotionRegions().size(), 
                    realResult.getMotionRegions().size(), 2);  // Allow small variance
    }
}
```

### Edge Case Testing
```java
@Nested
@DisplayName("Edge Cases")
class EdgeCaseTests {
    
    @Test
    @DisplayName("Should handle empty image list")
    void shouldHandleEmptyImageList() {
        List<BufferedImage> emptyList = Collections.emptyList();
        
        MotionResult result = motionAnalyzer.analyzeMotion(emptyList);
        
        assertNotNull(result);
        assertFalse(result.isMotionDetected());
        assertTrue(result.getMotionRegions().isEmpty());
    }
    
    @Test
    @DisplayName("Should handle single image")
    void shouldHandleSingleImage() {
        List<BufferedImage> singleImage = Arrays.asList(loadImage("test.png"));
        
        MotionResult result = motionAnalyzer.analyzeMotion(singleImage);
        
        assertNotNull(result);
        assertFalse(result.isMotionDetected());  // Can't detect motion with one image
    }
    
    @Test
    @DisplayName("Should handle very small images")
    void shouldHandleVerySmallImages() {
        List<BufferedImage> tinyImages = Arrays.asList(
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
        );
        
        MotionResult result = motionAnalyzer.analyzeMotion(tinyImages);
        
        assertNotNull(result);
        // Should complete without errors
    }
}
```

## Performance Testing

### Mock Performance Validation
```java
@Test
@Timeout(value = 1, unit = TimeUnit.SECONDS)
public void testMockPerformance() {
    FrameworkSettings.mock = true;
    List<BufferedImage> images = loadLargeImageSequence();  // 10 HD images
    
    long startTime = System.nanoTime();
    
    for (int i = 0; i < 100; i++) {
        MotionResult result = motionAnalyzer.analyzeMotion(images);
        assertNotNull(result);
    }
    
    long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    
    // Mock should be very fast
    assertTrue(duration < 500, "100 iterations should complete in under 500ms");
}
```

### Memory Usage Testing
```java
@Test
public void testMemoryEfficiency() {
    FrameworkSettings.mock = true;
    
    Runtime runtime = Runtime.getRuntime();
    long initialMemory = runtime.totalMemory() - runtime.freeMemory();
    
    // Process many images
    for (int i = 0; i < 1000; i++) {
        List<BufferedImage> images = generateRandomImages(5);
        MotionResult result = motionAnalyzer.analyzeMotion(images);
        assertNotNull(result);
    }
    
    System.gc();
    long finalMemory = runtime.totalMemory() - runtime.freeMemory();
    long memoryIncrease = finalMemory - initialMemory;
    
    // Should not leak memory
    assertTrue(memoryIncrease < 50_000_000, "Memory increase should be less than 50MB");
}
```

## Integration Testing

### Testing with Spring Context
```java
@SpringBootTest
@ActiveProfiles("test")
public class MotionDetectionIntegrationTest extends BrobotTestBase {
    
    @Autowired
    private ApplicationContext context;
    
    @Test
    public void testSpringWiring() {
        // Verify correct bean is loaded based on mock mode
        PixelAnalyzer analyzer = context.getBean(PixelAnalyzer.class);
        
        if (FrameworkSettings.mock) {
            assertInstanceOf(MockPixelAnalyzer.class, analyzer);
        } else {
            assertInstanceOf(OpenCVPixelAnalyzer.class, analyzer);
        }
    }
    
    @Test
    public void testEndToEndMotionDetection() {
        // Test complete flow from image loading to result processing
        List<BufferedImage> images = imageLoader.loadSequence("test-sequence");
        MotionResult result = motionAnalyzer.analyzeMotion(images);
        
        if (result.isMotionDetected()) {
            List<Region> regions = result.getMotionRegions();
            regions.forEach(region -> {
                assertTrue(region.getWidth() > 0);
                assertTrue(region.getHeight() > 0);
            });
        }
    }
}
```

## Debugging Test Failures

### Enable Detailed Logging
```properties
# application-test.properties
logging.level.io.github.jspinak.brobot.analysis.motion=DEBUG
brobot.opencv.mock.debug=true
brobot.opencv.mock.trace-calls=true
```

### Using Test Fixtures
```java
@BeforeEach
public void setupTestFixtures() {
    // Create consistent test data
    testImages = TestImageGenerator.createMotionSequence(
        width: 640,
        height: 480,
        frameCount: 5,
        motionType: MotionType.LINEAR
    );
    
    // Configure mock to return predictable results
    when(mockDataGenerator.generateMotionResult(any(), any()))
        .thenReturn(createExpectedMotionResult());
}
```

### Assertion Helpers
```java
public class MotionAssertions {
    
    public static void assertValidMotionResult(MotionResult result) {
        assertNotNull(result, "MotionResult should not be null");
        assertNotNull(result.getMotionRegions(), "Motion regions should not be null");
        assertTrue(result.getConfidenceScore() >= 0 && result.getConfidenceScore() <= 1,
                  "Confidence score should be between 0 and 1");
        
        if (result.isMotionDetected()) {
            assertFalse(result.getMotionRegions().isEmpty(),
                       "Motion detected but no regions found");
        }
    }
    
    public static void assertMotionInRegion(MotionResult result, Region expectedRegion) {
        assertTrue(result.isMotionDetected(), "Expected motion to be detected");
        
        boolean foundInRegion = result.getMotionRegions().stream()
            .anyMatch(region -> region.overlaps(expectedRegion));
        
        assertTrue(foundInRegion, 
                  "Expected motion in region: " + expectedRegion);
    }
}
```

## CI/CD Considerations

### GitHub Actions Configuration
```yaml
# .github/workflows/motion-tests.yml
name: Motion Detection Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK
      uses: actions/setup-java@v2
      with:
        java-version: '11'
    
    - name: Run Motion Detection Tests
      run: |
        ./gradlew test --tests "*Motion*Test"
      env:
        BROBOT_MOCK: true  # Ensure mock mode in CI
    
    - name: Upload Test Results
      if: always()
      uses: actions/upload-artifact@v2
      with:
        name: motion-test-results
        path: build/reports/tests/
```

### Docker Testing
```dockerfile
# Dockerfile.test
FROM openjdk:11-jdk-slim

WORKDIR /app

COPY . .

# Run tests in mock mode (no display needed)
ENV BROBOT_MOCK=true

RUN ./gradlew test --tests "*Motion*Test"
```

## Best Practices

1. **Always extend BrobotTestBase** for proper mock configuration
2. **Use result objects** instead of raw Mat objects
3. **Configure mock behavior** appropriately for test scenarios
4. **Test edge cases** including empty inputs and extreme values
5. **Validate performance** to ensure mock efficiency
6. **Use parameterized tests** for comprehensive coverage
7. **Implement contract tests** to ensure mock/real consistency
8. **Record real data** for realistic mock responses
9. **Add integration tests** for end-to-end validation
10. **Monitor test execution time** to maintain fast feedback

## Troubleshooting

### Common Issues and Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| NullPointerException in tests | Direct OpenCV usage | Use analyzer interfaces |
| Tests fail in CI but pass locally | Display dependencies | Ensure mock mode is enabled |
| Inconsistent test results | Random mock data | Use fixed seeds or recordings |
| Slow test execution | Real OpenCV operations | Verify mock mode is active |
| Out of memory errors | Large image processing | Reduce image size in tests |

## Related Documentation
- [OpenCV Mock System Architecture](../opencv-mock-system/architecture.md)
- [Enhanced Mock Testing Documentation](./enhanced-mocking.md)
- [Test Logging Architecture](./test-logging-architecture.md)