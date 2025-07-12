# AutomationProjectManager Migration Guide

## Overview
This guide describes the creation and integration of the new `AutomationProjectManager` service-based architecture. Unlike SessionManager which was a refactoring, AutomationProjectManager is a new component that was referenced throughout the codebase but didn't exist.

## Architecture

### Service-Based Design
The AutomationProjectManager follows the same pattern as SessionManager with specialized services:

- **AutomationProjectManager** - Thin facade/orchestrator
- **ProjectLifecycleService** - Project creation, opening, closing
- **ProjectValidationService** - Structure and configuration validation
- **ProjectPersistenceService** - File I/O operations
- **ProjectDiscoveryService** - Project scanning and indexing
- **ProjectMigrationService** - Version migration handling

### Domain Models Created

#### AutomationProject
The core project model containing:
- Project metadata (id, name, version, author)
- File paths (project, config, images, data)
- Automation definition (buttons, settings, dependencies)
- Project state and lifecycle tracking
- History and runtime data

#### TaskButton
Represents UI buttons for automation tasks:
- Display properties (label, tooltip, icon, style)
- Execution details (taskClass, methodName, parameters)
- Layout information (row, column, spans)
- State (enabled, visible)

#### RunnerInterface
Contract for automation implementations:
- Lifecycle methods (initialize, run, pause, resume, stop)
- Task execution
- Status reporting

## Integration Steps

### 1. Add Spring Bean Configuration

Since AutomationProjectManager is annotated with `@Component`, Spring will automatically create and inject it. No additional configuration needed.

### 2. Update UI Components

Components currently trying to inject AutomationProjectManager will now work:

```java
// Previously would fail with NoSuchBeanDefinitionException
@Autowired
private AutomationProjectManager projectManager;

// Now works automatically
```

### 3. Implement UI Integration

Update panels to use the new API:

```java
// Create new project
AutomationProject project = projectManager.createProject("My Project", projectPath);

// Open existing project
AutomationProject project = projectManager.openProject(projectPath);

// Get active project
AutomationProject active = projectManager.getActiveProject();

// Add task button
TaskButton button = TaskButton.builder()
    .id("task1")
    .label("Run Task")
    .taskClass("com.example.MyTask")
    .methodName("execute")
    .style(TaskButton.ButtonStyle.PRIMARY)
    .build();
projectManager.addTaskButton(button);

// Save project
projectManager.saveProject();
```

### 4. Project Discovery

Configure project scan paths in application properties:

```properties
brobot.runner.projects.scan-paths=./projects,~/brobot-projects
brobot.runner.projects.auto-scan=true
```

Use discovery features:

```java
// Get all available projects
List<ProjectInfo> projects = projectManager.getAvailableProjects();

// Find projects by name
List<ProjectInfo> results = projectManager.getDiscoveryService()
    .findProjectsByName("test");

// Track recent projects
projectManager.getDiscoveryService()
    .recordProjectAccess(project.getId(), project.getName(), project.getProjectPath());
```

### 5. Project Validation

Validate projects before operations:

```java
ValidationResult result = projectManager.validateProject(project);
if (!result.isValid()) {
    // Handle validation errors
    result.getErrors().forEach(issue -> 
        log.error("{}: {}", issue.getCategory(), issue.getMessage())
    );
}
```

### 6. Migration Support

Projects are automatically migrated when opened:

```java
// Opens project and migrates if needed
AutomationProject project = projectManager.openProject(oldProjectPath);
```

## Common Use Cases

### Creating a New Project

```java
// Create project structure
Path projectPath = Paths.get("./projects/my-automation");
AutomationProject project = projectManager.createProject("My Automation", projectPath);

// Configure project
project.setDescription("Automates daily tasks");
project.setVersion("1.0.0");
project.setAuthor("Developer Name");

// Add automation definition
AutomationProject.AutomationDefinition automation = 
    AutomationProject.AutomationDefinition.builder()
        .mainClass("com.example.MyAutomation")
        .build();
project.setAutomation(automation);

// Add task buttons
TaskButton startButton = TaskButton.builder()
    .id("start")
    .label("Start")
    .taskClass("com.example.MyAutomation")
    .methodName("start")
    .style(TaskButton.ButtonStyle.PRIMARY)
    .row(0)
    .column(0)
    .build();
    
projectManager.addTaskButton(startButton);

// Save project
projectManager.saveProject();
```

### Loading and Running a Project

```java
// Open project
Path projectPath = Paths.get("./projects/existing");
AutomationProject project = projectManager.openProject(projectPath);

// Get automation class
String mainClass = project.getAutomation().getMainClass();

// Get task buttons for UI
List<TaskButton> buttons = project.getAutomation().getButtons();

// Update project state
project.setState(AutomationProject.ProjectState.RUNNING);
project.setLastExecuted(LocalDateTime.now());
projectManager.saveProject();
```

### Managing Recent Projects

```java
// Get recent projects
List<RecentProject> recent = projectManager.getRecentProjects();

// Pin a project
projectManager.getDiscoveryService().pinProject(project.getId());

// Clear history (except pinned)
projectManager.getDiscoveryService().clearRecentProjects();
```

## Error Handling

The new architecture provides better error handling:

```java
try {
    AutomationProject project = projectManager.openProject(projectPath);
} catch (IllegalArgumentException e) {
    // Invalid project structure
    log.error("Failed to open project: {}", e.getMessage());
} catch (RuntimeException e) {
    // Other errors (I/O, migration failures)
    log.error("Unexpected error: {}", e.getMessage());
}
```

## Benefits

1. **Separation of Concerns**: Each service has a single responsibility
2. **Testability**: Services can be tested independently
3. **Extensibility**: Easy to add new features to specific services
4. **Maintainability**: Smaller, focused classes are easier to understand
5. **Error Isolation**: Failures in one service don't affect others

## Migration from Direct File Access

If your code was directly accessing project files, migrate to use the API:

### Before
```java
// Direct file access
Path configFile = Paths.get("./project/config.json");
String json = Files.readString(configFile);
// Parse and use...
```

### After
```java
// Use AutomationProjectManager
AutomationProject project = projectManager.openProject(projectPath);
Map<String, Object> config = project.getConfiguration();
// Use configuration...
```

## Testing

Test with the provided test utilities:

```java
@Test
void testProjectLifecycle() {
    // Create project
    AutomationProject project = projectManager.createProject("Test", tempDir);
    assertThat(project).isNotNull();
    assertThat(project.getState()).isEqualTo(ProjectState.NEW);
    
    // Add buttons
    TaskButton button = createTestButton();
    projectManager.addTaskButton(button);
    
    // Save and reload
    projectManager.saveProject();
    projectManager.closeProject();
    
    AutomationProject reloaded = projectManager.openProject(tempDir);
    assertThat(reloaded.getAutomation().getButtons()).hasSize(1);
}
```

## Troubleshooting

### NoSuchBeanDefinitionException
Ensure the runner module is included in your Spring component scan.

### Project Not Found
Check that:
- Project path exists
- `project.json` file is present
- File permissions are correct

### Migration Failures
- Check logs for specific migration errors
- Backup files are created automatically
- Manual restore possible from `.backup` files

## Next Steps

1. Update all UI components to use AutomationProjectManager
2. Remove any hardcoded project file access
3. Implement project templates
4. Add project export/import UI
5. Create project wizards