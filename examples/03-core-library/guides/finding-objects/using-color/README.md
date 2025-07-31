# Using Color Example

This example demonstrates color-based object detection in Brobot using ColorFindOptions and HistogramFindOptions.

## Documentation Reference

This example corresponds to: `/docs/03-core-library/guides/finding-objects/using-color.md`

## What This Example Shows

1. **Color Analysis Strategies**:
   - KMEANS - Finding dominant colors with k-means clustering
   - MU - Mean color statistics (default)
   - CLASSIFICATION - Multi-class pixel classification

2. **Practical Examples**:
   - Finding red dots on a minimap
   - Combining color with pattern matching
   - Using histograms for complex images

3. **Advanced Features**:
   - Area filtering to remove noise
   - Integration with click actions
   - Adjustable similarity and diameter settings

## Key Concepts

### Color Strategies

```java
// KMEANS - Best for multi-colored objects
ColorFindOptions kmeansColor = new ColorFindOptions.Builder()
    .setColorStrategy(ColorFindOptions.Color.KMEANS)
    .setKmeans(3)  // Find 3 dominant colors
    .build();

// MU - Default, good for single colors
ColorFindOptions meanColor = new ColorFindOptions.Builder()
    .setColorStrategy(ColorFindOptions.Color.MU)
    .setDiameter(5)
    .build();

// CLASSIFICATION - Pixel-level classification
ColorFindOptions classification = new ColorFindOptions.Builder()
    .setColorStrategy(ColorFindOptions.Color.CLASSIFICATION)
    .build();
```

### Key Parameters

- **similarity**: 0.0-1.0, higher = more strict matching
- **diameter**: Minimum cluster size (e.g., 5 = 5x5 pixels)
- **kmeans**: Number of color centers to find
- **minArea/maxArea**: Filter results by size

## Running the Example

```bash
./gradlew bootRun
```

The example runs in mock mode. Check the `history/` folder after running to see color analysis visualizations.

## Color Analysis Visualizations

When `save-history: true`, Brobot creates visualizations showing:
- Color profiles built from target images
- Classification results on the scene
- Matches with their color contents
- K-means color centers

## Best Practices

1. **Multiple Image Samples**: Use multiple captures of the same object for better color profiles
2. **Appropriate Diameter**: Choose based on object size (larger objects = larger diameter)
3. **Combine Methods**: Use with pattern matching for better accuracy
4. **Test Different Strategies**: Try all three to see what works best
5. **Adjust Similarity**: Start low (0.7) and increase until false positives disappear
6. **Consider Lighting**: Account for screen brightness variations

## Creating Test Images

To run with real GUI:
1. Create an `images/` directory
2. Add color-based screenshots:
   - `color-target.png`
   - `reddot1.png`, `reddot2.png` (multiple samples)
   - `button-shape.png`
   - `landscape-image.png`
   - `color-region.png`
   - `colored-button.png`
   - `adjustable-target.png`
3. Set `brobot.core.mock: false` in application.yml

## Next Steps

1. Try the combining-finds example to see nested color searches
2. Explore motion detection for finding moving colored objects
3. Check histogram finding for complex image matching