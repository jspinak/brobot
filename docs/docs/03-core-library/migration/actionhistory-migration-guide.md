---
title: ActionConfig API Quick Reference
sidebar_position: 1
---

# ActionConfig API Quick Reference

## Overview

Brobot 1.1.0 uses the modern `ActionConfig` hierarchy for all action operations. This guide provides quick reference patterns for using ActionConfig with ActionHistory and ActionRecord.

## Why ActionConfig?

### Benefits of ActionConfig

1. **Type Safety**: Strongly-typed configuration classes for each action type
2. **Better IDE Support**: Enhanced autocomplete and inline documentation
3. **Cleaner Code**: Intuitive builder patterns with clear method names
4. **Performance**: Optimized caching and reduced overhead
5. **Future-Proof**: All new features use ActionConfig

---

## ActionConfig Class Reference

### Complete Mapping Table

| Action Type | ActionConfig Class | Package |
|------------|-------------------|---------|
| Pattern Find | `PatternFindOptions` | `io.github.jspinak.brobot.action.basic.find` |
| Click | `ClickOptions` | `io.github.jspinak.brobot.action.basic.click` |
| Type/Text | `TypeOptions` | `io.github.jspinak.brobot.action.basic.type` |
| Drag | `DragOptions` | `io.github.jspinak.brobot.action.composite.drag` |
| Vanish | `VanishOptions` | `io.github.jspinak.brobot.action.basic.vanish` |
| Mouse Move | `MouseMoveOptions` | `io.github.jspinak.brobot.action.basic.mouse` |
| Mouse Down | `MouseDownOptions` | `io.github.jspinak.brobot.action.basic.mouse` |
| Mouse Up | `MouseUpOptions` | `io.github.jspinak.brobot.action.basic.mouse` |
| Define Region | `DefineRegionOptions` | `io.github.jspinak.brobot.action.basic.region` |
| Highlight | `HighlightOptions` | `io.github.jspinak.brobot.action.basic.highlight` |

---

## Common Usage Patterns

### 1. Creating ActionRecords with ActionConfig

#### Pattern Find Record

```java
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;
import java.time.LocalDateTime;

// Create a find record with best match strategy
ActionRecord findRecord = new ActionRecord.Builder()
    .setActionConfig(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.BEST)
        .setSimilarity(0.85)
        .build())
    .addMatch(match)
    .setActionSuccess(true)
    .setTimeStamp(LocalDateTime.now())  // Optional - auto-set if omitted
    .build();
```

#### Click Record

```java
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;

// Create a double-click record
ActionRecord clickRecord = new ActionRecord.Builder()
    .setActionConfig(new ClickOptions.Builder()
        .setNumberOfClicks(2)  // Double-click
        .build())
    .setActionSuccess(true)
    .build();
```

#### Type/Text Record

```java
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;

// Create a typing record with modifiers
ActionRecord typeRecord = new ActionRecord.Builder()
    .setActionConfig(new TypeOptions.Builder()
        .setModifiers("^a")  // Ctrl+A
        .build())
    .setText("Hello World")
    .setActionSuccess(true)
    .build();
```

---

### 2. Using ActionHistory with ActionConfig

#### Querying Records by ActionConfig

```java
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import java.util.Optional;

ActionHistory history = new ActionHistory();

// Create find config
PatternFindOptions findConfig = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .build();

// Query for matching records
Optional<ActionRecord> record = history.getRandomSnapshot(findConfig);

if (record.isPresent()) {
    System.out.println("Found record with " +
        record.get().getMatchList().size() + " matches");
}
```

#### Getting Similar Snapshots

```java
import java.util.List;

// Get all records with similar ActionConfig
List<ActionRecord> similarRecords = history.getSimilarSnapshots(findConfig);

System.out.println("Found " + similarRecords.size() + " similar records");
```

#### Getting Random Text

```java
// Convenience method - internally uses PatternFindOptions
String randomText = history.getRandomText();
System.out.println("Random text from history: " + randomText);
```

---

### 3. PatternFindOptions Strategy Types

```java
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

// FIRST - Find first match
PatternFindOptions firstMatch = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.FIRST)
    .build();

// BEST - Find best match by similarity
PatternFindOptions bestMatch = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .build();

// ALL - Find all matches
PatternFindOptions allMatches = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.ALL)
    .build();

// EACH - Process each match individually
PatternFindOptions eachMatch = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.EACH)
    .build();
```

---

## Testing with ActionConfig

### Unit Testing ActionRecord Creation

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Test
void testActionRecordCreation() {
    PatternFindOptions config = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.BEST)
        .setSimilarity(0.9)
        .build();

    ActionRecord record = new ActionRecord.Builder()
        .setActionConfig(config)
        .setActionSuccess(true)
        .build();

    assertNotNull(record.getActionConfig());
    assertTrue(record.isActionSuccess());
    assertNotNull(record.getTimeStamp());
}
```

### Integration Testing with ActionHistory

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import io.github.jspinak.brobot.test.BrobotTestBase;

public class ActionHistoryIntegrationTest extends BrobotTestBase {

    private ActionHistory history;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        history = new ActionHistory();
    }

    @Test
    void testAddAndRetrieveRecord() {
        // Create and add record
        PatternFindOptions config = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();

        ActionRecord record = new ActionRecord.Builder()
            .setActionConfig(config)
            .setActionSuccess(true)
            .build();

        history.addSnapshot(record);

        // Retrieve record
        Optional<ActionRecord> retrieved = history.getRandomSnapshot(config);

        assertTrue(retrieved.isPresent());
        assertEquals(record, retrieved.get());
    }
}
```

For comprehensive testing examples, see [ActionHistory Integration Testing](/docs/04-testing/actionhistory-integration-testing).

---

## Complete Working Examples

### Example 1: Recording and Replaying Actions

```java
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;

public class ActionRecordingExample {

    private final ActionHistory history = new ActionHistory();

    public void recordFindAction(Match match) {
        ActionRecord record = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.85)
                .build())
            .addMatch(match)
            .setActionSuccess(true)
            .build();

        history.addSnapshot(record);
    }

    public void recordClickAction() {
        ActionRecord record = new ActionRecord.Builder()
            .setActionConfig(new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build())
            .setActionSuccess(true)
            .build();

        history.addSnapshot(record);
    }

    public void replayActions() {
        // Get all find actions
        PatternFindOptions findConfig = new PatternFindOptions.Builder().build();
        List<ActionRecord> findActions = history.getSimilarSnapshots(findConfig);

        System.out.println("Found " + findActions.size() + " find actions");

        // Get all click actions
        ClickOptions clickConfig = new ClickOptions.Builder().build();
        List<ActionRecord> clickActions = history.getSimilarSnapshots(clickConfig);

        System.out.println("Found " + clickActions.size() + " click actions");
    }
}
```

### Example 2: Action Statistics

```java
import java.util.Map;
import java.util.HashMap;

public class ActionStatistics {

    public Map<String, Integer> getActionCounts(ActionHistory history) {
        Map<String, Integer> counts = new HashMap<>();

        // Count find actions
        PatternFindOptions findConfig = new PatternFindOptions.Builder().build();
        int findCount = history.getSimilarSnapshots(findConfig).size();
        counts.put("FIND", findCount);

        // Count click actions
        ClickOptions clickConfig = new ClickOptions.Builder().build();
        int clickCount = history.getSimilarSnapshots(clickConfig).size();
        counts.put("CLICK", clickCount);

        // Count type actions
        TypeOptions typeConfig = new TypeOptions.Builder().build();
        int typeCount = history.getSimilarSnapshots(typeConfig).size();
        counts.put("TYPE", typeCount);

        return counts;
    }

    public void printStatistics(ActionHistory history) {
        Map<String, Integer> counts = getActionCounts(history);

        System.out.println("Action Statistics:");
        counts.forEach((action, count) ->
            System.out.println(action + ": " + count));
    }
}
```

---

## Troubleshooting

### Common Issues

#### 1. Import Resolution Errors

**Problem**: Cannot resolve symbol 'PatternFindOptions'

**Solution**: Add the correct import and ensure Brobot 1.1.0+ is in your dependencies:

```java
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
```

```gradle
dependencies {
    implementation 'io.github.jspinak:brobot-library:1.1.0'
}
```

#### 2. Null ActionConfig

**Problem**: ActionRecord has null ActionConfig

**Solution**: Always set ActionConfig when creating ActionRecord:

```java
// WRONG - Missing ActionConfig
ActionRecord record = new ActionRecord.Builder()
    .setActionSuccess(true)
    .build();  // ActionConfig will be null!

// CORRECT - Always include ActionConfig
ActionRecord record = new ActionRecord.Builder()
    .setActionConfig(new PatternFindOptions.Builder().build())
    .setActionSuccess(true)
    .build();
```

#### 3. Wrong Builder Pattern

**Problem**: Using `ClassName.builder()` instead of `new ClassName.Builder()`

**Solution**: Use the correct builder pattern:

```java
// WRONG
PatternFindOptions config = PatternFindOptions.builder()  // Doesn't exist
    .build();

// CORRECT
PatternFindOptions config = new PatternFindOptions.Builder()  // Use 'new'
    .build();
```

---

## Best Practices

### 1. Always Set ActionConfig

Every ActionRecord should have an ActionConfig:

```java
// Good - Explicit ActionConfig
ActionRecord record = new ActionRecord.Builder()
    .setActionConfig(new PatternFindOptions.Builder().build())
    .setActionSuccess(true)
    .build();
```

### 2. Use Specific Strategies

Choose appropriate PatternFindOptions strategies:

```java
// For single best match
PatternFindOptions.Strategy.BEST

// For first match (faster)
PatternFindOptions.Strategy.FIRST

// For all matches
PatternFindOptions.Strategy.ALL
```

### 3. Test with Mock Mode

Use BrobotTestBase for testing:

```java
public class MyTest extends BrobotTestBase {
    // Mock mode automatically enabled
    // Safe for headless environments
}
```

### 4. Configure Similarity Appropriately

```java
PatternFindOptions highPrecision = new PatternFindOptions.Builder()
    .setSimilarity(0.95)  // Very strict
    .build();

PatternFindOptions moderate = new PatternFindOptions.Builder()
    .setSimilarity(0.85)  // Balanced (default)
    .build();

PatternFindOptions lenient = new PatternFindOptions.Builder()
    .setSimilarity(0.70)  // More forgiving
    .build();
```

---

## Related Documentation

### Core API Documentation
- [ActionConfig API Reference](/docs/03-core-library/action-config/01-overview) - Complete API documentation
- [ActionConfig Examples](/docs/03-core-library/action-config/03-examples) - More usage examples
- [ActionConfig Reference](/docs/03-core-library/action-config/05-reference) - Detailed method reference

### Testing Resources
- [Testing Introduction](/docs/04-testing/testing-intro) - Testing overview
- [ActionHistory Integration Testing](/docs/04-testing/actionhistory-integration-testing) - Comprehensive testing guide
- [Unit Testing Guide](/docs/04-testing/unit-testing) - Unit test patterns
- [Mock Mode Guide](/docs/04-testing/mock-mode-guide) - Mock mode testing

### Architecture Documentation
- [Persistence Module Architecture](/docs/03-core-library/architecture/persistence-module) - ActionHistory persistence
- [ActionResult Architecture](/docs/03-core-library/architecture/actionresult-architecture) - ActionResult design

### Additional Migration Resources
- [ActionConfig Migration Guide](/docs/03-core-library/action-config/12-migration-guide) - Detailed migration patterns
- [Builder Migration Guide](/docs/03-core-library/migration/BUILDER_MIGRATION_GUIDE) - Builder pattern migration

---

## Quick Reference Card

```java
// Import statements
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.action.ActionHistory;

// Create ActionConfig
PatternFindOptions config = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .setSimilarity(0.85)
    .build();

// Create ActionRecord
ActionRecord record = new ActionRecord.Builder()
    .setActionConfig(config)
    .addMatch(match)
    .setActionSuccess(true)
    .build();

// Use with ActionHistory
ActionHistory history = new ActionHistory();
history.addSnapshot(record);
Optional<ActionRecord> found = history.getRandomSnapshot(config);
List<ActionRecord> similar = history.getSimilarSnapshots(config);
String text = history.getRandomText();
```

---

## Getting Help

1. **Documentation**: Refer to the [ActionConfig API Reference](/docs/03-core-library/action-config/01-overview)
2. **Examples**: See [ActionConfig Examples](/docs/03-core-library/action-config/03-examples)
3. **Testing**: Review [ActionHistory Integration Testing](/docs/04-testing/actionhistory-integration-testing)
4. **Issues**: Report problems at https://github.com/jspinak/brobot/issues
5. **Community**: Join the Brobot Discord server for support

---

**Document Version**: 2.0
**Last Updated**: 2025-01
**Brobot Version**: 1.1.0+
