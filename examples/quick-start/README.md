# Brobot Quick Start Example

This is a minimal example demonstrating the basic concepts of Brobot 1.1.0.

## What This Example Shows

- Basic find and click operations
- Using the new ActionConfig API
- Different find strategies (quick, precise, custom)
- Spring Boot integration

## Running the Example

1. Build the project:
   ```bash
   ./gradlew build
   ```

2. Add your image files to `src/main/resources/images/`:
   - `submit-button.png` - Screenshot of a button to click
   - `target.png` - Any UI element for the find examples

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

## Key Concepts

### 1. StateImage
Represents an image pattern to find on screen:
```java
StateImage buttonImage = new StateImage.Builder()
    .setName("submit-button")
    .addPattern("submit-button.png")
    .build();
```

### 2. PatternFindOptions
Configures how to search for images:
```java
// Quick search - fast but less accurate
PatternFindOptions.forQuickSearch()

// Precise search - slower but more accurate
PatternFindOptions.forPreciseSearch()

// Custom configuration
new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.ALL)
    .setSimilarity(0.85)
    .build()
```

### 3. Action Execution
The standard pattern for executing actions:
```java
// 1. Create object collection
ObjectCollection objects = new ObjectCollection.Builder()
    .withImages(images)
    .build();

// 2. Execute action
ActionResult result = action.perform(config, objects);

// 3. Check results
if (result.isSuccess()) {
    // Handle success
}
```

## Next Steps

- Add more complex patterns and state management
- See the `claude-automator` example for annotation-based states
- Explore action chaining and transitions