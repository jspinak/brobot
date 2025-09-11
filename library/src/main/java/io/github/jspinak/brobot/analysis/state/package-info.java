/**
 * State analysis and discovery algorithms.
 *
 * <p>This package provides advanced algorithms for analyzing application states and discovering new
 * states from visual data. It forms a critical component of Brobot's ability to learn and adapt to
 * GUI applications without explicit state definitions.
 *
 * <h2>State Analysis Concepts</h2>
 *
 * <p>State analysis in Brobot involves:
 *
 * <ul>
 *   <li><b>State Discovery</b> - Automatically identifying distinct application states
 *   <li><b>State Validation</b> - Verifying discovered states are meaningful
 *   <li><b>State Relationships</b> - Understanding transitions between states
 *   <li><b>State Evolution</b> - Tracking how states change over time
 * </ul>
 *
 * <h2>Subpackages</h2>
 *
 * <h3>discovery</h3>
 *
 * <p>Automated state discovery from visual observations:
 *
 * <ul>
 *   <li>Provisional state building from screenshots
 *   <li>State candidate evaluation and ranking
 *   <li>State consolidation and merging
 * </ul>
 *
 * <h2>State Discovery Process</h2>
 *
 * <ol>
 *   <li><b>Visual Capture</b> - Collect screenshots during exploration
 *   <li><b>Feature Extraction</b> - Identify visual elements and patterns
 *   <li><b>Clustering</b> - Group similar screenshots
 *   <li><b>State Candidates</b> - Create provisional states from clusters
 *   <li><b>Validation</b> - Verify states through navigation
 *   <li><b>Integration</b> - Add confirmed states to the model
 * </ol>
 *
 * <h2>Integration with Framework</h2>
 *
 * <p>State analysis connects with:
 *
 * <ul>
 *   <li><b>Scene Analysis</b> - Uses scene combinations for state identification
 *   <li><b>Color Analysis</b> - Leverages color profiles for state signatures
 *   <li><b>Motion Detection</b> - Identifies state changes through motion
 *   <li><b>Navigation</b> - Validates states through successful navigation
 * </ul>
 *
 * <h2>Applications</h2>
 *
 * <ul>
 *   <li><b>Automated Testing</b> - Discover all application states
 *   <li><b>Model Learning</b> - Build navigation models automatically
 *   <li><b>Change Detection</b> - Identify new or modified states
 *   <li><b>Coverage Analysis</b> - Ensure all states are tested
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state
 * @see io.github.jspinak.brobot.analysis.scene
 * @see io.github.jspinak.brobot.navigation
 */
package io.github.jspinak.brobot.analysis.state;
