# ConfigMetadataEditor Migration Guide

## Overview
The ConfigMetadataEditor has been refactored from a 719-line monolithic class into a modular architecture with separate services. The new RefactoredConfigMetadataEditor is only ~270 lines and coordinates between specialized services.

## Architecture Changes

### Old Structure (Monolithic)
```
ConfigMetadataEditor (719 lines)
├── JSON operations (regex-based)
├── File I/O
├── Validation logic
├── UI construction
├── State management
└── Event handling
```

### New Structure (Modular)
```
RefactoredConfigMetadataEditor (~270 lines) - Coordinator
├── ConfigJsonService - JSON operations with Jackson
├── ConfigFileService - File I/O with backup/restore
├── ConfigValidationService - Validation rules
├── ConfigFormBuilder - UI construction
├── ConfigStateManager - State and change tracking
└── Models
    ├── ConfigFormModel - Form data
    ├── ConfigData - Configuration data
    └── ValidationResult - Validation results
```

## Migration Steps

### 1. Update Spring Configuration

Replace the old ConfigMetadataEditor bean with the new services:

```java
@Configuration
public class UIConfiguration {
    
    @Bean
    public ConfigMetadataEditor configMetadataEditor(
            EventBus eventBus,
            AutomationProjectManager projectManager,
            ConfigJsonService jsonService,
            ConfigFileService fileService,
            ConfigValidationService validationService,
            ConfigFormBuilder formBuilder,
            ConfigStateManager stateManager) {
        
        // Use RefactoredConfigMetadataEditor
        return new RefactoredConfigMetadataEditor(
            eventBus, projectManager, jsonService, 
            fileService, validationService, formBuilder, stateManager
        );
    }
}
```

### 2. Update References

Find all references to ConfigMetadataEditor and ensure they still work:

```java
// Old usage
configEditor.loadConfiguration(configEntry);
configEditor.hasUnsavedChanges();
configEditor.clear();

// New usage - same API, works without changes!
configEditor.loadConfiguration(configEntry);
configEditor.hasUnsavedChanges();
configEditor.clear();
```

### 3. Test JSON Operations

The new implementation uses Jackson instead of regex:

**Old (regex-based):**
```java
private String extractJsonValue(String json, String key) {
    Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
    // Error-prone regex operations
}
```

**New (Jackson-based):**
```java
// Handled by ConfigJsonService
jsonService.extractValue(jsonNode, "app.version");
jsonService.updateValue(jsonNode, "app.version", "2.0.0");
```

### 4. Verify File Operations

The new implementation includes automatic backups:

**Features:**
- Automatic backup before save
- Keep last 10 backups
- Restore from backup on save failure
- Export to properties format

### 5. Update Tests

Create unit tests for each service:

```java
@Test
public void testConfigValidation() {
    ConfigValidationService validator = new ConfigValidationService();
    
    ValidationResult result = validator.validateVersion("1.0.0");
    assertTrue(result.isValid());
    
    result = validator.validateVersion("invalid");
    assertFalse(result.isValid());
}

@Test
public void testJsonOperations() {
    ConfigJsonService jsonService = new ConfigJsonService();
    
    JsonNode node = jsonService.updateValue(null, "name", "test");
    assertEquals("test", jsonService.extractValue(node, "name"));
}
```

## Benefits of Migration

### 1. **Better Maintainability**
- Each service has a single responsibility
- Easier to understand and modify
- Clear separation of concerns

### 2. **Improved Testability**
- Services can be unit tested independently
- Mock services for integration tests
- Better test coverage possible

### 3. **Enhanced Features**
- Automatic file backups
- Better JSON handling with Jackson
- Comprehensive validation
- State management with undo support

### 4. **Reusability**
- Services can be used by other components
- ConfigValidationService for any validation needs
- ConfigJsonService for any JSON operations

### 5. **Better Error Handling**
- Each service handles its specific errors
- Backup/restore on file operation failures
- Clear validation messages

## Rollback Plan

If issues arise, you can temporarily use both implementations:

1. Keep old ConfigMetadataEditor as `LegacyConfigMetadataEditor`
2. Use feature flag to switch between implementations
3. Gradually migrate and test
4. Remove legacy code after verification

## Common Issues and Solutions

### Issue 1: Spring Dependency Injection
**Problem:** Services not autowired correctly
**Solution:** Ensure all services are annotated with `@Service` or `@Component`

### Issue 2: JSON Format Changes
**Problem:** Jackson may format JSON differently than regex
**Solution:** The functionality remains the same, formatting differences are cosmetic

### Issue 3: Validation Differences
**Problem:** New validation may be stricter
**Solution:** Review validation rules in ConfigValidationService

## Next Steps

1. Run existing tests with new implementation
2. Add integration tests for the refactored editor
3. Monitor for any edge cases
4. Document any custom configurations
5. Remove old ConfigMetadataEditor after verification period