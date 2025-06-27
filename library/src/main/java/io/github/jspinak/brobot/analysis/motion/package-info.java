/**
 * Motion detection and dynamic pixel analysis.
 * 
 * <p>This package provides algorithms for detecting motion, tracking dynamic pixels,
 * and identifying moving objects in GUI applications. These capabilities enable
 * automation of animated interfaces, loading indicators, and dynamically updating
 * content.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.analysis.motion.MotionDetector} - 
 *       Primary motion detection between frames</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.motion.DynamicPixelFinder} - 
 *       Identifies pixels that change over time</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.motion.FindDynamicPixels} - 
 *       High-level dynamic pixel discovery</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.motion.PixelChangeDetector} - 
 *       Detects pixel-level changes between images</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.motion.MovingObjectSelector} - 
 *       Selects and tracks moving objects</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.motion.ChangedPixels} - 
 *       Data structure for changed pixel information</li>
 * </ul>
 * 
 * <h2>Motion Detection Process</h2>
 * 
 * <ol>
 *   <li><b>Frame Capture</b> - Collect sequential frames</li>
 *   <li><b>Difference Calculation</b> - Compare frames pixel by pixel</li>
 *   <li><b>Threshold Application</b> - Filter noise from actual motion</li>
 *   <li><b>Region Identification</b> - Group changed pixels into objects</li>
 *   <li><b>Object Tracking</b> - Follow moving elements across frames</li>
 * </ol>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Basic Motion Detection</h3>
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
 * <ul>
 *   <li>Simple difference between consecutive frames</li>
 *   <li>Fast but sensitive to noise</li>
 *   <li>Good for detecting any movement</li>
 * </ul>
 * 
 * <h3>Background Subtraction</h3>
 * <ul>
 *   <li>Compare against static background model</li>
 *   <li>More robust to lighting changes</li>
 *   <li>Requires background learning phase</li>
 * </ul>
 * 
 * <h3>Optical Flow</h3>
 * <ul>
 *   <li>Track pixel movement direction and speed</li>
 *   <li>Provides motion vectors</li>
 *   <li>Computationally intensive</li>
 * </ul>
 * 
 * <h2>Applications</h2>
 * 
 * <ul>
 *   <li><b>Progress Indicators</b> - Detect and wait for animations</li>
 *   <li><b>Dynamic Content</b> - Handle moving UI elements</li>
 *   <li><b>Video Regions</b> - Identify playing video areas</li>
 *   <li><b>Notifications</b> - Detect popup animations</li>
 *   <li><b>Transitions</b> - Monitor UI state changes</li>
 * </ul>
 * 
 * <h2>Configuration Options</h2>
 * 
 * <h3>Sensitivity Settings</h3>
 * <ul>
 *   <li><b>Threshold</b> - Minimum change to consider motion</li>
 *   <li><b>MinArea</b> - Minimum region size to track</li>
 *   <li><b>FrameCount</b> - Frames needed for analysis</li>
 *   <li><b>TimeWindow</b> - Duration to monitor changes</li>
 * </ul>
 * 
 * <h3>Noise Reduction</h3>
 * <ul>
 *   <li>Morphological operations (erosion/dilation)</li>
 *   <li>Gaussian blur pre-processing</li>
 *   <li>Minimum change persistence</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li>Adjust thresholds based on expected motion</li>
 *   <li>Use appropriate frame rates for motion type</li>
 *   <li>Apply noise reduction for cleaner results</li>
 *   <li>Consider computational cost for real-time use</li>
 *   <li>Combine with static matching for robustness</li>
 * </ol>
 * 
 * <h2>Performance Tips</h2>
 * 
 * <ul>
 *   <li>Downsample images for faster processing</li>
 *   <li>Process regions of interest only</li>
 *   <li>Cache motion masks when possible</li>
 *   <li>Use parallel processing for multiple regions</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.action.composite.wait
 * @see io.github.jspinak.brobot.imageUtils
 */
package io.github.jspinak.brobot.analysis.motion;