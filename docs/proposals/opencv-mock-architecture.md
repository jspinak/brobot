# PROPOSAL: OpenCV Mock System Architecture

**Status**: 🔵 Proposed Architecture
**Created**: 2025
**Purpose**: Design specification for comprehensive OpenCV abstraction layer

---

## ⚠️ Important: This is a Proposal Document

This document describes a **PROPOSED** architecture for an OpenCV Mock System that **does not currently exist** in Brobot. It serves as:

- **Design specification** for future OpenCV abstraction layer
- **Architectural blueprint** for testable OpenCV operations
- **Reference document** for API design discussions

### What Does NOT Exist (Proposed Components)

The following components are **design concepts only**:

| Component | Status | Description |
|-----------|--------|-------------|
| `PixelAnalyzer` | ❌ Not Implemented | Proposed interface for pixel-level operations |
| `MotionAnalyzer` | ❌ Not Implemented | Proposed interface for motion analysis |
| `MotionResult` | ❌ Not Implemented | Proposed result object with confidence scores |
| `ColorAnalysisResult` | ❌ Not Implemented | Proposed color analysis result object |
| `MockPixelAnalyzer` | ❌ Not Implemented | Proposed mock implementation |
| `OpenCVPixelAnalyzer` | ❌ Not Implemented | Proposed OpenCV implementation |
| `MockDataGenerator` | ❌ Not Implemented | Proposed mock data generation |
| `OpenCVMockProperties` | ❌ Not Implemented | Proposed configuration properties |
| `brobot.opencv.mock.*` | ❌ Not Implemented | Proposed configuration properties |

### Current Implementation (What Actually Exists)

Brobot currently provides these motion detection classes:

| Component | Status | Location |
|-----------|--------|----------|
| `MotionDetector` | ✅ Implemented | `brobot.analysis.motion.MotionDetector` |
| `FindDynamicPixels` | ✅ Implemented | `brobot.analysis.motion.FindDynamicPixels` |
| `PixelChangeDetector` | ✅ Implemented | `brobot.analysis.motion.PixelChangeDetector` |
| `MotionMetadata` | ✅ Implemented | `brobot.analysis.results.MotionMetadata` |
| `MockConfiguration` | ✅ Implemented | `brobot.config.mock.MockConfiguration` (different from proposed) |
| `MockModeManager` | ✅ Implemented | `brobot.config.mock.MockModeManager` |

**For the current implementation**, see:
- [MotionDetector Source](../../library/src/main/java/io/github/jspinak/brobot/analysis/motion/MotionDetector.java) - Current motion detection
- [Mock Mode Guide](../docs/docs/04-testing/mock-mode-guide.md) - Current mock mode documentation
- [Test Utilities](../docs/docs/04-testing/test-utilities.md) - Current testing framework

---

## Proposal Overview

This proposal outlines an OpenCV Mock System that provides a comprehensive abstraction layer for OpenCV operations in Brobot, enabling full testability in mock mode while maintaining production functionality.

## Architectural Principles

### 1. Separation of Concerns
The system separates OpenCV operations into three distinct layers:
- **Abstraction Layer**: Interfaces and result objects
- **Implementation Layer**: Mock and real implementations
- **Configuration Layer**: Spring-based conditional loading

### 2. Result-Oriented Design
All OpenCV operations return result objects instead of raw Mat objects:
```java
// Note: BrobotProperties must be injected as a dependency
@Autowired
private BrobotProperties brobotProperties;

public interface MotionAnalyzer {
    MotionResult analyzeMotion(List<BufferedImage> images);
}
```

### 3. Dependency Injection
Spring's conditional beans enable seamless switching between mock and real modes:
```java
@Bean
@ConditionalOnProperty(name = "brobot.mock", havingValue = "true")
public MotionAnalyzer mockMotionAnalyzer() {
    return new MockMotionAnalyzer();
}
```

## Core Components

### Result Objects

#### MotionResult
Encapsulates motion detection results:
```java
@Value
@Builder
@JsonDeserialize(builder = MotionResult.Builder.class)
public class MotionResult implements Serializable {
    private final boolean motionDetected;
    private final List<Region> motionRegions;
    private final double confidenceScore;
    private final byte[] changeMask;
    private final MotionMetadata metadata;
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        // Lombok generates implementation
    }
}
```

#### ColorAnalysisResult
Encapsulates color analysis results:
```java
@Value
@Builder
@JsonDeserialize(builder = ColorAnalysisResult.Builder.class)
public class ColorAnalysisResult implements Serializable {
    private final Map<String, Double> colorHistogram;
    private final List<DominantColor> dominantColors;
    private final List<ColorRegion> colorRegions;
    private final ColorStatistics statistics;
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        // Lombok generates implementation
    }
}
```

### Analyzer Interfaces

#### PixelAnalyzer
Base interface for pixel-level operations:
```java
public interface PixelAnalyzer {
    MotionResult detectChanges(List<BufferedImage> images, AnalysisOptions options);
    ColorAnalysisResult analyzeColors(BufferedImage image, ColorOptions options);
    ContourResult findContours(BufferedImage image, ContourOptions options);
}
```

#### MotionAnalyzer
Specialized interface for motion detection:
```java
public interface MotionAnalyzer {
    MotionResult analyzeMotion(List<BufferedImage> images);
    MotionResult analyzeMotionWithOptions(List<BufferedImage> images, MotionOptions options);
    List<MotionEvent> trackMotion(List<BufferedImage> images, TrackingOptions options);
}
```

### Implementation Classes

#### MockPixelAnalyzer
Provides configurable mock responses:
```java
@Component
@ConditionalOnProperty(name = "brobot.mock", havingValue = "true")
public class MockPixelAnalyzer implements PixelAnalyzer {
    
    private final MockDataGenerator dataGenerator;
    private final MockConfiguration config;
    
    @Override
    public MotionResult detectChanges(List<BufferedImage> images, AnalysisOptions options) {
        // Generate realistic mock data based on configuration
        return dataGenerator.generateMotionResult(images.size(), options);
    }
}
```

#### OpenCVPixelAnalyzer

> **Note**: PROPOSED class using actual Brobot utilities.

Executes real OpenCV operations:
```java
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import io.github.jspinak.brobot.illustratedHistory.draw.MatrixUtilities;
import org.opencv.core.Mat;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "brobot.mock", havingValue = "false", matchIfMissing = true)
public class OpenCVPixelAnalyzer implements PixelAnalyzer {  // PROPOSED class

    @Override
    public MotionResult detectChanges(List<BufferedImage> images, AnalysisOptions options) {
        // Convert to Mat objects using ACTUAL MatrixUtilities class
        List<Mat> mats = images.stream()
            .map(MatrixUtilities::bufferedImageToMat)  // Returns Optional<Mat>
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        // Perform OpenCV operations (proposed method)
        Mat changeMask = performChangeDetection(mats, options);

        // Convert to result object (proposed method)
        return buildMotionResult(changeMask, options);  // PROPOSED
    }

    // PROPOSED helper methods
    private Mat performChangeDetection(List<Mat> mats, AnalysisOptions options) {
        // Implementation would use actual MotionDetector
        return new Mat();
    }

    private MotionResult buildMotionResult(Mat changeMask, AnalysisOptions options) {
        // Implementation would convert Mat to MotionResult
        return MotionResult.builder().build();  // PROPOSED
    }
}
```

## Configuration

### Spring Configuration
```java
@Configuration
@EnableConfigurationProperties(OpenCVMockProperties.class)
public class OpenCVMockConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public MockDataGenerator mockDataGenerator(OpenCVMockProperties properties) {
        return new MockDataGenerator(properties);
    }
    
    @Bean
    @ConditionalOnProperty(name = "brobot.mock", havingValue = "true")
    public PixelAnalyzer mockPixelAnalyzer(MockDataGenerator generator) {
        return new MockPixelAnalyzer(generator);
    }
    
    @Bean
    @ConditionalOnProperty(name = "brobot.mock", havingValue = "false", matchIfMissing = true)
    public PixelAnalyzer openCVPixelAnalyzer() {
        return new OpenCVPixelAnalyzer();
    }
}
```

### Properties Configuration

> **Note**: The following properties are PROPOSED and do not currently exist.

```properties
# Enable mock mode (current property that exists)
brobot.mock=true

# PROPOSED: Mock data generation settings (these properties do not exist)
# brobot.opencv.mock.motion.default-confidence=0.85
# brobot.opencv.mock.motion.region-count=3
# brobot.opencv.mock.motion.min-region-size=50

# PROPOSED: Record and replay settings (these properties do not exist)
# brobot.opencv.mock.replay.enabled=false
# brobot.opencv.mock.replay.directory=src/test/resources/opencv-recordings
```

**Current actual properties** available in Brobot:
```properties
brobot.mock=false
brobot.mock.action.success.probability=1.0
brobot.mock.time-find-first=0.1
brobot.mock.time-find-all=0.2
```

## Data Flow

### Production Mode
1. Application calls analyzer interface method
2. OpenCVPixelAnalyzer receives request
3. Converts BufferedImages to Mat objects
4. Executes OpenCV operations
5. Converts results to result objects
6. Returns result to application

### Mock Mode
1. Application calls analyzer interface method
2. MockPixelAnalyzer receives request
3. Checks for recorded data (if replay enabled)
4. Generates mock data if no recording exists
5. Returns result to application

## Testing Strategy

### Contract Testing

> **Note**: All code examples use PROPOSED classes that do not currently exist.

Ensures mock and real implementations maintain consistency:
```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import io.github.jspinak.brobot.config.mock.MockModeManager;
import static org.junit.jupiter.api.Assertions.*;

@ParameterizedTest
@ValueSource(booleans = {true, false})
void testMotionDetectionContract(boolean useMock) {
    // Set mock mode using MockModeManager
    MockModeManager.setMockMode(useMock);  // CORRECT API
    PixelAnalyzer analyzer = getAnalyzer();  // PROPOSED class

    List<BufferedImage> images = loadTestImages();
    MotionResult result = analyzer.detectChanges(images, defaultOptions());  // PROPOSED API

    assertNotNull(result);
    assertNotNull(result.getMotionRegions());
    assertTrue(result.getConfidenceScore() >= 0 && result.getConfidenceScore() <= 1);
}

// Helper methods
private PixelAnalyzer getAnalyzer() {
    // Would retrieve from Spring context based on mock mode
    return context.getBean(PixelAnalyzer.class);
}

private List<BufferedImage> loadTestImages() {
    // Implementation for loading test images
    return new ArrayList<>();
}

private AnalysisOptions defaultOptions() {
    // PROPOSED: Return default analysis options
    return new AnalysisOptions();
}
```

### Performance Testing
Validates mock performance characteristics:
```java
@Test
void testMockPerformance() {
    // Mock mode is now configured via application.properties:
// brobot.core.mock=true;
    PixelAnalyzer analyzer = getAnalyzer();
    
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < 1000; i++) {
        analyzer.detectChanges(images, options);
    }
    long duration = System.currentTimeMillis() - startTime;
    
    assertTrue(duration < 1000, "1000 operations should complete in under 1 second");
}
```

## Migration Path

### Phase 1: Parallel Implementation
- New abstractions work alongside existing code
- Feature flags control adoption
- Monitoring compares results

### Phase 2: Gradual Migration
- Components migrated one at a time
- Extensive testing at each step
- Rollback capability maintained

### Phase 3: Deprecation
- Old direct OpenCV usage deprecated
- Migration guides provided
- Support period defined

### Phase 4: Removal
- Deprecated code removed
- Full adoption of new system
- Documentation updated

## Best Practices

### 1. Always Use Abstractions
```java
// Good
@Autowired
private MotionAnalyzer motionAnalyzer;

public void detectMotion() {
    MotionResult result = motionAnalyzer.analyzeMotion(images);
}

// Bad
public void detectMotion() {
    Mat mat = Imgcodecs.imread(path);  // Direct OpenCV usage
}
```

### 2. Configure Mock Behavior
```java
@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public MockConfiguration mockConfiguration() {
        return MockConfiguration.builder()
            .defaultMotionConfidence(0.9)
            .generateRealisticNoise(true)
            .build();
    }
}
```

### 3. Use Result Objects
```java
// Process results through the abstraction
MotionResult result = analyzer.detectMotion(images);
if (result.isMotionDetected()) {
    result.getMotionRegions().forEach(region -> {
        // Process each motion region
    });
}
```

## Troubleshooting

### Common Issues

#### Mock Data Not Realistic
- Adjust MockConfiguration parameters
- Enable record/replay for real data
- Use custom mock data generators

#### Performance Degradation
- Check circuit breaker status
- Review fallback configurations
- Monitor resource usage

#### Test Failures
- Verify mock mode settings
- Check contract test results
- Review configuration properties

## Related Documentation

### Related Proposals
- [Motion Detection Testing Strategy (Proposal)](./motion-detection-testing-strategy.md) - Companion proposal for testing this architecture

### Current Mock Mode Implementation
- [Mock Mode Guide](../docs/docs/04-testing/mock-mode-guide.md) - Current mock mode functionality
- [Mock Mode Manager](../docs/docs/04-testing/mock-mode-manager.md) - MockModeManager API
- [Enhanced Mock Testing](../docs/docs/03-core-library/testing/enhanced-mocking.md) - Advanced mock patterns
- [Test Utilities](../docs/docs/04-testing/test-utilities.md) - BrobotTestBase and helpers

### Current Motion Detection
- [MotionDetector Source](../library/src/main/java/io/github/jspinak/brobot/analysis/motion/MotionDetector.java) - Current implementation
- [FindDynamicPixels Interface](../library/src/main/java/io/github/jspinak/brobot/analysis/motion/FindDynamicPixels.java) - Motion detection interface
- [MotionMetadata Source](../library/src/main/java/io/github/jspinak/brobot/analysis/results/MotionMetadata.java) - Rich metadata class

### Testing Documentation
- [Testing Introduction](../docs/docs/04-testing/testing-intro.md) - Testing overview
- [Unit Testing Guide](../docs/docs/04-testing/unit-testing.md) - Unit test patterns
- [Integration Testing Guide](../docs/docs/04-testing/integration-testing.md) - Integration tests
- [CI/CD Testing](../docs/docs/03-core-library/testing/ci-cd-testing.md) - CI/CD pipelines

---

## Implementation Roadmap

This proposal could be implemented in phases:

### Phase 1: Core Abstractions (4-6 weeks)
- [ ] Define result objects (MotionResult, ColorAnalysisResult)
- [ ] Create analyzer interfaces (PixelAnalyzer, MotionAnalyzer)
- [ ] Design Options classes (AnalysisOptions, MotionOptions, etc.)

### Phase 2: Mock Implementation (3-4 weeks)
- [ ] Implement MockPixelAnalyzer
- [ ] Create MockDataGenerator
- [ ] Add proposed properties to BrobotProperties

### Phase 3: OpenCV Integration (4-5 weeks)
- [ ] Implement OpenCVPixelAnalyzer
- [ ] Integrate with existing MotionDetector
- [ ] Handle Mat ↔ Result conversions

### Phase 4: Spring Configuration (2-3 weeks)
- [ ] Create OpenCVMockConfiguration
- [ ] Add conditional bean loading
- [ ] Configure properties

### Phase 5: Testing & Documentation (3-4 weeks)
- [ ] Write contract tests
- [ ] Performance validation
- [ ] Migration guide
- [ ] API documentation

**Total Estimated Timeline**: 16-22 weeks

---

## Feedback and Contributions

This is a living proposal document. To contribute:

1. **Open an issue** on GitHub to discuss the proposal
2. **Submit improvements** via pull request
3. **Share use cases** that would benefit from this architecture
4. **Propose alternatives** for consideration

---

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-01 | Initial proposal creation |
| 1.1 | 2025-01 | Moved to proposals directory, added disclaimers and current implementation section |

**Status**: 🔵 Open for feedback and discussion