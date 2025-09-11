/**
 * Advanced image and scene analysis algorithms for GUI automation.
 *
 * <p>This package provides sophisticated computer vision and analysis capabilities that extend
 * beyond basic pattern matching. It implements algorithms for color analysis, motion detection,
 * histogram comparison, and scene understanding, enabling robust GUI element detection under
 * varying conditions.
 *
 * <h2>Analysis Categories</h2>
 *
 * <h3>Color Analysis</h3>
 *
 * <p>Statistical color profiling and pixel classification:
 *
 * <ul>
 *   <li>Multi-channel color space analysis (BGR and HSV)
 *   <li>K-means clustering for color segmentation
 *   <li>Pixel-level scoring and classification
 *   <li>Color profile matching with tolerance
 * </ul>
 *
 * <h3>Motion Detection</h3>
 *
 * <p>Dynamic pixel and movement analysis:
 *
 * <ul>
 *   <li>Frame differencing for motion detection
 *   <li>Dynamic pixel identification
 *   <li>Moving object selection and tracking
 *   <li>Temporal change analysis
 * </ul>
 *
 * <h3>Histogram Analysis</h3>
 *
 * <p>Statistical image comparison techniques:
 *
 * <ul>
 *   <li>Histogram extraction and comparison
 *   <li>Region-based histogram analysis
 *   <li>Multi-channel histogram matching
 *   <li>Statistical similarity metrics
 * </ul>
 *
 * <h3>Match Verification</h3>
 *
 * <p>Advanced match validation and fusion:
 *
 * <ul>
 *   <li>Edge-based match verification
 *   <li>Region-based match proofing
 *   <li>Size-based fusion decisions
 *   <li>Multi-criteria match validation
 * </ul>
 *
 * <h3>Scene Analysis</h3>
 *
 * <p>Comprehensive scene understanding:
 *
 * <ul>
 *   <li>Scene combination generation
 *   <li>Multi-image scene analysis
 *   <li>Scene classification and scoring
 *   <li>State discovery from visual data
 * </ul>
 *
 * <h2>Design Philosophy</h2>
 *
 * <p>The analysis package embodies several key principles:
 *
 * <ol>
 *   <li><b>Robustness</b> - Algorithms handle variations in lighting, color, and positioning
 *   <li><b>Efficiency</b> - Optimized for real-time GUI automation needs
 *   <li><b>Modularity</b> - Each analysis type can be used independently
 *   <li><b>Extensibility</b> - Easy to add new analysis algorithms
 *   <li><b>Accuracy</b> - Statistical methods provide confidence metrics
 * </ol>
 *
 * <h2>Common Use Cases</h2>
 *
 * <h3>Robust Element Detection</h3>
 *
 * <pre>{@code
 * // Use color analysis when images vary
 * ColorClusterFactory clusterFactory = new ColorClusterFactory();
 * ColorCluster buttonColors = clusterFactory.create(buttonSamples);
 *
 * SceneScoreCalculator scorer = new SceneScoreCalculator();
 * ActionResult result = scorer.findByColor(scene, buttonColors);
 * }</pre>
 *
 * <h3>Dynamic Content Handling</h3>
 *
 * <pre>{@code
 * // Detect motion for animated elements
 * MotionDetector detector = new MotionDetector();
 * Mat motionMask = detector.findMotion(frame1, frame2);
 *
 * MovingObjectSelector selector = new MovingObjectSelector();
 * List<Match> movingElements = selector.select(motionMask);
 * }</pre>
 *
 * <h3>State Discovery</h3>
 *
 * <pre>{@code
 * // Discover application states from visual data
 * ProvisionalStateBuilder stateBuilder = new ProvisionalStateBuilder();
 * List<State> discoveredStates = stateBuilder.analyzeScreens(screenshots);
 * }</pre>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 *   <li>Color analysis is computationally intensive - cache profiles when possible
 *   <li>Motion detection requires multiple frames - buffer appropriately
 *   <li>Histogram comparison is fast but memory intensive for large images
 *   <li>Scene analysis scales with number of target images
 * </ul>
 *
 * <h2>Integration with Core Framework</h2>
 *
 * <p>Analysis components enhance the basic find operations:
 *
 * <ul>
 *   <li>Provides alternative matching strategies when template matching fails
 *   <li>Enables detection of elements that change appearance
 *   <li>Supports state discovery and structure learning
 *   <li>Improves robustness through multi-modal analysis
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.action.basic.find
 * @see io.github.jspinak.brobot.model.analysis
 * @see io.github.jspinak.brobot.imageUtils
 */
package io.github.jspinak.brobot.analysis;
