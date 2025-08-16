# ActionResult Class Refactoring Plan

## Current Issues (1113 lines, 30+ responsibilities)
The ActionResult class violates the Single Responsibility Principle by handling:
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

## Proposed Architecture

### 1. Core Result Classes (Single Responsibility)

#### ActionResult (Simplified Core - ~150 lines)
```java
public class ActionResult {
    private String actionDescription;
    private boolean success;
    private ActionConfig actionConfig;
    private MatchCollection matches;
    private TimingData timing;
    private String actionId;
    
    // Delegated operations
    public MatchCollection getMatches() { return matches; }
    public TimingData getTiming() { return timing; }
    // Core success/failure methods only
}
```

#### MatchCollection (~200 lines)
```java
public class MatchCollection {
    private List<Match> matches;
    private List<Match> initialMatches;
    private int maxMatches;
    
    // All match-related operations
    public void add(Match... matches);
    public void sort(SortStrategy strategy);
    public Optional<Match> getBest();
    public List<Match> filter(MatchPredicate predicate);
    public MatchStatistics getStatistics();
}
```

#### TimingData (~100 lines)
```java
public class TimingData {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;
    private List<TimeSegment> segments;
    
    public void start();
    public void stop();
    public Duration getElapsed();
    public void addSegment(String name, Duration duration);
}
```

### 2. Specialized Result Components

#### TextExtractionResult (~80 lines)
```java
public class TextExtractionResult {
    private Text accumulatedText;
    private String selectedText;
    private Map<Match, String> matchTextMap;
    
    public void addText(String text);
    public void addMatchText(Match match, String text);
    public String getCombinedText();
}
```

#### StateTracker (~80 lines)
```java
public class StateTracker {
    private Set<String> activeStates;
    private Map<String, List<Match>> stateMatches;
    
    public void recordActiveState(String state);
    public void recordStateMatch(String state, Match match);
    public Set<String> getActiveStates();
}
```

#### RegionManager (~80 lines)
```java
public class RegionManager {
    private List<Region> definedRegions;
    private Map<String, Region> namedRegions;
    
    public void defineRegion(Region region);
    public void defineNamedRegion(String name, Region region);
    public Optional<Region> getRegion(String name);
}
```

#### MovementTracker (~60 lines)
```java
public class MovementTracker {
    private List<Movement> movements;
    
    public void recordMovement(Movement movement);
    public Optional<Movement> getFirstMovement();
    public List<Movement> getMovementSequence();
}
```

### 3. Analysis and Metrics

#### ActionAnalysis (~100 lines)
```java
public class ActionAnalysis {
    private SceneAnalyses sceneAnalyses;
    private Mat mask;
    private Map<String, Object> customAnalysis;
    
    public void addSceneAnalysis(SceneAnalysis analysis);
    public void setMask(Mat mask);
    public void addCustomAnalysis(String key, Object data);
}
```

#### ActionMetrics (~80 lines)
```java
public class ActionMetrics {
    private long executionTimeMs;
    private int matchCount;
    private double bestMatchConfidence;
    private int retryCount;
    private long retryTimeMs;
    
    // Metric calculation methods
}
```

#### ExecutionHistory (~60 lines)
```java
public class ExecutionHistory {
    private List<ActionRecord> records;
    private ActionLifecycle lifecycle;
    
    public void recordStep(ActionRecord record);
    public List<ActionRecord> getHistory();
}
```

### 4. Support Classes

#### MatchSorter (~60 lines)
```java
public class MatchSorter {
    public enum SortStrategy {
        SCORE_ASCENDING, SCORE_DESCENDING,
        SIZE_ASCENDING, SIZE_DESCENDING,
        DISTANCE_FROM_LOCATION
    }
    
    public static void sort(List<Match> matches, SortStrategy strategy);
    public static void sort(List<Match> matches, Comparator<Match> comparator);
}
```

#### MatchStatistics (~80 lines)
```java
public class MatchStatistics {
    public Optional<Region> getMedianRegion(List<Match> matches);
    public Optional<Location> getMedianLocation(List<Match> matches);
    public Optional<Match> getClosestTo(List<Match> matches, Location location);
    public double getAverageScore(List<Match> matches);
}
```

#### MatchFilter (~80 lines)
```java
public class MatchFilter {
    public static List<Match> byStateObject(List<Match> matches, String objectId);
    public static List<Match> byOwnerState(List<Match> matches, String stateName);
    public static List<Match> byMinScore(List<Match> matches, double minScore);
    public static List<Match> containing(List<Match> matches, Match target);
}
```

### 5. Builder Pattern for Complex Results

#### ActionResultBuilder (~100 lines)
```java
public class ActionResultBuilder {
    public ActionResultBuilder withMatches(MatchCollection matches);
    public ActionResultBuilder withTiming(TimingData timing);
    public ActionResultBuilder withText(TextExtractionResult text);
    public ActionResultBuilder withStates(StateTracker states);
    public ActionResultBuilder withAnalysis(ActionAnalysis analysis);
    public ActionResult build();
}
```

## Migration Strategy

### Phase 1: Extract Without Breaking API (2-3 weeks)
1. Create new component classes
2. Implement delegation in ActionResult
3. Add @Deprecated annotations to methods moving elsewhere
4. Maintain backward compatibility

### Phase 2: Update Internal Usage (2 weeks)
1. Update Action classes to use new components
2. Modify ActionLifecycle to work with components
3. Update logging to use new structure

### Phase 3: Update Tests (1 week)
1. Create tests for new components
2. Migrate existing ActionResult tests
3. Add integration tests

### Phase 4: Update Client Code (2-3 weeks)
1. Update examples to use new API
2. Update documentation
3. Create migration guide

### Phase 5: Remove Deprecated Code (1 week)
1. Remove deprecated methods
2. Clean up ActionResult to final form
3. Final testing and validation

## Benefits of Refactoring

### Maintainability
- Each class has single, clear responsibility
- Easier to understand and modify
- Reduced coupling between components

### Testability
- Smaller classes easier to unit test
- Mock individual components
- Better test coverage

### Performance
- Lazy initialization of unused components
- Reduced memory footprint for simple actions
- Better garbage collection

### Extensibility
- Easy to add new result types
- Plugin architecture for custom analysis
- Strategy patterns for sorting/filtering

## Risk Mitigation

### Backward Compatibility
- Keep ActionResult as facade initially
- Provide adapter methods
- Gradual deprecation cycle

### Testing Strategy
- Parallel test suites during migration
- Performance benchmarks before/after
- Integration test coverage

### Documentation
- Comprehensive migration guide
- Updated Javadocs
- Example code updates

## Implementation Order

1. **Week 1-2**: Core classes (MatchCollection, TimingData)
2. **Week 3**: Specialized components (TextExtractionResult, StateTracker)
3. **Week 4**: Analysis and metrics classes
4. **Week 5**: Support classes (sorters, filters, statistics)
5. **Week 6**: Builder and factory patterns
6. **Week 7-8**: Migration and testing
7. **Week 9**: Documentation and cleanup

## Success Metrics

- Reduce ActionResult to < 200 lines
- Each component class < 200 lines
- Maintain 100% backward compatibility initially
- Improve test coverage to > 90%
- Reduce coupling (measured by dependency analysis)
- Performance neutral or better

## Alternative Considerations

### Event-Driven Architecture
Instead of data accumulation, use event bus for result reporting.
- Pros: Decoupled, reactive
- Cons: More complex, debugging harder

### Functional Approach
Use immutable data structures and functional transformations.
- Pros: Thread-safe, predictable
- Cons: Java verbosity, learning curve

### Microkernel Pattern
Plugin-based architecture for result handlers.
- Pros: Highly extensible
- Cons: Over-engineering for current needs

## Conclusion

This refactoring will transform ActionResult from a 1113-line monolith into a well-structured system of focused components. The gradual migration approach ensures system stability while improving code quality, maintainability, and extensibility.