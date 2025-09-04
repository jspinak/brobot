#!/usr/bin/env python3
"""
Safe test runner for Brobot that avoids hanging issues.
Runs tests in small batches with aggressive timeouts.
"""

import subprocess
import sys
import time
import os
from pathlib import Path
from typing import List, Dict, Tuple
import signal
import fnmatch
import json
from datetime import datetime

class SafeTestRunner:
    def __init__(self, module="library"):
        self.module = module
        self.root_dir = Path(__file__).parent.parent.parent
        self.results = []
        self.total_passed = 0
        self.total_failed = 0
        self.total_skipped = 0
        
    def discover_tests(self, pattern=None) -> List[str]:
        """Discover all test classes matching the pattern."""
        test_dir = self.root_dir / self.module / "src/test/java"
        test_classes = []
        
        if not test_dir.exists():
            print(f"Error: Test directory not found: {test_dir}")
            return []
        
        for java_file in test_dir.rglob("*Test.java"):
            if "package-info" in str(java_file):
                continue
            relative_path = java_file.relative_to(test_dir)
            class_name = str(relative_path).replace("/", ".").replace(".java", "")
            test_classes.append(class_name)
        
        # Apply pattern filter if provided
        if pattern:
            if '*' in pattern or '?' in pattern:
                test_classes = [t for t in test_classes if fnmatch.fnmatch(t, pattern)]
            else:
                test_classes = [t for t in test_classes if pattern in t]
        
        return sorted(test_classes)
    
    def run_test_class(self, test_class: str, timeout: int = 30) -> Dict:
        """Run a single test class with timeout."""
        print(f"Running: {test_class}...", end=" ", flush=True)
        
        cmd = [
            "./gradlew",
            f":{self.module}:test",
            "--tests", test_class,
            "--no-daemon",
            "--no-build-cache",
            "--quiet"
        ]
        
        start_time = time.time()
        try:
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=timeout,
                cwd=self.root_dir
            )
            duration = time.time() - start_time
            
            if result.returncode == 0:
                print(f"✅ PASSED ({duration:.1f}s)")
                return {"class": test_class, "status": "passed", "duration": duration}
            else:
                # Try to extract failure reason
                error_msg = "Test failed"
                if "FAILED" in result.stdout:
                    lines = result.stdout.split('\n')
                    for i, line in enumerate(lines):
                        if "FAILED" in line and i > 0:
                            error_msg = lines[i-1][:100]
                print(f"❌ FAILED ({duration:.1f}s) - {error_msg}")
                return {"class": test_class, "status": "failed", "duration": duration, "error": error_msg}
                
        except subprocess.TimeoutExpired:
            duration = time.time() - start_time
            print(f"⏱️ TIMEOUT ({timeout}s)")
            # Kill any hanging gradle processes
            subprocess.run(["pkill", "-f", "gradle"], capture_output=True)
            return {"class": test_class, "status": "timeout", "duration": timeout}
        except Exception as e:
            print(f"❌ ERROR: {e}")
            return {"class": test_class, "status": "error", "error": str(e)}
    
    def run_batch(self, test_classes: List[str], batch_size: int = 10, timeout_per_test: int = 30):
        """Run tests in batches to avoid hanging."""
        total = len(test_classes)
        
        print(f"\n{'='*70}")
        print(f"Running {total} test classes in batches of {batch_size}")
        print(f"Timeout per test: {timeout_per_test}s")
        print(f"{'='*70}\n")
        
        for i in range(0, total, batch_size):
            batch = test_classes[i:i+batch_size]
            batch_num = i // batch_size + 1
            total_batches = (total + batch_size - 1) // batch_size
            
            print(f"\nBatch {batch_num}/{total_batches} ({len(batch)} tests):")
            print("-" * 50)
            
            for test_class in batch:
                result = self.run_test_class(test_class, timeout=timeout_per_test)
                self.results.append(result)
                
                # Update counters
                if result["status"] == "passed":
                    self.total_passed += 1
                elif result["status"] == "failed":
                    self.total_failed += 1
                else:
                    self.total_skipped += 1
            
            # Brief pause between batches to let system recover
            time.sleep(1)
    
    def print_summary(self):
        """Print test execution summary."""
        print(f"\n{'='*70}")
        print("TEST EXECUTION SUMMARY")
        print(f"{'='*70}")
        
        total = len(self.results)
        if total == 0:
            print("No tests executed!")
            return
        
        print(f"Total Tests: {total}")
        print(f"✅ Passed: {self.total_passed} ({self.total_passed*100//total}%)")
        print(f"❌ Failed: {self.total_failed}")
        print(f"⏱️ Timeout/Error: {self.total_skipped}")
        
        # Show failed tests
        failed = [r for r in self.results if r["status"] == "failed"]
        if failed:
            print(f"\nFailed Tests:")
            for test in failed[:10]:
                print(f"  - {test['class']}")
                if "error" in test:
                    print(f"    Error: {test['error'][:100]}")
            if len(failed) > 10:
                print(f"  ... and {len(failed) - 10} more")
        
        # Show timeout tests
        timeouts = [r for r in self.results if r["status"] == "timeout"]
        if timeouts:
            print(f"\nTimeout Tests:")
            for test in timeouts[:5]:
                print(f"  - {test['class']}")
            if len(timeouts) > 5:
                print(f"  ... and {len(timeouts) - 5} more")
    
    def save_results(self, filename=None):
        """Save results to JSON file."""
        if filename is None:
            timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
            filename = f"test-results-{self.module}-{timestamp}.json"
        
        filepath = self.root_dir / filename
        with open(filepath, 'w') as f:
            json.dump({
                "module": self.module,
                "timestamp": datetime.now().isoformat(),
                "summary": {
                    "total": len(self.results),
                    "passed": self.total_passed,
                    "failed": self.total_failed,
                    "skipped": self.total_skipped
                },
                "results": self.results
            }, f, indent=2)
        
        print(f"\nResults saved to: {filepath}")
    
def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='Safe Brobot test runner')
    parser.add_argument('module', nargs='?', default='library', 
                        help='Module to test (default: library)')
    parser.add_argument('--pattern', '-p', type=str,
                        help='Pattern to match test classes (supports wildcards)')
    parser.add_argument('--batch-size', '-b', type=int, default=10,
                        help='Number of tests to run in each batch (default: 10)')
    parser.add_argument('--timeout', '-t', type=int, default=30,
                        help='Timeout per test in seconds (default: 30)')
    parser.add_argument('--compile-only', action='store_true',
                        help='Only compile tests, do not run')
    
    args = parser.parse_args()
    
    runner = SafeTestRunner(module=args.module)
    
    # Compile tests first
    if args.compile_only or True:  # Always compile first
        print("Compiling tests...")
        compile_cmd = ["./gradlew", f":{args.module}:compileTestJava", "--no-daemon"]
        try:
            result = subprocess.run(compile_cmd, capture_output=True, text=True, 
                                  timeout=120, cwd=runner.root_dir)
            if result.returncode != 0:
                print("❌ Compilation failed!")
                print(result.stderr)
                sys.exit(1)
            print("✅ Compilation successful!")
            
            if args.compile_only:
                sys.exit(0)
        except subprocess.TimeoutExpired:
            print("❌ Compilation timed out!")
            sys.exit(1)
    
    # Discover tests
    tests = runner.discover_tests(pattern=args.pattern)
    
    if not tests:
        print(f"No tests found matching pattern: {args.pattern}")
        sys.exit(1)
    
    print(f"Found {len(tests)} test classes")
    
    # Run tests
    runner.run_batch(tests, batch_size=args.batch_size, timeout_per_test=args.timeout)
    
    # Print summary
    runner.print_summary()
    
    # Save results
    runner.save_results()
    
    # Exit with appropriate code
    sys.exit(0 if runner.total_failed == 0 else 1)

if __name__ == "__main__":
    main()