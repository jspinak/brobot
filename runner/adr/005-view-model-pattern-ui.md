# ADR-005: View Model Pattern for UI Components

## Status
Accepted

## Context
Current UI implementation has several issues:
- Business logic mixed with JavaFX components
- Direct service dependencies in UI classes
- Difficult to test UI logic
- Tight coupling between view and business logic
- No clear separation of concerns

Example problems:
- `ConfigurationPanel` directly calls services
- UI components manage state and persistence
- Testing requires JavaFX runtime

## Decision
Implement MVVM (Model-View-ViewModel) pattern for all UI components:

### 1. View Model Interface
```java
public interface ViewModel {
    void initialize();
    void dispose();
    DiagnosticInfo getDiagnostics();
}

public interface ConfigurationViewModel extends ViewModel {
    // Observable properties
    ReadOnlyStringProperty projectPathProperty();
    ReadOnlyBooleanProperty loadingProperty();
    ReadOnlyBooleanProperty validProperty();
    ObservableList<ValidationError> validationErrors();
    
    // Commands
    Command loadConfigurationCommand();
    Command saveConfigurationCommand();
    Command validateCommand();
}
```

### 2. View Implementation
```java
public class ConfigurationView extends BrobotCard {
    private final ConfigurationViewModel viewModel;
    
    public ConfigurationView(ConfigurationViewModel viewModel) {
        this.viewModel = viewModel;
        setupUI();
        bindToViewModel();
    }
    
    private void bindToViewModel() {
        // Bind UI to view model properties
        pathField.textProperty().bindBidirectional(
            viewModel.projectPathProperty()
        );
        loadButton.disableProperty().bind(
            viewModel.loadingProperty()
        );
    }
}
```

### 3. View Model Implementation
```java
@Component
@Scope("prototype")
public class ConfigurationViewModelImpl implements ConfigurationViewModel {
    // Services injected here, not in view
    private final ConfigurationService configService;
    private final ValidationService validationService;
    
    // Observable properties
    private final StringProperty projectPath = new SimpleStringProperty();
    private final BooleanProperty loading = new SimpleBooleanProperty();
    
    // Commands with business logic
    private final Command loadCommand = new AsyncCommand(
        this::loadConfiguration,
        loading.not()
    );
}
```

### 4. Command Pattern
```java
public interface Command {
    void execute();
    ReadOnlyBooleanProperty canExecuteProperty();
    ReadOnlyBooleanProperty executingProperty();
}

public class AsyncCommand implements Command {
    private final Supplier<CompletableFuture<?>> action;
    private final BooleanBinding canExecute;
    private final BooleanProperty executing;
}
```

## Consequences

### Positive
- **Testability**: View models can be tested without UI
- **Separation**: Clear boundary between UI and logic
- **Reusability**: View models can be reused
- **Maintainability**: Changes isolated to appropriate layer
- **Data Binding**: Reactive UI updates

### Negative
- **Complexity**: Additional abstraction layer
- **Boilerplate**: More code for simple UIs
- **Learning Curve**: Team needs MVVM knowledge
- **Memory**: Observable properties overhead

### Mitigation
- Provide base classes and utilities
- Create code generators for common patterns
- Use for complex UIs only (simple UIs can be direct)
- Comprehensive examples and documentation

## Implementation Examples

### 1. List View Model
```java
public interface LogViewerViewModel extends ViewModel {
    ObservableList<LogEntryViewModel> logEntries();
    ReadOnlyIntegerProperty totalCountProperty();
    ReadOnlyBooleanProperty hasMoreProperty();
    
    Command refreshCommand();
    Command loadMoreCommand();
    Command exportCommand();
    Command clearCommand();
}
```

### 2. Form View Model
```java
public interface FormViewModel<T> extends ViewModel {
    ObjectProperty<T> modelProperty();
    ReadOnlyBooleanProperty dirtyProperty();
    ReadOnlyBooleanProperty validProperty();
    ObservableList<ValidationMessage> validationMessages();
    
    Command saveCommand();
    Command resetCommand();
    Command validateCommand();
}
```

### 3. Testing View Models
```java
@Test
void testLoadConfiguration() {
    // Given
    ConfigurationService mockService = mock(ConfigurationService.class);
    when(mockService.load(anyString())).thenReturn(testConfig);
    
    ConfigurationViewModel viewModel = new ConfigurationViewModelImpl(
        mockService, validationService
    );
    
    // When
    viewModel.projectPathProperty().set("/test/path");
    viewModel.loadConfigurationCommand().execute();
    
    // Then
    await().untilAsserted(() -> {
        assertThat(viewModel.loadingProperty().get()).isFalse();
        assertThat(viewModel.validProperty().get()).isTrue();
    });
}
```

## Migration Strategy
1. Start with new UI components
2. Refactor complex existing UIs first
3. Simple UIs can remain as-is
4. Provide migration guide and tools

## References
- MVVM Pattern
- WPF/Silverlight Architecture
- JavaFX Property Binding
- ReactiveX Patterns