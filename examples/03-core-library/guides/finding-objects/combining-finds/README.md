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

### When to Use Each

- **NESTED**: When you need to find something within something else
  - Text within panels
  - Icons within toolbars
  - Colors within shapes

- **CONFIRM**: When you need to validate matches meet criteria
  - Buttons with correct state
  - Elements with expected properties
  - Multi-criteria matching

- **SEQUENTIAL** (default): When searches are independent
  - Find A, then find B elsewhere
  - Multiple unrelated targets

## Running the Example

```bash
./gradlew bootRun
```

The example runs in mock mode. Check the `history/` folder to see visualizations of how nested and confirmed searches work.

## Multi-Stage Filtering Pattern

The example shows a powerful pattern for accurate finding:

1. **Broad Search** - Low similarity, find all candidates
2. **Shape Filter** - Medium similarity, filter by shape
3. **Color Confirm** - High similarity, final validation

This approach minimizes false negatives while eliminating false positives.

## Creating Test Images

To run with real GUI:
1. Create subdirectories in `images/`:
   - `patterns/`, `colors/`, `ui/`, `interactive/`, `comparison/`, `filter/`
2. Add appropriate screenshots for each example
3. Set `brobot.core.mock: false` in application.yml

## Best Practices

1. **Start Broad, Refine Narrow** - Use NESTED for progressive refinement
2. **Validate Important Matches** - Use CONFIRM for critical elements
3. **Use ActionChainBuilder** - For complex multi-step chains
4. **Set Appropriate Similarities** - Lower for initial finds, higher for confirmations
5. **Check History Folder** - Visual output helps understand what happened

## Next Steps

1. Try the motion detection example for finding moving objects
2. Explore conditional chains for more complex logic
3. Check the action-config examples for advanced patterns