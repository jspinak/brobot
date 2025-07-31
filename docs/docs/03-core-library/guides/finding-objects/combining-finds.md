---
sidebar_position: 2
---

# Combining Find Operations

Combining multiple find operations in the same Action can give us better results.
With the new ActionChainOptions, you can create nested or confirmed find operations.

Combining find methods can give us more accurate matches in scenarios where the 
form and color of an object are not unique. Take the example below, where we are looking
for the yellow bars above the kobolds (the top-left bar has blue circles on it). 
A relatively solid bar of color will correspond to other places on the screen, including 
the green and red bars above the character. On the other hand, the yellow color of 
the bars would also be found in other places, including on the character's weapon and
interspersed throughout the grass. One way to narrow down our search is to look for 
both a pattern and a color.  

## Using ActionChainOptions

The new ActionChainOptions class provides a clean way to chain multiple find operations together with different strategies.

### Nested Finds

Nested Finds find objects inside the matches from the previous Find operation. This is useful when you want to find specific elements within larger patterns.

```java
// Find pattern matches first, then search for color within those matches
PatternFindOptions patternFind = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.8)
        .build();

ColorFindOptions colorFind = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.MU)  // Use mean color statistics
        .setDiameter(5)
        .setSimilarity(0.9)
        .build();

ActionChainOptions nestedChain = new ActionChainOptions.Builder(patternFind)
        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
        .then(colorFind)
        .build();

// Execute the chain
@Autowired
private Action action; // obtain from Spring context or dependency injection

ObjectCollection objectCollection = new ObjectCollection.Builder()
        .withImages(barImage, yellowColorSample)
        .build();
        
ActionResult result = action.perform(nestedChain, objectCollection);
```

In the below example, all pattern matches from the initial find operation are drawn in 
blue bounding boxes, and the color matches are drawn in pink bounding boxes. To the 
right of the scene are the contents of the color matches. As expected, all color matches 
are some variation of yellow, showing that they are taken only from the pattern matches of
yellow bars and not from the red or green bars.  

![nestedFind](/img/color/nestedFind.png)  

## Confirmed Finds

Confirmed Finds look for matches inside the matches from the first Find operation. 
All subsequent Find operations are performed on the match regions from the first operation.
If a match is found, the match region from the first Find operation will be returned. 
For a match to exist, all subsequent Find operations need to succeed within its region. 

```java
// Find pattern matches and confirm with color
PatternFindOptions patternFind = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.8)
        .build();

ColorFindOptions colorConfirm = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.MU)
        .setDiameter(5)
        .setSimilarity(0.85)
        .build();

ActionChainOptions confirmedChain = new ActionChainOptions.Builder(patternFind)
        .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
        .then(colorConfirm)
        .build();

// Execute the chain
@Autowired
private Action action; // obtain from Spring context or dependency injection

ObjectCollection objectCollection = new ObjectCollection.Builder()
        .withImages(barImage, yellowColorSample)
        .build();
        
ActionResult result = action.perform(confirmedChain, objectCollection);
```

In the below example, the pattern matches from the initial find operation are drawn in
blue and the color matches are drawn in pink. To the right of the scene are the contents of the 
color matches. The pattern match is selected in its original size. Only the yellow bars are selected.  

![confirmedFind](/img/color/confirmedFind.png)  

## Multiple Chained Operations

You can chain more than two operations together for complex scenarios:

```java
// Find all patterns, filter by color, then filter by size
PatternFindOptions findPatterns = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .build();

ColorFindOptions filterByColor = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.KMEANS)
        .setKmeans(2)  // Look for 2 dominant colors
        .setSimilarity(0.85)
        .build();

// You can add another color filter or pattern filter here
// For example, filter by a different color strategy
ColorFindOptions filterByClassification = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.CLASSIFICATION)
        .setSimilarity(0.9)
        .build();

ActionChainOptions complexChain = new ActionChainOptions.Builder(findPatterns)
        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
        .then(filterByColor)
        .then(filterByClassification)
        .build();
```

## Practical Examples

### Finding Yellow Health Bars

```java
// First find all bar-shaped patterns
PatternFindOptions barPatterns = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.7)  // Lower similarity to catch all bars
        .build();

// Then filter for yellow color
ColorFindOptions yellowFilter = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.MU)
        .setDiameter(10)  // Larger diameter for solid color areas
        .setSimilarity(0.9)  // High similarity for color matching
        .build();

// Create nested chain to find yellow bars
ActionChainOptions findYellowBars = new ActionChainOptions.Builder(barPatterns)
        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
        .then(yellowFilter)
        .setPauseAfterEnd(0.5)  // Add pause after finding
        .build();

// Execute
StateImage barImage = new StateImage.Builder()
        .setName("health_bar")
        .addPatterns("bar_pattern.png")
        .build();

ObjectCollection objects = new ObjectCollection.Builder()
        .withImages(barImage)
        .build();

@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult yellowBars = action.perform(findYellowBars, objects);
```

### Confirming UI Elements

```java
// Find buttons by pattern, confirm by color to reduce false positives
PatternFindOptions buttonPattern = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.8)
        .build();

ColorFindOptions buttonColor = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.CLASSIFICATION)
        .setSimilarity(0.85)
        .build();

ActionChainOptions confirmButtons = new ActionChainOptions.Builder(buttonPattern)
        .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
        .then(buttonColor)
        .build();
```

## Best Practices

1. **Choose the Right Strategy**
   - Use NESTED when searching for elements within elements
   - Use CONFIRM when you need to validate matches with additional criteria

2. **Order Matters**
   - Put the most selective operation first to reduce processing time
   - Pattern matching is usually faster than color analysis

3. **Adjust Similarities**
   - Use lower similarity for the first operation to catch more candidates
   - Use higher similarity for subsequent operations to filter accurately

4. **Test Incrementally**
   - Test each operation separately before chaining
   - Use illustration to visualize what each step finds

For more detailed information on color finding, see the [Using Color](using-color.md) guide.