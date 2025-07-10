# ADR-002: AI-Friendly Architecture Patterns

## Status
Accepted

## Context
As AI assistants become integral to software development, code architecture must evolve to be more AI-friendly. Current challenges include:
- Implicit context requiring human knowledge
- Lack of diagnostic capabilities for debugging
- Unclear execution flows
- Missing behavioral contracts
- Insufficient test documentation

AI assistants need explicit context, traceable execution, and comprehensive diagnostics to effectively understand, test, and debug code.

## Decision
We will implement AI-friendly patterns throughout the codebase:

### 1. Diagnostic Infrastructure
Every major component implements `DiagnosticCapable`:
```java
public interface DiagnosticCapable {
    DiagnosticInfo getDiagnosticInfo();
    void enableDiagnosticMode(boolean enabled);
}
```

### 2. Explicit Context
Replace implicit knowledge with explicit declarations:
```java
// Bad: Magic numbers
if (size > 100) { handleLarge(); }

// Good: Explicit context
private static final int MAX_INLINE_SIZE = 100; // Threshold for streaming
private static final String SIZE_REASON = "Performance degrades above 100";
```

### 3. Correlation IDs
Track execution flow across components:
```java
MDC.put("correlationId", UUID.randomUUID().toString());
```

### 4. Behavioral Contracts
Document invariants and state transitions:
```java
/**
 * Behavioral Contract:
 * - Only one execution active at a time
 * - State transitions: IDLE -> RUNNING -> COMPLETED
 * - All operations are idempotent
 */
```

### 5. Test Patterns
Scenario-based tests with clear structure:
```java
@Test
@TestScenario(
    given = "No active session",
    when = "startSession called",
    then = "Session created and persisted"
)
```

## Consequences

### Positive
- **AI Debugging**: AI can request diagnostics and trace execution
- **Faster Issue Resolution**: Clear context speeds up debugging
- **Better Documentation**: Self-documenting code and tests
- **Reduced Onboarding**: AI can understand codebase faster
- **Improved Collaboration**: Clear contracts between components

### Negative
- **Additional Code**: Diagnostic infrastructure adds complexity
- **Performance Overhead**: Diagnostics and tracing have cost
- **Verbosity**: More explicit declarations increase code size
- **Maintenance**: Contracts and documentation need updates

### Mitigation
- Make diagnostics optional (enable only when needed)
- Use annotations to reduce boilerplate
- Automate contract validation
- Generate documentation from code

## Examples

### Diagnostic Implementation
```java
@Override
public DiagnosticInfo getDiagnosticInfo() {
    return DiagnosticInfo.builder()
        .component("SessionService")
        .state("activeSession", currentSession != null)
        .state("sessionId", currentSession?.getId())
        .state("duration", currentSession?.getDuration())
        .build();
}
```

### Test Data Builder
```java
Session testSession = SessionBuilder.anExpiredSession()
    .withProjectName("test-project")
    .withDiagnosticMode(true)
    .build();
```

## References
- AI-Friendly Code Patterns (AI-info/AI-FRIENDLY-CODE-PATTERNS.md)
- Diagnostic Infrastructure Blueprint
- Correlation ID Pattern
- Given-When-Then test structure