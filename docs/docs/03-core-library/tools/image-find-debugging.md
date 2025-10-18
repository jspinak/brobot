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
├── 2025-09-13_10-20-30/        # Session timestamp format: yyyy-MM-dd_HH-mm-ss
│   ├── screenshots/
│   │   ├── 001-LoginButton.png
│   │   └── 002-SubmitButton.png
│   ├── patterns/
│   │   ├── 001-LoginButton-pattern.png
│   │   └── 002-SubmitButton-pattern.png
│   ├── comparisons/
│   │   ├── 001-LoginButton-comparison.png
│   │   └── 002-SubmitButton-comparison.png
│   ├── visual/
│   │   ├── 001-LoginButton-annotated.png
│   │   └── 002-SubmitButton-annotated.png
│   └── logs/
│       └── session-summary.json
```

## Configuration

### Profile-Based Configuration

Brobot uses [Spring profiles](../../04-testing/profile-based-testing.md) for clean configuration:

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

> **Note**: Additional properties exist for advanced configuration including `visual.show-failed-regions`, `visual.create-heatmap`, logging properties, and real-time monitoring. See the [Properties Reference](../configuration/properties-reference.md) for a complete list.

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

   Similarity is configured per-operation, not globally:
   ```java
   PatternFindOptions options = new PatternFindOptions.Builder()
       .setSimilarity(0.7)  // Lower for more lenient matching
       .build();

   ActionResult result = action.find(options, stateImage);
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
- Lower similarity threshold in your `PatternFindOptions.Builder().setSimilarity(0.7)`
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

### Issue: ClassNotFoundException for ImageFindDebugger

**Symptoms**: `java.lang.ClassNotFoundException: io.github.jspinak.brobot.debug.ImageFindDebugger`

**Cause**: Debug classes are conditional beans that only load when debugging is enabled.

**Solution**:

1. **Enable debug mode in properties**:
   ```properties
   brobot.debug.image.enabled=true
   ```

2. **Check Spring Boot autoconfiguration**:
   - Ensure `@EnableAutoConfiguration` or `@SpringBootApplication` is present
   - Verify Brobot library is in classpath

3. **Check for conditional bean issues**:
   ```java
   // Debug classes use @ConditionalOnProperty
   @ConditionalOnProperty(name = "brobot.debug.image.enabled", havingValue = "true")
   ```

4. **Verify in logs**:
   ```
   INFO  Conditional bean ImageFindDebugger matched
   INFO  ImageFindDebugger initialized
   ```

### Issue: AOP Not Intercepting Find Operations

**Symptoms**: Debug output not appearing even when enabled, no interception logs

**Cause**: Spring AOP or AspectJ not properly configured

**Solutions**:

1. **Ensure Spring AOP is enabled**:
   ```java
   @SpringBootApplication
   @EnableAspectJAutoProxy  // Add this annotation
   public class MyApplication {
       public static void main(String[] args) {
           SpringApplication.run(MyApplication.class, args);
       }
   }
   ```

2. **Verify AspectJ dependencies**:
   ```gradle
   implementation 'org.springframework.boot:spring-boot-starter-aop'
   implementation 'org.aspectj:aspectjweaver'
   ```

3. **Check AOP proxy creation**:
   ```properties
   # Enable CGLIB proxies if needed
   spring.aop.proxy-target-class=true
   ```

4. **Verify interceptor initialization**:
   ```
   INFO  FindOperationInterceptor initialized
   INFO  AOP pointcuts registered for Find.perform()
   ```

5. **Check method visibility**:
   - AOP requires public methods on Spring beans
   - Direct `new` instantiation bypasses AOP proxies

### Issue: Jansi Library Loading Errors

**Symptoms**: `java.lang.NoClassDefFoundError: org/fusesource/jansi/AnsiConsole`

**Cause**: Jansi not in classpath or version conflict

**Solutions**:

1. **Add Jansi dependency**:
   ```gradle
   // In library/build.gradle
   implementation 'org.fusesource.jansi:jansi:2.4.0'
   ```

2. **Check for version conflicts**:
   ```bash
   ./gradlew dependencies --configuration runtimeClasspath | grep jansi
   ```

3. **Exclude conflicting versions**:
   ```gradle
   configurations.all {
       exclude group: 'org.fusesource.jansi', module: 'jansi'
   }
   dependencies {
       implementation 'org.fusesource.jansi:jansi:2.4.0'
   }
   ```

4. **Fallback: Disable colors**:
   ```properties
   brobot.debug.image.console.use-colors=false
   ```

### Issue: Debug Files Not Saving

**Symptoms**: Console output works but no files in `debug/image-finding/` directory

**Cause**: File permissions, path issues, or file saving disabled

**Solutions**:

1. **Verify file saving is enabled**:
   ```properties
   brobot.debug.image.save-screenshots=true
   brobot.debug.image.save-patterns=true
   brobot.debug.image.save-comparisons=true
   ```

2. **Check output directory path**:
   ```properties
   # Use absolute path if relative path fails
   brobot.debug.image.output-dir=/absolute/path/to/debug/image-finding
   ```

3. **Verify write permissions**:
   ```bash
   # Check directory exists and is writable
   ls -la debug/
   mkdir -p debug/image-finding
   chmod 755 debug/image-finding
   ```

4. **Check disk space**:
   - Debug files can be large (screenshots, comparisons)
   - Ensure sufficient disk space available

5. **Review logs for I/O errors**:
   ```
   ERROR Failed to save screenshot: /path/to/file.png
   java.io.IOException: Permission denied
   ```

## Integration with CI/CD

For comprehensive CI/CD testing strategies, see the [CI/CD Testing Guide](../testing/ci-cd-testing.md).

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

Complete example showing session management. For more information on StateImage and state components, see the [States Guide](../../../01-getting-started/states.md#state-components-and-direct-access).

```java
package com.example.brobot.debugging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.debug.ImageFindDebugger;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;

@Component
public class ImageDebugExample {

    @Autowired
    private ImageFindDebugger debugger;

    @Autowired
    private Action action;

    public void debugImageFind() {
        // Example pattern to find
        StateImage stateImage = new StateImage.Builder()
            .addPatterns("button-pattern")
            .build();

        try {
            // Initialize session
            debugger.initializeSession();

            // Debug operations are automatically intercepted
            ActionResult result = action.find(stateImage);

        } finally {
            // Always finalize session, even if exception occurs
            debugger.finalizeSession();
        }
    }
}
```

### Custom Debug Info

Access detailed debugging information programmatically:

```java
package com.example.brobot.debugging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.debug.ImageFindDebugger;
import io.github.jspinak.brobot.debug.ImageFindDebugger.FindDebugInfo;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;

@Component
public class DebugInfoExample {

    @Autowired
    private ImageFindDebugger debugger;

    @Autowired
    private Action action;

    public void getDebugInfo() {
        // Create pattern to find
        StateImage stateImage = new StateImage.Builder()
            .addPatterns("login-button")
            .build();

        // Create object collection
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withImages(stateImage)
            .build();

        // Configure find options
        PatternFindOptions options = new PatternFindOptions.Builder()
            .setSimilarity(0.85)
            .build();

        // Perform find operation
        ActionResult result = action.find(options, objectCollection);

        // Get detailed debug info
        FindDebugInfo debugInfo =
            debugger.debugFindOperation(objectCollection, options, result);

        // Access debug information (getters are auto-generated by Lombok)
        System.out.println("Operation ID: " + debugInfo.getOperationId());
        System.out.println("Best Score: " + debugInfo.getBestScore());
        System.out.println("Duration: " + debugInfo.getSearchDuration() + "ms");
        System.out.println("Found: " + debugInfo.isFound());
        System.out.println("Match Count: " + debugInfo.getMatchCount());
    }
}
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

> **Note**: Performance impact percentages are approximate and vary based on system configuration, image sizes, screen resolution, pattern complexity, and operation types. Use these as general guidelines, not exact measurements. Actual impact may be higher on slower systems or when processing large images.

## Architecture

### Components

1. **ImageDebugConfig**: Configuration management via Spring properties
2. **FindOperationInterceptor**: AOP aspect intercepting find operations
3. **ImageFindDebugger**: Core orchestrator for debug operations
4. **VisualDebugRenderer**: Creates annotated images and comparisons
5. **AnsiColor**: Console coloring with Windows support via Jansi

### Spring AOP Integration

The system uses Spring AOP (see [AspectJ Usage Guide](../advanced/aspectj-usage-guide.md) for advanced AOP patterns) with `@Aspect` and `@Around` advice to intercept:
- `Find.perform()` operations
- `FindPipeline.saveMatchesToStateImages()` calls

This provides transparent debugging without code changes.

## Advanced Features

### Programmatic Control

Dynamically control debugging at runtime:

```java
package com.example.brobot.debugging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.debug.ImageDebugConfig;
import io.github.jspinak.brobot.debug.ImageDebugConfig.DebugLevel;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;

@Component
public class ProgrammaticDebugControl {

    @Autowired
    private ImageDebugConfig debugConfig;

    @Autowired
    private Action action;

    public void temporaryDebugMode() {
        // Save original settings
        boolean originalEnabled = debugConfig.isEnabled();
        DebugLevel originalLevel = debugConfig.getLevel();

        try {
            // Temporarily enable debugging
            debugConfig.setEnabled(true);
            debugConfig.setLevel(DebugLevel.VISUAL);

            // Example pattern to find
            StateImage stateImage = new StateImage.Builder()
                .addPatterns("login-button")
                .build();

            // Perform find operation with debugging enabled
            ActionResult result = action.find(stateImage);

        } finally {
            // Always restore original settings
            debugConfig.setEnabled(originalEnabled);
            debugConfig.setLevel(originalLevel);
        }
    }
}
```

## Related Documentation

- [Properties Reference](../configuration/properties-reference.md) - Complete property list
- [DPI Resolution Guide](../capture/dpi-resolution-guide.md) - Understanding DPI issues
- [Pattern Matching Guide](../../04-testing/debugging-pattern-matching.md) - Pattern matching troubleshooting
- [Mock Mode Guide](../../04-testing/mock-mode-guide.md) - Testing without GUI