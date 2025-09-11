/**
 * Provides screen capture and screenshot utilities for GUI automation.
 *
 * <p>This package contains specialized utilities for capturing screen content, managing display
 * information, and recording screenshot sequences. These tools are essential for visual element
 * detection and state verification in GUI automation workflows.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.util.image.capture.ScreenUtilities} - Screen dimension
 *       operations and display management
 *   <li>{@link io.github.jspinak.brobot.util.image.capture.ScreenshotCapture} - Single screenshot
 *       capture with multiple capture strategies
 *   <li>{@link io.github.jspinak.brobot.util.image.capture.ScreenshotRecorder} - Continuous
 *       screenshot capture for recording and monitoring
 * </ul>
 *
 * <h2>Capture Strategies</h2>
 *
 * <p>Multiple capture methods are supported for different scenarios:
 *
 * <ul>
 *   <li><strong>Robot API</strong>: Java's built-in screen capture (most compatible)
 *   <li><strong>Native Methods</strong>: Platform-specific optimized capture
 *   <li><strong>Region Capture</strong>: Efficient capture of specific screen areas
 *   <li><strong>Multi-Monitor</strong>: Support for multiple display configurations
 * </ul>
 *
 * <h2>Screen Management</h2>
 *
 * <p>ScreenUtilities provides:
 *
 * <ul>
 *   <li>Display enumeration and information
 *   <li>Screen boundary calculations
 *   <li>Safe region creation within screen bounds
 *   <li>Multi-monitor coordinate translation
 *   <li>DPI-aware dimension calculations
 * </ul>
 *
 * <h2>Continuous Recording</h2>
 *
 * <p>ScreenshotRecorder enables:
 *
 * <ul>
 *   <li>Scheduled screenshot capture at defined intervals
 *   <li>Circular buffer for recent screenshot history
 *   <li>Event-triggered capture for specific conditions
 *   <li>Memory-efficient storage with configurable limits
 * </ul>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 *   <li>Screenshot capture is relatively expensive; cache when possible
 *   <li>Region capture is faster than full-screen capture
 *   <li>Native methods may offer better performance but less compatibility
 *   <li>Continuous recording requires careful memory management
 * </ul>
 *
 * <h2>Platform Differences</h2>
 *
 * <p>Capture behavior varies by platform:
 *
 * <ul>
 *   <li><strong>Windows</strong>: Generally fastest with native methods
 *   <li><strong>macOS</strong>: May require accessibility permissions
 *   <li><strong>Linux</strong>: Performance depends on window manager
 *   <li><strong>High DPI</strong>: Requires special handling for scaled displays
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
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
 *
 * <ul>
 *   <li>Minimize capture frequency to reduce CPU usage
 *   <li>Use region capture when full screen isn't needed
 *   <li>Implement proper error handling for capture failures
 *   <li>Clean up resources after continuous recording
 *   <li>Consider mock captures for testing scenarios
 * </ul>
 *
 * <h2>Error Handling</h2>
 *
 * <p>Common issues and solutions:
 *
 * <ul>
 *   <li>Permission denied: Ensure proper OS permissions
 *   <li>Out of memory: Limit recording duration and buffer size
 *   <li>Invalid region: Validate against screen bounds
 *   <li>Multi-monitor issues: Use absolute coordinates
 * </ul>
 *
 * @see io.github.jspinak.brobot.util.image.core
 * @see io.github.jspinak.brobot.model.element.Region
 * @see java.awt.Robot
 * @since 1.0
 */
package io.github.jspinak.brobot.util.image.capture;
