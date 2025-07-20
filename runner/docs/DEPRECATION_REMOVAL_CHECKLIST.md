# Deprecation Removal Checklist

## When: Version 3.0

## Pre-Removal Verification

### 1. Usage Analysis
- [ ] Search codebase for any remaining usage of deprecated classes
- [ ] Check external projects that depend on Brobot Runner
- [ ] Verify all examples use refactored components
- [ ] Confirm documentation is updated

### 2. Migration Verification
- [ ] All production code uses refactored components
- [ ] All tests updated to use new architecture
- [ ] Performance metrics show no regression
- [ ] No critical bugs reported with refactored components

## Components to Remove

### UI Panels
- [ ] `AutomationPanel.java` → Replaced by `UnifiedAutomationPanel`
- [ ] `EnhancedAutomationPanel.java` → Replaced by `UnifiedAutomationPanel`
- [ ] `ResourceMonitorPanel.java` → Replaced by `RefactoredResourceMonitorPanel`
- [ ] `ConfigDetailsPanel.java` → Replaced by `RefactoredConfigDetailsPanel`
- [ ] `ExecutionDashboardPanel.java` → Replaced by `RefactoredExecutionDashboardPanel`
- [ ] `AtlantaLogsPanel.java` → Replaced by `RefactoredAtlantaLogsPanel`

### Factory Methods
- [ ] `UiComponentFactory.createLegacyAutomationPanel()`
- [ ] `UiComponentFactory.createResourceMonitorPanel()`
- [ ] `UiComponentFactory.createAtlantaLogsPanel()`

### Support Classes
- [ ] Any singleton getInstance() methods
- [ ] Direct Platform.runLater() usage in panels
- [ ] Manual label creation in panels

## Removal Process

### Step 1: Final Warning (Version 2.9)
```java
// Add to all deprecated classes
@Deprecated(since = "2.5", forRemoval = true)
@Scheduled(forRemoval = "3.0")
public class OldPanel {
    static {
        log.warn("This class will be removed in version 3.0. Please migrate to RefactoredPanel");
    }
}
```

### Step 2: Create Migration Branch
```bash
git checkout -b feature/remove-deprecated-ui-components
```

### Step 3: Remove Files
```bash
# Remove deprecated panels
rm AutomationPanel.java
rm EnhancedAutomationPanel.java
rm ResourceMonitorPanel.java
rm ConfigDetailsPanel.java
rm ExecutionDashboardPanel.java
rm AtlantaLogsPanel.java
```

### Step 4: Update UiComponentFactory
- Remove all deprecated methods
- Update JavaDoc
- Ensure no references remain

### Step 5: Update Imports
```bash
# Find and update any remaining imports
grep -r "import.*AutomationPanel" .
grep -r "import.*ResourceMonitorPanel" .
# etc...
```

### Step 6: Run Tests
```bash
./gradlew clean build
./gradlew test
```

### Step 7: Update Documentation
- [ ] Remove references to deprecated components from README
- [ ] Update API documentation
- [ ] Update migration guide to show completion
- [ ] Archive old documentation

## Post-Removal Tasks

### 1. Performance Validation
- [ ] Run performance benchmarks
- [ ] Compare with pre-removal metrics
- [ ] Document any improvements

### 2. Release Notes
```markdown
## Breaking Changes in 3.0

### Removed Deprecated UI Components
The following deprecated UI components have been removed:
- AutomationPanel → Use UnifiedAutomationPanel
- ResourceMonitorPanel → Use RefactoredResourceMonitorPanel
- [List all removed components]

### Migration Required
Projects using the removed components must update to use the refactored versions.
See MIGRATION_GUIDE.md for details.
```

### 3. Communication
- [ ] Email announcement to users
- [ ] Blog post about the refactoring benefits
- [ ] Update Stack Overflow answers if any exist

## Rollback Plan

If critical issues are discovered:

1. **Immediate Rollback**
   ```bash
   git revert <removal-commit>
   ./gradlew clean build
   ```

2. **Extend Deprecation**
   - Mark as deprecated until version 4.0
   - Address discovered issues
   - Provide additional migration support

3. **Gradual Removal**
   - Consider removing components one at a time
   - Start with least-used components
   - Monitor for issues after each removal

## Success Criteria

- [ ] Zero compilation errors after removal
- [ ] All tests pass
- [ ] No performance degradation
- [ ] No increase in memory usage
- [ ] Positive feedback from early adopters
- [ ] Smooth migration for existing users

## Notes

- Keep this checklist updated as new deprecated components are added
- Review quarterly to ensure timeline is still appropriate
- Consider user feedback before proceeding with removal
- Document any lessons learned for future refactoring projects