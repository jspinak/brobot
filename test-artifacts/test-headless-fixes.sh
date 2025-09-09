#!/bin/bash

echo "Testing headless environment fixes..."
echo "======================================"

# Set headless mode for testing
export JAVA_OPTS="-Djava.awt.headless=true"

# Test 1: ConfigurationTest
echo ""
echo "Test 1: ConfigurationTest.testEarlyInitialization"
echo "--------------------------------------------------"
timeout 10 ./gradlew test --tests "ConfigurationTest.testEarlyInitialization" --no-daemon --info 2>&1 | grep -E "(PASSED|FAILED|SKIPPED|Mock mode|Skipping)" | head -20

# Test 2: DatasetCollectionAspectTest
echo ""
echo "Test 2: DatasetCollectionAspectTest.shouldShutdownGracefully"
echo "--------------------------------------------------------------"
timeout 10 ./gradlew test --tests "DatasetCollectionAspectTest.shouldShutdownGracefully" --no-daemon --info 2>&1 | grep -E "(PASSED|FAILED|SKIPPED|shutdown|timeout)" | head -20

# Test 3: DPIScalingTest
echo ""
echo "Test 3: DPIScalingTest.testScalingSettings"
echo "-------------------------------------------"
timeout 10 ./gradlew test --tests "DPIScalingTest.testScalingSettings" --no-daemon --info 2>&1 | grep -E "(PASSED|FAILED|SKIPPED|Skipping DPI|mock mode)" | head -20

echo ""
echo "Test run completed."