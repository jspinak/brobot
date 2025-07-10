# ADR-004: Repository Pattern for Data Access

## Status
Accepted

## Context
Current data access is scattered throughout the codebase:
- File I/O mixed with business logic
- JSON serialization in service classes
- No abstraction over storage mechanism
- Difficult to test (requires real files)
- Hard to switch storage backends

This violates SRP and makes testing complex.

## Decision
Adopt the Repository pattern for all data access:

### 1. Repository Interface
```java
public interface Repository<T, ID> {
    CompletableFuture<T> save(T entity);
    CompletableFuture<Optional<T>> findById(ID id);
    CompletableFuture<List<T>> findAll();
    CompletableFuture<Boolean> delete(ID id);
}
```

### 2. Specialized Repositories
```java
public interface SessionRepository extends Repository<Session, String> {
    CompletableFuture<List<Session>> findByStatus(SessionStatus status);
    CompletableFuture<List<Session>> findByProjectName(String projectName);
    CompletableFuture<Void> deleteExpiredSessions(Duration olderThan);
}
```

### 3. Implementation Separation
```java
// File-based implementation
@Repository
public class FileSessionRepository implements SessionRepository {
    // File I/O implementation
}

// Database implementation (future)
@Repository
@Profile("database")
public class DatabaseSessionRepository implements SessionRepository {
    // JPA/JDBC implementation
}
```

### 4. Service Layer Usage
```java
@Service
public class SessionService {
    private final SessionRepository repository; // Interface only
    
    public Session startSession(String projectName) {
        Session session = createSession(projectName);
        return repository.save(session).join();
    }
}
```

## Consequences

### Positive
- **Testability**: Easy to mock repositories
- **Flexibility**: Switch storage backends easily
- **Separation**: Business logic separate from persistence
- **Consistency**: Uniform data access patterns
- **Evolution**: Add caching, auditing transparently

### Negative
- **Abstraction**: Additional layer of indirection
- **Async Complexity**: CompletableFuture handling
- **Mapping**: Entity to storage format conversion
- **Transactions**: Complex transaction boundaries

### Mitigation
- Provide repository base classes
- Create test utilities for common scenarios
- Use Spring Data patterns where applicable
- Document transaction boundaries clearly

## Implementation Guidelines

### 1. Repository Structure
```
repository/
├── interfaces/
│   ├── SessionRepository.java
│   ├── ConfigurationRepository.java
│   └── LogRepository.java
├── file/
│   ├── FileSessionRepository.java
│   └── FileRepositoryBase.java
└── memory/
    └── InMemorySessionRepository.java (for testing)
```

### 2. Testing Pattern
```java
@Test
void testWithMockRepository() {
    // Given
    SessionRepository mockRepo = mock(SessionRepository.class);
    when(mockRepo.save(any())).thenReturn(
        CompletableFuture.completedFuture(testSession)
    );
    
    // When
    SessionService service = new SessionService(mockRepo);
    Session result = service.startSession("test");
    
    // Then
    verify(mockRepo).save(argThat(s -> 
        s.getProjectName().equals("test")
    ));
}
```

### 3. Error Handling
```java
public class RepositoryException extends RuntimeException {
    private final RepositoryOperation operation;
    private final String entityType;
    private final Object entityId;
}
```

## Migration Path
1. Create repository interfaces
2. Implement file-based repositories
3. Refactor services to use repositories
4. Add in-memory implementations for tests
5. Consider database implementations

## References
- Domain-Driven Design (Eric Evans)
- Repository Pattern (Martin Fowler)
- Spring Data Repository
- Clean Architecture (Robert Martin)