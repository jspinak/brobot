/**
 * Motion detection and dynamic pixel analysis.
 *
 * <p>This package provides algorithms for detecting motion, tracking dynamic pixels, and
 * identifying moving objects in GUI applications. These capabilities enable automation of animated
 * interfaces, loading indicators, and dynamically updating content.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.analysis.motion.MotionDetector} - Primary motion detection
 *       between frames
 *   <li>{@link io.github.jspinak.brobot.analysis.motion.DynamicPixelFinder} - Identifies pixels
 *       that change over time
 *   <li>{@link io.github.jspinak.brobot.analysis.motion.FindDynamicPixels} - High-level dynamic
 *       pixel discovery
 *   <li>{@link io.github.jspinak.brobot.analysis.motion.PixelChangeDetector} - Detects pixel-level
 *       changes between images
 *   <li>{@link io.github.jspinak.brobot.analysis.motion.MovingObjectSelector} - Selects and tracks
 *       moving objects
 *   <li>{@link io.github.jspinak.brobot.analysis.motion.ChangedPixels} - Data structure for changed
 *       pixel information
 * </ul>
 *
 * <h2>Motion Detection Process</h2>
 *
 * <ol>
 *   <li><b>Frame Capture</b> - Collect sequential frames
 *   <li><b>Difference Calculation</b> - Compare frames pixel by pixel
 *   <li><b>Threshold Application</b> - Filter noise from actual motion
 *   <li><b>Region Identification</b> - Group changed pixels into objects
 *   <li><b>Object Tracking</b> - Follow moving elements across frames
 * </ol>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Motion Detection</h3>
 *
 * <pre>{@code
 * // Detect motion between two frames
 * MotionDetector detector = new MotionDetector();
 * Mat frame1 = captureScreen();
 * Thread.sleep(100);
 * Mat frame2 = captureScreen();
 *
 * Mat motionMask = detector.detectMotion(frame1, frame2);
 * List<Rect> movingRegions = detector.getMovingRegions(motionMask);
 * }</pre>
 *
 * <h3>Dynamic Pixel Discovery</h3>
 *
 * <pre>{@code
 * // Find pixels that change frequently
 * DynamicPixelFinder pixelFinder = new DynamicPixelFinder();
 *
 * // Capture multiple frames
 * List<Mat> frames = captureFrames(10, 100); // 10 frames, 100ms apart
 *
 * ChangedPixels dynamicPixels = pixelFinder.findDynamic(frames);
 * Mat dynamicMask = dynamicPixels.getMask();
 *
 * // Use mask to ignore dynamic regions in matching
 * }</pre>
 *
 * <h3>Moving Object Selection</h3>
 *
 * <pre>{@code
 * // Select and track moving objects
 * MovingObjectSelector selector = new MovingObjectSelector();
 *
 * // Configure selection criteria
 * selector.setMinSize(50, 50);
 * selector.setMaxObjects(5);
 *
 * List<Match> movingObjects = selector.select(
 *     previousFrame,
 *     currentFrame,
 *     motionThreshold
 * );
 *
 * // Track objects
 * for (Match obj : movingObjects) {
 *     trackObject(obj);
 * }
 * }</pre>
 *
 * <h3>Loading Indicator Detection</h3>
 *
 * <pre>{@code
 * // Detect spinning or animated loading indicators
 * FindDynamicPixels dynamicFinder = new FindDynamicPixels();
 *
 * // Monitor region for changes
 * Rect loadingRegion = new Rect(100, 100, 50, 50);
 * boolean isAnimating = dynamicFinder.isRegionDynamic(
 *     loadingRegion,
 *     duration,
 *     changeThreshold
 * );
 *
 * if (isAnimating) {
 *     waitForLoadingComplete();
 * }
 * }</pre>
 *
 * <h2>Motion Analysis Types</h2>
 *
 * <h3>Frame Differencing</h3>
 *
 * <ul>
 *   <li>Simple difference between consecutive frames
 *   <li>Fast but sensitive to noise
 *   <li>Good for detecting any movement
 * </ul>
 *
 * <h3>Background Subtraction</h3>
 *
 * <ul>
 *   <li>Compare against static background model
 *   <li>More robust to lighting changes
 *   <li>Requires background learning phase
 * </ul>
 *
 * <h3>Optical Flow</h3>
 *
 * <ul>
 *   <li>Track pixel movement direction and speed
 *   <li>Provides motion vectors
 *   <li>Computationally intensive
 * </ul>
 *
 * <h2>Applications</h2>
 *
 * <ul>
 *   <li><b>Progress Indicators</b> - Detect and wait for animations
 *   <li><b>Dynamic Content</b> - Handle moving UI elements
 *   <li><b>Video Regions</b> - Identify playing video areas
 *   <li><b>Notifications</b> - Detect popup animations
 *   <li><b>Transitions</b> - Monitor UI state changes
 * </ul>
 *
 * <h2>Configuration Options</h2>
 *
 * <h3>Sensitivity Settings</h3>
 *
 * <ul>
 *   <li><b>Threshold</b> - Minimum change to consider motion
 *   <li><b>MinArea</b> - Minimum region size to track
 *   <li><b>FrameCount</b> - Frames needed for analysis
 *   <li><b>TimeWindow</b> - Duration to monitor changes
 * </ul>
 *
 * <h3>Noise Reduction</h3>
 *
 * <ul>
 *   <li>Morphological operations (erosion/dilation)
 *   <li>Gaussian blur pre-processing
 *   <li>Minimum change persistence
 * </ul>
 *
 * <h2>Best Practices</h2>
 *
 * <ol>
 *   <li>Adjust thresholds based on expected motion
 *   <li>Use appropriate frame rates for motion type
 *   <li>Apply noise reduction for cleaner results
 *   <li>Consider computational cost for real-time use
 *   <li>Combine with static matching for robustness
 * </ol>
 *
 * <h2>Performance Tips</h2>
 *
 * <ul>
 *   <li>Downsample images for faster processing
 *   <li>Process regions of interest only
 *   <li>Cache motion masks when possible
 *   <li>Use parallel processing for multiple regions
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.action.composite.wait
 * @see io.github.jspinak.brobot.imageUtils
 */
package io.github.jspinak.brobot.analysis.motion;
