# Combining Finds Example

This example demonstrates how to combine multiple find operations using ActionChainOptions with NESTED and CONFIRM strategies.

## Documentation Reference

This example corresponds to: `/docs/03-core-library/guides/finding-objects/combining-finds.md`

## What This Example Shows

1. **NESTED Strategy** - Searching within previous results
   - Find bars, then find yellow within those bars
   - Refinement approach for precise targeting

2. **CONFIRM Strategy** - Validating results with second search
   - Find buttons, confirm they have the right color
   - Returns original matches that pass confirmation

3. **Practical Examples**:
   - Finding colored text within UI panels
   - Detecting interactive elements with hover states
   - Multi-stage filtering from broad to narrow

4. **Strategy Comparison** - Side-by-side comparison of all three strategies

## Key Concepts

### Builder Pattern Options

Brobot provides two ways to create action chains:

**1. ActionChainOptions.Builder (Traditional Pattern):**
```java
// Explicit configuration with setStrategy()
ActionChainOptions nestedFind = new ActionChainOptions.Builder(findBars)
    .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .then(findYellow)
    .setPauseAfterEnd(0.5)
    .build();
```

**2. ActionChainBuilder (Fluent API):**
```java
// Cleaner fluent interface with withStrategy()
ActionChainOptions nestedFind = ActionChainBuilder.of(findBars)
    .then(findYellow)
    .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .pauseAfterEnd(0.5)
    .build();
```

**Key Differences:**
- `ActionChainOptions.Builder` uses `setStrategy()`, `setPauseAfterEnd()`
- `ActionChainBuilder` uses `withStrategy()`, `pauseAfterEnd()` (more fluent)
- Both produce identical `ActionChainOptions` objects
- `ActionChainBuilder.of()` is preferred for readability

**When to Use Each:**
- **ActionChainBuilder**: Multi-step chains (3+ actions), complex configurations
- **ActionChainOptions.Builder**: Simple 2-action chains, legacy code compatibility

### NESTED vs CONFIRM

**NESTED Strategy:**
```java
// Each action searches WITHIN the results of the previous
ActionChainOptions nestedFind = new ActionChainOptions.Builder(findBars)
    .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .then(findYellow)
    .build();
// Result: Yellow regions found INSIDE the bar patterns
```

**CONFIRM Strategy:**
```java
// Each action validates the results of the previous
ActionChainOptions confirmedFind = new ActionChainOptions.Builder(findButtons)
    .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
    .then(confirmColor)
    .build();
// Result: Original button matches that passed color confirmation
```

### When to Use Each Strategy

**Decision Guide:**

```
Need to find X within Y?
    YES -> Use NESTED
    |
    NO -> Need to validate X has property Y?
        YES -> Use CONFIRM
        |
        NO -> Finding independent elements?
            YES -> Use SEQUENTIAL (default)
```

**Detailed Scenarios:**

**NESTED** - When you need to find something within something else:
- Text within panels (`findColoredTextInPanels()` example)
- Icons within toolbars
- Colors within shapes
- Multi-level UI hierarchy navigation
- Progressive refinement from broad to specific

**CONFIRM** - When you need to validate matches meet criteria:
- Buttons with correct state (`detectInteractiveElements()` example)
- Elements with expected properties
- Multi-criteria matching (`confirmUIElements()` example)
- Reducing false positives
- Quality assurance checks

**SEQUENTIAL** (default) - When searches are independent:
- Find A, then find B elsewhere
- Multiple unrelated targets
- No spatial or logical relationship between searches
- Collecting diverse elements

## Example Methods Reference

The `CombiningFindsExamples.java` class contains 8 demonstration methods:

1. **`demonstrateNestedStrategy()`** - Basic NESTED usage with pattern and color
2. **`demonstrateConfirmStrategy()`** - Basic CONFIRM usage for validation
3. **`findColoredTextInPanels()`** - Complex 3-stage nested search (panels → text → color)
4. **`detectInteractiveElements()`** - Finding interactive UI elements with hover states
5. **`compareStrategies()`** - Side-by-side comparison of SEQUENTIAL, NESTED, and CONFIRM
6. **`multiStageFiltering()`** - Best practice: broad → medium → narrow filtering
7. **`findYellowHealthBars()`** - Documentation example: finding colored health bars
8. **`confirmUIElements()`** - Documentation example: validating UI button patterns

Each method demonstrates different patterns and can be run independently.

## Running the Example

```bash
./gradlew bootRun
```

The example runs in mock mode by default. Check the `history/` folder to see visualizations of how nested and confirmed searches work.

## Configuration Options

### Application Properties

Configure Brobot behavior in `application.yml` or `application.properties`:

**Mock Mode (for headless testing):**
```yaml
brobot:
  mock: true  # Enable mock mode (no real screenshots)
```

**Image Paths:**
```yaml
brobot:
  core:
    image-path: "images/"  # Base directory for pattern images
```

**Logging Configuration:**
```yaml
brobot:
  logging:
    verbosity: VERBOSE  # Options: MINIMAL, NORMAL, VERBOSE
  console:
    actions:
      enabled: true
      level: VERBOSE  # Log action execution details
```

### Common Configuration Profiles

**Development (with real GUI):**
```yaml
brobot:
  mock: false
  logging:
    verbosity: VERBOSE
  console:
    actions:
      enabled: true
```

**CI/CD Pipeline (headless):**
```yaml
brobot:
  mock: true
  logging:
    verbosity: NORMAL
  console:
    actions:
      enabled: false  # Reduce log noise
```

**Debugging (maximum visibility):**
```yaml
brobot:
  mock: false
  logging:
    verbosity: VERBOSE
  console:
    actions:
      enabled: true
      level: VERBOSE
  screenshots:
    save-history: true  # Save to history/ folder
```

## Multi-Stage Filtering Pattern

The `multiStageFiltering()` example demonstrates a powerful pattern for accurate finding:

**Strategy: Funnel from Broad to Narrow**

1. **Stage 1 - Broad Search** (similarity: 0.6)
   - Low threshold to capture all candidates
   - Minimizes false negatives
   - May include some false positives

2. **Stage 2 - Shape Filter** (similarity: 0.8)
   - Medium threshold refines by shape
   - Filters out obviously wrong matches
   - Reduces search space

3. **Stage 3 - Color Confirm** (similarity: 0.9)
   - High threshold for final validation
   - Eliminates remaining false positives
   - Returns only high-confidence matches

**Code Example:**
```java
ActionChainOptions multiStage = ActionChainBuilder.of(broadSearch)
    .then(shapeFilter)
    .then(colorConfirm)
    .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .build();
```

**Benefits:**
- Balances recall (finding all targets) with precision (avoiding false matches)
- More reliable than single-stage high-threshold search
- Adapts to varying image quality and lighting

## Troubleshooting

### Empty Result Sets

**Problem:** Chain returns zero matches even though targets exist.

**Common Causes:**
1. **Wrong Strategy** - Using NESTED when targets aren't spatially related
   ```java
   // WRONG: Second search looks inside first results
   .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)

   // RIGHT: Independent searches
   .setStrategy(ActionChainOptions.ChainingStrategy.SEQUENTIAL)
   ```

2. **Similarity Too High** - First stage filters out everything
   ```java
   // WRONG: Too strict for initial search
   .setSimilarity(0.95)  // Stage 1

   // RIGHT: Progressive thresholds
   .setSimilarity(0.6)   // Stage 1: broad
   .setSimilarity(0.8)   // Stage 2: medium
   .setSimilarity(0.9)   // Stage 3: narrow
   ```

3. **Search Region Too Small** - NESTED searches within tiny regions
   ```java
   // Check match sizes in logs:
   log.info("Match region: {}x{}", match.w(), match.h());
   // If <10px, patterns won't fit inside
   ```

### Mock Mode Issues

**Problem:** Tests fail with "mock mode enabled but no mock data".

**Solution:**
```yaml
# Ensure mock property is set correctly
brobot:
  mock: true  # Use lowercase 'true', not 'True'
```

**Problem:** Mock mode returns unrealistic results.

**Solution:** Mock mode uses simplified matching. For realistic testing:
```yaml
brobot:
  mock: false  # Test with real screenshots
```

### Pattern Matching Failures

**Problem:** Similar-looking elements not being matched.

**Diagnosis:**
1. Check history folder for visual output
2. Review similarity thresholds
3. Verify image format (PNG recommended)

**Solutions:**
```java
// Increase tolerance
.setSimilarity(0.7)  // Down from 0.9

// Use multiple pattern variations
StateImage button = new StateImage.Builder()
    .addPatterns("button-normal.png", "button-hover.png", "button-pressed.png")
    .build();

// Try different color strategies
ColorFindOptions.Builder()
    .setColorStrategy(ColorFindOptions.Color.MU)      // Mean color
    // OR
    .setColorStrategy(ColorFindOptions.Color.KMEANS)  // Cluster-based
    // OR
    .setColorStrategy(ColorFindOptions.Color.CLASSIFICATION) // ML-based
```

### Performance Issues

**Problem:** Action chains take too long to execute.

**Causes & Solutions:**

1. **Too Many Candidates in Early Stages**
   ```java
   // Add maxMatches to limit processing
   PatternFindOptions.Builder()
       .setStrategy(PatternFindOptions.Strategy.ALL)
       .setMaxMatches(50)  // Stop after 50 matches
   ```

2. **High-Resolution Screenshots**
   ```java
   // Reduce search region
   PatternFindOptions.Builder()
       .addSearchRegion(targetRegion)  // Don't search entire screen
   ```

3. **Unnecessary Pauses**
   ```java
   // Remove or reduce pauses
   ActionChainBuilder.of(find)
       .pauseBeforeBegin(0.1)  // Down from 0.5
       .pauseAfterEnd(0.1)
   ```

### Wrong Strategy Selection

**Symptoms:**
- Getting first action results instead of refined results (used CONFIRM when should use NESTED)
- Getting nested results when you wanted validation (used NESTED when should use CONFIRM)
- Getting combined unrelated results (missing strategy entirely)

**Decision Table:**

| Goal | Strategy | Returns |
|------|----------|---------|
| Find X inside Y | NESTED | Regions of X found within Y's boundaries |
| Verify Y has property X | CONFIRM | Original Y matches that passed X check |
| Find X, then Y | SEQUENTIAL | Both X and Y matches (independent) |

## Creating Test Images

To run with real GUI:
1. Create subdirectories in `images/`:
   - `patterns/`, `colors/`, `ui/`, `interactive/`, `comparison/`, `filter/`
2. Add appropriate screenshots for each example
3. Set `brobot.mock: false` in application.yml

## Detailed Code Examples

### Example 1: Basic Nested Search (Finding Yellow Health Bars)

This example demonstrates the most common NESTED pattern - finding a specific color within a pattern.

```java
public void findYellowHealthBars() {
    // Stage 1: Find all bar-shaped patterns (broad search)
    PatternFindOptions barPatterns = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.7)  // Lower threshold catches all bars
        .build();

    // Stage 2: Filter for yellow color within bars
    ColorFindOptions yellowFilter = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.MU)  // Mean color matching
        .setDiameter(10)     // Sample area size
        .setSimilarity(0.9)  // High precision for color
        .build();

    // Create nested chain
    ActionChainOptions findYellowBars = new ActionChainOptions.Builder(barPatterns)
        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
        .then(yellowFilter)
        .build();

    // Execute
    StateImage barImage = new StateImage.Builder()
        .addPatterns("bar_pattern.png")
        .build();

    ObjectCollection objects = new ObjectCollection.Builder()
        .withImages(barImage)
        .build();

    ActionResult result = action.perform(findYellowBars, objects);
    log.info("Found {} yellow health bars", result.getMatchList().size());
}
```

**What happens:** Brobot first finds all bar patterns, then searches for yellow color only within those bar regions, returning the yellow areas.

### Example 2: Confirmation Pattern (Validating UI Elements)

This example shows how to use CONFIRM to validate that found elements have the expected properties.

```java
public void confirmUIElements() {
    // Stage 1: Find button patterns
    PatternFindOptions buttonPattern = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.8)
        .build();

    // Stage 2: Confirm buttons have the correct color
    ColorFindOptions buttonColor = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.CLASSIFICATION)
        .setSimilarity(0.85)
        .build();

    // Create confirmation chain
    ActionChainOptions confirmButtons = new ActionChainOptions.Builder(buttonPattern)
        .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
        .then(buttonColor)
        .build();

    StateImage buttonImage = new StateImage.Builder()
        .addPatterns("button_pattern.png")
        .build();

    StateImage colorSample = new StateImage.Builder()
        .addPatterns("button_color.png")
        .build();

    ObjectCollection objects = new ObjectCollection.Builder()
        .withImages(buttonImage, colorSample)
        .build();

    ActionResult result = action.perform(confirmButtons, objects);
    log.info("Found {} confirmed buttons", result.getMatchList().size());
}
```

**What happens:** Brobot finds all button patterns, then validates each one has the correct color. Returns only the original button matches that passed color validation.

### Example 3: Complex Multi-Stage Chain (Colored Text in Panels)

This example demonstrates a 3-stage nested search using ActionChainBuilder for cleaner code.

```java
public void findColoredTextInPanels() {
    // Stage 1: Find all UI panels
    PatternFindOptions findPanels = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.75)
        .build();

    // Stage 2: Find text regions within panels
    PatternFindOptions findTextRegions = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.7)
        .build();

    // Stage 3: Confirm text is red
    ColorFindOptions confirmRedText = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.MU)
        .setSimilarity(0.9)
        .build();

    // Build 3-stage chain with ActionChainBuilder
    ActionChainOptions findRedTextInPanels = ActionChainBuilder.of(findPanels)
        .then(findTextRegions)
        .then(confirmRedText)
        .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
        .build();

    StateImage panelImage = new StateImage.Builder()
        .addPatterns("ui/panel-border")
        .build();

    StateImage textPattern = new StateImage.Builder()
        .addPatterns("ui/text-region")
        .build();

    StateImage redColor = new StateImage.Builder()
        .addPatterns("colors/red-text")
        .build();

    ObjectCollection objects = new ObjectCollection.Builder()
        .withImages(panelImage, textPattern, redColor)
        .build();

    ActionResult result = action.perform(findRedTextInPanels, objects);
    log.info("Found {} red text regions", result.getMatchList().size());
}
```

**What happens:**
1. Find all panels on screen
2. Within each panel, find text regions
3. Within each text region, confirm red color
4. Return the red color regions found inside text regions inside panels

### Example 4: Interactive Element Detection with Click

This example combines CONFIRM strategy with a click action.

```java
public void detectInteractiveElements() {
    // Stage 1: Find button shape
    PatternFindOptions findButtonShape = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.8)
        .build();

    // Stage 2: Confirm hover state (color-based)
    ColorFindOptions confirmHoverColor = new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.KMEANS)
        .setKmeans(2)  // Look for button color + highlight
        .setSimilarity(0.85)
        .build();

    // Stage 3: Click confirmed buttons
    ClickOptions clickButton = new ClickOptions.Builder()
        .setNumberOfClicks(1)
        .build();

    // Chain: find -> confirm -> click
    ActionChainOptions interactiveChain = new ActionChainOptions.Builder(findButtonShape)
        .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
        .then(confirmHoverColor)
        .then(clickButton)
        .build();

    StateImage buttonShape = new StateImage.Builder()
        .addPatterns("interactive/button-shape")
        .build();

    StateImage hoverColor = new StateImage.Builder()
        .addPatterns("interactive/hover-highlight")
        .build();

    ObjectCollection objects = new ObjectCollection.Builder()
        .withImages(buttonShape, hoverColor)
        .build();

    ActionResult result = action.perform(interactiveChain, objects);
    if (result.isSuccess()) {
        log.info("Successfully clicked interactive button");
    }
}
```

**What happens:** Finds buttons, confirms they have hover state, clicks the confirmed ones. Only buttons that pass color confirmation get clicked.

## Best Practices

1. **Start Broad, Refine Narrow** - Use NESTED for progressive refinement
   - Stage 1: Low similarity (0.6-0.7) to catch all candidates
   - Stage 2: Medium similarity (0.75-0.85) for shape filtering
   - Stage 3: High similarity (0.9+) for final validation

2. **Validate Important Matches** - Use CONFIRM for critical elements
   - Reduces false positives without re-searching
   - Returns original matches that passed validation
   - Ideal for quality checks before actions

3. **Use ActionChainBuilder for Complex Chains**
   - Cleaner syntax with fluent API
   - Better readability for 3+ stage chains
   - Easier to maintain and modify

4. **Set Appropriate Similarities**
   - Pattern matching: 0.7-0.9 depending on variability
   - Color matching: 0.85-0.95 for precise color validation
   - First stage always lower than subsequent stages

5. **Check History Folder**
   - Enable with `brobot.screenshots.save-history: true`
   - Visual output shows exactly what was found at each stage
   - Essential for debugging unexpected results

6. **Use Multiple Pattern Variations**
   ```java
   StateImage.Builder()
       .addPatterns("button-normal.png", "button-hover.png")
       .build();
   ```

7. **Limit Search Regions**
   ```java
   PatternFindOptions.Builder()
       .addSearchRegion(knownRegion)  // Faster than full screen
   ```

8. **Test Strategies with compareStrategies() Example**
   - Run side-by-side comparison to understand behavior
   - Verify which strategy produces expected results
   - Learn how strategies differ in practice

## Next Steps

1. Try the motion detection example for finding moving objects
2. Explore conditional chains for more complex logic
3. Check the action-config examples for advanced patterns
4. Review `/docs/03-core-library/guides/finding-objects/combining-finds.md` for complete documentation