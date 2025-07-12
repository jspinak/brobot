# SessionManager Migration Guide

## Overview
This guide describes the migration from the monolithic `SessionManager` to the refactored service-based architecture.

## Architecture Changes

### Before (Monolithic SessionManager)
- Single class with 544+ lines
- Mixed responsibilities: lifecycle, persistence, state, autosave, discovery
- Difficult to test individual features
- High coupling between components

### After (Service-Based Architecture)
- **SessionManager** - Thin facade/orchestrator (258 lines)
- **SessionLifecycleService** - Session lifecycle management
- **SessionPersistenceService** - File I/O operations
- **SessionStateService** - Application state capture/restore
- **SessionAutosaveService** - Automatic saving
- **SessionDiscoveryService** - Session search and discovery

## Migration Steps

### 1. Update Spring Configuration

The new SessionManager uses the same `@Component` annotation, so Spring will automatically use the refactored version if you:

```java
// Remove or rename the old SessionManager
// Ensure RefactoredSessionManager is annotated with @Component
```

### 2. Update Import Statements

```java
// Old
import io.github.jspinak.brobot.runner.session.SessionManager;

// New (if renamed)
import io.github.jspinak.brobot.runner.session.RefactoredSessionManager;
```

### 3. API Compatibility

The refactored SessionManager maintains full API compatibility. All existing methods are preserved:

#### Session Lifecycle
- `startNewSession(String projectName, String configPath, String imagePath)`
- `endCurrentSession()`
- `isSessionActive()`
- `getCurrentSession()`
- `restoreSession(String sessionId)`
- `deleteSession(String sessionId)`

#### Persistence
- `saveSession(Session session)`
- `loadSession(String sessionId)`
- `getAllSessionSummaries()`
- `sessionExists(String sessionId)`

#### Autosave
- `autosaveCurrentSession()`
- `getLastAutosaveTime()`
- `setAutosaveInterval(long minutes)`
- `getAutosaveInterval()`
- `enableAutosave()`
- `disableAutosave()`
- `isAutosaveEnabled()`

#### Session Data
- `updateSessionData(String key, Object value)`
- `addSessionEvent(String type, String description)`
- `addSessionEvent(String type, String description, String details)`

## Component-Specific Migration

### SessionManagementPanel
No changes required - all methods used are preserved:
- `getAllSessionSummaries()`
- `restoreSession(sessionId)`
- `deleteSession(sessionId)`

### ResourceMonitorPanel
No changes required - all methods used are preserved:
- `isSessionActive()`
- `getLastAutosaveTime()`
- `getCurrentSession()`

### UiComponentFactory
No changes required if using dependency injection.

## Testing the Migration

1. **Unit Tests**: Run existing SessionManager tests
   ```bash
   ./gradlew :runner:test --tests "*SessionManager*"
   ```

2. **Integration Tests**: Verify UI components work correctly
   ```bash
   ./gradlew :runner:test --tests "*SessionManagementPanel*"
   ./gradlew :runner:test --tests "*ResourceMonitorPanel*"
   ```

3. **Manual Testing**:
   - Start the application
   - Create a new session
   - Verify autosave works
   - Restore a session
   - Delete a session

## Rollback Plan

If issues occur:

1. Keep the old SessionManager class as `LegacySessionManager`
2. Use a feature flag to switch between implementations:

```java
@Configuration
public class SessionConfig {
    @Bean
    @ConditionalOnProperty(name = "session.use-legacy", havingValue = "true")
    public SessionManager legacySessionManager() {
        return new LegacySessionManager(...);
    }
    
    @Bean
    @ConditionalOnProperty(name = "session.use-legacy", havingValue = "false", matchIfMissing = true)
    public SessionManager refactoredSessionManager() {
        return new RefactoredSessionManager(...);
    }
}
```

## Benefits After Migration

1. **Testability**: Each service can be tested independently
2. **Maintainability**: Clear separation of concerns
3. **Extensibility**: Easy to add new features to specific services
4. **Performance**: Services can be optimized independently
5. **Debugging**: Clearer stack traces and error messages

## Next Steps

1. Update all components to use the refactored SessionManager
2. Run comprehensive tests
3. Monitor for any performance regressions
4. Consider removing the old SessionManager after a successful migration period
5. Document any custom extensions or modifications

## Support

For questions or issues during migration:
- Check the test suite for usage examples
- Review the JavaDoc in each service class
- Consult the REFACTORING_ROADMAP.md for context