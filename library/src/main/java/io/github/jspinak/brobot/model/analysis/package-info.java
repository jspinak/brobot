/**
 * Advanced analysis models for scene understanding and pattern classification.
 *
 * <p>This package provides sophisticated analysis capabilities that go beyond simple pattern
 * matching. It includes color analysis, scene classification, and state discovery mechanisms that
 * enable intelligent GUI understanding and adaptive automation.
 *
 * <h2>Analysis Categories</h2>
 *
 * <h3>Color Analysis</h3>
 *
 * <p>Statistical color profiling and matching across multiple color spaces:
 *
 * <ul>
 *   <li>Multi-channel color statistics (BGR, HSV)
 *   <li>Pixel-level classification and scoring
 *   <li>Color cluster analysis for robust matching
 *   <li>Lighting-tolerant color profiles
 * </ul>
 *
 * <h3>Scene Analysis</h3>
 *
 * <p>Comprehensive scene understanding and classification:
 *
 * <ul>
 *   <li>Scene identification based on visual features
 *   <li>Multi-image scene analysis and comparison
 *   <li>Temporal scene tracking and changes
 *   <li>Scene combination detection
 * </ul>
 *
 * <h3>State Discovery</h3>
 *
 * <p>Automated discovery of application states:
 *
 * <ul>
 *   <li>Provisional state identification
 *   <li>Image-to-scene mapping
 *   <li>State structure inference
 *   <li>Dynamic state model building
 * </ul>
 *
 * <h2>Key Concepts</h2>
 *
 * <h3>Color Profiles</h3>
 *
 * <p>Color analysis uses statistical profiles rather than exact matching:
 *
 * <pre>{@code
 * // Define color profile for a UI element
 * ColorCluster buttonColor = new ColorCluster();
 * buttonColor.setSchema(ColorSchemaName.HSV, hsvProfile);
 * buttonColor.setSchema(ColorSchemaName.BGR, bgrProfile);
 *
 * // Use for matching with tolerance
 * PixelProfile pixelProfile = new PixelProfile(buttonColor);
 * double matchScore = pixelProfile.scorePixel(targetPixel);
 * }</pre>
 *
 * <h3>Scene Classification</h3>
 *
 * <p>Scenes are classified based on multiple visual features:
 *
 * <pre>{@code
 * SceneAnalysis analysis = new SceneAnalysis(currentScene);
 * analysis.addAnalysis(ColorSchemaName.HSV, Analysis.INDICES_2D, classificationMat);
 *
 * // Determine which state images are present
 * List<StateImage> detectedImages = analysis.getStateImageObjects();
 * Set<String> activeStates = analysis.getActiveStates();
 * }</pre>
 *
 * <h3>State Discovery</h3>
 *
 * <p>Automated learning of application structure:
 *
 * <pre>{@code
 * ProvisionalState discovered = new ProvisionalState();
 * discovered.addImage(foundElement);
 * discovered.analyzeStructure();
 *
 * if (discovered.isStable()) {
 *     State newState = discovered.toState();
 *     stateModel.add(newState);
 * }
 * }</pre>
 *
 * <h2>Analysis Pipeline</h2>
 *
 * <ol>
 *   <li><b>Capture</b> - Screen or region capture
 *   <li><b>Preprocessing</b> - Color space conversion, filtering
 *   <li><b>Feature Extraction</b> - Color statistics, patterns
 *   <li><b>Classification</b> - Match against known profiles
 *   <li><b>Aggregation</b> - Combine results for decision
 * </ol>
 *
 * <h2>Use Cases</h2>
 *
 * <h3>Dynamic UI Detection</h3>
 *
 * <p>Detect UI elements that change appearance but maintain color identity:
 *
 * <ul>
 *   <li>Progress bars with varying fill levels
 *   <li>Buttons with hover/pressed states
 *   <li>Theme-aware interface elements
 * </ul>
 *
 * <h3>State Recognition</h3>
 *
 * <p>Identify application states through scene analysis:
 *
 * <ul>
 *   <li>Login screens vs main application
 *   <li>Modal dialogs and overlays
 *   <li>Loading and transition states
 * </ul>
 *
 * <h3>Adaptive Automation</h3>
 *
 * <p>Build automation that learns and adapts:
 *
 * <ul>
 *   <li>Discover new states automatically
 *   <li>Update color profiles for changing conditions
 *   <li>Identify UI restructuring
 * </ul>
 *
 * <h2>Integration</h2>
 *
 * <p>Analysis models integrate with core automation:
 *
 * <ul>
 *   <li>Find actions use color analysis for matching
 *   <li>State detection uses scene analysis
 *   <li>Transitions can trigger on scene changes
 *   <li>History tracking includes analysis results
 * </ul>
 *
 * <h2>Performance Considerations</h2>
 *
 * <p>Analysis operations can be computationally intensive:
 *
 * <ul>
 *   <li>Cache color profiles and reuse when possible
 *   <li>Limit analysis regions for better performance
 *   <li>Use appropriate color space for the task
 *   <li>Consider multi-threading for parallel analysis
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.action.basic.find.color
 * @see io.github.jspinak.brobot.imageUtils
 * @see io.github.jspinak.brobot.model.state
 */
package io.github.jspinak.brobot.model.analysis;
