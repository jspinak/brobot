# ADR-001: Single Responsibility Principle Refactoring

## Status
Accepted

## Context
The Brobot Runner codebase contains multiple large classes (400-700+ lines) that violate the Single Responsibility Principle (SRP). Key examples include:
- `ExecutionController` (380 lines, 7+ responsibilities)
- `SessionManager` (543 lines, 10+ responsibilities)
- `ConfigMetadataEditor` (720 lines, 6+ responsibilities)

These violations lead to:
- Difficult testing (high coupling, complex mocking)
- Hard to understand code (multiple concerns mixed)
- Risky changes (modifying one aspect affects others)
- Poor reusability (functionality tied to specific contexts)

## Decision
We will systematically refactor all classes violating SRP by:
1. Decomposing monolithic classes into focused, single-purpose components
2. Applying consistent patterns across similar refactorings
3. Maintaining backward compatibility during migration
4. Adding comprehensive diagnostics for debugging

### Refactoring Pattern
Each large class will be split following this pattern:
```
original/
└── MonolithicClass.java (500+ lines)

refactored/
├── core/
│   └── CoreService.java (business logic)
├── repository/
│   └── DataRepository.java (persistence)
├── state/
│   └── StateManager.java (state management)
├── events/
│   └── EventPublisher.java (event handling)
└── controller/
    └── Controller.java (thin orchestration)
```

## Consequences

### Positive
- **Improved Testability**: Each component can be tested in isolation
- **Better Maintainability**: Clear boundaries between concerns
- **Enhanced Debugging**: Diagnostic capabilities in each component
- **Easier Extension**: New features added without modifying existing code
- **Team Scalability**: Different developers can work on different components

### Negative
- **Initial Complexity**: More files and packages to navigate
- **Migration Effort**: Significant refactoring work required
- **Learning Curve**: Team needs to understand new structure
- **Potential Over-engineering**: Risk of creating too many small classes

### Mitigation
- Use consistent patterns to reduce cognitive load
- Provide comprehensive documentation and examples
- Implement gradually with feature flags
- Monitor metrics to ensure improvements

## References
- Martin, Robert C. "Clean Code: A Handbook of Agile Software Craftsmanship"
- Single Responsibility Principle (SOLID)
- Domain-Driven Design patterns