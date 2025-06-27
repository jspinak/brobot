/**
 * Color-based pattern matching and scene analysis.
 * 
 * <p>This package provides advanced color analysis capabilities for GUI element detection
 * and scene recognition. It enables finding elements based on color profiles, analyzing
 * pixel distributions, and performing scene classification based on color characteristics.</p>
 * 
 * <h2>Core Capabilities</h2>
 * 
 * <ul>
 *   <li><b>Pixel Analysis</b> - Individual and collective pixel color matching</li>
 *   <li><b>Scene Detection</b> - Identify scenes based on color distribution patterns</li>
 *   <li><b>Color Scoring</b> - Quantitative analysis of color similarity and distribution</li>
 * </ul>
 * 
 * <h2>Key Classes</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.color.GetScenes}</b> - 
 *       Analyzes screen regions to identify scenes based on predefined color profiles</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.color.GetPixelAnalysisCollectionScores}</b> - 
 *       Performs detailed pixel-level analysis and scoring for color matching</li>
 * </ul>
 * 
 * <h2>Use Cases</h2>
 * 
 * <ul>
 *   <li>Detecting UI elements that lack distinct visual patterns but have consistent colors</li>
 *   <li>Scene classification for state recognition (e.g., different game screens)</li>
 *   <li>Finding regions with specific color characteristics (e.g., health bars, progress indicators)</li>
 *   <li>Robust element detection in dynamic interfaces where shapes change but colors remain constant</li>
 * </ul>
 * 
 * <h2>Color Analysis Process</h2>
 * 
 * <ol>
 *   <li>Define color profiles for target elements or scenes</li>
 *   <li>Capture screen region or full screen</li>
 *   <li>Analyze pixel distributions against defined profiles</li>
 *   <li>Score matches based on color similarity and distribution patterns</li>
 *   <li>Return matches with confidence scores</li>
 * </ol>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Detect a scene based on color profile
 * GetScenes getScenes = new GetScenes(...);
 * ActionOptions options = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.FIND)
 *     .setFind(Find.CUSTOM)
 *     .build();
 * 
 * ObjectCollection scenes = new ObjectCollection.Builder()
 *     .withScenes(sceneProfiles)
 *     .build();
 * 
 * ActionResult result = getScenes.perform(new ActionResult(), scenes);
 * 
 * // Analyze pixel distributions
 * GetPixelAnalysisCollectionScores pixelAnalysis = new GetPixelAnalysisCollectionScores(...);
 * ActionResult analysisResult = pixelAnalysis.perform(new ActionResult(), targets);
 * }</pre>
 * 
 * @see io.github.jspinak.brobot.imageUtils.SceneAnalysis
 * @see io.github.jspinak.brobot.PixelProfile.PixelAnalysis
 */
package io.github.jspinak.brobot.action.basic.find.color;