# Brobot Logging Verbosity Configuration

## Overview

The Brobot framework now supports configurable logging verbosity levels to control the amount of detail in log output. This feature allows you to switch between concise normal logging and detailed verbose logging based on your needs.

## Verbosity Levels

### NORMAL Mode (Default)
Normal mode provides concise logging with only essential information:
- Action type (CLICK, FIND, TYPE, etc.)
- State and object acted on
- Results (success/failure)
- Match coordinates (if configured)

Example output in NORMAL mode:
```
FIND LoginButton [SUCCESS] @(452,312)
CLICK LoginButton [SUCCESS]
TYPE [SUCCESS]
STATE: LoginPage -> Dashboard [SUCCESS]
```

### VERBOSE Mode
Verbose mode includes all available information:
- Everything from NORMAL mode
- Detailed timing information
- Search regions
- Match scores
- Performance metrics
- Additional metadata
- Session IDs
- Stack traces for errors

Example output in VERBOSE mode:
```
[session-123] Action: FIND on LoginButton {matchCount=1, searchTime=145} (267ms)
[session-123] Transition: LoginPage -> Dashboard [450ms]
```

## Configuration

### Via Properties File

Set the verbosity level in your `application.properties`:

```properties
# Set to NORMAL or VERBOSE
brobot.logging.verbosity=NORMAL

# Normal mode settings
brobot.logging.normal.show-timing=false
brobot.logging.normal.show-match-coordinates=true
brobot.logging.normal.show-match-count=true
brobot.logging.normal.use-compact-format=true
brobot.logging.normal.max-object-name-length=30

# Verbose mode settings
brobot.logging.verbose.show-search-regions=true
brobot.logging.verbose.show-match-scores=true
brobot.logging.verbose.show-action-options=true
brobot.logging.verbose.show-performance-breakdown=true
brobot.logging.verbose.show-metadata=true
brobot.logging.verbose.show-stack-traces=true
```

### Programmatically

You can also change the verbosity level at runtime:

```java
@Autowired
private BrobotLogger brobotLogger;

// Set to verbose mode
brobotLogger.setVerbosity(LoggingVerbosityConfig.VerbosityLevel.VERBOSE);

// Check current verbosity
LoggingVerbosityConfig.VerbosityLevel current = brobotLogger.getVerbosity();

// Set back to normal
brobotLogger.setVerbosity(LoggingVerbosityConfig.VerbosityLevel.NORMAL);
```

## Use Cases

### When to Use NORMAL Mode
- Production environments
- Automated test execution
- When you only need to see what happened and whether it succeeded
- Performance-critical scenarios where minimal logging overhead is desired

### When to Use VERBOSE Mode
- Development and debugging
- Troubleshooting test failures
- Performance analysis
- Understanding the framework's internal behavior
- Training new team members

## Integration with Existing Logging

The verbosity configuration works seamlessly with existing Brobot logging features:
- Console output respects verbosity settings
- SLF4J backend logging adapts to verbosity level
- Visual feedback (highlighting) remains independent
- Structured logging to ActionLogger can be enabled separately

## Examples

### Example 1: Debugging a Failed Find Operation

With NORMAL mode:
```
FIND SubmitButton [FAILED]
```

With VERBOSE mode:
```
[test-session-001] Action: FIND on SubmitButton {searchRegion=(0,0,1920,1080), similarity=0.8, timeout=3000} (3015ms) [FAILED]
   └─ No matches found above similarity threshold
   └─ Best match score: 0.65 at (834,445)
```

### Example 2: State Transition

With NORMAL mode:
```
STATE: LoginPage -> Dashboard [SUCCESS]
```

With VERBOSE mode:
```
[test-session-001] Transition: LoginPage -> Dashboard [SUCCESS]
   ├─ Active states before: [LoginPage]
   ├─ Transition path: LoginPage → LoginToDashboard → Dashboard
   ├─ Actions performed: 3
   └─ Total duration: 1245ms
```

## Best Practices

1. **Default to NORMAL in Production**: Keep logs concise in production to reduce noise and storage requirements.

2. **Use VERBOSE for Debugging**: Switch to verbose mode when investigating issues or optimizing performance.

3. **Configure Per Environment**: Use different property files for different environments:
   - `application-dev.properties`: `brobot.logging.verbosity=VERBOSE`
   - `application-prod.properties`: `brobot.logging.verbosity=NORMAL`

4. **Dynamic Switching**: For long-running applications, implement a mechanism to switch verbosity levels without restart:
   ```java
   @RestController
   public class LoggingController {
       @PostMapping("/logging/verbosity/{level}")
       public void setVerbosity(@PathVariable String level) {
           brobotLogger.setVerbosity(
               LoggingVerbosityConfig.VerbosityLevel.valueOf(level.toUpperCase())
           );
       }
   }
   ```

5. **Combine with Log Levels**: Use verbosity in conjunction with SLF4J log levels for fine-grained control:
   - INFO level + NORMAL verbosity: Essential operational information
   - DEBUG level + VERBOSE verbosity: Complete debugging information