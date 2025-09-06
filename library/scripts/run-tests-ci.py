#!/usr/bin/env python3
"""
CI-optimized test runner for Brobot.
Designed to prevent timeouts and provide better progress reporting in CI/CD environments.
"""

import subprocess
import sys
import time
import os
import json
import platform
import signal
from pathlib import Path
from datetime import datetime
from typing import List, Dict, Tuple

class CITestRunner:
    def __init__(self, module="library", timeout=30, batch_size=10):
        self.module = module
        self.timeout = timeout
        self.batch_size = batch_size
        self.root_dir = Path(__file__).parent.parent.parent
        self.is_ci = os.environ.get('CI', 'false').lower() == 'true'
        self.is_github_actions = os.environ.get('GITHUB_ACTIONS', 'false').lower() == 'true'
        self.is_macos = platform.system() == "Darwin"
        
        # Results tracking
        self.total_tests = 0
        self.passed = 0
        self.failed = 0
        self.skipped = 0
        self.timed_out = 0
        self.start_time = time.time()
        
        # Determine gradle command
        if platform.system() == "Windows":
            # On Windows, use gradlew.bat without ./ prefix
            if (self.root_dir / "gradlew.bat").exists():
                self.gradle_cmd = "gradlew.bat"
            else:
                self.gradle_cmd = "gradlew"
        else:
            self.gradle_cmd = "./gradlew"
        
        # CI-specific environment setup
        if self.is_ci:
            self.setup_ci_environment()
    
    def setup_ci_environment(self):
        """Configure environment for CI execution."""
        os.environ['GRADLE_OPTS'] = '-Dorg.gradle.daemon=false -Dorg.gradle.parallel=false'
        os.environ['JAVA_TOOL_OPTIONS'] = '-Djava.awt.headless=true'
        
        # macOS-specific settings
        if self.is_macos:
            os.environ['JAVA_TOOL_OPTIONS'] += ' -Djava.awt.headless=true -Dapple.awt.UIElement=true'
            # Reduce memory usage on macOS CI runners
            os.environ['GRADLE_OPTS'] += ' -Xmx1g'
    
    def print_progress(self, message: str):
        """Print progress message with timestamp for CI visibility."""
        timestamp = datetime.now().strftime('%H:%M:%S')
        print(f"[{timestamp}] {message}", flush=True)
        
        # GitHub Actions specific progress indicator
        if self.is_github_actions:
            print(f"::debug::{message}")
    
    def run_command_with_timeout(self, cmd: str, timeout: int = None) -> Tuple[int, str]:
        """Run command with timeout and progress reporting."""
        if timeout is None:
            timeout = self.timeout
        
        self.print_progress(f"Running: {cmd}")
        
        try:
            # Start process
            process = subprocess.Popen(
                cmd,
                shell=True,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                cwd=self.root_dir,
                preexec_fn=os.setsid if platform.system() != "Windows" else None
            )
            
            # Monitor with timeout
            output = []
            start_time = time.time()
            last_output_time = start_time
            
            while True:
                # Check for timeout
                elapsed = time.time() - start_time
                if elapsed > timeout:
                    self.print_progress(f"Timeout after {elapsed:.1f}s, killing process...")
                    if platform.system() == "Windows":
                        try:
                            subprocess.run(['taskkill', '/F', '/T', '/PID', str(process.pid)], 
                                         capture_output=True, check=False)
                        except Exception as e:
                            self.print_progress(f"Failed to kill process: {e}")
                            process.terminate()
                    else:
                        os.killpg(os.getpgid(process.pid), signal.SIGTERM)
                    return -1, f"Timeout after {timeout}s"
                
                # Check for output (non-blocking)
                line = process.stdout.readline()
                if line:
                    output.append(line)
                    last_output_time = time.time()
                    
                    # Show progress every 10 lines in CI
                    if len(output) % 10 == 0:
                        self.print_progress(f"Tests running... ({len(output)} lines of output)")
                
                # Check if process finished
                if process.poll() is not None:
                    # Read remaining output
                    remaining = process.stdout.read()
                    if remaining:
                        output.append(remaining)
                    break
                
                # Brief sleep to prevent CPU spinning
                time.sleep(0.1)
            
            return process.returncode, ''.join(output)
            
        except Exception as e:
            self.print_progress(f"Error: {e}")
            return -2, str(e)
    
    def compile_tests(self) -> bool:
        """Compile tests before running."""
        self.print_progress(f"Compiling tests for {self.module}...")
        
        # On Windows CI, add extra JVM memory settings
        extra_opts = ""
        if platform.system() == "Windows" and self.is_ci:
            extra_opts = " -Dorg.gradle.jvmargs=-Xmx2g"
        
        cmd = f"{self.gradle_cmd} :{self.module}:compileTestJava --no-daemon{extra_opts}"
        returncode, output = self.run_command_with_timeout(cmd, timeout=300)
        
        if returncode != 0:
            # Show more output for debugging
            self.print_progress(f"Compilation failed with exit code {returncode}")
            self.print_progress(f"Last 1000 chars of output: {output[-1000:]}")
            return False
        
        self.print_progress("Compilation successful")
        return True
    
    def get_test_classes(self) -> List[str]:
        """Get all test classes, excluding problematic ones in CI."""
        test_dir = self.root_dir / self.module / "src/test/java"
        test_classes = []
        
        # Patterns to exclude in CI
        exclude_patterns = []
        if self.is_ci:
            exclude_patterns = [
                "JavaFx", "JavaFX", "javafx",  # JavaFX tests
                "VisibleWindow",                # GUI tests
                "Screenshot",                    # Screenshot tests that might hang
                "Debug",                        # Debug tests
                "Performance",                  # Performance tests
                "Benchmark"                     # Benchmark tests
            ]
            
            # macOS-specific exclusions
            if self.is_macos:
                exclude_patterns.extend([
                    "Display", "Screen", "Monitor"
                ])
        
        for java_file in test_dir.rglob("*Test.java"):
            file_str = str(java_file)
            
            # Skip excluded patterns
            if any(pattern in file_str for pattern in exclude_patterns):
                continue
                
            if "package-info" in file_str:
                continue
                
            relative_path = java_file.relative_to(test_dir)
            class_name = str(relative_path).replace("/", ".").replace("\\", ".").replace(".java", "")
            test_classes.append(class_name)
        
        return sorted(test_classes)
    
    def run_test_batch(self, test_classes: List[str]) -> Dict:
        """Run a batch of test classes."""
        if not test_classes:
            return {"passed": 0, "failed": 0, "skipped": 0}
        
        # Build test filter
        test_filter = " --tests ".join([f"'{tc}'" for tc in test_classes])
        cmd = f"{self.gradle_cmd} :{self.module}:test --tests {test_filter} --no-daemon --continue"
        
        returncode, output = self.run_command_with_timeout(cmd)
        
        # Parse results
        result = {"passed": 0, "failed": 0, "skipped": 0}
        
        if returncode == -1:
            self.print_progress(f"Batch timed out: {test_classes[0]}...")
            result["failed"] = len(test_classes)
        elif "BUILD SUCCESSFUL" in output:
            result["passed"] = len(test_classes)
        else:
            # Try to parse actual results
            if "tests completed" in output:
                import re
                match = re.search(r'(\d+) tests completed, (\d+) failed', output)
                if match:
                    result["passed"] = int(match.group(1)) - int(match.group(2))
                    result["failed"] = int(match.group(2))
            else:
                result["failed"] = len(test_classes)
        
        return result
    
    def run_tests(self):
        """Run all tests with CI optimizations."""
        self.print_progress(f"Starting CI test run for {self.module}")
        self.print_progress(f"Environment: CI={self.is_ci}, GitHub={self.is_github_actions}, macOS={self.is_macos}")
        
        # Compile first
        if not self.compile_tests():
            self.print_progress("Compilation failed, aborting")
            return 1
        
        # Get test classes
        test_classes = self.get_test_classes()
        self.total_tests = len(test_classes)
        self.print_progress(f"Found {self.total_tests} test classes")
        
        # Run in batches
        for i in range(0, len(test_classes), self.batch_size):
            batch = test_classes[i:i + self.batch_size]
            batch_num = i // self.batch_size + 1
            total_batches = (len(test_classes) + self.batch_size - 1) // self.batch_size
            
            self.print_progress(f"Running batch {batch_num}/{total_batches} ({len(batch)} tests)")
            
            results = self.run_test_batch(batch)
            self.passed += results["passed"]
            self.failed += results["failed"]
            self.skipped += results.get("skipped", 0)
            
            # Progress update
            elapsed = time.time() - self.start_time
            self.print_progress(f"Progress: {i + len(batch)}/{self.total_tests} tests, "
                              f"Passed: {self.passed}, Failed: {self.failed}, "
                              f"Time: {elapsed:.1f}s")
            
            # Keep CI alive with periodic output
            if self.is_github_actions and batch_num % 5 == 0:
                print(f"::notice::Test progress: {i + len(batch)}/{self.total_tests} completed")
        
        # Final summary
        self.print_summary()
        
        return 0 if self.failed == 0 else 1
    
    def print_summary(self):
        """Print test execution summary."""
        elapsed = time.time() - self.start_time
        
        print("\n" + "=" * 80)
        print("TEST EXECUTION SUMMARY")
        print("=" * 80)
        print(f"Module: {self.module}")
        print(f"Total Test Classes: {self.total_tests}")
        print(f"Passed: {self.passed}")
        print(f"Failed: {self.failed}")
        print(f"Skipped: {self.skipped}")
        print(f"Timed Out: {self.timed_out}")
        print(f"Total Time: {elapsed:.1f}s ({elapsed/60:.1f} minutes)")
        print("=" * 80)
        
        if self.is_github_actions:
            # GitHub Actions summary
            print(f"::notice::Test Results - Passed: {self.passed}, Failed: {self.failed}")
            if self.failed > 0:
                print(f"::error::Tests failed: {self.failed} test classes")

def main():
    """Main entry point."""
    import argparse
    
    parser = argparse.ArgumentParser(description='CI-optimized test runner for Brobot')
    parser.add_argument('module', help='Module to test (library, library-test, etc.)')
    parser.add_argument('--timeout', type=int, default=30, help='Timeout per test batch (seconds)')
    parser.add_argument('--batch-size', type=int, default=10, help='Number of tests per batch')
    
    args = parser.parse_args()
    
    runner = CITestRunner(
        module=args.module,
        timeout=args.timeout,
        batch_size=args.batch_size
    )
    
    sys.exit(runner.run_tests())

if __name__ == "__main__":
    main()