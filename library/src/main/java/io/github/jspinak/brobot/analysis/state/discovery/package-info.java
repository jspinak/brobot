/**
 * Automated state discovery and provisional state management.
 * 
 * <p>This package implements algorithms for automatically discovering application
 * states from visual observations. It creates provisional states that can be
 * validated and promoted to full states in the navigation model, enabling Brobot
 * to learn GUI structure without manual state definition.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.analysis.state.discovery.ProvisionalStateBuilder} - 
 *       Builds candidate states from visual data</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.state.discovery.ProvisionalStateStore} - 
 *       Manages and persists discovered provisional states</li>
 * </ul>
 * 
 * <h2>State Discovery Algorithm</h2>
 * 
 * <ol>
 *   <li><b>Screenshot Collection</b> - Gather screenshots during exploration</li>
 *   <li><b>Visual Analysis</b> - Extract features from each screenshot</li>
 *   <li><b>Similarity Clustering</b> - Group similar screenshots together</li>
 *   <li><b>Representative Selection</b> - Choose best examples from clusters</li>
 *   <li><b>Element Extraction</b> - Identify interactive elements</li>
 *   <li><b>State Construction</b> - Build provisional state objects</li>
 *   <li><b>Validation Planning</b> - Define tests to confirm states</li>
 * </ol>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Basic State Discovery</h3>
 * <pre>{@code
 * // Discover states from collected screenshots
 * ProvisionalStateBuilder builder = new ProvisionalStateBuilder();
 * List<Mat> screenshots = exploreApplication();
 * 
 * List<ProvisionalState> candidates = builder.discover(screenshots);
 * 
 * for (ProvisionalState candidate : candidates) {
 *     System.out.println("Found state with " + 
 *         candidate.getElements().size() + " elements");
 * }
 * }</pre>
 * 
 * <h3>Incremental Discovery</h3>
 * <pre>{@code
 * // Add new observations to existing discovery
 * ProvisionalStateStore store = new ProvisionalStateStore();
 * 
 * // Load existing provisional states
 * store.load();
 * 
 * // Process new screenshot
 * Mat newScreen = captureScreen();
 * ProvisionalState candidate = builder.analyzeScreen(newScreen);
 * 
 * if (store.isNovel(candidate)) {
 *     store.add(candidate);
 *     System.out.println("Discovered new state!");
 * }
 * }</pre>
 * 
 * <h3>State Validation</h3>
 * <pre>{@code
 * // Validate provisional states through navigation
 * for (ProvisionalState provisional : store.getUnvalidated()) {
 *     // Try to navigate to the state
 *     boolean reached = tryNavigateTo(provisional);
 *     
 *     if (reached) {
 *         // Promote to full state
 *         State confirmed = provisional.toState();
 *         stateModel.add(confirmed);
 *         store.markValidated(provisional);
 *     }
 * }
 * }</pre>
 * 
 * <h3>Similarity Analysis</h3>
 * <pre>{@code
 * // Find similar provisional states
 * ProvisionalState target = getCurrentProvisional();
 * 
 * List<ProvisionalState> similar = store.findSimilar(
 *     target,
 *     0.85  // 85% similarity threshold
 * );
 * 
 * // Merge similar states
 * if (similar.size() > 1) {
 *     ProvisionalState merged = builder.merge(similar);
 *     store.replace(similar, merged);
 * }
 * }</pre>
 * 
 * <h2>Discovery Strategies</h2>
 * 
 * <h3>Visual Clustering</h3>
 * <ul>
 *   <li>Histogram-based similarity</li>
 *   <li>Structural element matching</li>
 *   <li>Color profile comparison</li>
 *   <li>Layout pattern recognition</li>
 * </ul>
 * 
 * <h3>Element Detection</h3>
 * <ul>
 *   <li>Button and control identification</li>
 *   <li>Text region extraction</li>
 *   <li>Interactive area discovery</li>
 *   <li>Navigation element finding</li>
 * </ul>
 * 
 * <h3>State Characteristics</h3>
 * <ul>
 *   <li>Unique visual signature</li>
 *   <li>Consistent element set</li>
 *   <li>Stable layout structure</li>
 *   <li>Reproducible navigation</li>
 * </ul>
 * 
 * <h2>Provisional State Lifecycle</h2>
 * 
 * <ol>
 *   <li><b>Discovery</b> - Initial identification from screenshots</li>
 *   <li><b>Refinement</b> - Merge similar candidates</li>
 *   <li><b>Storage</b> - Persist for later validation</li>
 *   <li><b>Validation</b> - Test through navigation</li>
 *   <li><b>Promotion</b> - Convert to full state</li>
 *   <li><b>Rejection</b> - Discard invalid candidates</li>
 * </ol>
 * 
 * <h2>Configuration Options</h2>
 * 
 * <h3>Discovery Parameters</h3>
 * <ul>
 *   <li>Similarity threshold for clustering</li>
 *   <li>Minimum elements for valid state</li>
 *   <li>Maximum provisional states to maintain</li>
 *   <li>Validation attempt limit</li>
 * </ul>
 * 
 * <h3>Analysis Settings</h3>
 * <ul>
 *   <li>Feature extraction methods</li>
 *   <li>Clustering algorithm choice</li>
 *   <li>Element detection sensitivity</li>
 *   <li>Layout comparison metrics</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li>Collect diverse screenshots for better discovery</li>
 *   <li>Validate provisional states promptly</li>
 *   <li>Merge similar states to avoid duplication</li>
 *   <li>Store metadata for debugging discovery</li>
 *   <li>Monitor discovery performance metrics</li>
 * </ol>
 * 
 * <h2>Integration Points</h2>
 * 
 * <ul>
 *   <li>Scene analysis for element extraction</li>
 *   <li>Color profiles for state signatures</li>
 *   <li>Navigation system for validation</li>
 *   <li>State management for promotion</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state
 * @see io.github.jspinak.brobot.analysis.scene
 * @see io.github.jspinak.brobot.statemanagement
 */
package io.github.jspinak.brobot.analysis.state.discovery;