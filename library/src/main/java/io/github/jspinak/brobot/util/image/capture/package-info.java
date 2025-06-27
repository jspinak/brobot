/**
 * Provides screen capture and screenshot utilities for GUI automation.
 * 
 * <p>This package contains specialized utilities for capturing screen content,
 * managing display information, and recording screenshot sequences. These tools
 * are essential for visual element detection and state verification in GUI
 * automation workflows.
 * 
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.util.image.capture.ScreenUtilities} - 
 *       Screen dimension operations and display management</li>
 *   <li>{@link io.github.jspinak.brobot.util.image.capture.ScreenshotCapture} - 
 *       Single screenshot capture with multiple capture strategies</li>
 *   <li>{@link io.github.jspinak.brobot.util.image.capture.ScreenshotRecorder} - 
 *       Continuous screenshot capture for recording and monitoring</li>
 * </ul>
 * 
 * <h2>Capture Strategies</h2>
 * <p>Multiple capture methods are supported for different scenarios:
 * <ul>
 *   <li><strong>Robot API</strong>: Java's built-in screen capture (most compatible)</li>
 *   <li><strong>Native Methods</strong>: Platform-specific optimized capture</li>
 *   <li><strong>Region Capture</strong>: Efficient capture of specific screen areas</li>
 *   <li><strong>Multi-Monitor</strong>: Support for multiple display configurations</li>
 * </ul>
 * 
 * <h2>Screen Management</h2>
 * <p>ScreenUtilities provides:
 * <ul>
 *   <li>Display enumeration and information</li>
 *   <li>Screen boundary calculations</li>
 *   <li>Safe region creation within screen bounds</li>
 *   <li>Multi-monitor coordinate translation</li>
 *   <li>DPI-aware dimension calculations</li>
 * </ul>
 * 
 * <h2>Continuous Recording</h2>
 * <p>ScreenshotRecorder enables:
 * <ul>
 *   <li>Scheduled screenshot capture at defined intervals</li>
 *   <li>Circular buffer for recent screenshot history</li>
 *   <li>Event-triggered capture for specific conditions</li>
 *   <li>Memory-efficient storage with configurable limits</li>
 * </ul>
 * 
 * <h2>Performance Considerations</h2>
 * <ul>
 *   <li>Screenshot capture is relatively expensive; cache when possible</li>
 *   <li>Region capture is faster than full-screen capture</li>
 *   <li>Native methods may offer better performance but less compatibility</li>
 *   <li>Continuous recording requires careful memory management</li>
 * </ul>
 * 
 * <h2>Platform Differences</h2>
 * <p>Capture behavior varies by platform:
 * <ul>
 *   <li><strong>Windows</strong>: Generally fastest with native methods</li>
 *   <li><strong>macOS</strong>: May require accessibility permissions</li>
 *   <li><strong>Linux</strong>: Performance depends on window manager</li>
 *   <li><strong>High DPI</strong>: Requires special handling for scaled displays</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Capture full screen
 * ScreenshotCapture capture = context.getBean(ScreenshotCapture.class);
 * BufferedImage fullScreen = capture.captureScreen();
 * 
 * // Capture specific region
 * Region region = new Region(100, 100, 500, 400);
 * BufferedImage regionCapture = capture.captureRegion(region);
 * 
 * // Get screen information
 * ScreenUtilities screenUtils = context.getBean(ScreenUtilities.class);
 * Rectangle screenBounds = screenUtils.getScreenBounds();
 * List<GraphicsDevice> monitors = screenUtils.getMonitors();
 * 
 * // Start continuous recording
 * ScreenshotRecorder recorder = context.getBean(ScreenshotRecorder.class);
 * recorder.startRecording(1000); // Capture every second
 * // ... perform actions ...
 * List<BufferedImage> captures = recorder.stopRecording();
 * }</pre>
 * 
 * <h2>Best Practices</h2>
 * <ul>
 *   <li>Minimize capture frequency to reduce CPU usage</li>
 *   <li>Use region capture when full screen isn't needed</li>
 *   <li>Implement proper error handling for capture failures</li>
 *   <li>Clean up resources after continuous recording</li>
 *   <li>Consider mock captures for testing scenarios</li>
 * </ul>
 * 
 * <h2>Error Handling</h2>
 * <p>Common issues and solutions:
 * <ul>
 *   <li>Permission denied: Ensure proper OS permissions</li>
 *   <li>Out of memory: Limit recording duration and buffer size</li>
 *   <li>Invalid region: Validate against screen bounds</li>
 *   <li>Multi-monitor issues: Use absolute coordinates</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.util.image.core
 * @see io.github.jspinak.brobot.model.element.Region
 * @see java.awt.Robot
 * @since 1.0
 */
package io.github.jspinak.brobot.util.image.capture;