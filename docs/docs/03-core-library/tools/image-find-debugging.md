# Image Find Debugging System

## Overview

The Brobot Image Find Debugging System provides comprehensive debugging capabilities for troubleshooting pattern matching and image finding issues. It offers colorful console output, visual annotations, and detailed HTML/JSON reports to help developers understand why images are not being found.

## Features

### 1. **Colorful Console Output**
- Real-time feedback with ANSI colors
- Success/failure indicators with visual symbols (✅ ❌)
- Similarity scores and timing information
- Pattern dimensions and match counts

### 2. **Visual Debugging**
- Annotated screenshots showing search regions
- Match highlights with similarity scores
- Comparison grids showing pattern vs matched regions
- Failed region indicators

### 3. **Detailed Reports**
- HTML reports with interactive timeline
- JSON reports for programmatic analysis
- Session statistics and summaries
- Performance metrics

### 4. **File Saving**
- Screenshots of each find operation
- Pattern images for reference
- Visual comparison grids
- All outputs organized by session

## Configuration

### Enabling Debug Mode

Add these properties to your `application.properties`:

```properties
# Enable image find debugging
brobot.debug.image.enabled=true

# Set debug level (OFF, BASIC, DETAILED, VISUAL, FULL)
brobot.debug.image.level=DETAILED

# Enable visual debugging
brobot.debug.image.visual.enabled=true

# Enable colorful console output
brobot.debug.image.console.use-colors=true
```

### Using Debug Profile

For comprehensive debugging, use the provided debug profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=debug'
```

This enables:
- Colorful console output
- Visual annotations
- File saving
- Detailed logging
- Report generation

## Debug Levels

### OFF
No debugging output

### BASIC
- Success/failure status
- Basic timing information

### DETAILED
Everything from BASIC plus:
- Similarity scores
- Search parameters
- Match locations
- Failure reasons

### VISUAL
Everything from DETAILED plus:
- Visual annotations
- Screenshot saving
- Comparison grids

### FULL
Everything from VISUAL plus:
- Memory usage
- Performance metrics
- Complete operation traces

## Console Output Examples

### Successful Find
```
[10:23:45.123] FIND #1 LoginButton (120x40) ✅ FOUND [95.2%] ⏳ 234ms
  Search: threshold=0.80 region=Region(x=0, y=0, w=1920, h=1080)
  Matches:
    • Match at (850,450) score=0.952
```

### Failed Find
```
[10:23:46.456] FIND #2 SubmitButton (100x35) ❌ NOT FOUND best: 68.5% ⏳ 567ms
  Search: threshold=0.80
  - Best match (68.5%) below threshold (80.0%)
```

## Visual Output

### Annotated Screenshots
- Blue dashed lines: Search regions
- Green rectangles: Successful matches
- Yellow rectangle: Best match
- Red X: Failed searches

### Comparison Grids
Side-by-side comparison of:
- Original pattern
- Found matches
- Similarity scores

## Report Generation

### HTML Reports
Located at: `debug/image-finding/{session-id}/reports/report.html`

Contains:
- Session summary statistics
- Timeline of all operations
- Success rate metrics
- Interactive operation cards

### JSON Reports
Located at: `debug/image-finding/{session-id}/reports/report.json`

Structured data for:
- Automated analysis
- CI/CD integration
- Performance tracking

## Troubleshooting Common Issues

### Images Not Found

1. **Check similarity threshold**
   ```properties
   brobot.find.similarity=0.7  # Lower for more lenient matching
   ```

2. **Verify image format**
   - Use PNG format for best results
   - Avoid JPEG for UI elements
   - Ensure no scaling/compression

3. **Review debug output**
   - Check "best score" in console
   - Look at visual comparisons
   - Examine failure reasons

### Performance Issues

1. **Adjust debug level**
   ```properties
   brobot.debug.image.level=BASIC  # Reduce overhead
   ```

2. **Disable file saving**
   ```properties
   brobot.debug.image.save-screenshots=false
   brobot.debug.image.save-patterns=false
   ```

### Windows Snipping Tool Issues

When using Windows Snipping Tool (Win+Shift+S):
1. Save as PNG, not JPEG
2. Avoid resizing after capture
3. Check DPI scaling settings
4. Use debug mode to see actual similarity scores

## Integration with CI/CD

### GitHub Actions
```yaml
- name: Run tests with debug
  run: ./gradlew test --args='--spring.profiles.active=debug'
  
- name: Upload debug artifacts
  if: failure()
  uses: actions/upload-artifact@v2
  with:
    name: debug-reports
    path: debug/image-finding/**
```

### Jenkins
```groovy
stage('Test with Debug') {
    steps {
        sh './gradlew test -Dspring.profiles.active=debug'
    }
    post {
        failure {
            archiveArtifacts artifacts: 'debug/image-finding/**'
        }
    }
}
```

## API Usage

### Programmatic Access

```java
@Autowired
private ImageFindDebugger debugger;

// Initialize session
debugger.initializeSession();

// Debug operations are automatically intercepted
ActionResult result = action.find(stateImage);

// Finalize and generate reports
debugger.finalizeSession();
```

### Custom Debug Info

```java
ImageFindDebugger.FindDebugInfo debugInfo = 
    debugger.debugFindOperation(objectCollection, options, result);

System.out.println("Operation ID: " + debugInfo.getOperationId());
System.out.println("Best Score: " + debugInfo.getBestScore());
System.out.println("Duration: " + debugInfo.getSearchDuration() + "ms");
```

## Best Practices

1. **Development**: Use DETAILED or VISUAL level
2. **Testing**: Use BASIC level with file saving disabled
3. **Production**: Keep debugging OFF or use BASIC for critical operations
4. **CI/CD**: Enable on failure with artifact collection

## Performance Impact

| Level | Console Output | File I/O | Performance Impact |
|-------|---------------|----------|-------------------|
| OFF | None | None | 0% |
| BASIC | Minimal | None | ~2% |
| DETAILED | Moderate | None | ~5% |
| VISUAL | Heavy | Heavy | ~15-20% |
| FULL | Very Heavy | Very Heavy | ~25-30% |

## Support

For issues or questions:
- Check the debug reports first
- Review console output for clues
- Examine visual comparisons
- Adjust similarity thresholds
- Verify image quality and format