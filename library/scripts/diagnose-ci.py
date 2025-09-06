#!/usr/bin/env python3
"""
Diagnostic script for CI/CD test execution issues.
Helps identify why tests timeout or hang in CI environments.
"""

import subprocess
import platform
import os
import sys
import json
from pathlib import Path

def run_command(cmd, timeout=10):
    """Run command and capture output."""
    try:
        result = subprocess.run(
            cmd, 
            shell=True, 
            capture_output=True, 
            text=True, 
            timeout=timeout
        )
        return result.returncode, result.stdout, result.stderr
    except subprocess.TimeoutExpired:
        return -1, "", f"Command timed out after {timeout}s"
    except Exception as e:
        return -2, "", str(e)

def diagnose_environment():
    """Diagnose CI environment issues."""
    print("=" * 80)
    print("CI ENVIRONMENT DIAGNOSTICS")
    print("=" * 80)
    
    # System information
    print("\n## System Information")
    print(f"Platform: {platform.system()} {platform.release()}")
    print(f"Machine: {platform.machine()}")
    print(f"Python: {sys.version}")
    
    # Environment variables
    print("\n## CI Environment Variables")
    ci_vars = ['CI', 'GITHUB_ACTIONS', 'RUNNER_OS', 'RUNNER_ARCH', 
               'JAVA_HOME', 'GRADLE_OPTS', 'JAVA_TOOL_OPTIONS']
    for var in ci_vars:
        value = os.environ.get(var, 'Not set')
        print(f"{var}: {value}")
    
    # Java version
    print("\n## Java Configuration")
    code, stdout, stderr = run_command("java -version", timeout=5)
    if code == 0:
        print(f"Java version: {stderr if stderr else stdout}")
    else:
        print(f"Java check failed: {stderr}")
    
    # Gradle status
    print("\n## Gradle Status")
    gradle_cmd = "./gradlew" if os.path.exists("gradlew") else "gradle"
    
    # Check for running daemons
    code, stdout, stderr = run_command(f"{gradle_cmd} --status", timeout=10)
    if code == 0:
        print("Gradle daemons:")
        print(stdout[:500] if stdout else "No daemons running")
    else:
        print(f"Gradle status check failed: {stderr}")
    
    # Memory status
    print("\n## Memory Status")
    if platform.system() == "Darwin":  # macOS
        code, stdout, stderr = run_command("vm_stat", timeout=5)
        if code == 0:
            print(stdout[:500])
    elif platform.system() == "Linux":
        code, stdout, stderr = run_command("free -h", timeout=5)
        if code == 0:
            print(stdout)
    
    # Process information
    print("\n## Java Processes")
    if platform.system() == "Windows":
        cmd = "wmic process where \"name like '%java%'\" get processid,commandline"
    else:
        cmd = "ps aux | grep java | grep -v grep"
    
    code, stdout, stderr = run_command(cmd, timeout=5)
    if code == 0 and stdout:
        print(stdout[:1000])
    else:
        print("No Java processes found")
    
    # Test for headless mode
    print("\n## Headless Mode Test")
    test_code = """
import java.awt.GraphicsEnvironment;
public class HeadlessTest {
    public static void main(String[] args) {
        System.out.println("Headless: " + GraphicsEnvironment.isHeadless());
        System.out.println("Display: " + System.getenv("DISPLAY"));
    }
}
"""
    
    # Write and compile test
    with open("HeadlessTest.java", "w") as f:
        f.write(test_code)
    
    code, stdout, stderr = run_command("javac HeadlessTest.java", timeout=10)
    if code == 0:
        code, stdout, stderr = run_command("java HeadlessTest", timeout=5)
        if code == 0:
            print(stdout)
        else:
            print(f"Headless test failed: {stderr}")
    else:
        print(f"Failed to compile headless test: {stderr}")
    
    # Clean up
    try:
        os.remove("HeadlessTest.java")
        os.remove("HeadlessTest.class")
    except:
        pass
    
    # Check for problematic test patterns
    print("\n## Problematic Test Patterns")
    test_dir = Path("library/src/test/java")
    if test_dir.exists():
        problematic = []
        patterns = ["JavaFX", "JavaFx", "Display", "Screenshot", "Robot"]
        
        for java_file in test_dir.rglob("*Test.java"):
            content = java_file.read_text()
            for pattern in patterns:
                if pattern in content:
                    problematic.append((java_file.name, pattern))
        
        if problematic:
            print("Tests that might cause issues in CI:")
            for file, pattern in problematic[:10]:
                print(f"  - {file}: Contains '{pattern}'")
            if len(problematic) > 10:
                print(f"  ... and {len(problematic) - 10} more")
        else:
            print("No obviously problematic test patterns found")
    
    # Recent test failures
    print("\n## Recent Test Results")
    for result_file in Path(".").glob("test-results-*.json"):
        try:
            with open(result_file) as f:
                data = json.load(f)
                summary = data.get("summary", {})
                print(f"\nFile: {result_file.name}")
                print(f"  Passed: {len(summary.get('passed', []))}")
                print(f"  Failed: {len(summary.get('failed', []))}")
                print(f"  Timed out: {len(summary.get('timed_out', []))}")
                
                if summary.get('failed'):
                    print("  Failed tests:")
                    for test in summary['failed'][:5]:
                        print(f"    - {test}")
        except:
            pass
    
    print("\n" + "=" * 80)
    print("DIAGNOSTICS COMPLETE")
    print("=" * 80)

def suggest_fixes():
    """Suggest fixes based on environment."""
    print("\n## Suggested Fixes")
    
    is_ci = os.environ.get('CI', '').lower() == 'true'
    is_macos = platform.system() == "Darwin"
    is_github = os.environ.get('GITHUB_ACTIONS', '').lower() == 'true'
    
    if is_ci and is_macos:
        print("For macOS CI environments:")
        print("1. Use --no-daemon flag with Gradle")
        print("2. Set JAVA_TOOL_OPTIONS=-Djava.awt.headless=true")
        print("3. Reduce batch size to 3-5 tests")
        print("4. Increase timeout to 30-45 seconds per batch")
        print("5. Clear Gradle caches before running")
        print("6. Use Python test runner script instead of direct Gradle")
    
    if is_github:
        print("\nFor GitHub Actions:")
        print("1. Use 'continue-on-error: true' for test steps")
        print("2. Split tests into smaller jobs")
        print("3. Use matrix strategy for parallelization")
        print("4. Upload artifacts even on failure")
        print("5. Set explicit timeout-minutes on each step")
    
    print("\nGeneral recommendations:")
    print("- Exclude GUI/display tests in CI with @DisabledIfSystemProperty")
    print("- Use mock mode for all tests (extend BrobotTestBase)")
    print("- Implement proper test timeouts with @Timeout annotation")
    print("- Use TestSynchronization utility instead of Thread.sleep")

if __name__ == "__main__":
    diagnose_environment()
    suggest_fixes()
    
    # Exit with special code to indicate diagnostic run
    sys.exit(42)  # Special exit code for diagnostics