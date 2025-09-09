#!/bin/bash

# Check for tests that might use Spring context and cause hanging
echo "Checking for Spring-related tests that might cause hanging..."
echo "============================================================"

# Search for potentially problematic patterns in test files
echo ""
echo "1. Tests using @SpringBootTest (should be in library-test module):"
grep -r "@SpringBootTest" library/src/test/java --include="*.java" | head -10

echo ""
echo "2. Tests using ApplicationContextRunner (should be in library-test module):"
grep -r "ApplicationContextRunner" library/src/test/java --include="*.java" | head -10

echo ""
echo "3. Tests using @Autowired in test classes (might cause issues):"
grep -r "@Autowired" library/src/test/java --include="*.java" | head -10

echo ""
echo "4. Tests with @PostConstruct (can cause blocking):"
grep -r "@PostConstruct" library/src/test/java --include="*.java" | head -10

echo ""
echo "5. Tests using TestConfiguration (Spring context):"
grep -r "@TestConfiguration" library/src/test/java --include="*.java" | head -10

echo ""
echo "6. Tests with MockBean (Spring context):"
grep -r "@MockBean" library/src/test/java --include="*.java" | head -10

echo ""
echo "7. Tests that might block on startup:"
grep -r "BrobotStartup\|InitializationOrchestrator\|AutoConfiguration" library/src/test/java --include="*.java" | head -10

echo ""
echo "============================================================"
echo "Summary:"
echo "If any of these patterns are found in the library module tests,"
echo "they should be moved to library-test module or refactored to"
echo "avoid Spring context initialization which causes hanging."