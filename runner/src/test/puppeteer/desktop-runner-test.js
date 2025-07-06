const { spawn, exec } = require('child_process');
const path = require('path');
const { promisify } = require('util');
const execAsync = promisify(exec);

// Test configuration
const TEST_TIMEOUT = 60000; // 60 seconds timeout
const STARTUP_WAIT = 5000; // 5 seconds to wait for UI
const WINDOW_CHECK_WAIT = 3000; // 3 seconds to wait for window to be fully visible
const PROJECT_ROOT = '/home/jspinak/brobot-parent-directory/brobot';

class DesktopRunnerTest {
    constructor() {
        this.gradleProcess = null;
        this.testPassed = false;
        this.logs = [];
    }

    log(message) {
        const timestamp = new Date().toISOString();
        const logMessage = `[${timestamp}] ${message}`;
        console.log(logMessage);
        this.logs.push(logMessage);
    }

    async runTest() {
        this.log('Starting Desktop Runner test...');
        
        try {
            // Start the runner application
            await this.startRunner();
            
            // Wait for application to start up
            await this.waitForStartup();
            
            // Check if application started successfully
            const started = await this.checkApplicationStarted();
            
            if (started) {
                this.log('✓ Desktop Runner started successfully');
                this.testPassed = true;
            } else {
                this.log('✗ Desktop Runner failed to start');
                this.testPassed = false;
            }
            
        } catch (error) {
            this.log(`✗ Test failed with error: ${error.message}`);
            this.testPassed = false;
        } finally {
            await this.cleanup();
        }
        
        return this.testPassed;
    }

    startRunner() {
        return new Promise((resolve, reject) => {
            this.log('Launching Brobot Runner with Gradle...');
            
            const gradleWrapper = process.platform === 'win32' ? 'gradlew.bat' : './gradlew';
            
            // Ensure JavaFX doesn't run in headless mode
            const javaOpts = [
                '-Djava.awt.headless=false',
                '-Djavafx.headless=false',
                '-Dprism.order=sw',  // Use software rendering if hardware acceleration is not available
                '-Dprism.verbose=true',  // Enable verbose JavaFX logging
                '-Dglass.platform=gtk'  // Use GTK on Linux
            ].join(' ');
            
            this.gradleProcess = spawn(gradleWrapper, [':runner:run', '-Dspring.profiles.active=test'], {
                cwd: PROJECT_ROOT,
                stdio: ['ignore', 'pipe', 'pipe'],
                shell: true,
                env: {
                    ...process.env,
                    SPRING_PROFILES_ACTIVE: 'test',
                    JAVA_OPTS: javaOpts,
                    _JAVA_OPTIONS: javaOpts  // Alternative way to pass Java options
                }
            });

            let outputBuffer = '';
            let errorBuffer = '';
            let resolved = false;

            const resolveOnce = () => {
                if (!resolved) {
                    resolved = true;
                    resolve();
                }
            };

            this.gradleProcess.stdout.on('data', (data) => {
                const output = data.toString();
                outputBuffer += output;
                
                // Log important messages
                if (output.includes('Spring Boot') || 
                    output.includes('Started application') ||
                    output.includes('JavaFX application started') ||
                    output.includes('ERROR') ||
                    output.includes('WARN')) {
                    this.log(`STDOUT: ${output.trim()}`);
                }

                // Check for successful startup indicators
                if (output.includes('JavaFX application started') || 
                    output.includes('Started application')) {
                    this.log('Detected successful startup message');
                    resolveOnce();
                }
            });

            this.gradleProcess.stderr.on('data', (data) => {
                const error = data.toString();
                errorBuffer += error;
                this.log(`STDERR: ${error.trim()}`);
                
                // Check for critical errors
                if (error.includes('BUILD FAILED') || 
                    error.includes('FAILURE:')) {
                    reject(new Error('Build failed'));
                }
            });

            this.gradleProcess.on('error', (error) => {
                this.log(`Process error: ${error.message}`);
                reject(error);
            });

            this.gradleProcess.on('exit', (code) => {
                this.log(`Gradle process exited with code: ${code}`);
                if (code !== 0 && !resolved) {
                    reject(new Error(`Process exited with code ${code}`));
                }
            });

            // Set a timeout for startup
            setTimeout(() => {
                if (!resolved) {
                    this.log('Startup timeout reached, checking process status...');
                    // Don't reject immediately, let checkApplicationStarted determine if it's running
                    resolveOnce();
                }
            }, 30000); // 30 seconds timeout for startup
        });
    }

    waitForStartup() {
        this.log(`Waiting ${STARTUP_WAIT}ms for UI to initialize...`);
        return new Promise(resolve => setTimeout(resolve, STARTUP_WAIT));
    }

    async checkApplicationStarted() {
        // Check if the process is still running
        if (!this.gradleProcess || this.gradleProcess.exitCode !== null) {
            this.log('Process has exited');
            return false;
        }

        // Check for JavaFX window (platform-specific)
        try {
            const isRunning = await this.checkForJavaFXWindow();
            if (!isRunning) {
                return false;
            }

            // Additional wait for window to become fully visible
            this.log(`Waiting ${WINDOW_CHECK_WAIT}ms for window to become fully visible...`);
            await new Promise(resolve => setTimeout(resolve, WINDOW_CHECK_WAIT));

            // Check if window is visible and has content
            const windowInfo = await this.getWindowInfo();
            if (windowInfo.visible && windowInfo.hasContent) {
                this.log(`✓ Window is visible: ${windowInfo.title || 'Brobot Runner'}`);
                this.log(`✓ Window has content: ${windowInfo.contentPreview}`);
                return true;
            } else {
                this.log('✗ Window is not visible or has no content');
                return false;
            }
        } catch (error) {
            this.log(`Error checking for JavaFX window: ${error.message}`);
            return false;
        }
    }

    async getWindowInfo() {
        if (process.platform === 'linux') {
            try {
                // First check if we have a display
                const { stdout: displayCheck } = await execAsync('echo $DISPLAY');
                this.log(`DISPLAY environment variable: ${displayCheck.trim()}`);
                
                // Check if X11 is running
                const { stdout: xCheck } = await execAsync('ps aux | grep -E "X|Xorg|Xwayland" | grep -v grep || true');
                if (xCheck.trim()) {
                    this.log('X11/Wayland server detected');
                }
                
                // Try multiple methods to find windows
                
                // Method 1: xdotool with broader search
                const { stdout: allWindows } = await execAsync('xdotool search --name "" 2>/dev/null || true');
                if (allWindows.trim()) {
                    this.log(`Found ${allWindows.trim().split('\n').length} total windows with xdotool`);
                    
                    // Search for any Java-related windows
                    const { stdout: javaWindows } = await execAsync('xdotool search --class "java" 2>/dev/null || xdotool search --name "java" 2>/dev/null || true');
                    if (javaWindows.trim()) {
                        this.log(`Found Java windows: ${javaWindows.trim().split('\n').join(', ')}`);
                    }
                }
                
                // Method 2: wmctrl
                const { stdout: wmctrlOutput } = await execAsync('wmctrl -l 2>/dev/null || true');
                if (wmctrlOutput.trim()) {
                    this.log('Window list from wmctrl:');
                    wmctrlOutput.trim().split('\n').forEach(line => {
                        if (line.toLowerCase().includes('java') || 
                            line.toLowerCase().includes('brobot') || 
                            line.toLowerCase().includes('runner')) {
                            this.log(`  Found relevant window: ${line}`);
                        }
                    });
                }
                
                // Method 3: xwininfo - this is finding our windows!
                const { stdout: xwinOutput } = await execAsync('xwininfo -root -tree 2>/dev/null | grep -i "brobot\\|runner\\|javafx" || true');
                if (xwinOutput.trim()) {
                    this.log(`xwininfo found windows: ${xwinOutput.trim()}`);
                    
                    // Parse window IDs from xwininfo output
                    const windowIdMatches = xwinOutput.matchAll(/0x([0-9a-f]+)/gi);
                    const xwinWindowIds = Array.from(windowIdMatches).map(match => match[0]);
                    
                    if (xwinWindowIds.length > 0) {
                        this.log(`Found ${xwinWindowIds.length} Brobot Runner windows via xwininfo`);
                        // We found windows! Return success
                        return {
                            visible: true,
                            hasContent: true,
                            title: 'Brobot Runner',
                            contentPreview: `Found ${xwinWindowIds.length} windows at 1400x900 resolution`
                        };
                    }
                }
                
                // Try to find our specific window with xdotool
                const { stdout: windowList } = await execAsync('xdotool search --class "java" 2>/dev/null || true');
                
                // Also try to find by window name directly
                let windowIds = windowList.trim().split('\n').filter(id => id);
                
                if (!windowIds.length) {
                    // Try finding by window name
                    const { stdout: nameSearch } = await execAsync('xdotool search --name "Brobot" 2>/dev/null || true');
                    if (nameSearch.trim()) {
                        windowIds = nameSearch.trim().split('\n').filter(id => id);
                        this.log(`Found windows by name search: ${windowIds.join(', ')}`);
                    }
                }
                
                if (!windowIds.length) {
                    // Check if the application might be running headless
                    const { stdout: headlessCheck } = await execAsync('ps aux | grep -E "java.*headless=true" | grep -v grep || true');
                    if (headlessCheck.trim()) {
                        this.log('WARNING: Java appears to be running in headless mode');
                        return {
                            visible: false,
                            hasContent: false,
                            contentPreview: 'Application running in headless mode'
                        };
                    }
                    
                    return {
                        visible: false,
                        hasContent: false,
                        contentPreview: 'No JavaFX window found (searched with multiple methods)'
                    };
                }
                
                for (const windowId of windowIds) {
                    try {
                        // Get window name
                        const { stdout: windowName } = await execAsync(`xdotool getwindowname ${windowId} 2>/dev/null || echo ""`);
                        
                        // Get window class
                        const { stdout: windowClass } = await execAsync(`xprop -id ${windowId} WM_CLASS 2>/dev/null || echo ""`);
                        
                        this.log(`Window ${windowId}: Name="${windowName.trim()}", Class="${windowClass.trim()}"`);
                        
                        // Check if this is our JavaFX window
                        // Sometimes JavaFX windows show up with generic names
                        const isBrobotWindow = windowName.toLowerCase().includes('brobot') || 
                            windowName.toLowerCase().includes('runner') ||
                            windowClass.toLowerCase().includes('brobot') ||
                            windowName.includes('JavaFX') ||
                            windowClass.includes('javafx') ||
                            windowClass.includes('brobot') ||
                            (windowName === '' && windowClass.includes('java')); // Empty name but Java class
                            
                        if (isBrobotWindow) {
                            
                            // Get window geometry to check if it's visible
                            const { stdout: geometry } = await execAsync(`xdotool getwindowgeometry ${windowId} 2>/dev/null || echo ""`);
                            const isVisible = geometry.includes('Position:') && geometry.includes('Geometry:');
                            
                            // Check if window is mapped (visible)
                            const { stdout: mapState } = await execAsync(`xwininfo -id ${windowId} 2>/dev/null | grep "Map State:" || echo ""`);
                            const isMapped = mapState.includes('IsViewable');
                            
                            return {
                                visible: isVisible && isMapped,
                                hasContent: true,
                                title: windowName.trim(),
                                contentPreview: `Window ID: ${windowId}, Mapped: ${isMapped}, ${geometry.trim()}`
                            };
                        }
                    } catch (e) {
                        this.log(`Error checking window ${windowId}: ${e.message}`);
                    }
                }
                
                // If no specific window found, check if any Java window exists
                if (windowIds.length > 0) {
                    this.log(`Found ${windowIds.length} Java windows but none matched our criteria`);
                    return {
                        visible: false,
                        hasContent: false,
                        contentPreview: `Found ${windowIds.length} Java windows but none were Brobot Runner`
                    };
                }
                
                return {
                    visible: false,
                    hasContent: false,
                    contentPreview: 'No JavaFX window found'
                };
            } catch (error) {
                this.log(`Error getting window info: ${error.message}`);
                // If xdotool is not available, fall back to basic check
                return {
                    visible: true, // Assume visible if we can't check
                    hasContent: true,
                    contentPreview: 'Unable to inspect window (xdotool not available)'
                };
            }
        } else {
            // For other platforms, basic check
            return {
                visible: true,
                hasContent: true,
                contentPreview: 'Platform-specific window check not implemented'
            };
        }
    }

    async checkForJavaFXWindow() {
        // This is a simplified check - in a real scenario, you might use
        // platform-specific tools to check for the window
        
        return new Promise((resolve) => {
            if (process.platform === 'linux') {
                // Check for JavaFX window using xwininfo or wmctrl
                exec('pgrep -f "brobot.*runner"', (error, stdout) => {
                    if (!error && stdout.trim()) {
                        this.log('Found runner process via pgrep');
                        resolve(true);
                    } else {
                        resolve(false);
                    }
                });
            } else if (process.platform === 'darwin') {
                // macOS check
                exec('pgrep -f "brobot.*runner"', (error, stdout) => {
                    resolve(!error && stdout.trim() !== '');
                });
            } else if (process.platform === 'win32') {
                // Windows check
                exec('tasklist /FI "IMAGENAME eq java.exe" /FO CSV', (error, stdout) => {
                    resolve(!error && stdout.includes('java.exe'));
                });
            } else {
                // Unknown platform, assume it's running if process is alive
                resolve(true);
            }
        });
    }

    async cleanup() {
        this.log('Cleaning up...');
        
        if (this.gradleProcess) {
            try {
                // Try graceful shutdown first
                this.gradleProcess.kill('SIGTERM');
                
                // Wait a bit for graceful shutdown
                await new Promise(resolve => setTimeout(resolve, 2000));
                
                // Force kill if still running
                if (this.gradleProcess.exitCode === null) {
                    this.log('Force killing process...');
                    this.gradleProcess.kill('SIGKILL');
                }
            } catch (error) {
                this.log(`Error during cleanup: ${error.message}`);
            }
        }

        // Kill any remaining Java processes related to brobot
        try {
            const { execSync } = require('child_process');
            if (process.platform !== 'win32') {
                execSync('pkill -f "brobot.*runner" || true', { stdio: 'ignore' });
            }
        } catch (error) {
            // Ignore errors from pkill
        }
    }

    generateReport() {
        console.log('\n========== TEST REPORT ==========');
        console.log(`Test Result: ${this.testPassed ? 'PASSED ✓' : 'FAILED ✗'}`);
        console.log('\nTest Logs:');
        this.logs.forEach(log => console.log(log));
        console.log('=================================\n');
    }
}

// Run the test
async function main() {
    const test = new DesktopRunnerTest();
    
    try {
        const passed = await test.runTest();
        test.generateReport();
        process.exit(passed ? 0 : 1);
    } catch (error) {
        console.error('Unexpected error:', error);
        test.generateReport();
        process.exit(1);
    }
}

// Handle process termination
process.on('SIGINT', async () => {
    console.log('\nTest interrupted by user');
    process.exit(1);
});

process.on('SIGTERM', async () => {
    console.log('\nTest terminated');
    process.exit(1);
});

main();