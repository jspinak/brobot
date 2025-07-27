# Visual Feedback Example

This example demonstrates Brobot's visual feedback and illustration capabilities, showing how to monitor action execution, analyze results, and generate visual documentation of automation flows.

## Key Features

### 1. Real-time Action Visualization
Monitor action execution with visual feedback:
```java
ActionConfig searchWithVisuals = find(searchBox)
    .withIllustration(true)
    .withVisualizationPath("screenshots/search-flow")
    .withBeforeActionLog("Searching with visual tracking...")
    .then(click())
    .then(type("search query"))
    .build();
```

### 2. Result Analysis and Reporting
Analyze action results with detailed visual feedback:
```
[ACTION] Looking for search box...
[VISUAL] Screenshot saved: screenshots/search-flow/01-before-search.png
[ACTION] Found search box at (450, 120) with 92% confidence
[VISUAL] Match highlighted: screenshots/search-flow/02-match-found.png
[ACTION] Successfully clicked search box
[VISUAL] After-click state: screenshots/search-flow/03-after-click.png
```

### 3. Illustration Generation
Automatically generate step-by-step visual documentation:
- Before/after screenshots for each action
- Match highlighting with confidence scores
- Annotated images showing action locations
- Timeline visualization of automation flow

### 4. Performance Analytics
Track and visualize performance metrics:
- Action execution times
- Match confidence distributions
- Success/failure patterns
- Resource usage over time

## Running the Example

1. Configure illustration settings in `application.yml`
2. Place test images in `src/main/resources/images/`
3. Run: `./gradlew bootRun`
4. Check `build/illustrations/` for generated visual feedback

## Project Structure

```
visual-feedback/
├── src/main/java/com/example/visual/
│   ├── VisualFeedbackApplication.java    # Main application
│   ├── demos/
│   │   ├── IllustrationDemo.java         # Basic illustration examples
│   │   ├── AnalysisDemo.java            # Result analysis examples
│   │   ├── PerformanceDemo.java         # Performance monitoring
│   │   └── ReportingDemo.java           # Report generation
│   ├── analyzers/
│   │   ├── ActionAnalyzer.java          # Custom analysis logic
│   │   └── PerformanceTracker.java      # Performance monitoring
│   └── reporters/
│       ├── VisualReporter.java          # Visual report generation
│       └── AnalyticsReporter.java       # Analytics reporting
└── src/main/resources/
    ├── application.yml                   # Configuration
    ├── images/                          # Test UI elements
    └── templates/                       # Report templates
```

## Visual Feedback Examples

### 1. Search Flow with Illustrations
```java
ActionConfig illustratedSearch = find(searchIcon)
    .withIllustration(true)
    .withBeforeActionLog("Starting illustrated search...")
    .withSuccessLog("Search icon found - confidence: {confidence}%")
    .then(click())
    .withSuccessLog("Search activated")
    .then(type("example query"))
    .withSuccessLog("Query entered")
    .then(find(searchButton))
    .withSuccessLog("Search button located")
    .then(click())
    .withSuccessLog("Search executed")
    .withAfterActionLog("Search flow completed in {duration}ms")
    .build();
```

**Generated Output:**
```
screenshots/search-flow/
├── 01-initial-state.png          # Before starting
├── 02-search-icon-found.png      # Icon highlighted
├── 03-after-icon-click.png       # Search box activated
├── 04-query-entered.png          # Text typed
├── 05-search-button-found.png    # Button highlighted
├── 06-search-executed.png        # Final state
└── flow-summary.html             # Interactive timeline
```

### 2. Form Validation with Analysis
```java
ActionConfig validatedForm = find(nameField)
    .withIllustration(true)
    .withAnalysis(true)
    .withBeforeActionLog("Validating form field...")
    .then(click())
    .then(type("John Doe"))
    .then(find(emailField))
    .withSuccessLog("Email field found - position: ({x}, {y})")
    .then(click())
    .then(type("john@example.com"))
    .withValidation(email -> email.contains("@"))
    .then(find(submitButton))
    .then(click())
    .withSuccessLog("Form submitted successfully")
    .build();
```

### 3. Performance Monitoring
```java
ActionConfig monitoredActions = find(menuButton)
    .withPerformanceTracking(true)
    .withBeforeActionLog("Starting performance monitoring...")
    .then(click())
    .withMetric("menu_open_time")
    .then(find(settingsOption))
    .withMetric("settings_find_time")
    .then(hover())
    .withMetric("submenu_response_time")
    .then(find(profileOption))
    .then(click())
    .withAfterActionLog("Navigation completed - metrics recorded")
    .build();
```

## Configuration Options

### Illustration Settings
```yaml
brobot:
  illustration:
    enabled: true
    auto-open: true
    save-path: "build/illustrations"
    format: "PNG"
    quality: 95
    annotations:
      enabled: true
      highlight-matches: true
      show-confidence: true
      show-coordinates: true
```

### Analysis Settings
```yaml
brobot:
  analysis:
    enabled: true
    capture-screenshots: true
    track-performance: true
    generate-reports: true
    report-format: ["HTML", "JSON"]
```

## Custom Analysis

### Creating Custom Analyzers
```java
@Component
public class CustomActionAnalyzer implements ActionAnalyzer {
    
    @Override
    public AnalysisResult analyze(ActionResult result, BufferedImage screenshot) {
        // Custom analysis logic
        return AnalysisResult.builder()
            .confidence(result.getBestMatch().getScore())
            .executionTime(result.getDuration())
            .coordinates(result.getBestMatch().getMatch())
            .customMetrics(calculateCustomMetrics(screenshot))
            .build();
    }
}
```

### Performance Tracking
```java
@Component
public class PerformanceTracker {
    
    public void trackAction(ActionConfig config, ActionResult result) {
        MetricCollector.record("action.duration", result.getDuration());
        MetricCollector.record("action.confidence", result.getBestMatch().getScore());
        MetricCollector.increment("action.count");
        
        if (!result.isSuccess()) {
            MetricCollector.increment("action.failures");
        }
    }
}
```

## Generated Reports

### HTML Timeline Report
Interactive timeline showing:
- Action sequence with timestamps
- Screenshots at each step
- Performance metrics
- Success/failure indicators
- Zoom and filter capabilities

### JSON Analytics Report
```json
{
  "session": {
    "id": "session-123",
    "duration": 15420,
    "totalActions": 24,
    "successRate": 95.8
  },
  "actions": [
    {
      "type": "FIND",
      "duration": 342,
      "confidence": 0.92,
      "success": true,
      "screenshot": "01-search-icon.png"
    }
  ],
  "performance": {
    "averageActionTime": 641,
    "averageConfidence": 0.89,
    "peakMemoryUsage": "145MB"
  }
}
```

## Use Cases

1. **Debugging Automation** - Visual feedback helps identify why actions fail
2. **Documentation** - Generate step-by-step guides automatically
3. **Performance Optimization** - Identify slow actions and optimize
4. **Quality Assurance** - Validate automation reliability with metrics
5. **Training** - Create visual demonstrations of automation flows