# Quick Reference Guide for Brobot Runner Refactoring

## 🚀 Getting Started

### Essential Commands
```bash
# Run validation on your refactoring
./gradlew validateRefactoring -Pcomponent=SessionManager

# Run benchmarks before changes
./gradlew benchmark:baseline -Pcomponent=SessionManager

# Generate diagnostic report
./gradlew diagnostics:report

# Run AI-friendly tests
./gradlew test -Pai.mode=true -Pdiagnostics=true
```

## 📋 Refactoring Checklist

### Before Starting
- [ ] Read the component's current tests
- [ ] Run baseline performance benchmarks
- [ ] Document current public API
- [ ] Identify all dependencies
- [ ] Create feature branch: `feature/refactor-[component]`

### During Refactoring
- [ ] Follow SRP - one class, one responsibility
- [ ] Implement `DiagnosticCapable` interface
- [ ] Add correlation ID support
- [ ] Create repository interfaces for data access
- [ ] Use view models for UI components
- [ ] Write tests for each new class
- [ ] Add behavioral contracts in JavaDoc

### After Refactoring
- [ ] Run validation tools
- [ ] Compare performance benchmarks
- [ ] Update documentation
- [ ] Create pull request with validation report

## 🏗️ Common Patterns

### 1. Service Pattern
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class MyService implements DiagnosticCapable {
    private final MyRepository repository;
    private final EventPublisher eventPublisher;
    
    public Result doOperation(Input input) {
        String correlationId = MDC.get("correlationId");
        log.info("[{}] Starting operation", correlationId);
        
        // Business logic here
        
        return result;
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
            .component("MyService")
            .state("key", "value")
            .build();
    }
}
```

### 2. Repository Pattern
```java
public interface MyRepository {
    CompletableFuture<Entity> save(Entity entity);
    CompletableFuture<Optional<Entity>> findById(String id);
}

@Repository
public class FileMyRepository implements MyRepository {
    // Implementation
}
```

### 3. View Model Pattern
```java
public interface MyViewModel extends ViewModel {
    // Observable properties
    ReadOnlyStringProperty titleProperty();
    ReadOnlyBooleanProperty loadingProperty();
    
    // Commands
    Command saveCommand();
}
```

### 4. Test Pattern
```java
@Test
@TestScenario(
    given = "Valid input data",
    when = "Operation is executed",
    then = "Expected result is returned"
)
void testOperation_WithValidInput_ShouldSucceed() {
    // Given
    Input input = TestDataBuilder.validInput().build();
    
    // When
    Result result = service.doOperation(input);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.isSuccess()).isTrue();
}
```

## 🔧 Diagnostic Tools

### Enable Diagnostics
```java
// In application.properties
diagnostics.enabled=true
diagnostics.endpoint.enabled=true
diagnostics.correlation-id.enabled=true

// In code
@EnableDiagnostics
public class MyComponent {
    // Component code
}
```

### Access Diagnostics
```bash
# Get system diagnostics
curl http://localhost:8080/api/diagnostics/snapshot

# Get component diagnostics
curl http://localhost:8080/api/diagnostics/component/SessionService

# Get execution traces
curl http://localhost:8080/api/diagnostics/traces
```

## 📊 Metrics to Track

### Code Quality Metrics
| Metric | Before Goal | After Goal |
|--------|------------|------------|
| Class Size | > 300 lines | < 200 lines |
| Methods per Class | > 15 | < 10 |
| Cyclomatic Complexity | > 10 | < 7 |
| Test Coverage | < 60% | > 80% |

### Performance Metrics
| Metric | Acceptable Range |
|--------|-----------------|
| Startup Time | < 5% increase |
| Memory Usage | < 10% increase |
| Response Time | < 5% increase |
| Throughput | No decrease |

## 🛠️ IDE Setup

### IntelliJ IDEA
1. Install plugins:
   - Lombok
   - Spring Boot
   - Diagrams.net Integration

2. Enable annotation processing:
   - Settings → Build → Compiler → Annotation Processors
   - Check "Enable annotation processing"

3. Configure code style:
   - Import `code-style/intellij-brobot-style.xml`

### VS Code
1. Install extensions:
   - Java Extension Pack
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. Configure settings:
   ```json
   {
     "java.format.settings.url": "./code-style/eclipse-brobot-style.xml",
     "java.saveActions.organizeImports": true
   }
   ```

## 🚨 Common Pitfalls

### 1. Forgetting Correlation IDs
```java
// ❌ Bad
public void process() {
    log.info("Processing");
}

// ✅ Good
public void process() {
    String correlationId = MDC.get("correlationId");
    log.info("[{}] Processing", correlationId);
}
```

### 2. Missing Diagnostics
```java
// ❌ Bad
public class MyService {
    // No diagnostics
}

// ✅ Good
public class MyService implements DiagnosticCapable {
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        // Return current state
    }
}
```

### 3. Tight Coupling
```java
// ❌ Bad
public class MyService {
    private FileRepository repository = new FileRepository();
}

// ✅ Good
public class MyService {
    private final Repository repository; // Interface
    
    public MyService(Repository repository) {
        this.repository = repository;
    }
}
```

## 📚 Resources

### Documentation
- [Refactoring Strategy](REFACTORING_STRATEGY.md)
- [AI-Friendly Patterns](../AI-info/AI-FRIENDLY-CODE-PATTERNS.md)
- [Implementation Examples](IMPLEMENTATION_EXAMPLES.md)

### Migration Guides
- [ExecutionController Migration](MIGRATION_GUIDE_EXECUTION_CONTROLLER.md)
- [SessionManager Migration](MIGRATION_GUIDE_SESSION_MANAGER.md)

### Architecture Decisions
- [ADR-001: SRP Refactoring](adr/001-single-responsibility-refactoring.md)
- [ADR-002: AI-Friendly Architecture](adr/002-ai-friendly-architecture.md)
- [ADR-003: Plugin Architecture](adr/003-plugin-architecture.md)

## 💡 Tips for Success

1. **Start Small**: Begin with one component and learn
2. **Test First**: Write tests before refactoring
3. **Incremental Changes**: Small, reviewable PRs
4. **Document Decisions**: Update ADRs as needed
5. **Measure Impact**: Use validation tools
6. **Collaborate**: Pair programming helps
7. **Ask for Help**: Use AI assistants for guidance

## 🆘 Getting Help

### Internal Resources
- Slack: #brobot-refactoring
- Wiki: Internal Refactoring Guide
- Team Leads: Architecture decisions

### AI Assistant Commands
```
// Get refactoring suggestions
"Help me refactor SessionManager following SRP"

// Review code
"Review this refactored ExecutionService for SRP compliance"

// Debug issues
"Debug why my diagnostic endpoint returns null"

// Generate tests
"Generate tests for this SessionRepository implementation"
```

## 📈 Progress Tracking

### Component Status
| Component | Status | Owner | PR |
|-----------|--------|-------|-----|
| ExecutionController | 🟡 In Progress | @dev1 | #123 |
| SessionManager | 🟢 Complete | @dev2 | #124 |
| ConfigMetadataEditor | 🔴 Not Started | - | - |
| LogExportService | 🟡 In Progress | @dev3 | #125 |

### Legend
- 🟢 Complete and validated
- 🟡 In progress
- 🔴 Not started
- ✅ Merged

---

**Remember**: The goal is maintainable, testable, AI-friendly code. When in doubt, favor clarity over cleverness.