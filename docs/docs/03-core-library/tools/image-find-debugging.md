# Image Find Debugging System

Comprehensive debugging system for troubleshooting image pattern matching issues in Brobot applications.

## Overview

The Image Find Debugging System provides detailed insights into why patterns may not be found during automation. It uses Spring AOP to intercept find operations and provides:

- **Colorful console output** with match details (Windows support via Jansi)
- **Visual annotations** on screenshots showing search regions and matches
- **Comparison grids** showing pattern vs found regions
- **Session-based file organization** for easy debugging
- **Performance metrics** and similarity scores
- **HTML/JSON reports** for analysis

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

## Quick Start

### Enable Debugging

Add to your run command:
```bash
# Windows/Linux live automation with debugging
./gradlew bootRun --args='--spring.profiles.active=debug'
```

### Debug Output Location

Debug files are saved to:
```
debug/image-finding/
├── session-20250913-102030/
│   ├── 001-LoginButton/
│   │   ├── screenshot.png
│   │   ├── pattern.png
│   │   ├── annotated.png
│   │   └── comparison-grid.png
│   ├── 002-SubmitButton/
│   │   └── ... similar files ...
│   └── session-summary.json
```

## Configuration

### Profile-Based Configuration

Brobot uses Spring profiles for clean configuration:

| Profile | Purpose | Properties File |
|---------|---------|----------------|
| (none) | Live automation | `application.properties` |
| `debug` | Live with debugging | `application-debug.properties` |
| `mock` | Testing without GUI | `application-mock.properties` |
| `mock,debug` | Mock with debugging | Both profiles combined |

### Key Properties

```properties
# Master switch
brobot.debug.image.enabled=true

# Debug level (OFF, BASIC, DETAILED, VISUAL, FULL)
brobot.debug.image.level=DETAILED

# File saving
brobot.debug.image.save-screenshots=true
brobot.debug.image.save-patterns=true
brobot.debug.image.save-comparisons=true
brobot.debug.image.output-dir=debug/image-finding

# Visual features
brobot.debug.image.visual.enabled=true
brobot.debug.image.visual.show-search-regions=true
brobot.debug.image.visual.show-match-scores=true
brobot.debug.image.visual.highlight-best-match=true
brobot.debug.image.visual.create-comparison-grid=true

# Console output (Windows support via Jansi)
brobot.debug.image.console.use-colors=true
brobot.debug.image.console.show-box=true
brobot.debug.image.console.show-timestamp=true
```

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

## Console Output

### Colorful Output on Windows

The debug system uses the Jansi library to enable ANSI colors on Windows terminals:

```
✅ IMAGE FIND DEBUGGER: Session initialized
→ FIND START: LoginButton
╔══════════════════════════════════════════════════════════════╗
║                    FIND OPERATION DEBUG                       ║
╠══════════════════════════════════════════════════════════════╣
║ Pattern: LoginButton                                           ║
║ Similarity: 0.85                                               ║
║ Status: ✓ SUCCESS                                              ║
║ Best Match: 0.92 at (450, 320)                               ║
║ Time: 145ms                                                   ║
╚══════════════════════════════════════════════════════════════╝
```

### Color Meanings

- 🟢 **Green**: Successful matches
- 🔴 **Red**: Failed searches or errors
- 🟡 **Yellow**: Warnings or low similarity scores
- 🔵 **Blue**: Headers and important information
- ⚪ **Gray**: Detailed/verbose information

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

### Issue: Images Cut with Windows Snipping Tool Not Found

**Symptoms**: Patterns captured with Win+Shift+S aren't matching

**Debug Steps**:

1. Enable DETAILED or VISUAL debugging:
```properties
brobot.debug.image.level=VISUAL
```

2. Check the debug output for:
   - DPI differences between pattern and screen
   - Color depth mismatches
   - Scaling issues

3. Review the comparison grid to see visual differences

**Common Solutions**:
- Lower similarity threshold: `brobot.find.similarity=0.7`
- Check DPI settings in Windows Display Settings
- Ensure consistent color profiles
- Save patterns as PNG format
- Avoid resizing after capture

### Issue: No Debug Output Appearing

**Check**:
1. Correct profile is active:
```bash
# Look for: "The following 1 profile is active: "debug""
```

2. Debug is enabled in properties:
```properties
brobot.debug.image.enabled=true
```

3. AOP interceptor is initialized:
```
✅ FindOperationInterceptor initialized
```

### Issue: No Colors in Console (Windows)

**Solution**: The Jansi library should auto-enable colors. If not:

1. Check Jansi is in classpath:
```gradle
implementation 'org.fusesource.jansi:jansi:2.4.0'
```

2. Verify initialization message:
```
[Brobot Debug] Windows detected - ANSI colors enabled via Jansi
```

3. Try different terminal (Windows Terminal, Git Bash, etc.)

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

## Architecture

### Components

1. **ImageDebugConfig**: Configuration management via Spring properties
2. **FindOperationInterceptor**: AOP aspect intercepting find operations
3. **ImageFindDebugger**: Core orchestrator for debug operations
4. **VisualDebugRenderer**: Creates annotated images and comparisons
5. **AnsiColor**: Console coloring with Windows support via Jansi

### Spring AOP Integration

The system uses `@Aspect` and `@Around` advice to intercept:
- `Find.perform()` operations
- `FindPipeline.saveMatchesToStateImages()` calls

This provides transparent debugging without code changes.

## Advanced Features

### Custom Debug Handlers

Implement custom debug handling:

```java
@Component
public class CustomDebugHandler {
    
    @EventListener
    public void handleFindDebugEvent(FindDebugEvent event) {
        // Custom logic for debug events
        if (event.getSimilarity() < 0.5) {
            // Alert on very low similarities
            notifyLowSimilarity(event);
        }
    }
}
```

### Programmatic Control

```java
@Autowired
private ImageDebugConfig debugConfig;

// Temporarily enable debugging
debugConfig.setEnabled(true);
debugConfig.setLevel(DebugLevel.VISUAL);

// Perform find operation
ActionResult result = action.find(stateImage);

// Restore settings
debugConfig.setEnabled(false);
```

## Related Documentation

- [Properties Reference](../configuration/properties-reference.md) - Complete property list
- [DPI Resolution Guide](../capture/dpi-resolution-guide.md) - Understanding DPI issues
- [Pattern Matching Guide](../../04-testing/debugging-pattern-matching.md) - Pattern matching troubleshooting
- [Mock Mode Guide](../../04-testing/mock-mode-guide.md) - Testing without GUI