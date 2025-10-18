# Brobot Developer Documentation

Welcome to the Brobot developer documentation! This directory contains resources for developers contributing to the Brobot framework itself.

## 📚 Documentation Overview

### For Contributors

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **[CONTRIBUTING.md](./CONTRIBUTING.md)** | Complete contribution guide | Before submitting any code |
| **[DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md)** | Deep-dive into architecture and development | When working on framework features |
| **[API_DOCUMENTATION_TEMPLATE.md](./API_DOCUMENTATION_TEMPLATE.md)** | Javadoc standards and templates | When documenting new APIs |

## 🚀 Quick Start for Contributors

### 1. First-Time Setup

```bash
# Fork and clone the repository
git clone https://github.com/YOUR_USERNAME/brobot.git
cd brobot

# Build the project
./gradlew build

# Run tests
./gradlew test --no-daemon
```

### 2. Before You Start Coding

- [ ] Read [CONTRIBUTING.md](./CONTRIBUTING.md)
- [ ] Understand the coding standards
- [ ] Set up your IDE (see CONTRIBUTING.md)
- [ ] Familiarize yourself with the project structure

### 3. Development Workflow

```bash
# Create a feature branch
git checkout -b feature/your-feature-name

# Make your changes
# ... edit files ...

# Test your changes
./gradlew test --no-daemon

# Commit with conventional commit message
git commit -m "feat: add new feature"

# Push and create pull request
git push origin feature/your-feature-name
```

## 📖 What's in Each Guide?

### CONTRIBUTING.md

**Complete contribution workflow:**
- ✅ Code of conduct
- ✅ Getting started checklist
- ✅ Coding standards and style guide
- ✅ Testing requirements
- ✅ Documentation standards
- ✅ Pull request process
- ✅ Review criteria

**Best for:**
- First-time contributors
- Understanding the contribution process
- Quick reference for coding standards

### DEVELOPER_GUIDE.md

**Deep technical documentation:**
- 🏗️ Architecture overview
- 📦 Module structure explanation
- 🔧 Core concepts (Actions, States, Matching)
- 🛠️ Development setup and configuration
- 🧪 Testing strategies
- 🐛 Debugging techniques
- ⚡ Performance optimization
- 📋 Common development tasks
- 🚢 Release process

**Best for:**
- Understanding Brobot internals
- Implementing new features
- Performance optimization
- Complex debugging scenarios

### API_DOCUMENTATION_TEMPLATE.md

**Javadoc standards:**
- 📝 Class documentation template
- 📝 Method documentation template
- 📝 Builder method documentation
- 📝 Enum documentation
- 📝 Package documentation

**Best for:**
- Writing new public APIs
- Documenting new classes and methods
- Ensuring consistent documentation style

## 🎯 Common Scenarios

### "I want to fix a bug"

1. Read: [CONTRIBUTING.md § Bug Fixes](./CONTRIBUTING.md#development-workflow)
2. Create branch: `git checkout -b fix/issue-123-description`
3. Write test demonstrating the bug
4. Fix the bug
5. Ensure test passes
6. Submit PR referencing the issue

### "I want to add a new feature"

1. Read: [DEVELOPER_GUIDE.md § Adding a New Action](./DEVELOPER_GUIDE.md#adding-a-new-action)
2. Understand architecture: [DEVELOPER_GUIDE.md § Architecture Overview](./DEVELOPER_GUIDE.md#architecture-overview)
3. Create branch: `git checkout -b feature/feature-name`
4. Implement with tests
5. Document the feature
6. Submit PR with description

### "I want to improve documentation"

1. Read: [CONTRIBUTING.md § Documentation Standards](./CONTRIBUTING.md#documentation-standards)
2. For user docs: Edit files in `/docs/docs/`
3. For API docs: Follow [API_DOCUMENTATION_TEMPLATE.md](./API_DOCUMENTATION_TEMPLATE.md)
4. Ensure examples compile
5. Submit PR: `docs: improve XYZ documentation`

### "Tests are failing in my PR"

1. Check: [DEVELOPER_GUIDE.md § Test Debugging](./DEVELOPER_GUIDE.md#test-debugging)
2. Run locally: `./gradlew test --no-daemon`
3. Check test extends `BrobotTestBase`
4. Verify mock mode enabled
5. Review CI logs for specific errors

## 🔧 Development Tools

### Essential Commands

```bash
# Build everything
./gradlew build

# Run tests (avoid daemon for stability)
./gradlew test --no-daemon

# Run tests with coverage
./gradlew test jacocoTestReport

# Clean build
./gradlew clean build

# Run specific test
./gradlew test --tests "MyTest" --no-daemon

# For large test suites (6000+ tests)
python3 library/scripts/run-all-tests.py library --mode parallel
```

### IDE Setup

**IntelliJ IDEA (Recommended):**
- Enable Lombok plugin
- Enable annotation processing
- Import code style (see CONTRIBUTING.md)
- Set Java 17+ SDK

**Eclipse:**
- Install Lombok
- Configure Java 17+
- Import Gradle project

## 📊 Project Statistics

- **Core Library:** 300+ classes
- **Test Suite:** 6000+ tests
- **User Documentation:** 100+ guides
- **Supported Java:** 17+
- **Build Tool:** Gradle 8.0+

## 🌳 Repository Structure

```
brobot/
├── library/              # Core Brobot framework (no Spring Boot)
├── library-test/         # Integration tests (with Spring Boot)
├── docs/
│   ├── docs/            # User documentation (published)
│   └── developer/       # Developer documentation (this folder)
├── examples/            # Example projects
├── build.gradle         # Root build configuration
└── settings.gradle      # Multi-module settings
```

## 🤝 Getting Help

### For Development Questions

1. **Check existing documentation:**
   - [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) - Architecture and internals
   - [CONTRIBUTING.md](./CONTRIBUTING.md) - Process and standards

2. **Search existing issues:**
   - Look for similar bugs/features
   - Check closed issues for solutions

3. **Open a discussion:**
   - For architecture questions
   - For feature proposals
   - For general development help

4. **Open an issue:**
   - For bugs
   - For feature requests
   - For documentation improvements

### For Contribution Process

- **Pull Request Questions:** Comment on your PR
- **Review Feedback:** Respond to review comments
- **Merge Conflicts:** Ask in PR comments for guidance

## 📝 Documentation Standards

### Code Must Be Documented

All public APIs require Javadoc:

```java
/**
 * Finds all matches of the pattern on the screen.
 *
 * @param pattern The pattern to search for. Must not be null.
 * @param similarity Minimum similarity (0.0 to 1.0). Default: 0.7.
 * @return List of matches, empty if none found. Never null.
 * @throws IllegalArgumentException if similarity out of range
 */
public List<Match> findPattern(Pattern pattern, double similarity) {
```

See [API_DOCUMENTATION_TEMPLATE.md](./API_DOCUMENTATION_TEMPLATE.md) for complete examples.

### User-Facing Changes Must Update Docs

When changing public APIs:
- Update relevant user guides in `/docs/docs/`
- Add complete code examples
- Include expected output
- Update troubleshooting sections
- Add cross-references

## ✅ Contribution Checklist

Before submitting a PR:

- [ ] Code follows style guidelines (see CONTRIBUTING.md)
- [ ] All tests pass locally (`./gradlew test --no-daemon`)
- [ ] New tests added for new features
- [ ] Public APIs have Javadoc
- [ ] User documentation updated (if user-facing change)
- [ ] Commit messages follow convention (`feat:`, `fix:`, etc.)
- [ ] No merge conflicts with main branch
- [ ] PR description is clear and complete

## 🔐 Code of Conduct

We are committed to providing a welcoming and inclusive environment. Please:

- ✅ Be respectful and constructive
- ✅ Welcome newcomers
- ✅ Give credit where due
- ✅ Focus on what's best for the community
- ❌ No harassment or discrimination
- ❌ No spam or self-promotion

See [CONTRIBUTING.md § Code of Conduct](./CONTRIBUTING.md#code-of-conduct) for details.

## 🎓 Learning Resources

### Understanding Brobot

1. **Start with examples:** `/examples/` directory
2. **Read user documentation:** `/docs/docs/01-getting-started/`
3. **Review core classes:** `Action`, `StateService`, `PatternFindOptions`
4. **Study tests:** See how features are tested

### Understanding GUI Automation

- **SikuliX Documentation:** https://sikulix-2014.readthedocs.io/
- **OpenCV:** Understanding image matching algorithms
- **Java Robot:** Understanding low-level input simulation

## 🚀 Advanced Topics

For advanced development scenarios, see:

- [DEVELOPER_GUIDE.md § Custom Matcher Implementation](./DEVELOPER_GUIDE.md#custom-matcher-implementation)
- [DEVELOPER_GUIDE.md § Performance Optimization](./DEVELOPER_GUIDE.md#performance-optimization)
- [DEVELOPER_GUIDE.md § Multi-Monitor Configuration](./DEVELOPER_GUIDE.md#multi-monitor-custom-configuration)

## 📞 Contact

- **Issues:** https://github.com/jspinak/brobot/issues
- **Discussions:** https://github.com/jspinak/brobot/discussions
- **Pull Requests:** https://github.com/jspinak/brobot/pulls

---

Thank you for contributing to Brobot! Your efforts help make GUI automation more accessible and reliable for everyone. 🎉
