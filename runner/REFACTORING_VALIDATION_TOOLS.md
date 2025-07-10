# Automated Refactoring Validation Tools

## Overview

This document defines automated tools and processes to validate that refactoring efforts maintain functionality, improve code quality, and achieve the stated goals without introducing regressions.

## Core Validation Framework

### 1. Refactoring Validator Engine

```java
@Component
@Slf4j
public class RefactoringValidator {
    private final List<ValidationRule> rules;
    private final MetricsCollector metricsCollector;
    private final TestSuiteRunner testRunner;
    private final PerformanceBenchmark benchmark;
    
    public ValidationReport validateRefactoring(
            String componentName,
            RefactoringContext context) {
        
        log.info("Starting validation for refactoring: {}", componentName);
        
        ValidationReport report = ValidationReport.builder()
            .componentName(componentName)
            .startTime(Instant.now())
            .context(context)
            .build();
        
        try {
            // 1. Pre-refactoring metrics
            Metrics beforeMetrics = metricsCollector.collect(context.getBeforePath());
            report.setBeforeMetrics(beforeMetrics);
            
            // 2. Post-refactoring metrics
            Metrics afterMetrics = metricsCollector.collect(context.getAfterPath());
            report.setAfterMetrics(afterMetrics);
            
            // 3. Run validation rules
            List<ValidationResult> ruleResults = rules.stream()
                .map(rule -> rule.validate(context, beforeMetrics, afterMetrics))
                .collect(Collectors.toList());
            report.setRuleResults(ruleResults);
            
            // 4. Test compatibility
            TestCompatibilityResult testResult = validateTestCompatibility(context);
            report.setTestCompatibility(testResult);
            
            // 5. Performance comparison
            PerformanceComparison perfComparison = benchmark.compare(
                context.getBeforePath(), 
                context.getAfterPath()
            );
            report.setPerformanceComparison(perfComparison);
            
            // 6. API compatibility
            ApiCompatibilityResult apiResult = validateApiCompatibility(context);
            report.setApiCompatibility(apiResult);
            
            // 7. Generate summary
            report.setSummary(generateSummary(report));
            
        } catch (Exception e) {
            log.error("Validation failed", e);
            report.setError(e);
        }
        
        report.setEndTime(Instant.now());
        return report;
    }
}
```

### 2. Validation Rules

```java
// Base validation rule
public interface ValidationRule {
    String getName();
    ValidationResult validate(RefactoringContext context, 
                            Metrics before, 
                            Metrics after);
}

// Single Responsibility Principle validator
@Component
public class SRPValidationRule implements ValidationRule {
    
    @Override
    public String getName() {
        return "Single Responsibility Principle";
    }
    
    @Override
    public ValidationResult validate(RefactoringContext context, 
                                   Metrics before, 
                                   Metrics after) {
        ValidationResult result = new ValidationResult(getName());
        
        // Check class size reduction
        if (after.getAverageClassSize() < before.getAverageClassSize()) {
            result.addSuccess("Average class size reduced from " + 
                before.getAverageClassSize() + " to " + 
                after.getAverageClassSize());
        }
        
        // Check method count per class
        if (after.getAverageMethodsPerClass() < before.getAverageMethodsPerClass()) {
            result.addSuccess("Average methods per class reduced from " +
                before.getAverageMethodsPerClass() + " to " +
                after.getAverageMethodsPerClass());
        }
        
        // Check cohesion metrics
        double cohesionImprovement = after.getAverageCohesion() - before.getAverageCohesion();
        if (cohesionImprovement > 0) {
            result.addSuccess(String.format("Cohesion improved by %.2f%%", 
                cohesionImprovement * 100));
        }
        
        // Check for God classes
        List<ClassMetrics> godClasses = after.getClasses().stream()
            .filter(c -> c.getLineCount() > 300 || c.getMethodCount() > 20)
            .collect(Collectors.toList());
            
        if (godClasses.isEmpty()) {
            result.addSuccess("No God classes detected");
        } else {
            godClasses.forEach(gc -> 
                result.addWarning("Potential God class: " + gc.getName() + 
                    " (" + gc.getLineCount() + " lines, " + 
                    gc.getMethodCount() + " methods)")
            );
        }
        
        return result;
    }
}

// Dependency validation rule
@Component
public class DependencyValidationRule implements ValidationRule {
    
    @Override
    public ValidationResult validate(RefactoringContext context, 
                                   Metrics before, 
                                   Metrics after) {
        ValidationResult result = new ValidationResult("Dependency Management");
        
        // Check coupling metrics
        if (after.getAverageCoupling() < before.getAverageCoupling()) {
            result.addSuccess("Coupling reduced from " + 
                before.getAverageCoupling() + " to " + 
                after.getAverageCoupling());
        }
        
        // Check circular dependencies
        List<CircularDependency> circularDeps = findCircularDependencies(after);
        if (circularDeps.isEmpty()) {
            result.addSuccess("No circular dependencies found");
        } else {
            circularDeps.forEach(cd -> 
                result.addError("Circular dependency: " + cd.describe())
            );
        }
        
        // Check dependency injection usage
        long diUsage = after.getClasses().stream()
            .filter(c -> c.usesConstructorInjection())
            .count();
        
        double diPercentage = (double) diUsage / after.getClasses().size() * 100;
        result.addInfo(String.format("%.1f%% of classes use constructor injection", 
            diPercentage));
        
        return result;
    }
}
```

### 3. Code Metrics Collector

```java
@Component
public class MetricsCollector {
    private final JavaParser javaParser;
    private final ComplexityCalculator complexityCalculator;
    
    public Metrics collect(Path sourcePath) {
        log.info("Collecting metrics from: {}", sourcePath);
        
        List<CompilationUnit> compilationUnits = parseJavaFiles(sourcePath);
        
        Metrics metrics = new Metrics();
        metrics.setSourcePath(sourcePath);
        metrics.setTimestamp(Instant.now());
        
        // Collect class-level metrics
        List<ClassMetrics> classMetrics = compilationUnits.stream()
            .flatMap(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream())
            .map(this::analyzeClass)
            .collect(Collectors.toList());
        metrics.setClasses(classMetrics);
        
        // Calculate aggregate metrics
        metrics.setTotalClasses(classMetrics.size());
        metrics.setTotalMethods(classMetrics.stream()
            .mapToInt(ClassMetrics::getMethodCount)
            .sum());
        metrics.setTotalLines(classMetrics.stream()
            .mapToInt(ClassMetrics::getLineCount)
            .sum());
        
        // Calculate averages
        metrics.setAverageClassSize(
            metrics.getTotalLines() / (double) metrics.getTotalClasses()
        );
        metrics.setAverageMethodsPerClass(
            metrics.getTotalMethods() / (double) metrics.getTotalClasses()
        );
        
        // Calculate complexity metrics
        metrics.setAverageComplexity(calculateAverageComplexity(classMetrics));
        metrics.setMaxComplexity(calculateMaxComplexity(classMetrics));
        
        // Calculate coupling and cohesion
        metrics.setAverageCoupling(calculateAverageCoupling(classMetrics));
        metrics.setAverageCohesion(calculateAverageCohesion(classMetrics));
        
        return metrics;
    }
    
    private ClassMetrics analyzeClass(ClassOrInterfaceDeclaration classDecl) {
        return ClassMetrics.builder()
            .name(classDecl.getNameAsString())
            .packageName(getPackageName(classDecl))
            .lineCount(calculateLines(classDecl))
            .methodCount(classDecl.getMethods().size())
            .fieldCount(classDecl.getFields().size())
            .complexity(complexityCalculator.calculate(classDecl))
            .dependencies(extractDependencies(classDecl))
            .usesConstructorInjection(hasConstructorInjection(classDecl))
            .annotations(extractAnnotations(classDecl))
            .build();
    }
}
```

### 4. Test Compatibility Validator

```java
@Component
public class TestCompatibilityValidator {
    private final TestRunner testRunner;
    private final TestAnalyzer testAnalyzer;
    
    public TestCompatibilityResult validate(RefactoringContext context) {
        log.info("Validating test compatibility for refactoring");
        
        TestCompatibilityResult result = new TestCompatibilityResult();
        
        // 1. Run tests against old implementation
        TestRunResult beforeResults = testRunner.runTests(
            context.getBeforePath(),
            context.getTestPath()
        );
        result.setBeforeResults(beforeResults);
        
        // 2. Run tests against new implementation
        TestRunResult afterResults = testRunner.runTests(
            context.getAfterPath(),
            context.getTestPath()
        );
        result.setAfterResults(afterResults);
        
        // 3. Compare results
        result.setCompatible(
            afterResults.getPassedCount() >= beforeResults.getPassedCount()
        );
        
        // 4. Analyze new failures
        List<TestFailure> newFailures = findNewFailures(
            beforeResults, 
            afterResults
        );
        result.setNewFailures(newFailures);
        
        // 5. Analyze fixed tests
        List<TestCase> fixedTests = findFixedTests(
            beforeResults, 
            afterResults
        );
        result.setFixedTests(fixedTests);
        
        // 6. Coverage comparison
        CoverageComparison coverage = compareCoverage(
            beforeResults.getCoverage(),
            afterResults.getCoverage()
        );
        result.setCoverageComparison(coverage);
        
        return result;
    }
}
```

### 5. Performance Benchmark

```java
@Component
public class PerformanceBenchmark {
    private final BenchmarkRunner benchmarkRunner;
    
    public PerformanceComparison compare(Path before, Path after) {
        log.info("Running performance benchmarks");
        
        // Define benchmark scenarios
        List<BenchmarkScenario> scenarios = List.of(
            new StartupTimeBenchmark(),
            new MemoryUsageBenchmark(),
            new ThroughputBenchmark(),
            new LatencyBenchmark()
        );
        
        PerformanceComparison comparison = new PerformanceComparison();
        
        for (BenchmarkScenario scenario : scenarios) {
            // Run benchmark on old implementation
            BenchmarkResult beforeResult = benchmarkRunner.run(
                scenario, 
                before
            );
            
            // Run benchmark on new implementation
            BenchmarkResult afterResult = benchmarkRunner.run(
                scenario, 
                after
            );
            
            // Compare results
            PerformanceMetric metric = PerformanceMetric.builder()
                .name(scenario.getName())
                .beforeValue(beforeResult.getValue())
                .afterValue(afterResult.getValue())
                .improvement(calculateImprovement(
                    beforeResult.getValue(), 
                    afterResult.getValue()
                ))
                .unit(scenario.getUnit())
                .build();
                
            comparison.addMetric(metric);
        }
        
        return comparison;
    }
}

// Example benchmark scenario
public class StartupTimeBenchmark implements BenchmarkScenario {
    
    @Override
    public BenchmarkResult run(Path implementation) {
        List<Long> measurements = new ArrayList<>();
        
        // Warm up
        for (int i = 0; i < 3; i++) {
            measureStartupTime(implementation);
        }
        
        // Actual measurements
        for (int i = 0; i < 10; i++) {
            measurements.add(measureStartupTime(implementation));
        }
        
        // Calculate statistics
        double average = measurements.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
            
        return BenchmarkResult.builder()
            .value(average)
            .measurements(measurements)
            .unit("ms")
            .build();
    }
    
    private long measureStartupTime(Path implementation) {
        long startTime = System.currentTimeMillis();
        
        // Start application
        ProcessBuilder pb = new ProcessBuilder(
            "java", "-jar", implementation.toString()
        );
        
        try {
            Process process = pb.start();
            
            // Wait for application to be ready
            waitForApplicationReady(process);
            
            long endTime = System.currentTimeMillis();
            
            // Shutdown application
            process.destroy();
            
            return endTime - startTime;
            
        } catch (Exception e) {
            throw new BenchmarkException("Failed to measure startup time", e);
        }
    }
}
```

### 6. API Compatibility Checker

```java
@Component
public class ApiCompatibilityChecker {
    private final ApiAnalyzer apiAnalyzer;
    
    public ApiCompatibilityResult check(RefactoringContext context) {
        log.info("Checking API compatibility");
        
        // Extract API from old implementation
        Api oldApi = apiAnalyzer.extractApi(context.getBeforePath());
        
        // Extract API from new implementation
        Api newApi = apiAnalyzer.extractApi(context.getAfterPath());
        
        // Compare APIs
        ApiCompatibilityResult result = new ApiCompatibilityResult();
        
        // Check for removed methods
        List<MethodSignature> removedMethods = findRemovedMethods(oldApi, newApi);
        result.setRemovedMethods(removedMethods);
        
        // Check for changed signatures
        List<SignatureChange> signatureChanges = findSignatureChanges(oldApi, newApi);
        result.setSignatureChanges(signatureChanges);
        
        // Check for new methods
        List<MethodSignature> addedMethods = findAddedMethods(oldApi, newApi);
        result.setAddedMethods(addedMethods);
        
        // Determine compatibility level
        if (removedMethods.isEmpty() && signatureChanges.isEmpty()) {
            result.setCompatibilityLevel(CompatibilityLevel.FULLY_COMPATIBLE);
        } else if (removedMethods.isEmpty()) {
            result.setCompatibilityLevel(CompatibilityLevel.SOURCE_COMPATIBLE);
        } else {
            result.setCompatibilityLevel(CompatibilityLevel.BREAKING_CHANGES);
        }
        
        return result;
    }
}
```

### 7. Validation Report Generator

```java
@Component
public class ValidationReportGenerator {
    private final VelocityEngine velocityEngine;
    
    public void generateReport(ValidationReport report, Path outputPath) {
        log.info("Generating validation report");
        
        // Generate HTML report
        String htmlReport = generateHtmlReport(report);
        Path htmlPath = outputPath.resolve("validation-report.html");
        Files.writeString(htmlPath, htmlReport);
        
        // Generate Markdown report for documentation
        String markdownReport = generateMarkdownReport(report);
        Path mdPath = outputPath.resolve("validation-report.md");
        Files.writeString(mdPath, markdownReport);
        
        // Generate JSON report for tooling
        String jsonReport = generateJsonReport(report);
        Path jsonPath = outputPath.resolve("validation-report.json");
        Files.writeString(jsonPath, jsonReport);
        
        // Generate summary for CI/CD
        if (!report.isSuccess()) {
            generateFailureSummary(report, outputPath);
        }
    }
    
    private String generateMarkdownReport(ValidationReport report) {
        StringBuilder md = new StringBuilder();
        
        md.append("# Refactoring Validation Report\n\n");
        md.append("**Component**: ").append(report.getComponentName()).append("\n");
        md.append("**Date**: ").append(report.getStartTime()).append("\n");
        md.append("**Status**: ").append(report.isSuccess() ? "✅ PASSED" : "❌ FAILED").append("\n\n");
        
        // Metrics comparison
        md.append("## Metrics Comparison\n\n");
        md.append("| Metric | Before | After | Change |\n");
        md.append("|--------|--------|-------|--------|\n");
        
        appendMetricRow(md, "Total Classes", 
            report.getBeforeMetrics().getTotalClasses(),
            report.getAfterMetrics().getTotalClasses());
            
        appendMetricRow(md, "Average Class Size", 
            report.getBeforeMetrics().getAverageClassSize(),
            report.getAfterMetrics().getAverageClassSize());
            
        appendMetricRow(md, "Average Complexity", 
            report.getBeforeMetrics().getAverageComplexity(),
            report.getAfterMetrics().getAverageComplexity());
        
        // Rule results
        md.append("\n## Validation Rules\n\n");
        for (ValidationResult rule : report.getRuleResults()) {
            md.append("### ").append(rule.getRuleName()).append("\n");
            md.append("**Status**: ").append(rule.getStatus()).append("\n\n");
            
            if (!rule.getSuccesses().isEmpty()) {
                md.append("✅ **Successes**:\n");
                rule.getSuccesses().forEach(s -> 
                    md.append("- ").append(s).append("\n"));
            }
            
            if (!rule.getWarnings().isEmpty()) {
                md.append("\n⚠️ **Warnings**:\n");
                rule.getWarnings().forEach(w -> 
                    md.append("- ").append(w).append("\n"));
            }
            
            if (!rule.getErrors().isEmpty()) {
                md.append("\n❌ **Errors**:\n");
                rule.getErrors().forEach(e -> 
                    md.append("- ").append(e).append("\n"));
            }
        }
        
        // Performance comparison
        md.append("\n## Performance Comparison\n\n");
        PerformanceComparison perf = report.getPerformanceComparison();
        
        md.append("| Metric | Before | After | Change |\n");
        md.append("|--------|--------|-------|--------|\n");
        
        for (PerformanceMetric metric : perf.getMetrics()) {
            md.append("| ").append(metric.getName());
            md.append(" | ").append(formatValue(metric.getBeforeValue(), metric.getUnit()));
            md.append(" | ").append(formatValue(metric.getAfterValue(), metric.getUnit()));
            md.append(" | ").append(formatPercentage(metric.getImprovement()));
            md.append(" |\n");
        }
        
        return md.toString();
    }
}
```

### 8. Continuous Validation Pipeline

```java
@Component
public class ContinuousValidationPipeline {
    private final RefactoringValidator validator;
    private final GitService gitService;
    private final NotificationService notificationService;
    
    @EventListener
    public void onPullRequest(PullRequestEvent event) {
        if (isRefactoringPR(event)) {
            runValidation(event);
        }
    }
    
    private void runValidation(PullRequestEvent event) {
        try {
            // Checkout branches
            Path beforePath = gitService.checkout(event.getBaseBranch());
            Path afterPath = gitService.checkout(event.getHeadBranch());
            
            // Create context
            RefactoringContext context = RefactoringContext.builder()
                .componentName(detectComponentName(event))
                .beforePath(beforePath)
                .afterPath(afterPath)
                .testPath(findTestPath(event))
                .build();
            
            // Run validation
            ValidationReport report = validator.validateRefactoring(
                context.getComponentName(),
                context
            );
            
            // Post results to PR
            postValidationResults(event, report);
            
            // Update PR status
            updatePullRequestStatus(event, report);
            
        } catch (Exception e) {
            log.error("Validation pipeline failed", e);
            notificationService.notifyFailure(event, e);
        }
    }
}
```

## Usage Examples

### 1. Command Line Tool

```bash
# Validate ExecutionController refactoring
java -jar refactoring-validator.jar \
  --component ExecutionController \
  --before ./legacy/ExecutionController.java \
  --after ./refactored/execution/ \
  --tests ./src/test/java/execution/

# Validate with specific rules
java -jar refactoring-validator.jar \
  --component SessionManager \
  --before ./legacy/SessionManager.java \
  --after ./refactored/session/ \
  --rules SRP,Dependencies,Performance

# Generate detailed report
java -jar refactoring-validator.jar \
  --component ConfigurationPanel \
  --before ./legacy/ui/ \
  --after ./refactored/ui/ \
  --report-format html,markdown,json \
  --output ./validation-reports/
```

### 2. Gradle Plugin

```gradle
plugins {
    id 'com.brobot.refactoring-validator' version '1.0.0'
}

refactoringValidation {
    validations {
        executionController {
            componentName = 'ExecutionController'
            beforePath = file('src/main/java/legacy/ExecutionController.java')
            afterPath = file('src/main/java/execution/')
            testPath = file('src/test/java/execution/')
            
            rules {
                enable 'SRP', 'Dependencies', 'Performance'
                srpThresholds {
                    maxClassSize = 200
                    maxMethodsPerClass = 10
                }
            }
        }
    }
    
    reports {
        html.enabled = true
        markdown.enabled = true
        json.enabled = true
        outputDir = file('build/validation-reports')
    }
    
    failOnViolation = true
}
```

### 3. IDE Integration

```java
// IntelliJ IDEA Plugin
public class RefactoringValidatorAction extends AnAction {
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        
        // Run validation
        RefactoringContext context = buildContext(project, files);
        ValidationReport report = validator.validateRefactoring(
            "SelectedComponent", 
            context
        );
        
        // Show results in tool window
        RefactoringValidatorToolWindow toolWindow = 
            RefactoringValidatorToolWindow.getInstance(project);
        toolWindow.showReport(report);
    }
}
```

## Benefits

1. **Automated Quality Assurance**: Ensures refactoring improves code quality
2. **Regression Prevention**: Catches breaking changes early
3. **Performance Validation**: Ensures no performance degradation
4. **Continuous Feedback**: Integrates with CI/CD pipeline
5. **Objective Metrics**: Provides quantifiable improvements
6. **Documentation**: Generates reports for stakeholders

This comprehensive validation framework ensures that refactoring efforts achieve their goals while maintaining system stability and performance.