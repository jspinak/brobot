# ConfigMetadataEditor Refactoring Guide

## Overview
Refactoring ConfigMetadataEditor (720 lines) to follow Single Responsibility Principle by extracting specialized services.

## Current State Analysis

### Responsibilities
1. **UI Creation** - Building forms, toolbars, status bars
2. **Data Management** - Loading/saving configuration metadata
3. **JSON Processing** - Parsing and updating JSON config files
4. **State Tracking** - Managing unsaved changes
5. **Event Handling** - Config change notifications
6. **Field Management** - Dynamic metadata fields
7. **Validation** - User input validation and dialogs

### Issues
- Mixed UI and business logic
- JSON manipulation using regex (error-prone)
- Tight coupling between components
- Limited testability
- Hard to extend with new metadata types

## Target Architecture

### Services to Extract

#### 1. ConfigMetadataService
**Responsibility**: Manage configuration metadata operations
- Load configuration metadata
- Save configuration changes
- Update project config files
- Validate metadata

#### 2. JsonConfigHandler
**Responsibility**: Handle JSON configuration operations
- Parse JSON safely
- Update JSON values
- Validate JSON structure
- Handle nested properties

#### 3. MetadataFormFactory
**Responsibility**: Create and manage form UI components
- Build form sections
- Create metadata fields
- Handle field layouts
- Apply styling

#### 4. FormStateManager
**Responsibility**: Track form state and changes
- Monitor field changes
- Track unsaved modifications
- Handle change confirmations
- Manage form enable/disable

#### 5. MetadataFieldRegistry
**Responsibility**: Manage metadata field definitions
- Register field types
- Create field instances
- Validate field values
- Handle custom fields

### ConfigMetadataEditor (Refactored)
**Responsibility**: Orchestrate metadata editing
- Coordinate services
- Handle user interactions
- Manage UI lifecycle
- Publish events

## Migration Steps

### Phase 1: Extract JsonConfigHandler
1. Create JsonConfigHandler service
2. Move JSON parsing/updating logic
3. Replace regex with proper JSON library
4. Add comprehensive error handling

### Phase 2: Extract ConfigMetadataService
1. Create ConfigMetadataService
2. Move load/save operations
3. Move project config update logic
4. Add transaction support

### Phase 3: Extract FormStateManager
1. Create FormStateManager
2. Move change tracking logic
3. Move confirmation dialogs
4. Add state persistence

### Phase 4: Extract MetadataFormFactory
1. Create MetadataFormFactory
2. Move UI creation methods
3. Implement builder pattern
4. Add theming support

### Phase 5: Extract MetadataFieldRegistry
1. Create MetadataFieldRegistry
2. Define field type interface
3. Move field creation logic
4. Add field validation

### Phase 6: Refactor ConfigMetadataEditor
1. Remove extracted logic
2. Inject dependencies
3. Implement orchestration
4. Add proper error handling

## Implementation Details

### JsonConfigHandler
```java
@Service
public class JsonConfigHandler {
    public Optional<String> extractValue(String json, String key);
    public String updateValue(String json, String key, String value);
    public boolean validateJson(String json);
    public Map<String, Object> parseToMap(String json);
}
```

### ConfigMetadataService
```java
@Service
public class ConfigMetadataService {
    public ConfigMetadata loadMetadata(ConfigEntry config);
    public void saveMetadata(ConfigEntry config, ConfigMetadata metadata);
    public void updateProjectConfig(Path configPath, Map<String, String> updates);
    public ValidationResult validateMetadata(ConfigMetadata metadata);
}
```

### FormStateManager
```java
@Service
public class FormStateManager {
    public void trackField(Node field, String key);
    public boolean hasUnsavedChanges();
    public void markAsModified();
    public void resetModifiedState();
    public boolean confirmDiscardChanges();
}
```

### MetadataFormFactory
```java
@Service
public class MetadataFormFactory {
    public TitledPane createProjectMetadataSection();
    public TitledPane createAdditionalMetadataSection();
    public TitledPane createFilePathsSection();
    public TextField createMetadataField(String label, String key);
}
```

### MetadataFieldRegistry
```java
@Service
public class MetadataFieldRegistry {
    public void registerFieldType(String type, FieldDefinition definition);
    public Control createField(String type, String key, Object value);
    public boolean validateField(String type, Object value);
    public List<FieldDefinition> getAvailableFields();
}
```

## Testing Strategy

### Unit Tests
- JsonConfigHandler: JSON parsing, updating, validation
- ConfigMetadataService: Load/save operations, validation
- FormStateManager: Change tracking, state management
- MetadataFormFactory: UI component creation
- MetadataFieldRegistry: Field creation, validation

### Integration Tests
- End-to-end metadata editing
- Configuration persistence
- Form state management
- Event publishing

## Benefits

1. **Single Responsibility** - Each service has one clear purpose
2. **Testability** - Services can be tested independently
3. **Reusability** - Services can be used in other contexts
4. **Maintainability** - Easier to modify specific aspects
5. **Extensibility** - Easy to add new field types
6. **Reliability** - Proper JSON handling
7. **Performance** - Optimized operations

## Risks and Mitigation

### Risk: Breaking existing functionality
**Mitigation**: Comprehensive test coverage before refactoring

### Risk: Performance degradation
**Mitigation**: Profile before and after, optimize critical paths

### Risk: Complex dependency management
**Mitigation**: Use Spring's dependency injection properly

### Risk: UI responsiveness
**Mitigation**: Keep UI operations on JavaFX thread

## Success Metrics

- Reduce ConfigMetadataEditor from 720 to ~200 lines
- Achieve 80%+ test coverage
- No regression in functionality
- Improved error handling
- Better JSON processing reliability