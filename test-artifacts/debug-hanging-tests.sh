#!/bin/bash

# Script to debug hanging tests by running them individually with timeout
# Usage: ./debug-hanging-tests.sh [module] [timeout_seconds]

MODULE=${1:-library}
TIMEOUT=${2:-60}
RESULTS_FILE="test-results-$(date +%Y%m%d-%H%M%S).log"

echo "======================================"
echo "Debugging Hanging Tests"
echo "Module: $MODULE"
echo "Timeout per test: ${TIMEOUT}s"
echo "Results file: $RESULTS_FILE"
echo "======================================"

# Stop any existing Gradle daemons
echo "Stopping Gradle daemons..."
./gradlew --stop

# Function to run a single test with timeout
run_test_with_timeout() {
    local test_class=$1
    local start_time=$(date +%s)
    
    echo -n "Testing $test_class... "
    
    # Run test with timeout
    timeout $TIMEOUT ./gradlew :$MODULE:test --tests "$test_class" --no-daemon --no-build-cache > /tmp/test_output_$$.log 2>&1
    local exit_code=$?
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    if [ $exit_code -eq 124 ]; then
        echo "TIMEOUT (${TIMEOUT}s)" | tee -a $RESULTS_FILE
        echo "  [HANGING] $test_class - Exceeded ${TIMEOUT}s timeout" >> $RESULTS_FILE
        return 1
    elif [ $exit_code -eq 0 ]; then
        echo "PASSED (${duration}s)" | tee -a $RESULTS_FILE
        echo "  [PASSED] $test_class - Completed in ${duration}s" >> $RESULTS_FILE
        return 0
    else
        # Check if it's a compilation error or actual test failure
        if grep -q "compileTestJava FAILED" /tmp/test_output_$$.log; then
            echo "COMPILATION ERROR" | tee -a $RESULTS_FILE
            echo "  [COMPILE ERROR] $test_class" >> $RESULTS_FILE
        elif grep -q "No tests found" /tmp/test_output_$$.log; then
            echo "NO TESTS FOUND" | tee -a $RESULTS_FILE
            echo "  [SKIPPED] $test_class - No tests found" >> $RESULTS_FILE
        else
            echo "FAILED (${duration}s)" | tee -a $RESULTS_FILE
            echo "  [FAILED] $test_class - Test failed in ${duration}s" >> $RESULTS_FILE
        fi
        return 2
    fi
}

# Find all test classes
echo "Finding test classes..."
TEST_CLASSES=$(find $MODULE/src/test/java -name "*Test.java" -type f | \
    sed 's|.*/java/||' | \
    sed 's|/|.|g' | \
    sed 's|\.java||' | \
    sort)

TOTAL_TESTS=$(echo "$TEST_CLASSES" | wc -l)
echo "Found $TOTAL_TESTS test classes"
echo ""

# Initialize counters
PASSED=0
FAILED=0
TIMEOUT_COUNT=0
SKIPPED=0
COMPILE_ERRORS=0

# Run each test
CURRENT=0
for test_class in $TEST_CLASSES; do
    CURRENT=$((CURRENT + 1))
    echo "[$CURRENT/$TOTAL_TESTS] Running $test_class"
    
    if run_test_with_timeout "$test_class"; then
        PASSED=$((PASSED + 1))
    else
        if [ $? -eq 1 ]; then
            TIMEOUT_COUNT=$((TIMEOUT_COUNT + 1))
        else
            # Check the log for specific error type
            if grep -q "COMPILATION ERROR" $RESULTS_FILE | tail -1; then
                COMPILE_ERRORS=$((COMPILE_ERRORS + 1))
            elif grep -q "NO TESTS FOUND" $RESULTS_FILE | tail -1; then
                SKIPPED=$((SKIPPED + 1))
            else
                FAILED=$((FAILED + 1))
            fi
        fi
    fi
    
    # Clean up temp file
    rm -f /tmp/test_output_$$.log
done

echo ""
echo "======================================"
echo "Test Execution Summary"
echo "======================================"
echo "Total Tests: $TOTAL_TESTS"
echo "Passed: $PASSED"
echo "Failed: $FAILED"
echo "Timed Out: $TIMEOUT_COUNT"
echo "Skipped: $SKIPPED"
echo "Compile Errors: $COMPILE_ERRORS"
echo ""
echo "Results saved to: $RESULTS_FILE"

# List hanging tests if any
if [ $TIMEOUT_COUNT -gt 0 ]; then
    echo ""
    echo "HANGING TESTS (exceeded ${TIMEOUT}s timeout):"
    grep "\[HANGING\]" $RESULTS_FILE | sed 's/.*\[HANGING\] /  - /'
fi

# List failed tests if any
if [ $FAILED -gt 0 ]; then
    echo ""
    echo "FAILED TESTS:"
    grep "\[FAILED\]" $RESULTS_FILE | sed 's/.*\[FAILED\] /  - /' | head -10
    if [ $FAILED -gt 10 ]; then
        echo "  ... and $((FAILED - 10)) more"
    fi
fi

exit 0