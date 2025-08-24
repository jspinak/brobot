# Architectural Decisions - Brobot Runner Refactoring

## Overview

This document captures the key architectural decisions made during the Brobot Runner refactoring, particularly focusing on the SessionManager and AutomationProjectManager components.

## Key Design Principles

1. **Single Responsibility Principle (SRP)**: Each service has a single, well-defined responsibility
2. **Dependency Injection**: All services use constructor injection via Spring's `@Component` and `@Autowired`
3. **Event-Driven Architecture**: EventBus is used for loose coupling between components
4. **Adapter Pattern**: Used to bridge the gap between library and runner concepts
5. **Service-Based Architecture**: Complex functionality is broken down into focused services

## Major Architectural Decisions

### 1. Separation of Library and Runner Concepts

**Problem**: The library module (brobot) defines `AutomationProject` for automation state management, while the runner module needs project file management capabilities. UI components expected both behaviors from the same class.

**Decision**: Create separate but related concepts:
- **Library's `AutomationProject`**: Represents automation state and execution details
- **Runner's `ProjectDefinition`**: Represents project management, persistence, and lifecycle
- **`ProjectContext`**: Adapter that bridges both concepts

**Rationale**: This separation allows each module to evolve independently while maintaining compatibility.

### 2. Service-Based Architecture for AutomationProjectManager

**Problem**: The original AutomationProjectManager was becoming a "god class" with too many responsibilities.

**Decision**: Break down into focused services:
- `ProjectLifecycleService`: Handles project state transitions
- `ProjectPersistenceService`: Manages saving/loading projects
- `ProjectContextService`: Manages the relationship between ProjectDefinition and AutomationProject
- `ProjectAutomationLoader`: Loads automation projects from disk
- `ProjectUIService`: Provides UI-friendly operations

**Rationale**: Each service can be tested independently and modified without affecting others.

### 3. Backward Compatibility Strategy

**Problem**: Existing UI components depend on `getCurrentProject()` returning the library's `AutomationProject`.

**Decision**: Implement backward compatibility methods in AutomationProjectManager:
```java
@Deprecated
public AutomationProject getCurrentProject() {
    // Delegates to UIService or creates stub
}
```

**Rationale**: Allows gradual migration of UI components without breaking existing functionality.

### 4. Event-Driven Communication

**Problem**: Need loose coupling between services and UI components.

**Decision**: Use EventBus for:
- Project lifecycle events (created, opened, closed, etc.)
- Execution status updates
- Error notifications

**Rationale**: Components can react to events without direct dependencies.

### 5. Asynchronous Project Operations

**Problem**: Project loading and saving can be time-consuming operations.

**Decision**: Use `CompletableFuture` for async operations:
```java
public CompletableFuture<Optional<ProjectDefinition>> openProject(Path projectPath)
```

**Rationale**: Prevents UI blocking and allows for better error handling.

## Design Patterns Used

### 1. Adapter Pattern
- **Where**: `ProjectContext` class
- **Purpose**: Adapts between library's AutomationProject and runner's ProjectDefinition
- **Benefits**: Allows both concepts to coexist without modification

### 2. Builder Pattern
- **Where**: `ProjectDefinition`, `ProjectContext`, etc.
- **Purpose**: Construct complex objects with many optional parameters
- **Benefits**: Readable object creation, immutability support

### 3. Service Locator (via Spring DI)
- **Where**: Throughout the application
- **Purpose**: Manage service dependencies
- **Benefits**: Loose coupling, easy testing with mocks

### 4. Repository Pattern
- **Where**: `ProjectPersistenceService`
- **Purpose**: Abstract data persistence
- **Benefits**: Separation of business logic from data access

### 5. Observer Pattern (via EventBus)
- **Where**: Event handling throughout the application
- **Purpose**: Notify multiple components of state changes
- **Benefits**: Decoupled communication

## Migration Strategy

### Phase 1: SessionManager Refactoring ✓
- Created focused services for session management
- Maintained backward compatibility
- Updated UI components to use new APIs

### Phase 2: AutomationProjectManager Refactoring ✓
- Created service-based architecture
- Implemented adapter pattern for library/runner separation
- Added backward compatibility methods

### Phase 3: UI Component Migration (Future)
- Gradually update UI components to use ProjectUIService
- Remove deprecated methods once migration complete
- Implement proper error handling and loading states

## Error Handling Strategy

1. **Service Level**: Each service handles its own exceptions and logs errors
2. **Manager Level**: AutomationProjectManager provides high-level error handling
3. **UI Level**: Errors are communicated via EventBus for user notification

## Testing Strategy

1. **Unit Tests**: Each service has comprehensive unit tests
2. **Integration Tests**: Test service interactions
3. **Mock Objects**: Use Mockito for dependency mocking
4. **Test Coverage**: Aim for >80% coverage on critical paths

## Future Improvements

1. **Remove Deprecated Methods**: Once UI migration is complete
2. **Add Caching**: Implement caching in ProjectContextService
3. **Improve Async Handling**: Add progress reporting for long operations
4. **Enhanced Validation**: Add more comprehensive project validation
5. **Plugin Architecture**: Allow external modules to extend project functionality

## Conclusion

The refactoring successfully addresses the architectural issues while maintaining backward compatibility. The service-based architecture provides better maintainability, testability, and flexibility for future enhancements.