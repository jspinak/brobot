#!/usr/bin/env python3
"""
Enhanced test runner for Brobot's 6000+ test suite.
Handles large-scale test execution with batching, parallel processing, and failure recovery.
"""

import subprocess
import sys
import time
import os
import json
import concurrent.futures
from pathlib import Path
from datetime import datetime
from typing import List, Dict, Tuple
import threading
import signal

class TestRunner:
    def __init__(self, module="library", max_workers=4, timeout=60):
        self.module = module
        self.max_workers = max_workers
        self.timeout = timeout
        self.root_dir = Path(__file__).parent.parent.parent
        self.results = {
            "passed": [],
            "failed": [],
            "timed_out": [],
            "skipped": []
        }
        self.start_time = None
        self.lock = threading.Lock()
        
    def run_command(self, cmd: str, timeout: int = None) -> Tuple[int, str, str]:
        """Run a command with timeout and return result."""
        if timeout is None:
            timeout = self.timeout
            
        try:
            result = subprocess.run(
                cmd,
                shell=True,
                capture_output=True,
                text=True,
                timeout=timeout,
                cwd=self.root_dir
            )
            return result.returncode, result.stdout, result.stderr
        except subprocess.TimeoutExpired:
            return -1, "", f"Command timed out after {timeout} seconds"
        except Exception as e:
            return -2, "", str(e)
    
    def get_test_classes(self) -> List[str]:
        """Get list of all test classes in the module."""
        test_dir = self.root_dir / self.module / "src/test/java"
        test_classes = []
        
        for java_file in test_dir.rglob("*Test.java"):
            if "package-info" in str(java_file):
                continue
            relative_path = java_file.relative_to(test_dir)
            class_name = str(relative_path).replace("/", ".").replace(".java", "")
            test_classes.append(class_name)
        
        return sorted(test_classes)
    
    def run_single_test(self, test_class: str) -> Dict:
        """Run a single test class and return results."""
        cmd = f"./gradlew :{self.module}:test --tests '{test_class}' --no-daemon --no-build-cache"
        
        start = time.time()
        returncode, stdout, stderr = self.run_command(cmd)
        duration = time.time() - start
        
        result = {
            "class": test_class,
            "duration": duration,
            "timestamp": datetime.now().isoformat()
        }
        
        if returncode == -1:
            result["status"] = "timeout"
            result["error"] = "Test execution timed out"
        elif returncode == 0:
            result["status"] = "passed"
            # Try to extract test count from output
            if "tests" in stdout:
                import re
                match = re.search(r'(\d+) tests', stdout)
                if match:
                    result["test_count"] = int(match.group(1))
        else:
            result["status"] = "failed"
            result["error"] = stderr[:500] if stderr else "Unknown error"
        
        return result
    
    def run_test_batch(self, test_classes: List[str]) -> List[Dict]:
        """Run a batch of test classes."""
        results = []
        for test_class in test_classes:
            result = self.run_single_test(test_class)
            results.append(result)
            
            # Update global results
            with self.lock:
                if result["status"] == "passed":
                    self.results["passed"].append(test_class)
                elif result["status"] == "failed":
                    self.results["failed"].append(test_class)
                elif result["status"] == "timeout":
                    self.results["timed_out"].append(test_class)
                
                # Print progress
                total_done = len(self.results["passed"]) + len(self.results["failed"]) + len(self.results["timed_out"])
                print(f"[{total_done}/{len(self.all_tests)}] {test_class}: {result['status'].upper()} ({result['duration']:.1f}s)")
        
        return results
    
    def run_parallel(self, test_classes: List[str]) -> Dict:
        """Run tests in parallel batches."""
        # Split tests into batches
        batch_size = max(1, len(test_classes) // (self.max_workers * 4))
        batches = [test_classes[i:i+batch_size] for i in range(0, len(test_classes), batch_size)]
        
        all_results = []
        with concurrent.futures.ThreadPoolExecutor(max_workers=self.max_workers) as executor:
            futures = [executor.submit(self.run_test_batch, batch) for batch in batches]
            
            for future in concurrent.futures.as_completed(futures):
                try:
                    batch_results = future.result()
                    all_results.extend(batch_results)
                except Exception as e:
                    print(f"Error processing batch: {e}")
        
        return {
            "results": all_results,
            "summary": self.results
        }
    
    def compile_tests(self) -> bool:
        """Compile all test classes."""
        print(f"Compiling tests for {self.module}...")
        cmd = f"./gradlew :{self.module}:compileTestJava --no-daemon"
        returncode, stdout, stderr = self.run_command(cmd, timeout=120)
        
        if returncode != 0:
            print(f"Failed to compile tests: {stderr}")
            return False
        
        print("Tests compiled successfully")
        return True
    
    def estimate_total_tests(self) -> int:
        """Estimate total number of test methods (not just classes)."""
        # Run a quick test info task if available
        cmd = f"./gradlew :{self.module}:test --dry-run 2>/dev/null | grep -c '@Test' || echo '0'"
        returncode, stdout, stderr = self.run_command(cmd, timeout=10)
        
        try:
            count = int(stdout.strip())
            if count > 0:
                return count
        except:
            pass
        
        # Fallback: estimate based on average tests per class
        # Assuming average of 20 tests per test class for a large suite
        return len(self.all_tests) * 20
    
    def run(self, mode="parallel", specific_pattern=None):
        """Main entry point for running tests."""
        self.start_time = time.time()
        
        # First compile
        if not self.compile_tests():
            return False
        
        # Get test classes
        self.all_tests = self.get_test_classes()
        
        if specific_pattern:
            self.all_tests = [t for t in self.all_tests if specific_pattern in t]
        
        estimated_total = self.estimate_total_tests()
        
        print(f"\n{'='*80}")
        print(f"Brobot Test Suite Execution")
        print(f"Module: {self.module}")
        print(f"Test Classes: {len(self.all_tests)}")
        print(f"Estimated Test Methods: ~{estimated_total}")
        print(f"Execution Mode: {mode}")
        print(f"Max Workers: {self.max_workers}")
        print(f"Timeout per class: {self.timeout}s")
        print(f"{'='*80}\n")
        
        if mode == "parallel":
            results = self.run_parallel(self.all_tests)
        elif mode == "sequential":
            results = {
                "results": self.run_test_batch(self.all_tests),
                "summary": self.results
            }
        else:
            print(f"Unknown mode: {mode}")
            return False
        
        # Print summary
        self.print_summary(results)
        
        # Save detailed results
        self.save_results(results)
        
        return len(self.results["failed"]) == 0 and len(self.results["timed_out"]) == 0
    
    def print_summary(self, results: Dict):
        """Print test execution summary."""
        elapsed = time.time() - self.start_time
        
        print(f"\n{'='*80}")
        print(f"TEST EXECUTION SUMMARY")
        print(f"{'='*80}")
        print(f"Total Execution Time: {elapsed:.1f} seconds ({elapsed/60:.1f} minutes)")
        print(f"Test Classes Executed: {len(self.all_tests)}")
        print(f"Passed: {len(self.results['passed'])} ({len(self.results['passed'])*100//len(self.all_tests) if self.all_tests else 0}%)")
        print(f"Failed: {len(self.results['failed'])}")
        print(f"Timed Out: {len(self.results['timed_out'])}")
        print(f"Skipped: {len(self.results['skipped'])}")
        
        # Calculate test methods if available
        total_test_methods = sum(r.get("test_count", 0) for r in results["results"] if "test_count" in r)
        if total_test_methods > 0:
            print(f"\nTotal Test Methods Executed: {total_test_methods}")
        
        if self.results["failed"]:
            print(f"\n❌ Failed Tests:")
            for test in self.results["failed"][:10]:  # Show first 10
                print(f"  - {test}")
            if len(self.results["failed"]) > 10:
                print(f"  ... and {len(self.results['failed']) - 10} more")
        
        if self.results["timed_out"]:
            print(f"\n⏱️  Timed Out Tests:")
            for test in self.results["timed_out"][:10]:  # Show first 10
                print(f"  - {test}")
            if len(self.results["timed_out"]) > 10:
                print(f"  ... and {len(self.results['timed_out']) - 10} more")
    
    def save_results(self, results: Dict):
        """Save detailed results to JSON file."""
        output_file = self.root_dir / f"test-results-{self.module}-{datetime.now().strftime('%Y%m%d-%H%M%S')}.json"
        
        with open(output_file, 'w') as f:
            json.dump(results, f, indent=2, default=str)
        
        print(f"\nDetailed results saved to: {output_file}")

def main():
    """Main entry point."""
    import argparse
    
    parser = argparse.ArgumentParser(description='Run Brobot test suite')
    parser.add_argument('module', nargs='?', default='library', choices=['library', 'library-test'],
                        help='Module to test (default: library)')
    parser.add_argument('--mode', default='parallel', choices=['parallel', 'sequential'],
                        help='Execution mode (default: parallel)')
    parser.add_argument('--workers', type=int, default=4,
                        help='Number of parallel workers (default: 4)')
    parser.add_argument('--timeout', type=int, default=60,
                        help='Timeout per test class in seconds (default: 60)')
    parser.add_argument('--pattern', type=str,
                        help='Run only tests matching this pattern')
    parser.add_argument('--retry-failed', action='store_true',
                        help='Retry failed tests once')
    
    args = parser.parse_args()
    
    # Handle Ctrl+C gracefully
    def signal_handler(sig, frame):
        print('\n\nTest execution interrupted by user')
        sys.exit(1)
    
    signal.signal(signal.SIGINT, signal_handler)
    
    # Run tests
    runner = TestRunner(
        module=args.module,
        max_workers=args.workers,
        timeout=args.timeout
    )
    
    success = runner.run(mode=args.mode, specific_pattern=args.pattern)
    
    # Retry failed tests if requested
    if args.retry_failed and runner.results["failed"]:
        print(f"\n{'='*80}")
        print(f"RETRYING {len(runner.results['failed'])} FAILED TESTS")
        print(f"{'='*80}\n")
        
        retry_runner = TestRunner(
            module=args.module,
            max_workers=1,  # Use single worker for retries
            timeout=args.timeout * 2  # Double timeout for retries
        )
        retry_runner.all_tests = runner.results["failed"]
        retry_results = retry_runner.run_test_batch(runner.results["failed"])
        
        print(f"\nRetry Results:")
        print(f"  Passed on retry: {len(retry_runner.results['passed'])}")
        print(f"  Still failing: {len(retry_runner.results['failed'])}")
        
        success = len(retry_runner.results['failed']) == 0
    
    if success:
        print("\n✅ All tests completed successfully!")
        sys.exit(0)
    else:
        print("\n❌ Some tests failed or timed out")
        sys.exit(1)

if __name__ == "__main__":
    main()