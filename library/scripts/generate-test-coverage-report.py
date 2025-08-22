#!/usr/bin/env python3
"""
Generate detailed test coverage report and identify priority test targets.
"""

import re
import sys
from pathlib import Path
from typing import List, Tuple, Dict
import json

def parse_jacoco_html(html_path: str) -> List[Tuple[str, int, int]]:
    """Parse JaCoCo HTML report to extract coverage data."""
    packages = []
    
    with open(html_path, 'r') as f:
        html = f.read()
    
    tbody = re.search(r'<tbody>(.*?)</tbody>', html, re.DOTALL)
    if tbody:
        rows = re.findall(r'<tr>(.*?)</tr>', tbody.group(1), re.DOTALL)
        for row in rows:
            # Extract package name
            pkg_match = re.search(r'class="el_package">(.*?)</a>', row)
            if pkg_match:
                pkg = pkg_match.group(1)
                
                # Extract coverage percentage
                cov_match = re.search(r'<td class="ctr2"[^>]*>(\d+)%</td>', row)
                coverage = int(cov_match.group(1)) if cov_match else 0
                
                # Extract instruction counts
                instr_match = re.search(r'title="([0-9,]+)"[^>]*alt="[0-9,]+"/>', row)
                instructions = int(instr_match.group(1).replace(',', '')) if instr_match else 0
                
                packages.append((pkg, coverage, instructions))
    
    return packages

def find_untested_classes(src_dir: str, test_dir: str) -> List[str]:
    """Find classes without corresponding test files."""
    src_path = Path(src_dir)
    test_path = Path(test_dir)
    
    untested = []
    
    for java_file in src_path.rglob("*.java"):
        if "package-info.java" in str(java_file):
            continue
            
        # Get relative path and convert to test path
        rel_path = java_file.relative_to(src_path)
        test_file = test_path / rel_path.parent / f"{rel_path.stem}Test.java"
        
        if not test_file.exists():
            untested.append(str(rel_path))
    
    return untested

def generate_test_stub(class_path: str, package: str, class_name: str) -> str:
    """Generate a test stub for a class."""
    return f"""package {package};

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for {class_name}.
 * 
 * TODO: Implement test cases for:
 * - Normal operation (happy path)
 * - Null input handling
 * - Edge cases
 * - Error conditions
 * - Integration scenarios
 */
@DisplayName("{class_name} Tests")
public class {class_name}Test extends BrobotTestBase {{
    
    private {class_name} {class_name.lower()};
    
    @BeforeEach
    @Override
    public void setupTest() {{
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        // TODO: Initialize {class_name.lower()}
    }}
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {{
        
        @Test
        @DisplayName("Should create instance with default values")
        void shouldCreateWithDefaults() {{
            // TODO: Implement
            fail("Not implemented");
        }}
        
        @Test
        @DisplayName("Should handle null parameters")
        void shouldHandleNullParameters() {{
            // TODO: Implement
            fail("Not implemented");
        }}
    }}
    
    @Nested
    @DisplayName("Core Functionality")
    class CoreFunctionality {{
        
        @Test
        @DisplayName("Should perform main operation")
        void shouldPerformMainOperation() {{
            // TODO: Implement
            fail("Not implemented");
        }}
        
        @Test
        @DisplayName("Should handle edge cases")
        void shouldHandleEdgeCases() {{
            // TODO: Implement
            fail("Not implemented");
        }}
    }}
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {{
        
        @Test
        @DisplayName("Should handle invalid input")
        void shouldHandleInvalidInput() {{
            // TODO: Implement
            fail("Not implemented");
        }}
        
        @Test
        @DisplayName("Should handle exceptional conditions")
        void shouldHandleExceptionalConditions() {{
            // TODO: Implement
            fail("Not implemented");
        }}
    }}
}}
"""

def generate_coverage_report(packages: List[Tuple[str, int, int]], untested: List[str]) -> Dict:
    """Generate comprehensive coverage report."""
    
    # Sort packages by size and coverage
    packages.sort(key=lambda x: (-x[2], x[1]))
    
    # Calculate statistics
    total_instructions = sum(p[2] for p in packages)
    covered_instructions = sum(p[2] * p[1] // 100 for p in packages)
    overall_coverage = covered_instructions * 100 // total_instructions if total_instructions > 0 else 0
    
    # Group by coverage levels
    zero_coverage = [p for p in packages if p[1] == 0]
    low_coverage = [p for p in packages if 0 < p[1] < 30]
    medium_coverage = [p for p in packages if 30 <= p[1] < 60]
    high_coverage = [p for p in packages if p[1] >= 60]
    
    report = {
        "summary": {
            "overall_coverage": overall_coverage,
            "total_packages": len(packages),
            "total_instructions": total_instructions,
            "covered_instructions": covered_instructions,
            "untested_classes": len(untested)
        },
        "coverage_distribution": {
            "zero": len(zero_coverage),
            "low": len(low_coverage),
            "medium": len(medium_coverage),
            "high": len(high_coverage)
        },
        "priority_targets": {
            "largest_uncovered": [(p[0], p[2]) for p in zero_coverage[:10]],
            "largest_low_coverage": [(p[0], p[1], p[2]) for p in low_coverage[:10]],
            "quick_wins": [(p[0], p[1], p[2]) for p in packages if p[2] < 500 and p[1] < 30][:10]
        },
        "untested_classes": untested[:20]
    }
    
    return report

def print_report(report: Dict):
    """Print formatted coverage report."""
    print("=" * 80)
    print("CODE COVERAGE ANALYSIS REPORT")
    print("=" * 80)
    print()
    
    summary = report["summary"]
    print(f"Overall Coverage: {summary['overall_coverage']}%")
    print(f"Total Instructions: {summary['total_instructions']:,}")
    print(f"Covered Instructions: {summary['covered_instructions']:,}")
    print(f"Total Packages: {summary['total_packages']}")
    print(f"Untested Classes: {summary['untested_classes']}")
    print()
    
    dist = report["coverage_distribution"]
    print("Coverage Distribution:")
    print(f"  Zero Coverage (0%): {dist['zero']} packages")
    print(f"  Low Coverage (1-29%): {dist['low']} packages")
    print(f"  Medium Coverage (30-59%): {dist['medium']} packages")
    print(f"  High Coverage (60%+): {dist['high']} packages")
    print()
    
    print("Priority Targets - Largest Uncovered Packages:")
    print("-" * 80)
    for pkg, size in report["priority_targets"]["largest_uncovered"]:
        print(f"  {pkg:<60} {size:>10,} instructions")
    print()
    
    print("Quick Wins - Small Packages with Low Coverage:")
    print("-" * 80)
    for pkg, cov, size in report["priority_targets"]["quick_wins"][:5]:
        print(f"  {pkg:<50} {cov:>3}% {size:>6,} instructions")
    print()
    
    if report["untested_classes"]:
        print("Sample Untested Classes (showing first 10):")
        print("-" * 80)
        for cls in report["untested_classes"][:10]:
            print(f"  {cls}")

def main():
    # Paths
    jacoco_html = "build/jacocoHtml/index.html"
    src_dir = "src/main/java"
    test_dir = "src/test/java"
    
    # Parse coverage data
    packages = parse_jacoco_html(jacoco_html)
    
    # Find untested classes
    untested = find_untested_classes(src_dir, test_dir)
    
    # Generate report
    report = generate_coverage_report(packages, untested)
    
    # Print report
    print_report(report)
    
    # Save JSON report
    with open("coverage-analysis.json", "w") as f:
        json.dump(report, f, indent=2)
    
    print()
    print("Full report saved to: coverage-analysis.json")
    
    # Return exit code based on coverage
    if report["summary"]["overall_coverage"] < 60:
        sys.exit(1)  # Fail if coverage is below 60%
    
    return 0

if __name__ == "__main__":
    main()