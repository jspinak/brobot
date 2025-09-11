/**
 * Scene analysis and classification models.
 *
 * <p>This package provides comprehensive scene analysis capabilities for understanding and
 * classifying screen content. Scenes represent complete screen captures or regions that can be
 * analyzed for state detection, element identification, and visual changes.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis} - Single scene analysis
 *       with multi-image classification
 *   <li>{@link io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses} - Collection of scene
 *       analyses for temporal tracking
 *   <li>{@link io.github.jspinak.brobot.model.analysis.scene.SceneCombination} - Combined analysis
 *       of multiple scenes
 * </ul>
 *
 * <h2>Scene Analysis Process</h2>
 *
 * <p>Scene analysis follows a structured pipeline:
 *
 * <ol>
 *   <li><b>Capture</b> - Screenshot of screen or region
 *   <li><b>Preprocessing</b> - Color space conversion, filtering
 *   <li><b>Classification</b> - Pixel-by-pixel analysis against known patterns
 *   <li><b>Aggregation</b> - Combine pixel results into regions
 *   <li><b>Interpretation</b> - Determine states and elements present
 * </ol>
 *
 * <h2>Analysis Types</h2>
 *
 * <p>SceneAnalysis stores multiple analysis results:
 *
 * <ul>
 *   <li><b>SCENE</b> - Original captured scene in BGR/HSV
 *   <li><b>INDICES_3D</b> - Per-channel classification indices
 *   <li><b>INDICES_2D</b> - Flattened classification results
 *   <li><b>BGR_FROM_INDICES_2D</b> - Visualization of classification
 *   <li><b>SCORES</b> - Confidence scores per pixel
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Scene Analysis</h3>
 *
 * <pre>{@code
 * // Capture and analyze current screen
 * Scene currentScene = new Scene(captureScreen());
 *
 * // Create pixel profiles for known elements
 * List<PixelProfiles> profiles = Arrays.asList(
 *     new PixelProfiles(loginButton),
 *     new PixelProfiles(usernameField),
 *     new PixelProfiles(passwordField)
 * );
 *
 * // Perform analysis
 * SceneAnalysis analysis = new SceneAnalysis(profiles, currentScene);
 *
 * // Check what was found
 * Set<String> foundElements = analysis.getDetectedElements();
 * boolean isLoginScreen = foundElements.containsAll(
 *     Arrays.asList("loginButton", "usernameField", "passwordField")
 * );
 * }</pre>
 *
 * <h3>Classification Results</h3>
 *
 * <pre>{@code
 * // Get classification matrix
 * Mat classification = analysis.getAnalysis(
 *     ColorSchemaName.HSV,
 *     Analysis.INDICES_2D
 * );
 *
 * // Each pixel value indicates which profile matched best
 * // 0 = no match, 1 = first profile, 2 = second profile, etc.
 *
 * // Get visualization
 * Mat visualization = analysis.getAnalysis(
 *     ColorSchemaName.BGR,
 *     Analysis.BGR_FROM_INDICES_2D
 * );
 * // Colors represent different matched elements
 * }</pre>
 *
 * <h3>Temporal Analysis</h3>
 *
 * <pre>{@code
 * SceneAnalyses timeSeriesAnalysis = new SceneAnalyses();
 *
 * // Collect analyses over time
 * for (int i = 0; i < 10; i++) {
 *     Scene scene = new Scene(captureScreen());
 *     SceneAnalysis analysis = analyzeScene(scene);
 *     timeSeriesAnalysis.add(analysis);
 *     Thread.sleep(100);
 * }
 *
 * // Detect motion or changes
 * List<Region> changedRegions = timeSeriesAnalysis.findChangedRegions();
 * boolean hasMotion = timeSeriesAnalysis.detectMotion();
 * }</pre>
 *
 * <h2>Scene Combination</h2>
 *
 * <p>Multiple scenes can be combined for robust detection:
 *
 * <pre>{@code
 * SceneCombination combination = new SceneCombination();
 * combination.addScene(scene1, 0.5);  // 50% weight
 * combination.addScene(scene2, 0.3);  // 30% weight
 * combination.addScene(scene3, 0.2);  // 20% weight
 *
 * // Combined analysis is more robust to variations
 * SceneAnalysis combined = combination.analyze();
 * }</pre>
 *
 * <h2>State Detection</h2>
 *
 * <p>Scenes are primarily used for state detection:
 *
 * <pre>{@code
 * public State detectCurrentState(Scene scene) {
 *     // Analyze scene against all known state profiles
 *     Map<State, Double> stateScores = new HashMap<>();
 *
 *     for (State state : StateStore.getAllStates()) {
 *         SceneAnalysis analysis = analyzeForState(scene, state);
 *         double score = calculateStateScore(analysis);
 *         stateScores.put(state, score);
 *     }
 *
 *     // Return highest scoring state
 *     return stateScores.entrySet().stream()
 *         .max(Map.Entry.comparingByValue())
 *         .map(Map.Entry::getKey)
 *         .orElse(UnknownState.getInstance());
 * }
 * }</pre>
 *
 * <h2>Performance Optimization</h2>
 *
 * <h3>Region-Based Analysis</h3>
 *
 * <p>Limit analysis to specific regions for performance:
 *
 * <pre>{@code
 * Region headerRegion = new Region(0, 0, screenWidth, 100);
 * Scene headerScene = new Scene(captureRegion(headerRegion));
 * SceneAnalysis headerAnalysis = analyzeScene(headerScene);
 * }</pre>
 *
 * <h3>Caching</h3>
 *
 * <p>Cache analysis results for static content:
 *
 * <pre>{@code
 * Map<String, SceneAnalysis> cache = new HashMap<>();
 * String sceneHash = scene.getHash();
 *
 * if (cache.containsKey(sceneHash)) {
 *     return cache.get(sceneHash);
 * }
 *
 * SceneAnalysis analysis = performAnalysis(scene);
 * cache.put(sceneHash, analysis);
 * return analysis;
 * }</pre>
 *
 * <h2>Integration Points</h2>
 *
 * <ul>
 *   <li>State detection services use scene analysis
 *   <li>Motion detection relies on scene comparison
 *   <li>Color-based find operations use scene classification
 *   <li>History tracking stores scene analyses
 * </ul>
 *
 * <h2>Best Practices</h2>
 *
 * <ol>
 *   <li>Predefine pixel profiles for known UI elements
 *   <li>Use appropriate color spaces for the content type
 *   <li>Limit analysis regions when full-screen analysis isn't needed
 *   <li>Cache results for static content
 *   <li>Combine multiple analyses for robust detection
 *   <li>Monitor analysis performance and optimize as needed
 * </ol>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.model.analysis.color
 * @see io.github.jspinak.brobot.model.element.Scene
 * @see io.github.jspinak.brobot.state.stateDetection
 */
package io.github.jspinak.brobot.model.analysis.scene;
