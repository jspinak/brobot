# PROPOSAL: Motion Detection Testing Strategy

**Status**: 🔵 Proposed Architecture
**Created**: 2025
**Purpose**: Design specification for enhanced motion detection testing capabilities

---

## ⚠️ Important: This is a Proposal Document

This document describes a **PROPOSED** abstraction layer and testing strategy for motion detection that **does not currently exist** in Brobot. It serves as:

- **Design specification** for future motion detection API
- **Testing strategy blueprint** for when features are implemented
- **API design reference** for maintainers and contributors

### What Does NOT Exist (Proposed Components)

The following components are **design concepts only**:

| Component | Status | Description |
|-----------|--------|-------------|
| `MotionAnalyzer` | ❌ Not Implemented | Proposed high-level interface for motion analysis |
| `MotionResult` | ❌ Not Implemented | Proposed result object with confidence scores |
| `MotionOptions` | ❌ Not Implemented | Proposed builder-pattern configuration |
| `PixelAnalyzer` | ❌ Not Implemented | Proposed interface for pixel-level analysis |
| `@EnableRecording` | ❌ Not Implemented | Proposed annotation for test recording |
| `@UseRecording` | ❌ Not Implemented | Proposed annotation for test replay |
| `brobot.opencv.mock.*` properties | ❌ Not Implemented | Proposed configuration properties |

### Current Implementation (What Actually Exists)

Brobot currently provides these motion detection classes:

| Component | Status | Location |
|-----------|--------|----------|
| `MotionDetector` | ✅ Implemented | `brobot.analysis.motion.MotionDetector` |
| `PixelChangeDetector` | ✅ Implemented | `brobot.analysis.motion.PixelChangeDetector` |
| `MotionMetadata` | ✅ Implemented | `brobot.analysis.results.MotionMetadata` |
| `MotionFindOptions` | ✅ Implemented | `brobot.action.basic.find.motion.MotionFindOptions` |
| `DynamicPixelFinder` | ✅ Implemented | Motion detection interface |

**For testing the current implementation**, see:
- [Mat Testing Utilities](../docs/docs/04-testing/mat-testing-utilities.md) - Testing with OpenCV Mat objects
- [Mock Mode Guide](../docs/docs/04-testing/mock-mode-guide.md) - Mock mode configuration
- [Test Utilities](../docs/docs/04-testing/test-utilities.md) - BrobotTestBase and test helpers

---

## Proposal Overview

This proposal outlines a comprehensive testing strategy for an enhanced motion detection API that would provide:

1. **High-level abstraction** over OpenCV operations
2. **Rich result objects** with confidence scores and metadata
3. **Flexible configuration** via builder patterns
4. **Test recording and replay** for deterministic testing
5. **Seamless mock integration** with BrobotTestBase

---

## Table of Contents

1. [Proposed Architecture](#proposed-architecture)
2. [Test Setup](#test-setup)
3. [Writing Motion Detection Tests](#writing-motion-detection-tests)
4. [Mock Data Configuration](#mock-data-configuration)
5. [Testing Patterns](#testing-patterns)
6. [Performance Testing](#performance-testing)
7. [Integration Testing](#integration-testing)
8. [CI/CD Considerations](#cicd-considerations)
9. [Implementation Roadmap](#implementation-roadmap)
10. [Related Documentation](#related-documentation)

---

## Proposed Architecture

### Design Goals

The proposed motion detection testing architecture aims to:

- **Simplify testing** by abstracting OpenCV Mat operations
- **Enable deterministic tests** through recording and replay
- **Support mock mode** seamlessly in headless environments
- **Provide rich feedback** with confidence scores and region data
- **Integrate naturally** with existing Brobot testing patterns

### Prerequisites

- Understanding of Brobot's mock mode (`brobotProperties.getCore().isMock()`)
- Familiarity with OpenCV concepts (Mat, pixel analysis)
- Knowledge of Spring dependency injection
- Experience with JUnit 5 and testing patterns

### Current vs. Proposed API Comparison

**Current Implementation** (what exists today):

```java
import io.github.jspinak.brobot.analysis.motion.MotionDetector;
import org.opencv.core.Mat;

@Component
public class CurrentMotionExample {
    @Autowired
    private MotionDetector motionDetector;

    public void detectMotion() {
        // Works with OpenCV Mat objects directly
        Mat frame1 = loadMatFromImage("frame1.png");
        Mat frame2 = loadMatFromImage("frame2.png");

        // Returns Mat with dynamic pixel mask
        Mat dynamicPixels = motionDetector.getDynamicPixelMask(frame1, frame2);

        // Manual analysis required
        int motionPixels = countNonZeroPixels(dynamicPixels);
        boolean hasMotion = motionPixels > threshold;
    }
}
```

**Proposed Implementation** (this proposal):

```java
import io.github.jspinak.brobot.analysis.motion.MotionAnalyzer;  // PROPOSED
import java.awt.image.BufferedImage;

@Component
public class ProposedMotionExample {
    @Autowired
    private MotionAnalyzer motionAnalyzer;  // PROPOSED

    public void detectMotion() {
        // Works with BufferedImage directly
        List<BufferedImage> frames = loadImages("frame1.png", "frame2.png");

        // Returns rich result object with metadata
        MotionResult result = motionAnalyzer.analyzeMotion(frames);  // PROPOSED

        // Easy to use with built-in analysis
        if (result.isMotionDetected()) {
            double confidence = result.getConfidenceScore();
            List<Region> regions = result.getMotionRegions();
            // Ready to use in tests!
        }
    }
}
```

**Key Improvements**:
- 🎯 **Higher-level abstraction** - No direct Mat manipulation needed
- 📊 **Rich result objects** - Confidence scores, regions, metadata
- 🧪 **Testing-friendly** - BufferedImage instead of Mat
- 🔄 **Mock-compatible** - Seamless integration with BrobotTestBase
- 📝 **Better readability** - Clear API surface

---

## Test Setup

### Extending BrobotTestBase
All motion detection tests must extend `BrobotTestBase`:
```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

public class MotionDetectionTest extends BrobotTestBase {

    @Autowired
    private BrobotProperties brobotProperties;

    @Autowired
    private MotionAnalyzer motionAnalyzer;  // PROPOSED class - does not exist yet

    @Test
    public void testMotionDetection() {
        // Mock mode is automatically enabled by BrobotTestBase
        List<BufferedImage> images = loadTestImages();
        MotionResult result = motionAnalyzer.analyzeMotion(images);

        assertTrue(result.isMotionDetected());
    }

    // Helper method - implement based on your test data needs
    private List<BufferedImage> loadTestImages() {
        // TODO: Load from test resources or create test images
        return java.util.Arrays.asList(
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
        );
    }
}
```

### Configuration for Tests
Configure mock behavior in test resources:
```properties
# src/test/resources/application-test.properties
brobot.mock=true

# Note: The following properties are PROPOSED and do not currently exist:
# brobot.opencv.mock.motion.default-confidence=0.75
# brobot.opencv.mock.motion.region-count=2
# brobot.opencv.mock.replay.enabled=true
# brobot.opencv.mock.replay.directory=src/test/resources/motion-recordings
```

## Writing Motion Detection Tests

### Basic Motion Detection Test

> **Note**: All code examples in this proposal use PROPOSED classes that do not currently exist.

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Arrays;
import java.util.List;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

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
    MotionResult result = motionAnalyzer.analyzeMotion(images);  // PROPOSED API

    // Assert
    assertTrue(result.isMotionDetected());
    assertFalse(result.getMotionRegions().isEmpty());
    assertTrue(result.getConfidenceScore() > 0.5);
}

// Helper method implementation
private BufferedImage loadImage(String filename) {
    try {
        return ImageIO.read(new File("src/test/resources/" + filename));
    } catch (IOException e) {
        throw new RuntimeException("Failed to load test image: " + filename, e);
    }
}
```

### Testing with Custom Options
```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@Test
@DisplayName("Should apply custom threshold for motion detection")
public void shouldApplyCustomThreshold() {
    // Arrange
    MotionOptions options = MotionOptions.builder()  // PROPOSED API
        .threshold(100)  // High threshold
        .useGrayscale(true)
        .blurRadius(5)
        .build();

    List<BufferedImage> images = loadSequentialFrames();

    // Act
    MotionResult result = motionAnalyzer.analyzeMotionWithOptions(images, options);  // PROPOSED API

    // Assert
    // High threshold should reduce detected motion
    assertTrue(result.getMotionRegions().size() < 5);
}

// Helper method implementation
private List<BufferedImage> loadSequentialFrames() {
    List<BufferedImage> frames = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
        frames.add(loadImage("frame" + i + ".png"));
    }
    return frames;
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

> **Note**: This example shows PROPOSED API that does not currently exist. The actual `MockConfiguration` class is a Spring `@Configuration` class for defining beans, not a builder pattern configuration class.

```java
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MotionTestConfig {

    @Bean
    @Primary
    public MockConfiguration customMockConfig() {
        // PROPOSED API - does not currently exist
        return MockConfiguration.builder()
            .motionDetectionBehavior(MotionBehavior.REALISTIC)  // PROPOSED
            .defaultConfidence(0.85)  // PROPOSED
            .minRegionSize(100)  // PROPOSED
            .maxRegionSize(500)  // PROPOSED
            .noiseLevel(0.1)  // PROPOSED
            .build();
    }
}
```

### Using Record and Replay
```java
@Test
@EnableRecording  // PROPOSED annotation - does not currently exist
public void recordRealMotionData() {
    // This test will record real OpenCV outputs when run with mock=false
    // Mock mode is configured via application.properties:
    // brobot.mock=false
    
    List<BufferedImage> images = loadRealWorldSequence();
    MotionResult result = motionAnalyzer.analyzeMotion(images);
    
    // Results are automatically recorded for replay in mock mode
    assertTrue(result.isMotionDetected());
}

@Test
@UseRecording("recordRealMotionData")  // PROPOSED annotation - does not currently exist
public void testWithRecordedData() {
    // Mock mode is configured via application.properties:
    // brobot.mock=true
    
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
    // Mock mode is configured via application.properties:
    // brobot.mock=true
    MotionResult mockResult = motionAnalyzer.analyzeMotion(testImages);

    // Test with real (if available)
    if (isOpenCVAvailable()) {
        // Mock mode is configured via application.properties:
        // brobot.mock=false
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
    // Mock mode is configured via application.properties:
    // brobot.mock=true
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
    // Mock mode is configured via application.properties:
    // brobot.mock=true
    
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
        
        if (brobotProperties.getCore().isMock()) {
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
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@BeforeEach
public void setupTestFixtures() {
    // Create consistent test data using PROPOSED TestImageGenerator
    testImages = TestImageGenerator.builder()
        .width(640)
        .height(480)
        .frameCount(5)
        .motionType(MotionType.LINEAR)  // PROPOSED enum
        .build()
        .createMotionSequence();

    // Configure mock to return predictable results
    when(mockDataGenerator.generateMotionResult(any(), any()))
        .thenReturn(createExpectedMotionResult());
}

// Helper method
private MotionResult createExpectedMotionResult() {
    return MotionResult.builder()  // PROPOSED API
        .motionDetected(true)
        .confidenceScore(0.85)
        .motionRegions(List.of(
            new Region(10, 10, 100, 100),
            new Region(150, 50, 80, 80)
        ))
        .build();
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

When this proposed architecture is implemented, follow these best practices:

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

---

## Implementation Roadmap

### Phase 1: Core API Design (Estimated: 2-3 weeks)

**Objectives**:
- Define `MotionAnalyzer` interface with clear contracts
- Design `MotionResult` class with comprehensive metadata
- Create `MotionOptions` builder pattern
- Establish package structure

**Deliverables**:
- [ ] `MotionAnalyzer` interface definition
- [ ] `MotionResult` data class
- [ ] `MotionOptions` builder
- [ ] API documentation
- [ ] Design review completed

### Phase 2: Implementation (Estimated: 3-4 weeks)

**Objectives**:
- Implement `MotionAnalyzer` using existing `MotionDetector`
- Create mock implementation for testing
- Integrate with BrobotProperties for configuration
- Add Spring conditional bean loading

**Deliverables**:
- [ ] `OpenCVMotionAnalyzer` implementation
- [ ] `MockMotionAnalyzer` implementation
- [ ] Configuration properties (`brobot.opencv.mock.*`)
- [ ] Spring auto-configuration
- [ ] Unit tests for implementations

### Phase 3: Test Infrastructure (Estimated: 2-3 weeks)

**Objectives**:
- Implement recording and replay system
- Create `@EnableRecording` and `@UseRecording` annotations
- Build test data management utilities
- Create assertion helpers

**Deliverables**:
- [ ] Recording infrastructure
- [ ] Replay mechanism
- [ ] Test annotations
- [ ] `MotionAssertions` utility class
- [ ] Test data fixtures

### Phase 4: Documentation and Migration (Estimated: 1-2 weeks)

**Objectives**:
- Convert proposal to implementation guide
- Create migration guide from current API
- Write comprehensive examples
- Update related documentation

**Deliverables**:
- [ ] Implementation guide
- [ ] Migration documentation
- [ ] Example test suite
- [ ] Updated testing documentation

### Phase 5: Validation and Refinement (Estimated: 1-2 weeks)

**Objectives**:
- Test in real-world scenarios
- Gather feedback from users
- Performance optimization
- Bug fixes and refinements

**Deliverables**:
- [ ] Performance benchmarks
- [ ] User feedback incorporated
- [ ] Known issues documented
- [ ] Release notes

### Total Estimated Timeline: 9-14 weeks

### Success Criteria

- ✅ All proposed classes implemented and tested
- ✅ Mock mode works in headless environments
- ✅ Recording/replay system functional
- ✅ Performance acceptable (< 50ms per operation in mock mode)
- ✅ Documentation complete and accurate
- ✅ Migration path clear for existing users
- ✅ Integration tests passing in CI/CD

### Dependencies

- **OpenCV bindings**: Must remain compatible
- **Spring Boot**: Auto-configuration support
- **BrobotTestBase**: Integration without breaking changes
- **Mock framework**: Current mock infrastructure

### Risks and Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Breaking changes to existing API | High | Provide adapter layer for backwards compatibility |
| Performance overhead of abstraction | Medium | Benchmark and optimize critical paths |
| Complex recording/replay implementation | High | Start with simple file-based approach, iterate |
| Increased test complexity | Low | Comprehensive documentation and examples |

---

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

### Core Testing Guides (Current Implementation)
- [Testing Introduction](../docs/docs/04-testing/testing-intro.md) - Overview of Brobot testing approaches
- [Test Utilities - BrobotTestBase](../docs/docs/04-testing/test-utilities.md) - BrobotTestBase usage and test helpers
- [Unit Testing Guide](../docs/docs/04-testing/unit-testing.md) - Unit test patterns and best practices
- [Integration Testing Guide](../docs/docs/04-testing/integration-testing.md) - Integration test patterns with Spring
- [Mock Mode Guide](../docs/docs/04-testing/mock-mode-guide.md) - Mock mode configuration and fundamentals
- [Mat Testing Utilities](../docs/docs/04-testing/mat-testing-utilities.md) - OpenCV Mat testing utilities for **current** implementation

### Motion Detection Specific
- [MotionDetector Source](../library/src/main/java/io/github/jspinak/brobot/analysis/motion/MotionDetector.java) - Current motion detection implementation
- [PixelChangeDetector Source](../library/src/main/java/io/github/jspinak/brobot/analysis/motion/PixelChangeDetector.java) - Current pixel change detection
- [MotionFindOptions Source](../library/src/main/java/io/github/jspinak/brobot/action/basic/find/motion/MotionFindOptions.java) - Current configuration options

### Related Proposals
- [OpenCV Mock System Architecture](../docs/docs/03-core-library/opencv-mock/architecture.md) - PROPOSED OpenCV abstraction layer design
- [Test Logging Architecture](./test-logging-architecture.md) - Proposed test logging design (not implemented)

### CI/CD Resources
- [CI/CD Testing Guide](../docs/docs/03-core-library/testing/ci-cd-testing.md) - Running tests in CI/CD pipelines
- [Enhanced Mock Testing](../docs/docs/03-core-library/testing/enhanced-mocking.md) - Advanced mock scenarios and patterns

---

## Feedback and Contributions

This is a living proposal document. If you have feedback or would like to contribute to the design:

1. **Open an issue** on GitHub to discuss the proposal
2. **Submit improvements** to this document via pull request
3. **Share use cases** that would benefit from this architecture
4. **Propose alternative designs** for consideration

**Contact**: Brobot maintainers via GitHub issues

---

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-01 | Initial proposal creation |
| 1.1 | 2025-01 | Moved to proposals directory, improved framing, added roadmap |

**Status**: 🔵 Open for feedback and discussion