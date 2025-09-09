# Main Branch Collaboration Strategy for 5 Agents

## The Challenge
Having 5 agents work directly on the main branch poses significant risks:
- **Merge conflicts** (estimated 10-15 per day)
- **Test instability** during development
- **No isolation** for experimental changes
- **Difficult rollback** of problematic changes

## Recommended Approach: Rapid Integration Model

### Option 1: Short-Lived Feature Branches (RECOMMENDED)
**Duration**: 2-4 hours per branch
**Process**: Create → Develop → Test → Merge → Delete

```bash
# Agent 1 starts work (9 AM)
git checkout -b test/action-region-operations
# ... write 5-10 tests ...
git add .
git commit -m "test(action.region): Add region operation tests"
git checkout main
git pull origin main
git merge test/action-region-operations
git push origin main
git branch -d test/action-region-operations
# Total time: 2-3 hours

# Agent 1 starts next batch (12 PM)
git checkout -b test/action-find-scene
# ... repeat ...
```

**Benefits**:
- Changes reach main quickly (2-4 times daily per agent)
- Conflicts are small and manageable
- Each merge is tested and stable
- Main branch stays functional

### Option 2: Direct Main Branch with Strict Rules
If you absolutely must work on main directly, implement these rules:

#### Mandatory Practices

**1. Atomic Commits**
```bash
# GOOD: One test class per commit
git add src/test/java/brobot/action/RegionTest.java
git commit -m "test(action): Add RegionTest with 15 test cases"

# BAD: Multiple unrelated changes
git add .
git commit -m "Added various tests"
```

**2. Pull Before Every Push**
```bash
# ALWAYS do this sequence
git pull --rebase origin main
./gradlew test  # Ensure tests pass
git push origin main
```

**3. File Locking Convention**
Create `.current-work.md` in project root:

```markdown
# Current Work Assignments (Update before starting work)

## Active Files (DO NOT TOUCH)
- `RegionTest.java` - Agent 1 (10:00 AM - 12:00 PM)
- `HistoryTest.java` - Agent 2 (10:00 AM - 11:00 AM)
- `LoggingTest.java` - Agent 3 (09:00 AM - 11:30 AM)

## Scheduled Work
- `build.gradle` - Agent 5 (2:00 PM) - adding test dependencies
```

**4. Micro-Commits**
- Commit every 15-30 minutes
- Push every hour
- Never hold local changes > 2 hours

### Option 3: Hybrid Approach (BALANCED)

**Main Branch + Daily Integration Branches**

```bash
# Morning: Create daily branch
git checkout -b daily/2025-01-15
git push origin daily/2025-01-15

# All agents work on daily branch
# Agent 1
git checkout daily/2025-01-15
# ... work ...
git push origin daily/2025-01-15

# End of day: Integrate to main
git checkout main
git merge daily/2025-01-15
./gradlew test
git push origin main
```

## Conflict Resolution Strategy

### Package Ownership Map
To minimize conflicts, assign clear ownership:

```yaml
Agent 1:
  primary:
    - io.github.jspinak.brobot.action.internal.*
    - io.github.jspinak.brobot.action.basic.*
  shared-files:
    - build.gradle (coordinate changes)
    - BrobotTestBase.java (no modifications)

Agent 2:
  primary:
    - io.github.jspinak.brobot.tools.actionhistory.*
    - io.github.jspinak.brobot.tools.history.*
  shared-files:
    - build.gradle (coordinate changes)

Agent 3:
  primary:
    - io.github.jspinak.brobot.logging.*
    - io.github.jspinak.brobot.tools.logging.*
  shared-files:
    - logback-test.xml (coordinate changes)

Agent 4:
  primary:
    - io.github.jspinak.brobot.tools.testing.*
    - io.github.jspinak.brobot.annotations.*
  shared-files:
    - TestDataFactory.java (extend only)

Agent 5:
  primary:
    - io.github.jspinak.brobot.runner.json.*
    - io.github.jspinak.brobot.config.*
  shared-files:
    - application-test.properties
```

### Conflict Resolution Rules

**1. Build File Conflicts (build.gradle)**
```gradle
// Use sections with clear ownership comments
dependencies {
    // === Agent 1 Test Dependencies ===
    testImplementation 'org.mockito:mockito-inline:5.2.0'
    
    // === Agent 2 Test Dependencies ===
    testImplementation 'org.assertj:assertj-core:3.24.2'
    
    // === Agent 3 Test Dependencies ===
    testImplementation 'ch.qos.logback:logback-classic:1.4.14'
    
    // === Agent 4 Test Dependencies ===
    testImplementation 'org.junit.jupiter:junit-jupiter-params:5.10.1'
    
    // === Agent 5 Test Dependencies ===
    testImplementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3'
}
```

**2. Shared Test Utilities**
```java
// TestDataFactory.java - APPEND ONLY, NO MODIFICATIONS
public class TestDataFactory {
    // === Agent 1 Factories ===
    public static StateImage createStateImage() { }
    
    // === Agent 2 Factories ===
    public static ActionHistory createActionHistory() { }
    
    // === Agent 3 Factories ===
    public static LogEntry createLogEntry() { }
    
    // === Agent 4 Factories ===
    public static MockScenario createMockScenario() { }
    
    // === Agent 5 Factories ===
    public static JsonConfig createJsonConfig() { }
}
```

## Communication Protocol for Main Branch Work

### Slack/Discord Channels Structure
```
#coverage-main-branch
  ├── #agent-1-actions
  ├── #agent-2-history
  ├── #agent-3-logging
  ├── #agent-4-mocking
  ├── #agent-5-config
  └── #merge-coordination
```

### Before Starting Work
```markdown
@channel Starting work on:
- Package: `io.github.jspinak.brobot.action.internal.region`
- Files: Creating new `RegionOperationsTest.java`
- Estimated time: 2 hours
- Potential conflicts: None expected
```

### Before Modifying Shared Files
```markdown
@channel Need to modify shared file:
- File: `build.gradle`
- Change: Adding mockito-inline dependency
- Line numbers: 45-47
- Duration: 5 minutes
⏰ Please hold commits to build.gradle until 10:45 AM
```

### After Pushing to Main
```markdown
@channel Pushed to main:
- Commit: abc123f
- Tests added: 25
- Coverage impact: +1.2%
- All tests passing: ✅
```

## Continuous Integration Rules

### GitHub Actions Configuration
```yaml
name: Parallel Test Execution
on:
  push:
    branches: [main]
  
jobs:
  test-agent-1:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: ./gradlew test --tests "io.github.jspinak.brobot.action.*"
      
  test-agent-2:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: ./gradlew test --tests "io.github.jspinak.brobot.tools.history.*"
      
  # ... similar for other agents
  
  coverage-gate:
    needs: [test-agent-1, test-agent-2, test-agent-3, test-agent-4, test-agent-5]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: ./gradlew jacocoTestReport
      - run: |
          coverage=$(grep -o 'Total[^>]*>[^<]*%' build/jacocoHtml/index.html | grep -o '[0-9]\+')
          if [ $coverage -lt 23 ]; then
            echo "Coverage dropped below 23%!"
            exit 1
          fi
```

## Time Zone Coordination

If agents are in different time zones:

```markdown
# Work Schedule (UTC)
Agent 1 (PST):  15:00-23:00 UTC
Agent 2 (EST):  13:00-21:00 UTC  
Agent 3 (CET):  08:00-16:00 UTC
Agent 4 (IST):  04:00-12:00 UTC
Agent 5 (JST):  00:00-08:00 UTC

# Overlap Windows (for coordination)
- 13:00-16:00 UTC: Agents 1,2,3 overlap
- 08:00-12:00 UTC: Agents 3,4 overlap
- No full team overlap (use async communication)
```

## Emergency Procedures

### If Main Branch Breaks
```bash
# Immediate rollback
git revert HEAD
git push origin main

# Notify team
@channel Main branch broken by commit abc123f
Reverted with commit def456g
Issue: [describe problem]
```

### If Massive Conflict Occurs
```bash
# Create recovery branch
git checkout -b recovery/main-backup
git push origin recovery/main-backup

# Reset main to last known good
git checkout main
git reset --hard <last-good-commit>
git push --force-with-lease origin main
```

## Success Metrics for Main Branch Work

### Daily Metrics
- **Commits per day**: 20-30 (4-6 per agent)
- **Conflicts per day**: < 5
- **Build failures**: < 2
- **Coverage regression**: 0

### Weekly Metrics
- **Coverage increase**: +10-15%
- **Tests added**: 700-1000
- **Average PR size**: < 200 lines
- **Time to resolve conflicts**: < 15 minutes

## Recommendation Summary

**For 5 agents working toward main branch:**

1. **BEST**: Short-lived feature branches (2-4 hours) with frequent merges
2. **ACCEPTABLE**: Direct main with strict rules and coordination
3. **GOOD COMPROMISE**: Daily integration branches

**Critical Success Factors:**
- Clear package ownership
- Frequent communication
- Atomic commits
- Automated CI/CD gates
- Established conflict resolution process

The key is **frequent integration** (multiple times per day) rather than **continuous development** on main, which minimizes conflicts while keeping the main branch current.