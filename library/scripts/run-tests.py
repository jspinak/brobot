#!/usr/bin/env python3
"""
Python test runner for Brobot library tests.
This script runs tests in a more controlled manner to avoid hanging issues.
"""

import subprocess
import sys
import time
import os
from pathlib import Path

def run_command(cmd, timeout=120):
    """Run a command with timeout and return result."""
    try:
        print(f"Running: {cmd}")
        result = subprocess.run(
            cmd,
            shell=True,
            capture_output=True,
            text=True,
            timeout=timeout,
            cwd=Path(__file__).parent.parent.parent  # Go to brobot root
        )
        return result.returncode, result.stdout, result.stderr
    except subprocess.TimeoutExpired:
        print(f"Command timed out after {timeout} seconds")
        return -1, "", "Command timed out"

def run_single_test(test_class, module="library"):
    """Run a single test class."""
    cmd = f"./gradlew :{module}:test --tests '{test_class}' --no-daemon"
    return run_command(cmd, timeout=60)

def get_test_classes(module="library"):
    """Get list of all test classes in the module."""
    test_dir = Path(__file__).parent.parent.parent / module / "src/test/java"
    test_classes = []
    
    for java_file in test_dir.rglob("*Test.java"):
        # Convert file path to class name
        relative_path = java_file.relative_to(test_dir)
        class_name = str(relative_path).replace("/", ".").replace(".java", "")
        test_classes.append(class_name)
    
    return test_classes

def run_all_tests_individually(module="library"):
    """Run all tests individually to avoid hanging."""
    print(f"\n{'='*60}")
    print(f"Running tests for module: {module}")
    print(f"{'='*60}\n")
    
    # First, compile the tests
    print("Compiling tests...")
    returncode, stdout, stderr = run_command(f"./gradlew :{module}:compileTestJava", timeout=60)
    if returncode != 0:
        print(f"Failed to compile tests: {stderr}")
        return False
    
    # Get all test classes
    test_classes = get_test_classes(module)
    print(f"Found {len(test_classes)} test classes\n")
    
    passed = []
    failed = []
    timed_out = []
    
    for i, test_class in enumerate(test_classes, 1):
        print(f"[{i}/{len(test_classes)}] Running: {test_class}")
        returncode, stdout, stderr = run_single_test(test_class, module)
        
        if returncode == -1:
            timed_out.append(test_class)
            print(f"  ❌ TIMEOUT\n")
        elif returncode == 0:
            passed.append(test_class)
            print(f"  ✅ PASSED\n")
        else:
            failed.append(test_class)
            print(f"  ❌ FAILED\n")
            if stderr:
                print(f"  Error: {stderr[:200]}...\n")
        
        # Small delay between tests
        time.sleep(0.5)
    
    # Print summary
    print(f"\n{'='*60}")
    print("TEST SUMMARY")
    print(f"{'='*60}")
    print(f"Total: {len(test_classes)}")
    print(f"Passed: {len(passed)} ({len(passed)*100//len(test_classes) if test_classes else 0}%)")
    print(f"Failed: {len(failed)}")
    print(f"Timed Out: {len(timed_out)}")
    
    if failed:
        print(f"\nFailed Tests:")
        for test in failed:
            print(f"  - {test}")
    
    if timed_out:
        print(f"\nTimed Out Tests:")
        for test in timed_out:
            print(f"  - {test}")
    
    return len(failed) == 0 and len(timed_out) == 0

def run_batch_test(module="library"):
    """Try running all tests at once with a longer timeout."""
    print(f"\nAttempting batch test run for {module}...")
    cmd = f"./gradlew :{module}:test --no-daemon"
    returncode, stdout, stderr = run_command(cmd, timeout=300)
    
    if returncode == 0:
        print("✅ Batch test run successful!")
        return True
    elif returncode == -1:
        print("❌ Batch test run timed out")
        return False
    else:
        print(f"❌ Batch test run failed with code {returncode}")
        return False

def main():
    """Main entry point."""
    # Parse arguments
    if len(sys.argv) > 1:
        mode = sys.argv[1]
    else:
        mode = "individual"
    
    if len(sys.argv) > 2:
        module = sys.argv[2]
    else:
        module = "library"
    
    print(f"Brobot Test Runner")
    print(f"Mode: {mode}")
    print(f"Module: {module}")
    
    if mode == "batch":
        success = run_batch_test(module)
    elif mode == "individual":
        success = run_all_tests_individually(module)
    else:
        print(f"Unknown mode: {mode}")
        print("Usage: python run-tests.py [batch|individual] [library|library-test]")
        sys.exit(1)
    
    if success:
        print("\n✅ All tests completed successfully!")
        sys.exit(0)
    else:
        print("\n❌ Some tests failed or timed out")
        sys.exit(1)

if __name__ == "__main__":
    main()