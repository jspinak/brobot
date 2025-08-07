---
title: ActionHistory Migration Guide
sidebar_position: 1
---

# ActionHistory Migration Guide

## Overview

Brobot 1.2.0 introduces a major API modernization, transitioning from the legacy `ActionOptions` system to the new `ActionConfig` hierarchy. This guide helps you migrate your existing code to the modern API.

## Why Migrate?

### Benefits of the Modern API

1. **Type Safety**: Strongly-typed configuration classes for each action type
2. **Better IDE Support**: Enhanced autocomplete and documentation
3. **Cleaner Code**: More intuitive builder patterns
4. **Performance**: Optimized caching and reduced overhead
5. **Future-Proof**: All new features will use the modern API

### Deprecation Timeline

- **1.2.0**: Deprecation warnings introduced, migration tools available
- **1.3.0**: Enhanced warnings, deprecated methods marked for removal
- **2.0.0**: Legacy API removed completely

## Quick Start Migration

### Automated Migration Tool

The fastest way to migrate is using the automated migration tool:

```bash
# Download the migration tool
wget https://github.com/jspinak/brobot/releases/download/v1.2.0/brobot-migration-tool.jar

# Analyze your project
java -jar brobot-migration-tool.jar --path /your/project --mode analyze

# Review the report, then migrate
java -jar brobot-migration-tool.jar --path /your/project --mode migrate --backup true
```

### Manual Migration Steps

If you prefer manual migration, follow these patterns:

## Migration Patterns

### 1. ActionHistory Method Updates

#### Finding Records by Action Type

**Before (Deprecated):**
```java
Optional<ActionRecord> record = actionHistory.getRandomSnapshot(ActionOptions.Action.FIND);
```

**After (Modern):**
```java
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

PatternFindOptions findConfig = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .build();
Optional<ActionRecord> record = actionHistory.getRandomSnapshot(findConfig);
```

#### Getting Random Text

**Before:**
```java
String text = actionHistory.getRandomText();
// This internally used ActionOptions.Action.FIND
```

**After:**
```java
String text = actionHistory.getRandomText();
// Now uses PatternFindOptions internally - no code change needed!
```

### 2. ActionRecord Creation

#### Creating Find Records

**Before:**
```java
ActionRecord record = new ActionRecord.Builder()
    .setActionOptions(new ActionOptions.Builder()
        .setAction(ActionOptions.Action.FIND)
        .setFind(ActionOptions.Find.BEST)
        .setSimilarity(0.85)
        .build())
    .addMatch(match)
    .setActionSuccess(true)
    .build();
```

**After:**
```java
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

ActionRecord record = new ActionRecord.Builder()
    .setActionConfig(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.BEST)
        .setSimilarity(0.85)
        .build())
    .addMatch(match)
    .setActionSuccess(true)
    .build();
```

#### Creating Click Records

**Before:**
```java
ActionRecord record = new ActionRecord.Builder()
    .setActionOptions(new ActionOptions.Builder()
        .setAction(ActionOptions.Action.CLICK)
        .setClickType(ActionOptions.ClickType.DOUBLE)
        .setNumberOfClicks(2)
        .build())
    .setActionSuccess(true)
    .build();
```

**After:**
```java
import io.github.jspinak.brobot.action.basic.click.ClickOptions;

ActionRecord record = new ActionRecord.Builder()
    .setActionConfig(new ClickOptions.Builder()
        .setClickType(ClickOptions.Type.DOUBLE)
        .setNumberOfClicks(2)
        .build())
    .setActionSuccess(true)
    .build();
```

### 3. Type Text Actions

**Before:**
```java
ActionRecord record = new ActionRecord.Builder()
    .setActionOptions(new ActionOptions.Builder()
        .setAction(ActionOptions.Action.TYPE)
        .setModifierKeys(Arrays.asList(17, 65)) // Ctrl+A
        .build())
    .setText("Hello World")
    .build();
```

**After:**
```java
import io.github.jspinak.brobot.action.basic.type.TypeOptions;

ActionRecord record = new ActionRecord.Builder()
    .setActionConfig(new TypeOptions.Builder()
        .setModifierKeys(Arrays.asList(17, 65)) // Ctrl+A
        .build())
    .setText("Hello World")
    .build();
```

### 4. Common ActionConfig Classes

Here's a mapping of all action types to their modern equivalents:

| Legacy ActionOptions.Action | Modern ActionConfig Class | Import |
|---------------------------|-------------------------|--------|
| FIND | PatternFindOptions | `io.github.jspinak.brobot.action.basic.find.PatternFindOptions` |
| CLICK | ClickOptions | `io.github.jspinak.brobot.action.basic.click.ClickOptions` |
| TYPE | TypeOptions | `io.github.jspinak.brobot.action.basic.type.TypeOptions` |
| DRAG | DragOptions | `io.github.jspinak.brobot.action.composite.drag.DragOptions` |
| VANISH | VanishOptions | `io.github.jspinak.brobot.action.basic.vanish.VanishOptions` |
| MOVE | MouseMoveOptions | `io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions` |
| MOUSE_DOWN | MouseDownOptions | `io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions` |
| MOUSE_UP | MouseUpOptions | `io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions` |
| DEFINE | DefineRegionOptions | `io.github.jspinak.brobot.action.basic.region.DefineRegionOptions` |
| HIGHLIGHT | HighlightOptions | `io.github.jspinak.brobot.action.basic.highlight.HighlightOptions` |

### 5. Find Strategy Mapping

| Legacy Find Type | Modern Strategy |
|-----------------|----------------|
| ActionOptions.Find.FIRST | PatternFindOptions.Strategy.FIRST |
| ActionOptions.Find.BEST | PatternFindOptions.Strategy.BEST |
| ActionOptions.Find.ALL | PatternFindOptions.Strategy.ALL |
| ActionOptions.Find.EACH | PatternFindOptions.Strategy.EACH |

## Testing Your Migration

### 1. Run Existing Tests

```bash
# Backend tests
cd brobot-builder/backend
pytest

# Frontend tests  
cd brobot-builder/frontend
./run-tests-safe.sh
```

### 2. Check for Deprecation Warnings

Enable deprecation logging to see what still needs migration:

```java
// In your application.properties
logging.level.io.github.jspinak.brobot.tools.migration=DEBUG
```

### 3. Monitor Migration Metrics

The migration system tracks usage metrics:

```java
import io.github.jspinak.brobot.tools.migration.DeprecationMetrics;

// Generate a report
String report = DeprecationMetrics.getInstance().generateReport();
System.out.println(report);

// Check migration progress
double progress = DeprecationMetrics.getInstance()
    .getMigrationProgress(modernApiCalls);
System.out.println("Migration progress: " + progress + "%");
```

## Backward Compatibility

### Gradual Migration

The system maintains backward compatibility during the transition:

1. **Both APIs Work**: You can use ActionOptions and ActionConfig simultaneously
2. **Automatic Conversion**: The framework converts between formats as needed
3. **Mixed Records**: ActionHistory can contain both legacy and modern records

### Example: Mixed Usage

```java
ActionHistory history = new ActionHistory();

// Add modern record
history.addSnapshot(new ActionRecord.Builder()
    .setActionConfig(new PatternFindOptions.Builder().build())
    .build());

// Add legacy record (still works but deprecated)
history.addSnapshot(new ActionRecord.Builder()
    .setActionOptions(new ActionOptions.Builder()
        .setAction(ActionOptions.Action.FIND)
        .build())
    .build());

// Query with modern API - finds both!
Optional<ActionRecord> record = history.getRandomSnapshot(
    new PatternFindOptions.Builder().build()
);
```

## JSON Data Migration

### Automatic Migration

JSON files are automatically migrated when deserialized:

```java
import io.github.jspinak.brobot.tools.migration.ActionHistoryJsonConverter;

@Autowired
ActionHistoryJsonConverter jsonConverter;

// This automatically migrates legacy JSON
ActionHistory history = jsonConverter.deserialize(legacyJson);
```

### Batch JSON Migration

```java
// Migrate all JSON files in a directory
ActionHistoryJsonConverter.BatchMigrationResult result = 
    jsonConverter.migrateDirectory(
        Paths.get("/data/histories"),
        "*.json"
    );

System.out.println("Migrated " + result.successfulMigrations + " files");
```

## Spring Boot Integration

### Configuration

Add migration configuration to your Spring Boot application:

```java
@Configuration
@EnableScheduling
public class MigrationConfig {
    
    @Bean
    public ActionHistoryMigrationService migrationService(
            ActionConfigAdapter adapter) {
        return new ActionHistoryMigrationService(adapter);
    }
    
    @Bean
    public MigrationStatusTracker statusTracker() {
        return new MigrationStatusTracker();
    }
    
    @Bean
    public DeprecationMetrics deprecationMetrics() {
        return DeprecationMetrics.getInstance();
    }
}
```

### Monitoring Dashboard

Create an endpoint to monitor migration progress:

```java
@RestController
@RequestMapping("/api/migration")
public class MigrationController {
    
    @Autowired
    private MigrationStatusTracker statusTracker;
    
    @GetMapping("/status")
    public String getStatus() {
        return statusTracker.generateStatusReport();
    }
    
    @GetMapping("/progress")
    public Map<String, Object> getProgress() {
        MigrationStatus status = statusTracker.getStatus();
        return Map.of(
            "progress", status.getProgressPercentage(),
            "phase", status.getCurrentPhase(),
            "migrated", status.getMigratedFiles(),
            "total", status.getTotalFiles()
        );
    }
}
```

## Troubleshooting

### Common Issues

#### 1. Import Resolution Errors

**Problem**: Cannot resolve symbol 'PatternFindOptions'

**Solution**: Add the correct import:
```java
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
```

#### 2. ClassCastException

**Problem**: ClassCastException when mixing ActionOptions and ActionConfig

**Solution**: Ensure consistent usage within a single operation:
```java
// Don't mix types in the same query
actionHistory.getRandomSnapshot(findConfig); // Use ActionConfig
// OR
actionHistory.getRandomSnapshot(ActionOptions.Action.FIND); // Use legacy (deprecated)
```

#### 3. Null ActionConfig

**Problem**: ActionRecord has null ActionConfig after migration

**Solution**: Check if the record had ActionOptions before migration:
```java
if (record.getActionOptions() != null && record.getActionConfig() == null) {
    // Record needs migration
    record = migrationService.migrateActionRecord(record);
}
```

### Getting Help

1. **Check Logs**: Enable DEBUG logging for migration packages
2. **Run Analysis**: Use the migration tool in analyze mode first
3. **File Issues**: Report problems at https://github.com/jspinak/brobot/issues
4. **Community Support**: Join the Brobot Discord server

## Best Practices

### 1. Migrate by Module

Migrate one module at a time to minimize risk:

```bash
# Migrate only the core module first
java -jar brobot-migration-tool.jar \
  --path /project \
  --include "core/**/*.java" \
  --mode migrate
```

### 2. Use the Adapter Pattern

Create an adapter during transition:

```java
public class ActionHistoryAdapter {
    private final ActionHistory history;
    private final ActionConfigAdapter adapter;
    
    public Optional<ActionRecord> findRecord(Object config) {
        if (config instanceof ActionConfig) {
            return history.getRandomSnapshot((ActionConfig) config);
        } else if (config instanceof ActionOptions.Action) {
            // Deprecated path
            return history.getRandomSnapshot((ActionOptions.Action) config);
        }
        throw new IllegalArgumentException("Unknown config type");
    }
}
```

### 3. Monitor Production

Track deprecated API usage in production:

```java
@Component
public class MigrationMonitor {
    
    @EventListener(ApplicationReadyEvent.class)
    public void startMonitoring() {
        // Schedule periodic reports
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            DeprecationMetrics metrics = DeprecationMetrics.getInstance();
            if (metrics.getTotalDeprecatedCalls().get() > 1000) {
                log.warn("High deprecated API usage detected: {}", 
                    metrics.getMostUsedMethod());
            }
        }, 0, 1, TimeUnit.HOURS);
    }
}
```

## Next Steps

1. **Run the analysis tool** to assess your migration scope
2. **Create a migration plan** based on the analysis
3. **Set up monitoring** to track progress
4. **Migrate incrementally** starting with non-critical modules
5. **Test thoroughly** after each migration phase
6. **Update documentation** to use modern examples

For more information, see the [ActionConfig API Reference](/docs/core-library/action-config/overview) and [Migration Guide](/docs/core-library/guides/migration-guide).