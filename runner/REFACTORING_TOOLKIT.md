# Refactoring Toolkit for Brobot Runner

## Quick Reference Scripts and Tools

### 1. Class Analysis Script

```bash
#!/bin/bash
# analyze_class.sh - Analyze a Java class for refactoring potential

CLASS_FILE=$1

if [ -z "$CLASS_FILE" ]; then
    echo "Usage: ./analyze_class.sh <path/to/Class.java>"
    exit 1
fi

echo "Analyzing $CLASS_FILE..."
echo "================================="

# Line count
LINES=$(wc -l < "$CLASS_FILE")
echo "Total lines: $LINES"

# Method count
METHODS=$(grep -c "public\|private\|protected" "$CLASS_FILE")
echo "Method count: $METHODS"

# Dependency count
IMPORTS=$(grep -c "^import" "$CLASS_FILE")
echo "Import count: $IMPORTS"

# Responsibility indicators
echo -e "\nPotential responsibilities:"
grep -E "Manager|Service|Controller|Handler|Processor" "$CLASS_FILE" | head -5

# Complexity indicators
echo -e "\nComplexity indicators:"
echo "- Nested if statements: $(grep -c "if.*{" "$CLASS_FILE")"
echo "- Try-catch blocks: $(grep -c "try.*{" "$CLASS_FILE")"
echo "- Loops: $(grep -c "for\|while" "$CLASS_FILE")"
```

### 2. Service Extraction Template

```java
package io.github.jspinak.brobot.runner.[module].[service];

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * [Service description focusing on single responsibility]
 * 
 * This service is responsible for [specific responsibility].
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
public class [ServiceName] implements DiagnosticCapable {
    
    // Configuration constants
    private static final String SERVICE_NAME = "[ServiceName]";
    
    // Dependencies
    // TODO: Add required dependencies
    
    // State
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    /**
     * [Main service method]
     * 
     * @param [param] [description]
     * @return [description]
     */
    public [ReturnType] [methodName]([Parameters]) {
        log.debug("[Method] called with [parameters]");
        
        // TODO: Implement core logic
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] [Relevant diagnostic information]");
        }
        
        return [result];
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        // TODO: Add relevant diagnostic state
        
        return DiagnosticInfo.builder()
                .component(SERVICE_NAME)
                .states(states)
                .build();
    }
    
    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }
    
    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.info("Diagnostic mode {}", enabled ? "enabled" : "disabled");
    }
}
```

### 3. Test Template

```java
package io.github.jspinak.brobot.runner.[module].[service];

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for [ServiceName].
 * 
 * Tests [what this service does].
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("[ServiceName] Tests")
class [ServiceName]Test {
    
    // Dependencies
    @Mock
    private [Dependency] mockDependency;
    
    // System under test
    private [ServiceName] service;
    
    @BeforeEach
    void setUp() {
        service = new [ServiceName](mockDependency);
    }
    
    @Test
    @DisplayName("Should [expected behavior]")
    void should[TestName]() {
        // Given
        [SetupTestData]
        
        // When
        [ExecuteAction]
        
        // Then
        [VerifyResults]
    }
    
    @Test
    @DisplayName("Should handle [error scenario]")
    void shouldHandle[ErrorScenario]() {
        // Given
        [SetupErrorCondition]
        
        // When/Then
        assertThrows([ExpectedException].class, () -> {
            [ExecuteAction]
        });
    }
    
    @Test
    @DisplayName("Should provide diagnostic information")
    void shouldProvideDiagnosticInformation() {
        // When
        DiagnosticInfo info = service.getDiagnosticInfo();
        
        // Then
        assertNotNull(info);
        assertEquals("[ServiceName]", info.getComponent());
        assertNotNull(info.getStates());
    }
}
```

### 4. Refactoring Checklist

```markdown
## Pre-Refactoring Checklist
- [ ] Current class analyzed for responsibilities
- [ ] Dependencies mapped
- [ ] Test coverage checked
- [ ] Performance baseline captured
- [ ] API consumers identified

## During Refactoring
- [ ] Feature branch created
- [ ] Services extracted one at a time
- [ ] Each service has single responsibility
- [ ] Tests written for each service
- [ ] Original API maintained
- [ ] Diagnostic capabilities added

## Post-Refactoring
- [ ] All tests passing
- [ ] No performance degradation
- [ ] Code review completed
- [ ] Documentation updated
- [ ] Migration guide written
```

### 5. Common Refactoring Patterns

#### Extract Service Pattern
```java
// Before
public class MonolithicClass {
    public void complexMethod() {
        // Step 1: Validate input
        validateInput();
        
        // Step 2: Process data
        processData();
        
        // Step 3: Save results
        saveResults();
    }
}

// After
public class OrchestatorClass {
    private final ValidationService validationService;
    private final ProcessingService processingService;
    private final PersistenceService persistenceService;
    
    public void complexMethod() {
        validationService.validate(input);
        var processed = processingService.process(data);
        persistenceService.save(processed);
    }
}
```

#### Extract Context Pattern
```java
// Before
public void executeTask(String taskId, String taskName, 
                       Map<String, Object> options, 
                       Duration timeout, boolean async) {
    // Many parameters
}

// After
public void executeTask(TaskContext context) {
    // Single context object
}

@Builder
@Getter
public class TaskContext {
    private final String taskId;
    private final String taskName;
    private final TaskOptions options;
    private final Duration timeout;
    private final boolean async;
}
```

### 6. IDE Live Templates

#### IntelliJ IDEA Live Template for Service
```xml
<template name="service" value="@Service&#10;@Slf4j&#10;public class $NAME$Service implements DiagnosticCapable {&#10;    &#10;    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);&#10;    &#10;    public $NAME$Service() {&#10;        log.info(&quot;$NAME$Service initialized&quot;);&#10;    }&#10;    &#10;    @Override&#10;    public DiagnosticInfo getDiagnosticInfo() {&#10;        return DiagnosticInfo.builder()&#10;                .component(&quot;$NAME$Service&quot;)&#10;                .build();&#10;    }&#10;    &#10;    @Override&#10;    public boolean isDiagnosticModeEnabled() {&#10;        return diagnosticMode.get();&#10;    }&#10;    &#10;    @Override&#10;    public void enableDiagnosticMode(boolean enabled) {&#10;        diagnosticMode.set(enabled);&#10;    }&#10;}" toReformat="true" toShortenFQNames="true">
  <variable name="NAME" expression="" defaultValue="" alwaysStopAt="true" />
  <context>
    <option name="JAVA_DECLARATION" value="true" />
  </context>
</template>
```

### 7. Gradle Tasks for Refactoring

```gradle
// Add to build.gradle

task analyzeComplexity(type: JavaExec) {
    group = 'verification'
    description = 'Analyze code complexity'
    classpath = sourceSets.main.runtimeClasspath
    main = 'com.github.javaparser.JavaParserMetrics'
    args = ['src/main/java']
}

task generateRefactoringReport {
    group = 'documentation'
    description = 'Generate refactoring progress report'
    doLast {
        def report = new File("refactoring-report.md")
        report.text = """# Refactoring Progress Report
        
Generated: ${new Date()}

## Classes Refactored
${file('src/main/java').listFiles().findAll { it.name.endsWith('Service.java') }.size()} services created

## Test Coverage
${file('src/test/java').listFiles().findAll { it.name.endsWith('Test.java') }.size()} test classes
        """
    }
}
```

### 8. Git Hooks for Quality

```bash
#!/bin/bash
# .git/hooks/pre-commit
# Ensure no classes exceed 400 lines

for file in $(git diff --cached --name-only | grep \.java$); do
    lines=$(wc -l < "$file")
    if [ $lines -gt 400 ]; then
        echo "ERROR: $file has $lines lines (max 400)"
        echo "Consider refactoring before committing."
        exit 1
    fi
done
```

### 9. Monitoring Script

```bash
#!/bin/bash
# monitor_refactoring.sh - Track refactoring progress

echo "Refactoring Progress Monitor"
echo "============================"
echo ""

# Count services
SERVICES=$(find src/main/java -name "*Service.java" | wc -l)
echo "Services created: $SERVICES"

# Count tests
TESTS=$(find src/test/java -name "*Test.java" | wc -l)
echo "Test classes: $TESTS"

# Large classes remaining
echo -e "\nLarge classes (>300 lines):"
find src/main/java -name "*.java" -exec wc -l {} + | \
    awk '$1 > 300 {print $2 " (" $1 " lines)"}' | \
    grep -v Service | head -10

# Coverage summary
if [ -f "build/reports/jacoco/test/html/index.html" ]; then
    echo -e "\nTest Coverage:"
    grep -o '[0-9]*%' build/reports/jacoco/test/html/index.html | head -1
fi
```

### 10. Documentation Generator

```java
/**
 * Generates documentation for refactored services
 */
public class ServiceDocGenerator {
    public static void main(String[] args) {
        Path servicesDir = Paths.get("src/main/java/.../services");
        StringBuilder doc = new StringBuilder("# Service Documentation\n\n");
        
        Files.walk(servicesDir)
            .filter(p -> p.toString().endsWith("Service.java"))
            .forEach(p -> {
                String serviceName = p.getFileName().toString();
                doc.append("## ").append(serviceName).append("\n");
                doc.append("- Location: ").append(p).append("\n");
                doc.append("- Responsibility: TODO\n");
                doc.append("- Dependencies: TODO\n\n");
            });
            
        Files.write(Paths.get("SERVICE_DOCS.md"), doc.toString().getBytes());
    }
}
```

## Usage Guide

1. **Before starting**: Run analysis script on target class
2. **Create services**: Use service template for consistency
3. **Write tests**: Use test template for each service
4. **Check progress**: Run monitoring script regularly
5. **Document**: Generate documentation after each phase

This toolkit provides practical tools and templates to streamline the refactoring process and ensure consistency across the team.