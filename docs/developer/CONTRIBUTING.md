# Contributing to Brobot

Thank you for your interest in contributing to Brobot! This guide will help you get started with contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing Requirements](#testing-requirements)
- [Documentation Standards](#documentation-standards)
- [Submitting Changes](#submitting-changes)
- [Review Process](#review-process)

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inspiring community for all. Please be respectful and constructive in your interactions.

### Expected Behavior

- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

## Getting Started

### Prerequisites

Before contributing, ensure you have:

- **Java 17 or higher** installed
- **Gradle 8.0+** for building
- **Git** for version control
- **IDE** with Java support (IntelliJ IDEA recommended)
- **Understanding** of GUI automation concepts

### Setting Up Development Environment

1. **Fork the Repository**
   ```bash
   # Fork on GitHub, then clone your fork
   git clone https://github.com/YOUR_USERNAME/brobot.git
   cd brobot
   ```

2. **Build the Project**
   ```bash
   # Build all modules
   ./gradlew build

   # Run tests
   ./gradlew test --no-daemon
   ```

3. **Configure IDE**
   - Import as Gradle project
   - Enable annotation processing for Lombok
   - Set Java SDK to 17+
   - Configure code style (see [Coding Standards](#coding-standards))

### Project Structure

```
brobot/
├── library/                    # Core Brobot library
│   ├── src/main/java/         # Main source code
│   ├── src/main/resources/    # Configuration files
│   └── src/test/java/         # Unit tests
├── library-test/              # Integration tests
├── docs/                      # Documentation
│   ├── docs/                  # User documentation
│   └── developer/             # Developer documentation
├── examples/                  # Example projects
└── README.md                  # Project overview
```

## Development Workflow

### Creating a New Feature or Fix

1. **Create a Branch**
   ```bash
   # For features
   git checkout -b feature/short-description

   # For bug fixes
   git checkout -b fix/issue-number-description

   # For documentation
   git checkout -b docs/description
   ```

2. **Make Your Changes**
   - Follow coding standards
   - Write tests for new functionality
   - Update documentation as needed
   - Keep commits atomic and focused

3. **Test Your Changes**
   ```bash
   # Run unit tests
   ./gradlew :library:test --no-daemon

   # Run integration tests
   ./gradlew :library-test:test --no-daemon

   # Run all tests
   ./gradlew test --no-daemon
   ```

4. **Commit Your Changes**
   ```bash
   git add .
   git commit -m "feat: add new pattern matching algorithm"
   ```

### Commit Message Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation only
- `style:` - Code style changes (formatting, etc.)
- `refactor:` - Code refactoring
- `test:` - Adding or updating tests
- `chore:` - Maintenance tasks

**Examples:**
```bash
feat(actions): add support for multi-monitor find operations
fix(matcher): resolve similarity calculation edge case
docs(guide): update illustration system documentation
refactor(builder): simplify PatternFindOptions builder pattern
test(integration): add tests for state transition logic
```

## Coding Standards

### Java Code Style

#### Naming Conventions

```java
// Classes: PascalCase
public class PatternMatcher { }

// Methods: camelCase, use setXxx for setters
public void setMinSimilarity(double value) { }
public double getMinSimilarity() { }

// Variables: camelCase
private double minSimilarity;
private List<Match> matchResults;

// Constants: UPPER_SNAKE_CASE
public static final int DEFAULT_TIMEOUT = 30;
private static final String PATTERN_EXTENSION = ".png";

// Packages: lowercase
package io.github.jspinak.brobot.action.basic.find;
```

#### Code Organization

```java
public class ExampleClass {

    // 1. Constants
    private static final int MAX_ATTEMPTS = 3;

    // 2. Static fields
    private static final Logger logger = LoggerFactory.getLogger(ExampleClass.class);

    // 3. Instance fields
    private final String name;
    private double threshold;

    // 4. Constructors
    public ExampleClass(String name) {
        this.name = name;
    }

    // 5. Public methods
    public void performAction() { }

    // 6. Protected methods
    protected void validateState() { }

    // 7. Private methods
    private void internalHelper() { }

    // 8. Nested classes
    public static class Builder { }
}
```

#### Modern Patterns

**Use Builder Pattern for Complex Objects:**
```java
// Good: Modern builder with fluent API
PatternFindOptions options = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .setSimilarity(0.85)
    .setMaxWaitTime(Duration.ofSeconds(10))
    .build();

// Avoid: Multiple constructors
// PatternFindOptions options = new PatternFindOptions(strategy, 0.85, 10);
```

**Use Lombok Annotations:**
```java
import lombok.Getter;
import lombok.Builder;

@Getter
@Builder(toBuilder = true, builderClassName = "Builder")
public class MyConfig {
    private final double threshold;
    private final int maxAttempts;
}
```

**Prefer Immutability:**
```java
// Good: Immutable with final fields
public class MatchResult {
    private final Location location;
    private final double score;

    public MatchResult(Location location, double score) {
        this.location = location;
        this.score = score;
    }
}

// Avoid: Mutable state
// public void setScore(double score) { this.score = score; }
```

### Clean Code Principles

**Single Responsibility:**
```java
// Good: Each class has one responsibility
public class PatternFinder {
    public List<Match> find(Pattern pattern) { }
}

public class MatchScorer {
    public double calculateScore(Match match) { }
}

// Avoid: Multiple responsibilities
// public class PatternFinderAndScorer { }
```

**Meaningful Names:**
```java
// Good: Clear intent
public ActionResult findLoginButton(double minSimilarity) { }
private boolean isMatchQualityAcceptable(Match match) { }

// Avoid: Unclear names
// public ActionResult process(double val) { }
// private boolean check(Match m) { }
```

**Small Functions:**
```java
// Good: Focused, single-purpose method
public boolean isValidSimilarity(double similarity) {
    return similarity >= 0.0 && similarity <= 1.0;
}

// Avoid: Large, multi-purpose methods
```

## Testing Requirements

### Test Coverage Expectations

- **New Features:** 80%+ coverage required
- **Bug Fixes:** Add test demonstrating the bug, then fix
- **Refactoring:** Maintain or improve existing coverage

### Test Organization

**Unit Tests (library module):**
```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PatternMatcherTest extends BrobotTestBase {

    @Test
    public void testBasicPatternMatching() {
        // Arrange
        Pattern pattern = new Pattern("test-button.png");
        PatternMatcher matcher = new PatternMatcher();

        // Act
        List<Match> matches = matcher.find(pattern);

        // Assert
        assertFalse(matches.isEmpty());
        assertTrue(matches.get(0).getScore() > 0.8);
    }
}
```

**Integration Tests (library-test module):**
```java
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class ActionIntegrationTest {

    @Autowired
    private Action action;

    @Test
    public void testFullActionWorkflow() {
        // Test complete workflow with Spring context
    }
}
```

### Test Best Practices

1. **Always extend BrobotTestBase** for Brobot-specific tests
2. **Use descriptive test names:** `testFindPatternWithLowSimilarityThreshold`
3. **Follow AAA pattern:** Arrange, Act, Assert
4. **Test edge cases:** null inputs, boundary values, error conditions
5. **Use mock mode:** Tests should work in headless environments
6. **Keep tests fast:** Avoid Thread.sleep(), use mock timings

### Running Tests

```bash
# Run all tests
./gradlew test --no-daemon

# Run specific test class
./gradlew test --tests "PatternMatcherTest" --no-daemon

# Run with coverage
./gradlew test jacocoTestReport --no-daemon

# For large test suites, use Python runner
python3 library/scripts/run-all-tests.py library --mode parallel
```

## Documentation Standards

### Code Documentation

**Follow the API Documentation Template:** See [API_DOCUMENTATION_TEMPLATE.md](./API_DOCUMENTATION_TEMPLATE.md)

**Key Requirements:**

1. **All Public APIs Must Be Documented:**
   ```java
   /**
    * Finds all matches of the pattern in the current screen.
    *
    * @param pattern The pattern to search for. Must not be null.
    * @param similarity Minimum similarity threshold (0.0 to 1.0)
    * @return List of matches, empty if none found. Never null.
    * @throws IllegalArgumentException if similarity is out of range
    */
   public List<Match> findPattern(Pattern pattern, double similarity) {
   ```

2. **Include Examples in Complex APIs:**
   ```java
   /**
    * <p><b>Example Usage:</b></p>
    * <pre>{@code
    * PatternFindOptions options = new PatternFindOptions.Builder()
    *     .setSimilarity(0.85)
    *     .setMaxWaitTime(Duration.ofSeconds(10))
    *     .build();
    * }</pre>
    */
   ```

3. **Document Thread Safety:**
   ```java
   /**
    * <p><b>Thread Safety:</b> This class is thread-safe. Multiple threads
    * can safely call methods concurrently.</p>
    */
   ```

### User Documentation

**When to Update User Documentation:**

- Adding new features → Create guide in `/docs/docs/`
- Changing APIs → Update relevant user guides
- Fixing bugs that affect usage → Update troubleshooting sections
- Adding configuration options → Update properties reference

**Documentation Structure:**
```markdown
---
sidebar_position: 1
title: 'Feature Name'
description: 'Brief description'
keywords: [keyword1, keyword2]
---

# Feature Name

Brief introduction explaining the feature.

## Quick Start

Basic example showing immediate usage.

## Complete Examples

Full working examples with all imports.

## Configuration

Property-based configuration options.

## Best Practices

Recommended patterns and anti-patterns.

## Troubleshooting

Common issues and solutions.

## Related Documentation

Links to related guides.
```

### Documentation Quality Standards

✅ **Good Documentation:**
- Complete, compilable code examples
- All imports included
- Expected output shown
- Common use cases covered
- Troubleshooting section
- Cross-references to related docs

❌ **Avoid:**
- Code fragments without imports
- Hypothetical APIs that don't exist
- No working examples
- Outdated information

## Submitting Changes

### Before Submitting

**Checklist:**
- [ ] Code follows style guidelines
- [ ] All tests pass locally
- [ ] New tests added for new features
- [ ] Documentation updated
- [ ] Commit messages follow convention
- [ ] No merge conflicts with main branch

### Creating a Pull Request

1. **Push Your Branch**
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create Pull Request on GitHub**
   - Use descriptive title
   - Reference related issues
   - Describe changes clearly
   - Include screenshots if UI-related

3. **Pull Request Template**
   ```markdown
   ## Description
   Brief description of changes

   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update

   ## Testing
   - Describe tests added/updated
   - How to verify changes

   ## Checklist
   - [ ] Tests pass locally
   - [ ] Documentation updated
   - [ ] Follows coding standards

   ## Related Issues
   Closes #123
   ```

## Review Process

### What to Expect

1. **Automated Checks** - CI/CD pipeline runs tests
2. **Code Review** - Maintainer reviews changes
3. **Feedback** - Address review comments
4. **Approval** - Once approved, PR is merged

### Review Criteria

Code reviews focus on:
- **Correctness:** Does it work as intended?
- **Testing:** Adequate test coverage?
- **Style:** Follows coding standards?
- **Documentation:** User-facing changes documented?
- **Performance:** No obvious performance issues?
- **Breaking Changes:** Are they necessary and well-communicated?

### Responding to Feedback

- Address all comments
- Ask questions if unclear
- Make requested changes
- Update PR description if scope changes
- Be patient and respectful

## Getting Help

### Resources

- **User Documentation:** `/docs/docs/`
- **Developer Guide:** [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md)
- **API Template:** [API_DOCUMENTATION_TEMPLATE.md](./API_DOCUMENTATION_TEMPLATE.md)
- **Example Projects:** `/examples/`

### Communication Channels

- **Issues:** Report bugs and request features
- **Discussions:** Ask questions and share ideas
- **Pull Requests:** Submit code contributions

### Questions?

If you have questions about contributing:
1. Check existing documentation
2. Search closed issues for similar questions
3. Open a new discussion/issue with your question

---

Thank you for contributing to Brobot! Your efforts help make GUI automation more accessible and reliable for everyone.
