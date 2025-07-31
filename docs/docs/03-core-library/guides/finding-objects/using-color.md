---
sidebar_position: 1
---

# Using Color

Color can be used in a variety of ways to locate objects in a Brobot automation application.
With the new API, you use ColorFindOptions for color-based finding and HistogramFindOptions for histogram-based finding. A look under the hood at how Brobot assesses color can be found in the [Color Analysis Guide](/docs/core-library/guides/labeling/color-analysis).  

## ColorFindOptions

Color is found by comparing the color of each pixel in the scene to the colors in the target images. 
The way that the colors in the target images are represented affects the results, and can be modified 
by the methods `setKmeans` and `setSimilarity`. If k-means is set to `1`, the average color of the target 
images is calculated and compared to the pixels. If k-means is set to a number greater than `1`, multiple
colors centers are calculated and compared to the pixels. The number of colors centers is the k-means value. The similarity works in a way similar to pattern matching. The higher the similarity, the more similar the colors must be to be considered a match.

## Average Color

The color find action first builds a color profile for the Image from its associated 
image files. It then uses this color profile to find areas of similar color on the screen. 
When k-means is set to 1, the average color of all pixels in the target images is used to 
find matches. Pixels on the screen receive scores with small penalties for distance 
to the average HSV values and large penalties for being outside the ranges of HSV
values in the color profile.  

The image below is taken from the history folder of a Brobot application. The 
history folder is where illustrated screenshots, visual representations of actions
taken during execution, are stored. This illustrated screenshot shows the results
of a color find action. Brobot built a color profile for an Image of a red dot
(which being a Brobot Image object, allows for the inclusion of multiple image files, 
each captured from a different red dot). The Image is then searched for in the 
selected region (the minimap area), and the matches are shown as pink boxes. The 
colors shown at the far right of the illustrated screenshot are the contents of the 
matches. They show the colors that were found.

![illColor](/img/color/reddot.png)  

The below example shows the results of a similar operation on the red dot but
allowing for a larger diameter.  

![reddot](/img/color/reddot.png)  

Also written to the _history_ folder is the output of the classification operation
of the color find action. This output is a visual representation of where the target image's 
color was found on the entire scene. The search areas and matches are also shown. 
To the right of the scene, the image files of the red dot Brobot Image are displayed, as well
as the color centers used to find the matches (here, k-means is set to 2 and there are 2 color centers).  

![reddotclasses](/img/color/reddot_classes.png)  

Adjustable options include `similarity`, `diameter`, `kmeans`, and `maxMatchesToActOn`.

### Similarity

ColorFindOptions can be adjusted with respect to `similarity` and `diameter`. Similarity is 
a measurement similar to pattern matching. It has a scale of 0-1.00, where 1.00 is the most similar and 0 the least
similar.  

### Diameter

Diameter gives the minimum diameter of the color cluster. For example,
a diameter of 1 allows for any pixel to be a match whereas a diameter of 2 translates
to matches of size 2x2, where all 4 pixels need to meet the similarity requirements.

### K-Means Colors

Looking for the average color works well for objects that have just one color, but 
not so well for objects with multiple colors. In this case we want to search for 
parts of the screen that contain all the colors in the object. The k-Means search
method provides this functionality for us. It first determines the most prominent 
colors in the image and then looks for regions that match these colors. Matches are 
returned that contain colors most similar to the image's prominent colors. Not all
prominent colors must be in a region, but all colors in a region should be 
similar to one of the prominent colors.  

### MaxMatchesToActOn

Also used often is maxMatchesToActOn, which limits the returned matches to a
specific number. Setting similarity will return a variable number of matches, and
setting maxMatchesToActOn will return a number of matches less than or equal
to its value. For example, using a similarity of .95 may return 7 matches, and
combined with a maxMatchesToActOn of 5, the result will be the 5 best matches.

## Color Strategies

Brobot supports three color analysis strategies through ColorFindOptions:

### KMEANS
Finds a selected number of RGB color cluster centers for each image using the k-means algorithm.
This is useful for identifying dominant colors in images with multiple colors.

```java
ColorFindOptions kmeansColor = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.KMEANS)
        .setKmeans(3)  // Find 3 dominant colors
        .setDiameter(5)
        .setSimilarity(0.9)
        .setMaxMatchesToActOn(10)
        .build();

// Execute color finding
ObjectCollection objects = new ObjectCollection.Builder()
        .withImages(targetImage)
        .build();

@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult result = action.perform(kmeansColor, objects);
```

### MU (Mean Color)
Takes all pixels from all images and finds the min, max, mean, and standard deviation
of the HSV values to create a color profile. This is the default strategy.

```java
ColorFindOptions meanColor = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.MU)
        .setDiameter(5)
        .setSimilarity(0.95)
        .setAreaFiltering(AreaFilteringOptions.builder()
                .minArea(10)  // Filter out small noise
                .build())
        .build();

@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult result = action.perform(meanColor, objects);
```

### CLASSIFICATION
Performs a multi-class classification, assigning each pixel in the scene to the
most similar state image based on color profiles.

```java
ColorFindOptions classification = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.CLASSIFICATION)
        .setSimilarity(0.8)
        .build();

@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult result = action.perform(classification, objects);
```

## Histogram Finding

HistogramFindOptions searches for regions that have similar histograms to the given image. 
The histogram is calculated in HSV color space. Each image produces 5 histograms for
5 different areas of the image: an ellipse in the center, and the remaining 4 corner
regions. This is done to preserve some spatial information; for example, an image
of grass and blue sky would be more likely to match regions of grass and sky as 
compared to regions of a grassy hill above a lake.  

`Similarity` can be adjusted as well as the variables that control the number of 
HSV bins used to calculate the histograms: `hueBins`, `saturationBins`, and `valueBins`.
The default bin sizes for HSV, respectively, are 12, 2, and 1.  

```java
HistogramFindOptions histogramFind = new HistogramFindOptions.Builder()
        .setSimilarity(0.8)
        .setBinOptions(HSVBinOptions.builder()
                .hueBins(90)
                .saturationBins(2)
                .valueBins(1)
                .build())
        .setMaxMatchesToActOn(5)
        .build();

@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult result = action.perform(histogramFind, objects);
```

In the below example, maxMatchesToActOn is set to 5. The top match is the target image
used for the histogram find action. 

![histogram](/img/color/histogram.png)      

## Examples

### Finding Red Dots on a Minimap

```java
// Create an image object for the red dot
StateImage redDot = new StateImage.Builder()
        .setName("red_dot")
        .addPatterns("reddot1.png")
        .addPatterns("reddot2.png")  // Multiple samples improve accuracy
        .build();

// Configure color finding
ColorFindOptions findRedDots = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.MU)  // Use mean color
        .setDiameter(3)  // Minimum cluster size
        .setSimilarity(0.9)
        .setMaxMatchesToActOn(10)
        .setAreaFiltering(AreaFilteringOptions.builder()
                .minArea(5)  // Filter out noise
                .build())
        .build();

// Execute the find operation
ObjectCollection objects = new ObjectCollection.Builder()
        .withImages(redDot)
        .build();

@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult result = action.perform(findRedDots, objects);
```

### Combining Color with Pattern Matching

For more accurate results, combine pattern and color finding using ActionChainOptions:

```java
// First find patterns, then filter by color
PatternFindOptions patternFind = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.7)
        .build();

ColorFindOptions colorFilter = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.KMEANS)
        .setKmeans(2)
        .setSimilarity(0.85)
        .build();

ActionChainOptions combineColorPattern = new ActionChainOptions.Builder(patternFind)
        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
        .then(colorFilter)
        .build();

@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult result = action.perform(combineColorPattern, objects);
```

### Using Histograms for Complex Images

```java
HistogramFindOptions histogramFind = new HistogramFindOptions.Builder()
        .setSimilarity(0.8)
        .setBinOptions(HSVBinOptions.builder()
                .hueBins(90)
                .saturationBins(2)
                .valueBins(1)
                .build())
        .setMaxMatchesToActOn(5)
        .setIllustrate(HistogramFindOptions.Illustrate.YES)  // Save visual results
        .build();

ObjectCollection complexImages = new ObjectCollection.Builder()
        .withImages(targetImage)
        .build();

@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult histogramResult = action.perform(histogramFind, complexImages);
```

### Area Filtering with Color

ColorFindOptions supports area filtering to eliminate noise:

```java
// Find larger color regions only
ColorFindOptions largeColorRegions = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.MU)
        .setDiameter(10)
        .setSimilarity(0.85)
        .setAreaFiltering(AreaFilteringOptions.builder()
                .minArea(100)
                .maxArea(5000)
                .build())
        .build();
```

## Best Practices

1. **Multiple Samples**: Use multiple image samples to build better color profiles
2. **Appropriate Diameter**: Choose diameter based on the size of your target objects
3. **Combine Methods**: Use ActionChainOptions to combine color with pattern matching for better accuracy
4. **Test Different Strategies**: Try KMEANS, MU, and CLASSIFICATION to find what works best
5. **Adjust Similarity**: Start with lower values and increase until false positives are eliminated
6. **Consider Lighting**: Account for variations in screen brightness and ambient lighting
7. **Area Filtering**: Use minArea and maxArea to filter out noise and unwanted matches

## Integration with Other Actions

Color finding can be part of complex action chains:

```java
// Find colored button, then click it
ColorFindOptions findColoredButton = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.MU)
        .setSimilarity(0.9)
        .setMaxMatchesToActOn(1)
        .build();

ClickOptions clickButton = new ClickOptions.Builder()
        .setNumberOfClicks(1)
        .build();

ActionChainOptions findAndClick = new ActionChainOptions.Builder(findColoredButton)
        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
        .then(clickButton)
        .build();

@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult result = action.perform(findAndClick, objects);
```

For more information on combining find operations, see the [Combining Finds](combining-finds.md) guide.