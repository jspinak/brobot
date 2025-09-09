# Code Coverage Improvement Plan for Brobot

## Executive Summary
This plan outlines a coordinated effort by 5 agents working on the main branch to improve Brobot's code coverage from the current **23%** to a target of **60%+** within 4 weeks.

## Current State
- **Overall Coverage: 23%** (30,042 of 125,562 instructions)
- **Branch Coverage: 17%** (1,951 of 10,956 branches)
- **Method Coverage: 29%** (1,623 of 5,419 methods)
- **Class Coverage: 37%** (290 of 769 classes)
- **Total Tests: 3,663** (all passing)

## Essential Testing Documentation

### Core Testing Guides
- **[Enhanced Mocking Guide](brobot/docs/docs/03-core-library/testing/enhanced-mocking.md)** - Comprehensive mock testing patterns
- **[Mat Testing Utilities](brobot/docs/docs/04-testing/mat-testing-utilities.md)** - OpenCV Mat testing utilities
- **[Test Utilities Guide](brobot/docs/docs/04-testing/test-utilities.md)** - General test utilities and helpers
- **[Mock Mode Guide](brobot/docs/docs/04-testing/mock-mode-guide.md)** - Complete mock mode testing documentation
- **[Integration Testing](brobot/docs/docs/04-testing/integration-testing.md)** - Integration testing patterns
- **[Unit Testing](brobot/docs/docs/04-testing/unit-testing.md)** - Unit testing best practices

### Specialized Testing Resources
- **[ActionHistory Testing](brobot/docs/docs/04-testing/actionhistory-integration-testing.md)** - Testing action history components
- **[Mock Snapshots](brobot/docs/docs/04-testing/actionhistory-mock-snapshots.md)** - Snapshot testing patterns
- **[Pattern Matching Debug](brobot/docs/docs/04-testing/debugging-pattern-matching.md)** - Testing pattern matching
- **[Motion Detection Testing](brobot/docs/docs/03-core-library/testing/motion-detection-testing.md)** - Motion detection test patterns
- **[Mixed Mode Execution](brobot/docs/docs/04-testing/mixed-mode-execution.md)** - Hybrid mock/real testing

### Migration & Reference Guides
- **[Mock Mode Migration](brobot/docs/docs/04-testing/mock-mode-migration.md)** - Migrating to mock mode
- **[Testing Strategy](brobot/docs/docs/04-testing/TESTING_STRATEGY.md)** - Overall testing strategy
- **[AI Brobot Guide](brobot/docs/docs/ai-brobot-project-creation.md)** - Complete API reference for testing

## Agent Assignments

### Agent 1: Core Action & Internal Operations Specialist
**Coverage Goal: +8-10%**

**Focus Areas:**
- `io.github.jspinak.brobot.action.internal.*` (currently 0-7% coverage)
- `io.github.jspinak.brobot.action.basic.*` (currently 0-25% coverage)

**Priority Packages:**
| Package | Current Coverage | Uncovered Instructions | Priority |
|---------|-----------------|----------------------|----------|
| `action.internal.region` | 0% | 1,396 | HIGH |
| `action.internal.find.scene` | 7% | 1,312 | HIGH |
| `action.basic.find` | 25% | 1,650 | MEDIUM |
| `action.internal.execution` | 0% | ~800 | MEDIUM |

**Key Tasks:**
- Write integration tests for region operations using patterns from [Integration Testing Guide](brobot/docs/docs/04-testing/integration-testing.md)
- Create test scenarios for scene finding algorithms
- Mock SikuliX interactions using [Enhanced Mocking Guide](brobot/docs/docs/03-core-library/testing/enhanced-mocking.md)
- Test edge cases in pattern matching using [Pattern Matching Debug Guide](brobot/docs/docs/04-testing/debugging-pattern-matching.md)

**Test Implementation Strategy:**
```java
public class RegionOperationsTest extends BrobotTestBase {
    // Use MatTestUtils for OpenCV operations
    // Follow patterns from integration-testing.md
}
```

---

### Agent 2: History & Monitoring Systems Specialist
**Coverage Goal: +6-8%**

**Focus Areas:**
- `io.github.jspinak.brobot.tools.actionhistory` (0% - 3,131 uncovered)
- `io.github.jspinak.brobot.tools.history.*` (0% - 3,925 uncovered total)
- `io.github.jspinak.brobot.navigation.monitoring` (0% - 990 uncovered)

**Priority Packages:**
| Package | Current Coverage | Uncovered Instructions | Priority |
|---------|-----------------|----------------------|----------|
| `tools.actionhistory` | 0% | 3,131 | CRITICAL |
| `tools.history.draw` | 0% | 1,577 | HIGH |
| `tools.history.performance` | 0% | 912 | MEDIUM |
| `navigation.monitoring` | 0% | 990 | MEDIUM |

**Key Tasks:**
- Implement tests using [ActionHistory Integration Testing](brobot/docs/docs/04-testing/actionhistory-integration-testing.md)
- Create mock action sequences using [Mock Snapshots Guide](brobot/docs/docs/04-testing/actionhistory-mock-snapshots.md)
- Test performance metric collection
- Verify history visualization components

**Test Implementation Strategy:**
```java
public class ActionHistoryTest extends BrobotTestBase {
    // Use patterns from actionhistory-integration-testing.md
    // Implement snapshot testing from actionhistory-mock-snapshots.md
}
```

---

### Agent 3: Logging & Visual Systems Specialist
**Coverage Goal: +5-7%**

**Focus Areas:**
- `io.github.jspinak.brobot.logging.*` (0-20% coverage)
- `io.github.jspinak.brobot.tools.logging.*` (0% coverage)
- `io.github.jspinak.brobot.util.image.debug` (0% - 941 uncovered)

**Priority Packages:**
| Package | Current Coverage | Uncovered Instructions | Priority |
|---------|-----------------|----------------------|----------|
| `tools.logging.visual` | 1% | 1,570 | HIGH |
| `tools.logging.console` | 0% | 1,292 | HIGH |
| `logging.unified` | 20% | 1,949 | MEDIUM |
| `util.image.debug` | 0% | 941 | MEDIUM |

**Key Tasks:**
- Test all log formatters and output modes
- Verify ANSI color output handling
- Test visual debugging overlays using [Mat Testing Utilities](brobot/docs/docs/04-testing/mat-testing-utilities.md)
- Create tests for console output variations

---

### Agent 4: Testing Framework & Mock Systems Specialist
**Coverage Goal: +4-5%**

**Focus Areas:**
- `io.github.jspinak.brobot.tools.testing.mock.*` (0% coverage)
- `io.github.jspinak.brobot.tools.testing.exploration` (0% - 909 uncovered)
- `io.github.jspinak.brobot.annotations` (0% - 1,041 uncovered)

**Priority Packages:**
| Package | Current Coverage | Uncovered Instructions | Priority |
|---------|-----------------|----------------------|----------|
| `tools.testing.mock.verification` | 0% | 1,039 | HIGH |
| `tools.testing.exploration` | 0% | 909 | HIGH |
| `annotations` | 0% | 1,041 | MEDIUM |
| `tools.testing.mock.scenario` | 0% | ~700 | MEDIUM |

**Key Tasks:**
- Test the mock framework itself using [Mock Mode Guide](brobot/docs/docs/04-testing/mock-mode-guide.md)
- Create meta-tests for test utilities
- Verify annotation processing
- Test exploration strategies using [Mixed Mode Execution](brobot/docs/docs/04-testing/mixed-mode-execution.md)

---

### Agent 5: Configuration & Validation Specialist
**Coverage Goal: +7-9%**

**Focus Areas:**
- `io.github.jspinak.brobot.runner.json.*` (0-19% coverage)
- `io.github.jspinak.brobot.config` (15% - needs improvement)
- `io.github.jspinak.brobot.startup` (14% - 1,668 uncovered)

**Priority Packages:**
| Package | Current Coverage | Uncovered Instructions | Priority |
|---------|-----------------|----------------------|----------|
| `runner.json.validation.business` | 1% | 2,842 | CRITICAL |
| `runner.json.validation.crossref` | 19% | 2,364 | HIGH |
| `config` | 15% | 6,243 | HIGH |
| `startup` | 14% | 1,668 | MEDIUM |

**Key Tasks:**
- Test JSON schema validation
- Verify configuration loading/parsing
- Test startup sequences using patterns from [Unit Testing Guide](brobot/docs/docs/04-testing/unit-testing.md)
- Validate cross-references in configurations

---

## Implementation Timeline

### Week 1: Foundation (Target: 28% coverage)
**All Agents:**
- Set up test infrastructure using [Test Utilities Guide](brobot/docs/docs/04-testing/test-utilities.md)
- Review [BrobotTestBase](brobot/docs/docs/04-testing/mock-mode-guide.md) patterns
- Create shared test utilities extending `MatTestUtils` and `BrobotTestUtils`
- Establish mock data factories
- Document testing patterns

**Deliverables:**
- Test infrastructure setup complete
- Shared utilities created
- Initial tests for highest-priority packages

### Week 2: Core Implementation (Target: 38% coverage)
**Focus:** Core functionality testing

- **Agent 1 & 2**: Work on interconnected action/history tests
- **Agent 3**: Focus on logging (used by all components)
- **Agent 4**: Build mock infrastructure for others
- **Agent 5**: Test configuration (needed by all)

**Key Resources:**
- [Motion Detection Testing](brobot/docs/docs/03-core-library/testing/motion-detection-testing.md) for Agent 1
- [ActionHistory Integration](brobot/docs/docs/04-testing/actionhistory-integration-testing.md) for Agent 2

### Week 3: Advanced Testing (Target: 53% coverage)
**Focus:** Complex scenarios and edge cases

- Implement integration tests following [Integration Testing Guide](brobot/docs/docs/04-testing/integration-testing.md)
- Use [Mixed Mode Execution](brobot/docs/docs/04-testing/mixed-mode-execution.md) for hybrid testing
- Apply [Pattern Matching Debug](brobot/docs/docs/04-testing/debugging-pattern-matching.md) techniques

### Week 4: Integration & Polish (Target: 60% coverage)
**Focus:** Final push and quality assurance

- Cross-agent integration tests
- Performance testing
- Documentation updates
- Coverage gap analysis

## Shared Resources

### Test Data Repository Structure
```
/test-resources/
├── images/
│   ├── patterns/        # Sample images for pattern matching
│   ├── screenshots/     # Test screenshots
│   └── opencv/         # OpenCV test images
├── configs/
│   ├── states/         # Mock state configurations
│   └── actions/        # Test action sequences
├── data/
│   ├── performance/    # Performance baselines
│   └── snapshots/      # Test snapshots
└── fixtures/
    └── mock/           # Mock data factories
```

### Standard Testing Pattern
```java
/**
 * All tests MUST extend BrobotTestBase for proper mock mode setup
 * Reference: brobot/docs/docs/04-testing/mock-mode-guide.md
 */
public class ComponentTest extends BrobotTestBase {
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest(); // CRITICAL: Always call parent setup
        // Additional setup here
    }
    
    @Test
    public void testFeature() {
        // Use MatTestUtils for OpenCV operations
        Mat testMat = MatTestUtils.createSafeMat(100, 100, CvType.CV_8UC3);
        
        // Use shared mock factories
        StateImage mockImage = TestDataFactory.createStateImage();
        
        // Follow AAA pattern (Arrange-Act-Assert)
        // Arrange
        ObjectCollection collection = new ObjectCollection.Builder()
            .withStateImages(mockImage)
            .build();
            
        // Act
        ActionResult result = action.find(collection);
        
        // Assert
        assertTrue(result.isSuccess());
        
        // Cleanup
        MatTestUtils.safeRelease(testMat);
    }
}
```

## Git Workflow

### Branch Strategy
```bash
# Each agent creates feature branch
git checkout -b coverage/agent-1-actions
git checkout -b coverage/agent-2-history
git checkout -b coverage/agent-3-logging
git checkout -b coverage/agent-4-mocking
git checkout -b coverage/agent-5-config

# Daily integration
git checkout -b coverage/integration
git merge coverage/agent-1-actions
git merge coverage/agent-2-history
# ... merge all agent branches

# Weekly merge to main
git checkout main
git merge coverage/integration
```

### Commit Message Convention
```
test(package): Add tests for ComponentName

- Test coverage increased from X% to Y%
- Added N new test cases
- Covers edge cases for feature Z

Refs: #coverage-improvement
```

## Success Metrics

### Quantitative Metrics
- **Primary Goal**: Achieve 60% overall code coverage
- **Branch Coverage**: Increase to 35%+ 
- **Method Coverage**: Increase to 50%+
- **Test Execution Time**: < 60 seconds for full suite
- **Test Stability**: 0 flaky tests

### Qualitative Metrics
- All critical paths have test coverage
- Mock infrastructure is reusable
- Documentation is comprehensive
- Test patterns are consistent

### Weekly Milestones
| Week | Target Coverage | Tests Added | Key Deliverables |
|------|----------------|-------------|------------------|
| 1 | 28% | +500 | Test infrastructure, shared utilities |
| 2 | 38% | +800 | Core functionality tests |
| 3 | 53% | +1000 | Integration tests, edge cases |
| 4 | 60% | +500 | Polish, documentation, gap filling |

## Risk Mitigation

### Technical Risks

**Risk 1: Test Interdependencies**
- **Impact**: Test failures due to shared state
- **Mitigation**: 
  - Use `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`
  - Implement proper cleanup in `@AfterEach`
  - Follow isolation patterns from [Unit Testing Guide](brobot/docs/docs/04-testing/unit-testing.md)

**Risk 2: Performance Degradation**
- **Impact**: Slow test execution
- **Mitigation**:
  - Enable parallel test execution in Gradle
  - Use `@Tag` for test categorization
  - Implement test suites for selective runs

**Risk 3: OpenCV/Native Crashes**
- **Impact**: JVM crashes during testing
- **Mitigation**:
  - Always use `MatTestUtils` for Mat operations
  - Follow patterns from [Mat Testing Utilities](brobot/docs/docs/04-testing/mat-testing-utilities.md)
  - Implement proper resource cleanup

### Process Risks

**Risk 4: Merge Conflicts**
- **Impact**: Integration delays
- **Mitigation**:
  - Clear package ownership per agent
  - Daily integration branches
  - Frequent rebasing from main

**Risk 5: Coverage Regression**
- **Impact**: Loss of existing coverage
- **Mitigation**:
  - Set up coverage gates in CI/CD
  - Monitor coverage trends daily
  - Immediate rollback for coverage drops

## Communication Protocol

### Daily Standups
- **Time**: 10 AM EST (async via PR comments)
- **Format**: Yesterday/Today/Blockers
- **Location**: GitHub PR comments on integration branch

### Weekly Reviews
- **Coverage Report**: Generated every Friday
- **Review Meeting**: Monday morning
- **Deliverable**: Coverage trend analysis

### Communication Channels
- **Primary**: GitHub PR comments
- **Urgent**: Slack/Discord #coverage-improvement
- **Documentation**: Update this plan weekly

## Tooling & Automation

### Coverage Monitoring
```bash
# Generate coverage report
./gradlew :library:test :library:jacocoTestReport

# View coverage
open library/build/jacocoHtml/index.html

# Coverage check (fails if below threshold)
./gradlew :library:jacocoTestCoverageVerification
```

### Test Execution
```bash
# Run specific agent's tests
./gradlew test --tests "io.github.jspinak.brobot.action.*"

# Run with specific tags
./gradlew test -DincludeTags="unit,fast"

# Parallel execution
./gradlew test --parallel --max-workers=4
```

### Continuous Integration
```yaml
# .github/workflows/coverage.yml
name: Coverage Check
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
      - run: ./gradlew test jacocoTestReport
      - uses: codecov/codecov-action@v2
```

## Appendix: Quick Reference Links

### Essential Documentation
- [BrobotTestBase Usage](brobot/docs/docs/04-testing/mock-mode-guide.md#brobottestbase)
- [MatTestUtils API](brobot/docs/docs/04-testing/mat-testing-utilities.md)
- [Mock Mode Configuration](brobot/docs/docs/04-testing/mock-mode-manager.md)
- [Test Data Factories](brobot/docs/docs/04-testing/test-utilities.md#test-data-factories)

### API References
- [Brobot Action API](brobot/docs/docs/ai-brobot-project-creation.md)
- [Pattern Find Options](brobot/docs/docs/ai-brobot-project-creation.md#patternfindoptions)
- [ActionResult Components](brobot/docs/docs/03-core-library/migration/actionresult-refactoring.md)

### Troubleshooting
- [Debugging Pattern Matching](brobot/docs/docs/04-testing/debugging-pattern-matching.md)
- [Common Test Issues](brobot/docs/docs/04-testing/integration-testing.md#troubleshooting)
- [Mock Mode Issues](brobot/docs/docs/04-testing/mock-mode-migration.md#common-issues)

---

*This plan is a living document and will be updated weekly based on progress and learnings.*

**Last Updated**: 2025-08-25
**Next Review**: Week 1 Review (End of Week 1)
**Document Owner**: Test Coverage Team Lead