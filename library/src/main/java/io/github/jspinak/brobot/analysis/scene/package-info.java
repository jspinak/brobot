/**
 * Scene combination generation and analysis.
 * 
 * <p>This package provides algorithms for generating and analyzing combinations
 * of visual elements within scenes. It supports complex scene understanding by
 * exploring different interpretations of overlapping or ambiguous visual patterns,
 * particularly useful for state discovery and structure learning.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.analysis.scene.SceneCombinationGenerator} - 
 *       Generates possible element combinations from scenes</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.scene.SceneCombinationPopulator} - 
 *       Populates combinations with analysis data</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.scene.SceneCombinationStore} - 
 *       Stores and manages scene combinations</li>
 * </ul>
 * 
 * <h2>Scene Combination Process</h2>
 * 
 * <ol>
 *   <li><b>Element Detection</b> - Identify potential UI elements</li>
 *   <li><b>Combination Generation</b> - Create possible groupings</li>
 *   <li><b>Analysis</b> - Evaluate each combination's validity</li>
 *   <li><b>Scoring</b> - Rank combinations by likelihood</li>
 *   <li><b>Selection</b> - Choose optimal interpretation</li>
 * </ol>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Basic Combination Generation</h3>
 * <pre>{@code
 * // Generate element combinations from a scene
 * SceneCombinationGenerator generator = new SceneCombinationGenerator();
 * Scene scene = captureCurrentScene();
 * 
 * List<SceneCombination> combinations = generator.generate(scene);
 * 
 * // Each combination represents a different way to interpret
 * // the visual elements in the scene
 * for (SceneCombination combo : combinations) {
 *     List<Element> elements = combo.getElements();
 *     double score = combo.getScore();
 * }
 * }</pre>
 * 
 * <h3>Combination Analysis</h3>
 * <pre>{@code
 * // Populate combinations with detailed analysis
 * SceneCombinationPopulator populator = new SceneCombinationPopulator();
 * 
 * for (SceneCombination combo : combinations) {
 *     populator.populate(combo, scene);
 *     
 *     // Now combo contains:
 *     // - Element relationships
 *     // - Spatial arrangement scores
 *     // - Visual consistency metrics
 *     // - Semantic grouping confidence
 * }
 * }</pre>
 * 
 * <h3>Combination Storage</h3>
 * <pre>{@code
 * // Store and retrieve scene combinations
 * SceneCombinationStore store = new SceneCombinationStore();
 * 
 * // Store combinations for a state
 * store.storeCombinations(stateId, combinations);
 * 
 * // Retrieve best combination for state
 * SceneCombination best = store.getBestCombination(stateId);
 * 
 * // Find similar combinations across states
 * List<SceneCombination> similar = store.findSimilar(
 *     targetCombination,
 *     similarityThreshold
 * );
 * }</pre>
 * 
 * <h3>State Discovery</h3>
 * <pre>{@code
 * // Use combinations for state discovery
 * List<Scene> unknownScenes = captureUnknownScreens();
 * 
 * for (Scene scene : unknownScenes) {
 *     List<SceneCombination> combos = generator.generate(scene);
 *     
 *     // Find matching known state
 *     State matchingState = findMatchingState(combos);
 *     
 *     if (matchingState == null) {
 *         // Discovered new state
 *         State newState = createStateFromCombination(
 *             selectBestCombination(combos)
 *         );
 *     }
 * }
 * }</pre>
 * 
 * <h2>Combination Types</h2>
 * 
 * <h3>Spatial Combinations</h3>
 * <ul>
 *   <li>Horizontal groupings (menus, toolbars)</li>
 *   <li>Vertical groupings (lists, forms)</li>
 *   <li>Grid arrangements (icon grids)</li>
 *   <li>Hierarchical structures (trees)</li>
 * </ul>
 * 
 * <h3>Visual Combinations</h3>
 * <ul>
 *   <li>Color-based groupings</li>
 *   <li>Style-based associations</li>
 *   <li>Size-based clustering</li>
 *   <li>Shape similarity groups</li>
 * </ul>
 * 
 * <h3>Semantic Combinations</h3>
 * <ul>
 *   <li>Functional groupings (form fields)</li>
 *   <li>Content associations (related items)</li>
 *   <li>Interaction patterns (clickable sets)</li>
 * </ul>
 * 
 * <h2>Analysis Metrics</h2>
 * 
 * <ul>
 *   <li><b>Spatial Coherence</b> - How well elements align</li>
 *   <li><b>Visual Consistency</b> - Style similarity within groups</li>
 *   <li><b>Size Uniformity</b> - Element size distribution</li>
 *   <li><b>Density Patterns</b> - Spacing and distribution</li>
 *   <li><b>Boundary Clarity</b> - Group separation quality</li>
 * </ul>
 * 
 * <h2>Applications</h2>
 * 
 * <ul>
 *   <li><b>State Learning</b> - Discover application states automatically</li>
 *   <li><b>Structure Detection</b> - Identify UI layout patterns</li>
 *   <li><b>Element Grouping</b> - Find related UI components</li>
 *   <li><b>Change Detection</b> - Identify structural changes</li>
 *   <li><b>Pattern Mining</b> - Extract recurring UI patterns</li>
 * </ul>
 * 
 * <h2>Configuration</h2>
 * 
 * <h3>Generation Parameters</h3>
 * <ul>
 *   <li>Maximum elements per combination</li>
 *   <li>Minimum element size threshold</li>
 *   <li>Overlap tolerance</li>
 *   <li>Distance constraints</li>
 * </ul>
 * 
 * <h3>Scoring Weights</h3>
 * <ul>
 *   <li>Spatial arrangement weight</li>
 *   <li>Visual similarity weight</li>
 *   <li>Size consistency weight</li>
 *   <li>Historical match weight</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li>Limit combination count to avoid exponential growth</li>
 *   <li>Use domain knowledge to guide generation</li>
 *   <li>Cache analyzed combinations for performance</li>
 *   <li>Validate combinations against known patterns</li>
 *   <li>Combine with user feedback for accuracy</li>
 * </ol>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model.analysis.scene
 * @see io.github.jspinak.brobot.analysis.state
 */
package io.github.jspinak.brobot.analysis.scene;