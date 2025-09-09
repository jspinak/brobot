#!/bin/bash
# Fast test execution script for Brobot library
# Target: < 10 minute execution for all tests

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Brobot Fast Test Execution${NC}"
echo -e "${GREEN}========================================${NC}"

# Function to print execution time
print_time() {
    local duration=$1
    local minutes=$((duration / 60))
    local seconds=$((duration % 60))
    echo -e "${YELLOW}Execution time: ${minutes}m ${seconds}s${NC}"
}

# Clean previous test results
echo -e "\n${YELLOW}Cleaning previous test results...${NC}"
./gradlew clean --no-daemon -q

# Enable Gradle build cache
export GRADLE_OPTS="-Dorg.gradle.caching=true -Dorg.gradle.parallel=true -Dorg.gradle.workers.max=16"

# Run tests based on argument
MODE=${1:-parallel}

case $MODE in
    fast)
        echo -e "\n${GREEN}Running fast unit tests only...${NC}"
        START_TIME=$(date +%s)
        ./gradlew :library:fastTest --no-daemon --build-cache --parallel
        END_TIME=$(date +%s)
        ;;
    
    parallel)
        echo -e "\n${GREEN}Running all tests in parallel mode...${NC}"
        START_TIME=$(date +%s)
        ./gradlew :library:parallelTest --no-daemon --build-cache --parallel
        END_TIME=$(date +%s)
        ;;
    
    category)
        CATEGORY=${2:-unit}
        echo -e "\n${GREEN}Running ${CATEGORY} tests...${NC}"
        START_TIME=$(date +%s)
        ./gradlew :library:test${CATEGORY^} --no-daemon --build-cache --parallel
        END_TIME=$(date +%s)
        ;;
    
    profile)
        echo -e "\n${GREEN}Profiling test execution times...${NC}"
        START_TIME=$(date +%s)
        ./gradlew :library:profileTests --no-daemon --profile
        END_TIME=$(date +%s)
        echo -e "\n${YELLOW}Check build/reports/profile for detailed timing${NC}"
        ;;
    
    split)
        echo -e "\n${GREEN}Running tests in split mode for maximum parallelization...${NC}"
        START_TIME=$(date +%s)
        
        # Run different test categories in parallel using background processes
        echo -e "${YELLOW}Starting unit tests...${NC}"
        ./gradlew :library:testUnit --no-daemon --build-cache &
        PID1=$!
        
        echo -e "${YELLOW}Starting action tests...${NC}"
        ./gradlew :library:testAction --no-daemon --build-cache &
        PID2=$!
        
        echo -e "${YELLOW}Starting analysis tests...${NC}"
        ./gradlew :library:testAnalysis --no-daemon --build-cache &
        PID3=$!
        
        echo -e "${YELLOW}Starting config tests...${NC}"
        ./gradlew :library:testConfig --no-daemon --build-cache &
        PID4=$!
        
        # Wait for all background processes
        echo -e "\n${YELLOW}Waiting for all test suites to complete...${NC}"
        wait $PID1 $PID2 $PID3 $PID4
        
        END_TIME=$(date +%s)
        ;;
    
    *)
        echo -e "${RED}Usage: $0 [fast|parallel|category <name>|profile|split]${NC}"
        echo "  fast     - Run only fast unit tests"
        echo "  parallel - Run all tests in parallel (default)"
        echo "  category - Run specific test category (unit/integration/action/analysis/config)"
        echo "  profile  - Profile test execution times"
        echo "  split    - Run test categories in parallel processes"
        exit 1
        ;;
esac

# Calculate and display execution time
DURATION=$((END_TIME - START_TIME))
print_time $DURATION

# Check if execution time is under 10 minutes
if [ $DURATION -lt 600 ]; then
    echo -e "${GREEN}✓ Target achieved: Execution time under 10 minutes!${NC}"
else
    echo -e "${YELLOW}⚠ Execution time exceeded 10 minutes. Consider using 'fast' or 'split' mode.${NC}"
fi

# Display test report location
echo -e "\n${GREEN}Test reports available at:${NC}"
echo "  library/build/reports/tests/test/index.html"

# Check test results
if [ -f "library/build/test-results/test/TEST-*.xml" ]; then
    FAILED=$(grep -l 'failures="[1-9]' library/build/test-results/test/*.xml 2>/dev/null | wc -l)
    if [ "$FAILED" -gt 0 ]; then
        echo -e "${RED}✗ $FAILED test suites have failures${NC}"
        exit 1
    else
        echo -e "${GREEN}✓ All tests passed!${NC}"
    fi
fi