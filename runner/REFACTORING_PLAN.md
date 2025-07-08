# Brobot Runner Refactoring Plan

## Overview
This document outlines the comprehensive refactoring strategy for the Brobot Runner module, focusing on applying the Single Responsibility Principle (SRP) to improve modularity, maintainability, and fix UI styling issues.

## Key Principles
1. **Single Responsibility**: Each class should have one reason to change
2. **Composition over Inheritance**: Use composition to build complex components from simpler ones
3. **Dependency Injection**: Use Spring for loose coupling between components
4. **Consistent Styling**: Use base components for consistent UI appearance

## Phase 1: Base Component Library (✅ COMPLETED)
Created foundational UI components:
- `BrobotPanel`: Base panel with consistent styling
- `BrobotCard`: Card component using AtlantFX styling
- `GridBuilder`: Utility to prevent label overlapping issues

## Phase 2: ResourceMonitorPanel Refactoring (🔄 IN PROGRESS)
Decomposed the 400+ line class into:
- `ResourceMonitoringService`: Business logic for monitoring
- `ResourceChartPanel`: Chart visualization component
- `CacheManagementPanel`: Cache control UI
- `SessionManagementPanel`: Session management UI

## Phase 3: ConfigMetadataEditor Refactoring (719 lines)

### Current Issues:
- Mixed responsibilities: JSON handling, UI building, validation
- Tightly coupled to file operations
- Hard to test business logic

### Proposed Structure:
```
config/
├── services/
│   ├── ConfigJsonService.java          # JSON parsing/serialization
│   ├── ConfigValidationService.java    # Validation logic
│   └── ConfigPersistenceService.java   # File operations
├── builders/
│   ├── ConfigFormBuilder.java          # Form construction
│   └── ValidationResultBuilder.java    # Result display
├── models/
│   ├── ConfigFormModel.java           # Form data model
│   └── ValidationResult.java          # Validation result model
└── ConfigMetadataEditor.java          # Coordinator (150 lines)
```

### Key Improvements:
1. **Separation of Concerns**: JSON handling separate from UI
2. **Testability**: Can unit test validation without UI
3. **Reusability**: Form builder can be used elsewhere
4. **Maintainability**: Changes to validation don't affect UI

## Phase 4: LogViewerPanel Refactoring (649 lines)

### Current Issues:
- Mixed log parsing, filtering, exporting, and visualization
- Complex state management
- Performance issues with large logs

### Proposed Structure:
```
log/
├── services/
│   ├── LogParsingService.java         # Parse log entries
│   ├── LogFilterService.java          # Filter logic
│   └── LogExportService.java          # Export functionality
├── components/
│   ├── LogTableView.java              # Table display
│   ├── LogFilterPanel.java            # Filter controls
│   ├── LogVisualizationPanel.java    # Charts/graphs
│   └── LogSearchBar.java              # Search functionality
├── models/
│   ├── LogEntry.java                  # Log data model
│   ├── LogFilter.java                 # Filter criteria
│   └── LogStatistics.java            # Statistics model
└── LogViewerPanel.java                # Coordinator (100 lines)
```

### Key Improvements:
1. **Performance**: Virtual scrolling for large logs
2. **Modularity**: Each component can be tested independently
3. **Extensibility**: Easy to add new export formats or visualizations

## Phase 5: UnifiedAutomationPanel Refactoring (564 lines)

### Current Issues:
- Button creation mixed with business logic
- Status monitoring tightly coupled to UI
- Complex event handling

### Proposed Structure:
```
automation/
├── factories/
│   ├── AutomationButtonFactory.java   # Create UI buttons
│   └── StatusIndicatorFactory.java    # Status UI components
├── services/
│   ├── AutomationStatusService.java   # Monitor automation state
│   ├── AutomationControlService.java  # Control automation
│   └── AutomationEventService.java    # Event handling
├── components/
│   ├── AutomationControlBar.java      # Control buttons
│   ├── AutomationStatusPanel.java     # Status display
│   └── AutomationProgressBar.java     # Progress indicator
└── UnifiedAutomationPanel.java        # Coordinator (100 lines)
```

### Key Improvements:
1. **Flexibility**: Easy to add new automation controls
2. **Consistency**: All buttons created through factory
3. **Reactivity**: Status updates through event system

## Phase 6: SessionManager Refactoring (543 lines)

### Current Issues:
- Mixed session state, persistence, and autosave logic
- Complex lifecycle management
- Hard to extend session functionality

### Proposed Structure:
```
session/
├── services/
│   ├── SessionPersistenceService.java # Save/load sessions
│   ├── SessionStateService.java       # Session state management
│   ├── SessionAutosaveService.java    # Autosave functionality
│   └── SessionValidationService.java  # Validate session data
├── models/
│   ├── SessionState.java              # Session state model
│   ├── SessionMetadata.java           # Session metadata
│   └── SessionSnapshot.java           # Point-in-time capture
├── repositories/
│   └── SessionRepository.java         # Data access layer
└── SessionManager.java                # Coordinator (150 lines)
```

### Key Improvements:
1. **Reliability**: Separate autosave service with retry logic
2. **Scalability**: Repository pattern for future DB support
3. **Testability**: Mock services for unit testing

## Phase 7: ErrorHandler Refactoring (488 lines)

### Current Issues:
- Mixed error strategies, metrics, and enrichment
- Hard to add new error handling strategies
- Complex error categorization logic

### Proposed Structure:
```
error/
├── strategies/
│   ├── ErrorHandlingStrategy.java     # Strategy interface
│   ├── RetryStrategy.java             # Retry logic
│   ├── FallbackStrategy.java          # Fallback behavior
│   └── LoggingStrategy.java           # Error logging
├── services/
│   ├── ErrorMetricsService.java       # Track error metrics
│   ├── ErrorEnrichmentService.java    # Add context to errors
│   └── ErrorNotificationService.java  # User notifications
├── models/
│   ├── ErrorContext.java              # Error context model
│   └── ErrorMetrics.java              # Metrics model
└── ErrorHandler.java                  # Coordinator (100 lines)
```

### Key Improvements:
1. **Extensibility**: Easy to add new error strategies
2. **Observability**: Dedicated metrics service
3. **Context**: Rich error context for debugging

## Implementation Strategy

### Priority Order:
1. **High Priority**: ConfigMetadataEditor, LogViewerPanel (user-facing, high impact)
2. **Medium Priority**: UnifiedAutomationPanel, SessionManager (core functionality)
3. **Low Priority**: ErrorHandler (infrastructure)

### Testing Strategy:
1. Unit tests for each service
2. Integration tests for coordinators
3. UI tests for components

### Migration Approach:
1. Create new structure alongside old code
2. Gradually migrate functionality
3. Deprecate old code
4. Remove deprecated code after verification

## Expected Benefits

1. **Reduced Complexity**: No class over 200 lines
2. **Better Testing**: 80%+ unit test coverage possible
3. **Easier Maintenance**: Clear separation of concerns
4. **Improved Performance**: Optimized components
5. **Consistent UI**: Base components ensure uniform styling
6. **Fixed Label Overlapping**: GridBuilder prevents layout issues

## Next Steps

1. Complete ResourceMonitorPanel refactoring
2. Start ConfigMetadataEditor decomposition
3. Create unit tests for new services
4. Update documentation
5. Train team on new architecture