---
sidebar_position: 4
---

# ActionResult Architecture

## Table of Contents

- [Overview](#overview)
- [Architecture Evolution](#architecture-evolution)
- [Core Components](#core-components)
  - [1. MatchCollection](#1-matchcollection)
  - [2. TimingData](#2-timingdata)
  - [3. TextExtractionResult](#3-textextractionresult)
  - [4. StateTracker](#4-statetracker)
  - [5. RegionManager](#5-regionmanager)
  - [6. MovementTracker](#6-movementtracker)
  - [7. ActionAnalysis](#7-actionanalysis)
  - [8. ExecutionHistory](#8-executionhistory)
  - [9. ActionMetrics](#9-actionmetrics)
- [Support Components](#support-components)
- [Design Patterns](#design-patterns)
- [Integration Points](#integration-points)
- [Performance Characteristics](#performance-characteristics)
- [Extension Points](#extension-points)
- [Migration Guide](#migration-guide)
- [Testing Strategy](#testing-strategy)
- [Security Considerations](#security-considerations)
- [Future Enhancements](#future-enhancements)
- [Best Practices](#best-practices)
- [Component Reference](#component-reference)

## Overview

ActionResult serves as the universal return type for all actions in the Brobot framework, encapsulating comprehensive information generated during action execution. Version 2.0 introduces a component-based architecture that transforms the original monolithic design into a well-structured system of focused, single-responsibility components.

**Related Documentation**: [ActionResult Components Quick Reference](../action-config/17-actionresult-components.md) | [Upgrading to Latest](../migration/upgrading-to-latest.md) | [ActionConfig Overview](../action-config/01-overview.md)

## Architecture Evolution

### Legacy Architecture
The original ActionResult was a monolithic design handling 30+ responsibilities:
- Match collection management
- Text extraction and aggregation
- Timing and duration tracking
- Scene analysis data
- Region definitions
- Movement tracking
- Execution history
- State management
- Logging and metrics
- Sorting and filtering operations
- Statistical calculations
- Serialization concerns

### Component-Based Architecture (v2.0)
The refactored architecture delegates responsibilities to specialized components while maintaining complete backward compatibility through a facade pattern.

```
┌─────────────────────────────────────────────────────────────────┐
│                         ActionResult                             │
│                          (Facade)                                │
├─────────────────────────────────────────────────────────────────┤
│  Core Fields:                                                    │
│  - actionDescription: String                                     │
│  - success: boolean                                              │
│  - actionConfig: ActionConfig (see ActionConfig Overview)       │
│  - outputText: String                                           │
├─────────────────────────────────────────────────────────────────┤
│  Component Delegates (9 specialized components):                │
│  ┌──────────────────┐  ┌──────────────────┐                    │
│  │ MatchCollection   │  │ TimingData       │                    │
│  │ - matches         │  │ - startTime      │                    │
│  │ - sorting         │  │ - endTime        │                    │
│  │ - filtering       │  │ - duration       │                    │
│  └──────────────────┘  └──────────────────┘                    │
│  ┌──────────────────┐  ┌──────────────────┐                    │
│  │TextExtractionResult│ │ StateTracker     │                    │
│  │ - accumulated     │  │ - activeStates   │                    │
│  │ - selected        │  │ - stateMatches   │                    │
│  │ - matchText       │  │ - activations    │                    │
│  └──────────────────┘  └──────────────────┘                    │
│  ┌──────────────────┐  ┌──────────────────┐                    │
│  │ RegionManager     │  │ MovementTracker  │                    │
│  │ - definedRegions  │  │ - movements      │                    │
│  │ - namedRegions    │  │ - distances      │                    │
│  │ - unions          │  │ - paths          │                    │
│  └──────────────────┘  └──────────────────┘                    │
│  ┌──────────────────┐  ┌──────────────────┐                    │
│  │ ActionAnalysis    │  │ExecutionHistory  │                    │
│  │ - sceneAnalyses   │  │ - records        │                    │
│  │ - masks           │  │ - lifecycle      │                    │
│  │ - customAnalysis  │  │ - timeline       │                    │
│  └──────────────────┘  └──────────────────┘                    │
│  ┌──────────────────┐                                           │
│  │ ActionMetrics     │  Performance & efficiency tracking       │
│  │ - executionTimes  │                                           │
│  │ - phaseMetrics    │                                           │
│  │ - efficiencyScore │                                           │
│  └──────────────────┘                                           │
└─────────────────────────────────────────────────────────────────┘
```

## Core Components

> **Note**: Code examples in this document show class structures and method signatures for architectural understanding. For complete implementations, see the `io.github.jspinak.brobot.action.result` package in the Brobot source code.

### 1. MatchCollection
**Package**: `io.github.jspinak.brobot.action.result`
**Responsibility**: Manages all match-related operations

```java
public class MatchCollection {
    private List<Match> matches;           // Current matches after filtering/sorting
    private List<Match> initialMatches;    // Original unmodified matches for history
    private int maxMatches;                 // Limit to prevent memory issues

    // Key operations
    public void add(Match... matches);     // Add matches with automatic max enforcement
    public void sort(SortStrategy strategy); // Sort by score, size, or distance
    public Optional<Match> getBest();      // Get highest-scoring match
    public MatchCollection filter(Predicate<Match> predicate); // Filter with custom logic
    public MatchStatistics getStatistics(); // Delegate to statistics component
}
```

**Features**:
- Multiple sorting strategies (score, size, distance)
- Flexible filtering with predicates
- Statistical analysis delegation
- Set operations (minus, intersection)
- Automatic max match enforcement

### 2. TimingData
**Package**: `io.github.jspinak.brobot.action.result`
**Responsibility**: Handles all timing and duration tracking

```java
public class TimingData {
    private LocalDateTime startTime;         // Timestamp when action started
    private LocalDateTime endTime;           // Timestamp when action completed
    private Duration totalDuration;          // Calculated total duration
    private List<TimeSegment> segments;      // Phase-level timing breakdown

    // Key operations
    public void start();                     // Mark action start, record timestamp
    public void stop();                      // Mark action end, calculate duration
    public Duration getElapsed();            // Get time elapsed since start
    public void addSegment(String name, Duration duration); // Track phase timing
}
```

**Features**:
- Automatic timing calculation
- Phase timing support
- Overhead calculation
- Human-readable formatting

### 3. TextExtractionResult
**Package**: `io.github.jspinak.brobot.action.result`
**Responsibility**: Manages text extraction and OCR results

```java
public class TextExtractionResult {
    private Text accumulatedText;
    private String selectedText;
    private Map<Match, String> matchTextMap;
    
    // Key operations
    public void addText(String text);
    public void addMatchText(Match match, String text);
    public String getCombinedText();
}
```

**Features**:
- Accumulated text tracking
- Match-specific text mapping
- Selected text management
- Multiple text source merging

### 4. StateTracker
**Package**: `io.github.jspinak.brobot.action.result`
**Responsibility**: Tracks state information during execution

```java
public class StateTracker {
    private Set<String> activeStates;                           // Currently active states
    private Map<String, List<Match>> stateMatches;              // Matches grouped by state
    private Map<String, Integer> stateActivationCounts;         // Frequency tracking

    // Key operations
    public void recordActiveState(String stateName);            // Mark state as active
    public void recordStateMatch(String stateName, Match match); // Associate match with state
    public Optional<String> getMostActiveState();               // Get most frequent state
}
```

**Features**:
- Active state detection
- State-match associations
- Activation frequency tracking
- State activity analysis

### 5. RegionManager
**Package**: `io.github.jspinak.brobot.action.result`
**Responsibility**: Manages region definitions and operations

```java
public class RegionManager {
    private List<Region> definedRegions;
    private Map<String, Region> namedRegions;
    
    // Key operations
    public void defineRegion(Region region);
    public void defineNamedRegion(String name, Region region);
    public Optional<Region> getUnion();
    public Optional<Region> getIntersection();
}
```

**Features**:
- Anonymous and named regions
- Union/intersection calculations
- Area-based sorting
- Primary region access

### 6. MovementTracker
**Package**: `io.github.jspinak.brobot.action.result`
**Responsibility**: Tracks drag and movement operations

```java
public class MovementTracker {
    private List<Movement> movements;
    
    // Key operations
    public void recordMovement(Movement movement);
    public double getTotalDistance();
    public boolean isClosedPath(double tolerance);
    public Optional<Region> getBoundingBox();
}
```

**Features**:
- Movement sequence tracking
- Distance calculations
- Path analysis (closed/open)
- Bounding box computation

### 7. ActionAnalysis
**Package**: `io.github.jspinak.brobot.action.result`
**Responsibility**: Manages analysis data and results

```java
public class ActionAnalysis {
    private SceneAnalyses sceneAnalyses;
    private Mat mask;
    private Map<String, Object> customAnalysis;
    
    // Key operations
    public void addSceneAnalysis(SceneAnalysis analysis);
    public void addCustomAnalysis(String key, Object data);
    public <T> Optional<T> getCustomAnalysis(String key, Class<T> type);
}
```

**Features**:
- Scene analysis aggregation
- Binary mask management
- Custom analysis storage
- Type-safe retrieval

### 8. ExecutionHistory
**Package**: `io.github.jspinak.brobot.action.result`
**Responsibility**: Tracks action execution history

```java
public class ExecutionHistory {
    private List<ActionRecord> records;
    private ActionLifecycle lifecycle;

    // Key operations
    public void recordStep(ActionRecord record);
    public List<ActionRecord> getSuccessfulSteps();
    public double getSuccessRate();
    public String formatTimeline();
}
```

**Features**:
- Step-by-step recording
- Success/failure analysis
- Timeline visualization
- Duration tracking

### 9. ActionMetrics
**Package**: `io.github.jspinak.brobot.action.result`
**Responsibility**: Tracks performance metrics and efficiency scores

```java
public class ActionMetrics {
    private List<Duration> executionTimes;
    private Map<String, Duration> phaseMetrics;

    // Key operations
    public void recordExecutionTime(Duration duration);
    public void recordPhase(String phaseName, Duration duration);
    public double getEfficiencyScore();
    public String formatPerformance();
}
```

**Features**:
- Execution time tracking
- Phase-level performance metrics
- Efficiency score calculation
- Performance summary formatting

## Support Components

### MatchStatistics
Provides statistical analysis of match collections:
- Median region/location calculation
- Score distribution analysis
- Confidence level determination
- Density calculations
- Bounding box computation

### MatchFilter
Static utility for filtering operations:
- Filter by state object/owner
- Score-based filtering
- Area-based filtering
- Distance-based filtering
- Duplicate removal

### ActionResultBuilder
Fluent builder for constructing ActionResult instances:

```java
ActionResult result = new ActionResultBuilder()
    .withSuccess(true)
    .withDescription("Found button")
    .withMatches(matchList)
    .withTiming(startTime, endTime)
    .withActiveState("MainMenu")
    .build();
```

## Design Patterns

### 1. Facade Pattern
ActionResult acts as a facade, providing a simplified interface to the complex subsystem of components while maintaining backward compatibility.

### 2. Delegation Pattern
All operations are delegated to specialized components, keeping ActionResult focused on coordination rather than implementation.

### 3. Builder Pattern
ActionResultBuilder provides flexible construction with optional parameters and method chaining.

### 4. Strategy Pattern
MatchCollection uses strategy pattern for sorting operations with configurable strategies.

### 5. Repository Pattern
Components like RegionManager and StateTracker act as repositories for their respective domain objects.

## Integration Points

For complete information on Action classes, see the [Action Hierarchy](../../01-getting-started/action-hierarchy.md) documentation.

### Real-World Example: Login Flow Automation

Here's a complete example showing how ActionResult components work together in a real automation scenario:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Demonstrates ActionResult component usage in a login automation workflow.
 * Shows how components track matches, timing, state, and provide detailed results.
 */
@Component
public class LoginAutomation {

    @Autowired
    private Action action;

    public ActionResult performLogin(String username, String password) {
        // Use builder for clean construction
        ActionResult result = new ActionResultBuilder()
            .withDescription("Login workflow execution")
            .build();

        // Step 1: Find and click username field
        PatternFindOptions findOptions = PatternFindOptions.forPreciseSearch();
        ActionResult usernameResult = action.find(findOptions, getUsernameField());

        if (usernameResult.isSuccess()) {
            // Components automatically tracked:
            // - MatchCollection: Stores found username field match
            // - TimingData: Records find operation duration
            // - StateTracker: Records "LoginScreen" as active state

            result.add(usernameResult.getMatchList());
            action.type(username);

            // Step 2: Find and fill password field
            ActionResult passwordResult = action.find(findOptions, getPasswordField());

            if (passwordResult.isSuccess()) {
                result.add(passwordResult.getMatchList());
                action.type(password);

                // Step 3: Click login button
                ClickOptions clickOptions = new ClickOptions.Builder()
                    .setNumberOfClicks(1)
                    .build();

                ActionResult loginResult = action.click(clickOptions, getLoginButton());

                // Aggregate results
                result.add(loginResult.getMatchList());
                result.setSuccess(true);

                // Access component data for analysis
                System.out.println("Total matches found: " + result.getMatchCount());
                System.out.println("Execution time: " + result.getExecutionTimeMs() + "ms");
                System.out.println("Active states: " + result.getActiveStates());

                // ExecutionHistory tracks all steps
                // TimingData provides breakdown by operation
                // StateTracker confirms successful state transitions
            } else {
                result.setSuccess(false);
                System.err.println("Password field not found");
            }
        } else {
            result.setSuccess(false);
            System.err.println("Username field not found");
        }

        return result;
    }

    private ObjectCollection getUsernameField() { /* ... */ return null; }
    private ObjectCollection getPasswordField() { /* ... */ return null; }
    private ObjectCollection getLoginButton() { /* ... */ return null; }
}
```

**What This Example Demonstrates**:
- **ActionResultBuilder**: Clean construction with fluent API
- **MatchCollection**: Aggregates matches from multiple find operations
- **TimingData**: Automatically tracks total execution time
- **StateTracker**: Monitors state transitions (LoginScreen → LoggedIn)
- **ExecutionHistory**: Records each step for debugging
- **Component Facade**: Simple API hides component complexity

### With Action Classes
```java
public class Click {
    public ActionResult execute(ObjectCollection objects) {
        ActionResult result = new ActionResult(clickOptions);
        // Populate result using components
        result.add(foundMatch);
        result.setSuccess(true);
        return result;
    }
}
```

### With Logging System
```java
// Components provide formatted output
String matchSummary = result.getMatchCollection().getStatistics().format();
String timingSummary = result.getTimingData().format();
String stateSummary = result.getStateTracker().format();
```

### With Testing Framework

For comprehensive testing guidance, see [Testing Overview](../../04-testing/testing-intro.md) and [Testing Strategy](../../04-testing/testing-strategy.md).

```java
// Fine-grained assertions on components
assertThat(result.getMatchCount()).isEqualTo(3);
assertThat(result.getExecutionTimeMs()).isLessThan(1000);
assertThat(result.getActiveStates()).contains("LoginScreen");
```

## Performance Characteristics

### Memory Efficiency
- **Eager Initialization**: Components initialized at construction for consistent state
- **Shared References**: No unnecessary copying
- **Bounded Collections**: MaxMatches enforcement

### Computational Efficiency
- **O(1) Access**: Direct component access
- **O(n log n) Sorting**: Efficient match sorting
- **O(n) Filtering**: Linear filtering operations
- **Cached Statistics**: Statistics calculated once

### Garbage Collection
- **Reduced Object Graph**: Smaller, focused objects
- **Clear Ownership**: Components own their data
- **Explicit Cleanup**: Clear() methods on components

## Extension Points

### Custom Analysis Types
```java
// Add custom analysis data
result.getActionAnalysis().addCustomAnalysis("colorProfile", colorData);

// Retrieve with type safety
Optional<ColorProfile> profile = result.getActionAnalysis()
    .getCustomAnalysis("colorProfile", ColorProfile.class);
```

### Custom Sorting Strategies
```java
// Extend MatchCollection.SortStrategy
matchCollection.sort(SortStrategy.CUSTOM_RELEVANCE);
```

### Custom Statistics
```java
// Extend MatchStatistics for domain-specific metrics
public class GameMatchStatistics extends MatchStatistics {
    public double getAverageHealthBarFullness() { ... }
}
```

## Migration Guide

For complete migration details and examples, see the [Upgrading to Latest](../migration/upgrading-to-latest.md) guide.

### For API Consumers
**No changes required!** The facade maintains complete backward compatibility:

```java
// Old code continues to work
ActionResult result = action.execute();
result.add(match);
result.sortMatchObjects();
List<Match> matches = result.getMatchList();
```

### For Action Implementers
Use the builder for cleaner construction:

```java
// Old way
ActionResult result = new ActionResult();
result.setSuccess(true);
result.add(matches);
result.setDuration(duration);

// New way (optional)
ActionResult result = ActionResultBuilder.successWith(matches)
    .withTiming(startTime, endTime)
    .build();
```

### For Framework Extenders
Components are accessed through the facade's public API methods:

```java
// Access component data through facade methods
List<Match> matches = result.getMatchList();
long executionTime = result.getExecutionTimeMs();
Set<String> activeStates = result.getActiveStates();
MatchStatistics stats = result.getMatchStatistics();
double confidence = stats.getConfidence();
```

## Testing Strategy

### Component Unit Testing
Each component has focused unit tests:

```java
@Test
void testMatchCollectionSorting() {
    MatchCollection collection = new MatchCollection();
    collection.add(match1, match2, match3);
    collection.sortByScoreDescending();
    
    assertThat(collection.getBest()).contains(match3);
}
```

### Integration Testing
Test component interactions:

```java
@Test
void testActionResultDelegation() {
    ActionResult result = new ActionResult();
    result.add(match);
    
    // Verify delegation
    assertThat(result.getMatchCollection().contains(match)).isTrue();
    assertThat(result.getStateTracker().getActiveStates()).contains(stateName);
}
```

### Performance Testing
Verify performance characteristics:

```java
@Benchmark
public void benchmarkMatchSorting() {
    result.sortMatchObjectsDescending();
}
```

## Security Considerations

### Input Validation
- Components validate input parameters
- Null-safe operations throughout
- Bounded collection sizes

### Data Isolation
- Components encapsulate their data
- No shared mutable state
- Defensive copying where needed

### Serialization Safety
- @JsonIgnore on non-serializable fields
- Clean separation of transient data
- Controlled exposure of internal state

## Future Enhancements

### Planned Improvements
1. **Async Support**: Concurrent component operations
2. **Streaming API**: Process large result sets efficiently
3. **Event System**: Observable result changes
4. **Metrics Dashboard**: Real-time performance visualization
5. **Plugin Architecture**: Dynamic component registration

### Potential Optimizations
1. **Object Pooling**: Reuse component instances
2. **Lazy Loading**: Defer expensive calculations
3. **Caching Layer**: Cache computed statistics
4. **Compression**: Compress large match collections

## Best Practices

### Do's
- ✅ Use components for focused operations
- ✅ Leverage builder for complex construction
- ✅ Access statistics for analysis
- ✅ Clear components when done
- ✅ Use appropriate sorting strategies

### Don'ts
- ❌ Don't access component internals directly
- ❌ Don't modify collections while iterating
- ❌ Don't ignore null checks
- ❌ Don't bypass the facade for basic operations
- ❌ Don't store references to internal collections

## Component Reference

| Component | Lines | Responsibility | Key Methods |
|-----------|-------|---------------|-------------|
| MatchCollection | 369 | Match management | add(), sort(), filter(), getBest() |
| TimingData | 231 | Timing tracking | start(), stop(), getElapsed() |
| MatchStatistics | 294 | Statistical analysis | getMedian(), getConfidence() |
| TextExtractionResult | 219 | Text management | addText(), getCombinedText() |
| StateTracker | 230 | State tracking | recordActiveState(), getMostActiveState() |
| RegionManager | 266 | Region management | defineRegion(), getUnion() |
| MovementTracker | 265 | Movement tracking | recordMovement(), getTotalDistance() |
| ActionAnalysis | 230 | Analysis data | addSceneAnalysis(), addCustomAnalysis() |
| ActionMetrics | 302 | Performance metrics | recordExecutionTime(), getEfficiencyScore() |
| ExecutionHistory | 291 | Execution tracking | recordStep(), getSuccessRate() |
| MatchFilter | 332 | Filtering utilities | byMinScore(), nearLocation() |
| ActionResultBuilder | 424 | Result construction | withMatches(), build() |

## Conclusion

The refactored ActionResult architecture transforms a monolithic class into a well-structured system of focused components. This design provides:

- **Better Maintainability**: Each component has a single, clear responsibility
- **Enhanced Testability**: Smaller units are easier to test in isolation
- **Improved Extensibility**: New features can be added to specific components
- **Backward Compatibility**: Existing code continues to work unchanged
- **Performance Benefits**: Lazy initialization and focused operations

The component-based architecture positions ActionResult for future growth while maintaining the stability and reliability expected by existing consumers.