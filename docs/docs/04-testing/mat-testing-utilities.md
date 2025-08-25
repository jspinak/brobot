# Mat Testing Utilities

## Overview

The `MatTestUtils` class provides comprehensive utilities for creating, managing, and testing OpenCV Mat objects in Brobot tests. These utilities help prevent JVM crashes caused by invalid Mat operations and ensure consistent test data creation.

## Key Features

- **Safe Mat Creation**: Guaranteed valid Mat objects with proper initialization
- **Pattern Generation**: Pre-built patterns for testing image processing
- **Validation**: Built-in validation to catch issues before crashes occur
- **Memory Management**: Safe cleanup utilities to prevent memory leaks
- **Test Data Helpers**: Convenient methods for common test scenarios

## Why Use MatTestUtils?

OpenCV Mat operations can cause JVM crashes (SIGSEGV) when:
- Mats are uninitialized or empty
- Mats are already released
- Dimensions are invalid
- Native memory is corrupted

MatTestUtils prevents these issues by:
1. Always creating properly initialized Mats
2. Validating Mats before operations
3. Providing safe cleanup methods
4. Offering defensive programming patterns

## Basic Usage

### Import the Utilities

```java
import io.github.jspinak.brobot.test.utils.MatTestUtils;
import static org.bytedeco.opencv.global.opencv_core.*;
```

### Creating Safe Mats

```java
// Create a properly initialized Mat
Mat mat = MatTestUtils.createSafeMat(100, 100, CV_8UC3);

// Create a filled Mat with specific color
Mat redMat = MatTestUtils.createColorMat(100, 100, 0, 0, 255); // BGR format

// Create a grayscale Mat
Mat grayMat = MatTestUtils.createGrayMat(100, 100, 128); // Gray value 0-255
```

### Validation

Always validate Mats before risky operations:

```java
Mat mat = someOperation();
MatTestUtils.validateMat(mat, "operation result");  // Throws if invalid
```

### Safe Cleanup

```java
// Single Mat
MatTestUtils.safeRelease(mat);

// Multiple Mats
MatTestUtils.safeReleaseAll(mat1, mat2, mat3);

// MatVector
MatTestUtils.safeRelease(matVector);
```

## Pattern Generation

### Checkerboard Pattern

```java
Mat checkerboard = MatTestUtils.createCheckerboardMat(200, 200, 25);
// Creates 200x200 image with 25-pixel squares
```

### Gradient Pattern

```java
// Horizontal gradient
Mat horizontalGradient = MatTestUtils.createGradientMat(100, 100, true);

// Vertical gradient
Mat verticalGradient = MatTestUtils.createGradientMat(100, 100, false);
```

### Geometric Shapes

```java
// Rectangle
Mat rectangle = MatTestUtils.createShapeMat(100, 100, 0);

// Circle
Mat circle = MatTestUtils.createShapeMat(100, 100, 1);

// Line
Mat line = MatTestUtils.createShapeMat(100, 100, 2);
```

### Noise Patterns

```java
// Random noise for testing filters
Mat noisyImage = MatTestUtils.createNoiseMat(100, 100, CV_8UC3);
```

## Motion Detection Testing

### Creating Motion Sequences

```java
// Gradually changing images (for motion detection)
MatVector motionSequence = MatTestUtils.createMotionMatVector(
    5,      // frame count
    100,    // height
    100,    // width
    50      // change amount per frame
);
```

### Creating Changed Regions

```java
// Create sequence with specific changed region
MatVector withChange = MatTestUtils.createMatVectorWithChange(
    100, 100,   // dimensions
    1,          // which frame has change
    40, 40,     // change position
    20          // change size
);
```

## Mat Comparison

```java
// Compare two Mats with tolerance
boolean similar = MatTestUtils.areMatsEqual(mat1, mat2, 5.0);
// Returns true if average pixel difference is <= 5.0
```

## Debugging

```java
// Get human-readable Mat description
String description = MatTestUtils.describeMat(mat, "test_mat");
// Output: "test_mat: 100x100, type=16, channels=3"
```

## Best Practices

### 1. Always Use Try-Finally for Cleanup

```java
Mat mat1 = null;
Mat mat2 = null;
try {
    mat1 = MatTestUtils.createColorMat(100, 100, 255, 0, 0);
    mat2 = MatTestUtils.createColorMat(100, 100, 0, 255, 0);
    
    // Test operations
    MatTestUtils.validateMat(mat1, "mat1 before operation");
    performOperation(mat1, mat2);
    
} finally {
    MatTestUtils.safeReleaseAll(mat1, mat2);
}
```

### 2. Validate Before Operations

```java
@Test
void testImageProcessing() {
    Mat input = MatTestUtils.createGrayMat(100, 100, 128);
    MatTestUtils.validateMat(input, "input");
    
    Mat output = processImage(input);
    MatTestUtils.validateMat(output, "output");
    
    assertTrue(MatTestUtils.areMatsEqual(input, output, 10.0));
}
```

### 3. Use Descriptive Context in Validation

```java
MatTestUtils.validateMat(mat, "after dilation");  // Good
MatTestUtils.validateMat(mat, "mat");            // Less helpful
```

### 4. Create MatVectors Safely

```java
// Validate all Mats before creating MatVector
Mat mat1 = MatTestUtils.createColorMat(100, 100, 100, 100, 100);
Mat mat2 = MatTestUtils.createColorMat(100, 100, 150, 150, 150);
Mat mat3 = MatTestUtils.createColorMat(100, 100, 200, 200, 200);

MatTestUtils.validateMat(mat1, "mat1");
MatTestUtils.validateMat(mat2, "mat2");
MatTestUtils.validateMat(mat3, "mat3");

MatVector vector = new MatVector(mat1, mat2, mat3);
```

## Example Test

```java
public class MyImageProcessorTest extends BrobotTestBase {
    
    @Test
    void testEdgeDetection() {
        // Create test image with shape
        Mat input = MatTestUtils.createShapeMat(200, 200, 1); // Circle
        
        try {
            // Validate input
            MatTestUtils.validateMat(input, "circle input");
            
            // Process
            Mat edges = detectEdges(input);
            
            // Validate output
            MatTestUtils.validateMat(edges, "edge detection result");
            assertFalse(edges.empty());
            
            // Check that edges were found
            double edgeSum = sumElems(edges).get(0);
            assertTrue(edgeSum > 0, "Should detect circle edges");
            
        } finally {
            MatTestUtils.safeRelease(input);
        }
    }
    
    @Test
    void testMotionDetection() {
        // Create motion sequence
        MatVector frames = MatTestUtils.createMotionMatVector(3, 100, 100, 50);
        
        try {
            // Build detector
            MotionDetector detector = new MotionDetector.Builder()
                .setFrames(frames)
                .build();
            
            // Check motion detected
            Mat motionMask = detector.getMotionMask();
            MatTestUtils.validateMat(motionMask, "motion mask");
            
            double motion = sumElems(motionMask).get(0);
            assertTrue(motion > 0, "Should detect motion between frames");
            
        } finally {
            MatTestUtils.safeRelease(frames);
        }
    }
}
```

## Troubleshooting

### JVM Crashes (Exit Code 134)

If you still experience crashes:

1. **Check Mat dimensions**: Ensure rows and cols are positive
2. **Verify Mat types match**: Operations may require specific types
3. **Check native memory**: Use smaller Mats in tests
4. **Enable logging**: Add debug output before operations

```java
// Debug helper
System.out.println(MatTestUtils.describeMat(mat, "before_operation"));
```

### Common Issues and Solutions

| Issue | Solution |
|-------|----------|
| `Mat.isNull()` returns true | Use `createSafeMat()` instead of `new Mat()` |
| `Mat.empty()` returns true | Ensure dimensions > 0 when creating |
| SIGSEGV on release | Use `safeRelease()` which checks null/released state |
| Operations fail silently | Add `validateMat()` calls before operations |
| Memory leaks | Always use try-finally with `safeReleaseAll()` |

## Integration with PixelChangeDetector

When testing classes like `PixelChangeDetector` that perform complex Mat operations:

```java
@Test
void testPixelChangeDetection() {
    // Create safe test images
    Mat img1 = MatTestUtils.createColorMat(100, 100, 100, 100, 100);
    Mat img2 = MatTestUtils.createColorMat(100, 100, 100, 100, 100);
    
    // Add change region
    rectangle(img2, 
        new Point(40, 40), new Point(60, 60),
        new Scalar(200, 200, 200, 0), -1, 8, 0);
    
    // Validate before use
    MatTestUtils.validateMat(img1, "img1");
    MatTestUtils.validateMat(img2, "img2 with change");
    
    MatVector frames = new MatVector(img1, img2);
    
    try {
        PixelChangeDetector detector = new PixelChangeDetector.Builder()
            .setMats(frames)
            .useDilation(3, 3, CV_8U)  // Use safe kernel type
            .build();
        
        Mat changeMask = detector.getChangeMask();
        MatTestUtils.validateMat(changeMask, "change mask");
        
        // Verify changes detected
        assertTrue(countNonZero(changeMask) > 0);
        
    } finally {
        MatTestUtils.safeReleaseAll(img1, img2);
    }
}
```

## See Also

- [Mock Mode Testing](mock-mode-guide.md)
- [Test Utilities](test-utilities.md)
- [Unit Testing](unit-testing.md)
- [Integration Testing](integration-testing.md)