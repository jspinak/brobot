# OpenCV Mock System Architecture

## Overview
The OpenCV Mock System provides a comprehensive abstraction layer for OpenCV operations in Brobot, enabling full testability in mock mode while maintaining production functionality.

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
Executes real OpenCV operations:
```java
@Component
@ConditionalOnProperty(name = "brobot.mock", havingValue = "false", matchIfMissing = true)
public class OpenCVPixelAnalyzer implements PixelAnalyzer {
    
    @Override
    public MotionResult detectChanges(List<BufferedImage> images, AnalysisOptions options) {
        // Convert to Mat objects
        List<Mat> mats = images.stream()
            .map(ImageConverter::bufferedImageToMat)
            .collect(Collectors.toList());
        
        // Perform OpenCV operations
        Mat changeMask = performChangeDetection(mats, options);
        
        // Convert to result object
        return buildMotionResult(changeMask, options);
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
```properties
# Enable mock mode
brobot.core.mock=true

# Mock data generation settings
brobot.opencv.mock.motion.default-confidence=0.85
brobot.opencv.mock.motion.region-count=3
brobot.opencv.mock.motion.min-region-size=50

# Record and replay settings
brobot.opencv.mock.replay.enabled=false
brobot.opencv.mock.replay.directory=src/test/resources/opencv-recordings
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
Ensures mock and real implementations maintain consistency:
```java
@ParameterizedTest
@ValueSource(booleans = {true, false})
void testMotionDetectionContract(boolean useMock) {
    brobotProperties.getCore().isMock() = useMock;
    PixelAnalyzer analyzer = getAnalyzer();
    
    List<BufferedImage> images = loadTestImages();
    MotionResult result = analyzer.detectChanges(images, defaultOptions());
    
    assertNotNull(result);
    assertNotNull(result.getMotionRegions());
    assertTrue(result.getConfidenceScore() >= 0 && result.getConfidenceScore() <= 1);
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
- [Motion Detection Testing Guide](../testing/motion-detection-testing.md)