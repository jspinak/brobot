# LogViewerPanel Refactoring Guide

## Overview
Refactoring LogViewerPanel (649 lines) to follow Single Responsibility Principle by extracting specialized services.

## Current State Analysis

### Responsibilities
1. **UI Creation** - Building filter bar, table, detail panel, status bar
2. **State Visualization** - Drawing state transitions (inner class)
3. **View Model** - LogEntryViewModel (inner class) 
4. **Event Handling** - EventBus subscriptions and processing
5. **Export** - Text and CSV export functionality
6. **Filter Management** - Date range, level, type, search filtering
7. **Icon Management** - Loading and caching icons

### Issues
- God class with 649 lines handling multiple concerns
- Two large inner classes (StateVisualizationPanel, LogEntryViewModel)
- Export logic embedded in UI component
- Complex filter logic mixed with UI
- Duplicate functionality with AtlantaLogsPanel

## Target Architecture

### Services to Extract

#### 1. StateVisualizationService
**Responsibility**: Handle state transition visualization
- Draw state nodes
- Draw transition arrows
- Calculate layouts
- Manage canvas updates
- Support multiple visualization styles

#### 2. LogEntryViewModelFactory
**Responsibility**: Create and manage view models
- Transform LogData to view model
- Transform LogEvent to view model
- Format detailed text
- Provide property bindings

#### 3. LogFilterPanelFactory
**Responsibility**: Create filter UI components
- Create search field
- Create type/level filters
- Create date pickers
- Configure filter bindings
- Provide filter presets

#### 4. LogDetailPanelService
**Responsibility**: Manage log detail display
- Format log details
- Update detail views
- Handle tab management
- Coordinate with state visualization

#### 5. Use Existing Services
- **LogExportService** - Already extracted
- **LogFilterService** - Already extracted
- **LogTableFactory** - Already extracted
- **LogEventAdapter** - Already extracted
- **LogDataRepository** - Already extracted

### LogViewerPanel (Refactored)
**Responsibility**: Orchestrate log viewing
- Coordinate services
- Handle user interactions
- Manage panel lifecycle
- Update UI

## Migration Steps

### Phase 1: Extract StateVisualizationService
1. Move StateVisualizationPanel to separate file
2. Create service interface
3. Add layout algorithms
4. Support different visualization styles
5. Add animation support

### Phase 2: Extract LogEntryViewModelFactory
1. Move LogEntryViewModel to models package
2. Create factory service
3. Add transformation methods
4. Implement caching if needed
5. Add validation

### Phase 3: Extract LogFilterPanelFactory
1. Create factory for filter components
2. Move filter creation logic
3. Add filter preset support
4. Implement filter persistence
5. Add advanced filters

### Phase 4: Extract LogDetailPanelService
1. Create detail panel service
2. Move formatting logic
3. Add template support
4. Implement export from details
5. Add customization options

### Phase 5: Refactor LogViewerPanel
1. Inject all dependencies
2. Remove extracted logic
3. Implement thin orchestration
4. Add error handling
5. Update documentation

## Implementation Details

### StateVisualizationService
```java
@Service
public class StateVisualizationService {
    public Node createVisualization(VisualizationType type);
    public void updateStates(List<String> from, List<String> to);
    public void setCurrentState(String state);
    public void clearVisualization();
    public void setTheme(VisualizationTheme theme);
}
```

### LogEntryViewModelFactory
```java
@Service
public class LogEntryViewModelFactory {
    public LogEntryViewModel createFromLogData(LogData data);
    public LogEntryViewModel createFromLogEvent(LogEvent event);
    public List<LogEntryViewModel> createBatch(List<LogData> data);
    public String formatDetailedText(LogEntryViewModel model);
}
```

### LogFilterPanelFactory
```java
@Service
public class LogFilterPanelFactory {
    public Node createFilterBar(FilterConfiguration config);
    public void bindFilters(FilterConfiguration config, Consumer<FilterCriteria> onChange);
    public void applyPreset(FilterPreset preset);
    public void saveCurrentAsPreset(String name);
}
```

### LogDetailPanelService
```java
@Service
public class LogDetailPanelService {
    public Node createDetailPanel();
    public void updateDetails(LogEntryViewModel entry);
    public void clearDetails();
    public String exportDetails(ExportFormat format);
}
```

## Consolidation Opportunities

Since we have both AtlantaLogsPanel and LogViewerPanel:

1. **Identify Common Functionality**
   - Both handle log display
   - Both have filtering
   - Both support export
   - Both handle events

2. **Create Unified Solution**
   - Use same services for both
   - Create configurable panel
   - Support different layouts
   - Maintain backward compatibility

3. **Migration Path**
   - Update both to use same services
   - Create adapter if needed
   - Deprecate one implementation
   - Provide migration guide

## Benefits

1. **Reusability** - State visualization usable elsewhere
2. **Testability** - Each service independently testable
3. **Maintainability** - Clear separation of concerns
4. **Extensibility** - Easy to add new visualizations
5. **Performance** - Optimized rendering
6. **Consistency** - Shared services with AtlantaLogsPanel

## Risks and Mitigation

### Risk: Breaking existing functionality
**Mitigation**: Comprehensive testing, gradual migration

### Risk: Performance degradation
**Mitigation**: Profile visualization rendering, optimize layouts

### Risk: Complex dependency injection
**Mitigation**: Clear service boundaries, proper documentation

## Success Metrics

- Reduce LogViewerPanel from 649 to ~250 lines
- Extract 2 inner classes to separate files
- Achieve 80%+ test coverage on services
- Improve visualization performance
- Enable new visualization types