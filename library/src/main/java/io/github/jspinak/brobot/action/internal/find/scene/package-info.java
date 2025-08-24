/**
 * Scene-based pattern matching and analysis for GUI element recognition.
 * 
 * <p>This package handles the capture, analysis, and pattern matching of screen
 * scenes. It provides the infrastructure for working with screenshots and
 * performing various types of visual recognition within those captured scenes.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.scene.ScenePatternMatcher}</b> - 
 *       Performs image pattern matching and OCR text detection within captured scenes
 *       using Sikuli's Finder capabilities</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.scene.SceneAnalysisCollectionBuilder}</b> - 
 *       Orchestrates the creation and population of comprehensive scene analysis
 *       collections for various matching strategies</li>
 * </ul>
 * 
 * <h2>Scene Processing Workflow</h2>
 * 
 * <ol>
 *   <li><b>Scene Acquisition</b> - Capture screenshots or use provided scene images</li>
 *   <li><b>Scene Preparation</b> - Convert scenes to appropriate formats for analysis</li>
 *   <li><b>Pattern Matching</b> - Apply template matching algorithms to find patterns</li>
 *   <li><b>Text Detection</b> - Perform OCR to locate text elements when required</li>
 *   <li><b>Result Compilation</b> - Aggregate matches from all processing methods</li>
 *   <li><b>Resource Cleanup</b> - Properly dispose of Finder instances and temporary data</li>
 * </ol>
 * 
 * <h2>Key Capabilities</h2>
 * 
 * <ul>
 *   <li><b>Multi-Scene Support</b> - Process multiple scenes in a single operation
 *       for comprehensive coverage</li>
 *   <li><b>Hybrid Recognition</b> - Combines image template matching with OCR
 *       for flexible element detection</li>
 *   <li><b>Fixed Region Updates</b> - Efficiently handle patterns with known
 *       locations to optimize performance</li>
 *   <li><b>Scene Analysis Collections</b> - Build comprehensive data structures
 *       for advanced analysis like color matching</li>
 * </ul>
 * 
 * <h2>Integration with Sikuli</h2>
 * 
 * <p>This package wraps and extends Sikuli's pattern finding capabilities:</p>
 * <ul>
 *   <li>Manages Finder lifecycle for proper resource handling</li>
 *   <li>Provides consistent error handling and recovery</li>
 *   <li>Integrates Sikuli results with Brobot's Match model</li>
 *   <li>Adds scene management layer above basic finding</li>
 * </ul>
 * 
 * <h2>Performance Optimizations</h2>
 * 
 * <ul>
 *   <li><b>Scene Caching</b> - Reuse captured scenes when possible</li>
 *   <li><b>Early Termination</b> - Stop processing when sufficient matches found</li>
 *   <li><b>Region Limiting</b> - Restrict search to relevant screen areas</li>
 *   <li><b>Resource Management</b> - Careful cleanup of native resources</li>
 * </ul>
 * 
 * <h2>Scene Analysis Features</h2>
 * 
 * <p>Beyond basic pattern matching, scene components support:</p>
 * <ul>
 *   <li>Building comprehensive scene analysis for color-based matching</li>
 *   <li>Managing collections of scenes for temporal analysis</li>
 *   <li>Providing scene context for match post-processing</li>
 *   <li>Supporting various action strategies (FIND, VANISH, APPEAR)</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.basic.find.Find
 * @see io.github.jspinak.brobot.model.scene.Scene
 * @see org.sikuli.script.Finder
 */
package io.github.jspinak.brobot.action.internal.find.scene;