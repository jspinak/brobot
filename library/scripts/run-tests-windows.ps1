# Brobot PowerShell Test Runner for Windows
# Optimized for high-RAM systems (16GB+)

Write-Host "=========================================="
Write-Host "Brobot Windows Test Runner (PowerShell)"
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Configure JVM for maximum memory usage
$env:JAVA_OPTS = "-Xmx12G -Xms4G -XX:MaxMetaspaceSize=2G"
$env:JAVA_OPTS += " -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
$env:JAVA_OPTS += " -XX:+ParallelRefProcEnabled -XX:+DisableExplicitGC"
$env:JAVA_OPTS += " -XX:+AlwaysPreTouch -XX:G1HeapRegionSize=16M"

# Gradle options
$env:GRADLE_OPTS = $env:JAVA_OPTS + " -Dorg.gradle.daemon=false -Dorg.gradle.parallel=true"

Write-Host "Configuration:" -ForegroundColor Green
Write-Host "- Max Heap: 12GB (adjust based on your RAM)"
Write-Host "- Min Heap: 4GB"
Write-Host "- Parallel Workers: 8"
Write-Host "- Fork Every: 100 tests"
Write-Host ""

# Function to run tests with specific filters
function Run-BrobotTests {
    param(
        [string]$TestFilter = "*",
        [bool]$ExcludeDebug = $true
    )
    
    $excludePatterns = @()
    
    if ($ExcludeDebug) {
        # Exclude problematic test categories
        $excludePatterns = @(
            "*Debug*Test",
            "*Diagnostic*Test", 
            "*DPIScaling*Test",
            "*Resolution*Test",
            "*ScreenCapture*Test",
            "*IDEScreenshot*Test",
            "*Monitor*Test"
        )
    }
    
    Write-Host "Cleaning previous test results..." -ForegroundColor Yellow
    ./gradlew clean
    
    Write-Host ""
    Write-Host "Running tests..." -ForegroundColor Green
    Write-Host "This may take 2-5 minutes for 5722+ tests..."
    Write-Host ""
    
    $testArgs = @(
        "test",
        "-Dorg.gradle.jvmargs=`"$env:JAVA_OPTS`"",
        "-PmaxParallelForks=8",
        "-PforkEvery=100",
        "--continue",
        "--parallel",
        "--max-workers=8",
        "--no-daemon"
    )
    
    # Add exclusions
    foreach ($pattern in $excludePatterns) {
        $testArgs += "--tests"
        $testArgs += "!$pattern"
    }
    
    # Add inclusion filter if specified
    if ($TestFilter -ne "*") {
        $testArgs += "--tests"
        $testArgs += $TestFilter
    }
    
    & ./gradlew $testArgs
    
    return $LASTEXITCODE
}

# Main execution
$startTime = Get-Date

try {
    # Run main test suite (excluding problematic tests)
    $exitCode = Run-BrobotTests -ExcludeDebug $true
    
    $endTime = Get-Date
    $duration = $endTime - $startTime
    
    Write-Host ""
    Write-Host "=========================================="
    
    if ($exitCode -eq 0) {
        Write-Host "All tests passed successfully!" -ForegroundColor Green
    } else {
        Write-Host "Some tests failed." -ForegroundColor Yellow
        Write-Host "Analyzing failures..." -ForegroundColor Yellow
        
        # Parse test report for failures
        $reportPath = "build/reports/tests/test/index.html"
        if (Test-Path $reportPath) {
            $content = Get-Content $reportPath -Raw
            if ($content -match 'class="counter">(\d+)</div>.*?<p>failures</p>') {
                $failures = $matches[1]
                Write-Host "Failed tests: $failures" -ForegroundColor Red
            }
        }
    }
    
    Write-Host "==========================================" 
    Write-Host ""
    Write-Host "Execution time: $($duration.TotalMinutes.ToString('0.00')) minutes"
    Write-Host "Test report: build\reports\tests\test\index.html"
    Write-Host ""
    
    # Ask about coverage report
    $runCoverage = Read-Host "Generate coverage report? (y/n)"
    if ($runCoverage -eq "y") {
        Write-Host ""
        Write-Host "Generating JaCoCo coverage report..." -ForegroundColor Green
        ./gradlew jacocoTestReport
        Write-Host "Coverage report: build\jacocoHtml\index.html"
    }
    
} catch {
    Write-Host "Error occurred: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")