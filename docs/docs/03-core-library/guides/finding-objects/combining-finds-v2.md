---
sidebar_position: 2
---

# Combining Find Operations

Combining multiple find operations in the same Action can give us better results.
There are two ways to do this with Brobot: Nested Finds, and Confirmed Finds.

## Using the New ActionConfig API

With the new ActionConfig API introduced in Brobot 2.0, combining find operations is more intuitive and type-safe. You can now use specific find configuration classes and chain operations together.

### Pattern and Color Finding

When you need to find objects that match both a pattern and a specific color:

```java
// Find yellow bars using pattern matching first, then color filtering
PatternFindOptions patternOptions = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .build();

ColorFindOptions colorOptions = new ColorFindOptions.Builder()
        .setTargetColor(Color.YELLOW)
        .setColorTolerance(10)
        .build();

// Execute pattern finding first
ActionResult patternResult = new ActionResult();
patternResult.setActionConfig(patternOptions);
ActionInterface patternAction = actionService.getAction(patternOptions);
patternAction.perform(patternResult, objectCollection);

// Then filter by color
ActionResult colorResult = new ActionResult();
colorResult.setActionConfig(colorOptions);
colorResult.setPreviousResult(patternResult); // Chain the operations
ActionInterface colorAction = actionService.getAction(colorOptions);
colorAction.perform(colorResult, objectCollection);
```

Combining find methods can give us more accurate matches in scenarios where the 
form and color of an object are not unique. Take the example below, where we are looking
for the yellow bars above the kobolds (the top-left bar has blue circles on it). 
A relatively solid bar of color will correspond to other places on the screen, including 
the green and red bars above the character. On the other hand, the yellow color of 
the bars would also be found in other places, including on the character's weapon and
interspersed throughout the grass. One way to narrow down our search is to look for 
both a pattern and a color.  

## Nested Finds

Nested Finds find objects inside the matches from the previous Find operation. This is useful when you want to find specific elements within larger patterns.

```java
// Configure for nested finding
PatternFindOptions outerFind = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setNestedFind(true)  // Enable nested finding
        .build();

// The inner find will search within the results of the outer find
ColorFindOptions innerFind = new ColorFindOptions.Builder()
        .setTargetColor(Color.YELLOW)
        .setSearchInsidePrevious(true)  // Search inside previous results
        .build();
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
// Configure for confirmed finding
PatternFindOptions confirmedFind = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setKeepLargerMatches(true)  // Enable confirmed finding
        .build();

ColorFindOptions colorConfirm = new ColorFindOptions.Builder()
        .setTargetColor(Color.YELLOW)
        .setConfirmMode(true)  // Confirm matches from previous operation
        .build();
```

In the below example, the pattern matches from the initial find operation are drawn in
blue and the color matches are drawn in pink. To the right of the scene are the contents of the 
color matches. The pattern match is selected in its original size. Only the yellow bars are selected.  

![confirmedFind](/img/color/confirmedFind.png)  

## Advanced Chaining with ChainedFindOptions

For complex scenarios, you can use the `ChainedFindOptions` class to define a sequence of find operations:

```java
ChainedFindOptions chainedFind = new ChainedFindOptions.Builder()
        .addStep(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build())
        .addStep(new ColorFindOptions.Builder()
                .setTargetColor(Color.YELLOW)
                .build())
        .addStep(new SizeFindOptions.Builder()
                .setMinWidth(50)
                .setMinHeight(20)
                .build())
        .setChainMode(ChainedFindOptions.ChainMode.NESTED)
        .build();

ActionResult result = new ActionResult();
result.setActionConfig(chainedFind);
ActionInterface chainedAction = actionService.getAction(chainedFind);
chainedAction.perform(result, objectCollection);
```

## Migration from ActionOptions

If you're migrating from the old ActionOptions API:

**Old way:**
```java
ActionOptions color = new ActionOptions.Builder()
        .setAction(ActionOptions.Action.FIND)
        .setFind(ActionOptions.Find.ALL)
        .addFind(ActionOptions.Find.COLOR)
        .keepLargerMatches(true)
        .build();
```

**New way:**
```java
ChainedFindOptions chainedFind = new ChainedFindOptions.Builder()
        .addStep(PatternFindOptions.forAllMatches())
        .addStep(ColorFindOptions.forYellowObjects())
        .setChainMode(ChainedFindOptions.ChainMode.CONFIRMED)
        .build();
```

The new API provides better type safety, clearer intent, and more flexibility in combining different types of find operations.

For more information on the new ActionConfig API, see the [Migration Guide](/docs/migration-guide).