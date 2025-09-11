/**
 * Advanced match validation and fusion algorithms.
 *
 * <p>This package provides sophisticated techniques for validating pattern matches and fusing
 * multiple match results. It implements various proofing strategies to ensure match accuracy and
 * fusion algorithms to combine overlapping or related matches into optimal results.
 *
 * <h2>Core Components</h2>
 *
 * <h3>Match Validation</h3>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.analysis.match.MatchProofer} - Base interface for match
 *       validation strategies
 *   <li>{@link io.github.jspinak.brobot.analysis.match.EdgeBasedProofer} - Validates matches using
 *       edge detection
 *   <li>{@link io.github.jspinak.brobot.analysis.match.RegionBasedProofer} - Validates using
 *       regional analysis
 * </ul>
 *
 * <h3>Match Fusion</h3>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.analysis.match.MatchFusion} - Combines overlapping matches
 *       intelligently
 *   <li>{@link io.github.jspinak.brobot.analysis.match.MatchFusionDecider} - Interface for fusion
 *       decision strategies
 *   <li>{@link io.github.jspinak.brobot.analysis.match.AbsoluteSizeFusionDecider} - Fusion based on
 *       absolute size thresholds
 *   <li>{@link io.github.jspinak.brobot.analysis.match.RelativeSizeFusionDecider} - Fusion based on
 *       relative size comparisons
 * </ul>
 *
 * <h2>Match Validation Process</h2>
 *
 * <ol>
 *   <li><b>Initial Matching</b> - Template or feature-based matching
 *   <li><b>Validation</b> - Apply proofing strategies to verify matches
 *   <li><b>Scoring Adjustment</b> - Refine confidence scores
 *   <li><b>Filtering</b> - Remove invalid matches
 *   <li><b>Fusion</b> - Combine overlapping valid matches
 * </ol>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Edge-Based Validation</h3>
 *
 * <pre>{@code
 * // Validate matches using edge detection
 * EdgeBasedProofer edgeProofer = new EdgeBasedProofer();
 *
 * List<Match> validated = new ArrayList<>();
 * for (Match match : candidates) {
 *     if (edgeProofer.validate(match, scene)) {
 *         validated.add(match);
 *     }
 * }
 *
 * // Edge validation checks:
 * // - Edge density similarity
 * // - Edge orientation consistency
 * // - Structural integrity
 * }</pre>
 *
 * <h3>Region-Based Validation</h3>
 *
 * <pre>{@code
 * // Validate using regional properties
 * RegionBasedProofer regionProofer = new RegionBasedProofer();
 *
 * ProofingResult result = regionProofer.proof(match, scene);
 * if (result.isValid()) {
 *     match.setScore(result.getAdjustedScore());
 *     // Use validated match
 * }
 *
 * // Regional validation includes:
 * // - Color distribution matching
 * // - Texture consistency
 * // - Local feature verification
 * }</pre>
 *
 * <h3>Match Fusion</h3>
 *
 * <pre>{@code
 * // Fuse overlapping matches
 * MatchFusion fusion = new MatchFusion();
 * fusion.setDecider(new RelativeSizeFusionDecider(0.3)); // 30% overlap
 *
 * List<Match> fused = fusion.fuseMatches(allMatches);
 *
 * // Fusion combines:
 * // - Overlapping regions
 * // - Adjacent matches
 * // - Nested matches
 * }</pre>
 *
 * <h3>Custom Fusion Strategy</h3>
 *
 * <pre>{@code
 * // Implement custom fusion logic
 * MatchFusionDecider customDecider = new MatchFusionDecider() {
 *     @Override
 *     public boolean shouldFuse(Match m1, Match m2) {
 *         double overlap = calculateOverlap(m1, m2);
 *         double scoreDiff = Math.abs(m1.getScore() - m2.getScore());
 *
 *         // Fuse if high overlap and similar scores
 *         return overlap > 0.5 && scoreDiff < 0.1;
 *     }
 *
 *     @Override
 *     public Match fuse(Match m1, Match m2) {
 *         // Combine into optimal match
 *         return createFusedMatch(m1, m2);
 *     }
 * };
 * }</pre>
 *
 * <h2>Validation Strategies</h2>
 *
 * <h3>Edge-Based</h3>
 *
 * <ul>
 *   <li>Canny edge detection comparison
 *   <li>Gradient magnitude analysis
 *   <li>Edge continuity verification
 *   <li>Corner point matching
 * </ul>
 *
 * <h3>Region-Based</h3>
 *
 * <ul>
 *   <li>Histogram comparison
 *   <li>Texture analysis
 *   <li>Color coherence
 *   <li>Statistical moments
 * </ul>
 *
 * <h2>Fusion Strategies</h2>
 *
 * <h3>Overlap-Based</h3>
 *
 * <ul>
 *   <li>IoU (Intersection over Union) threshold
 *   <li>Percentage overlap of smaller region
 *   <li>Center distance threshold
 * </ul>
 *
 * <h3>Score-Based</h3>
 *
 * <ul>
 *   <li>Weighted average by confidence
 *   <li>Maximum score selection
 *   <li>Score distribution analysis
 * </ul>
 *
 * <h2>Best Practices</h2>
 *
 * <ol>
 *   <li>Combine multiple validation strategies for robustness
 *   <li>Adjust thresholds based on application requirements
 *   <li>Profile validation overhead for performance
 *   <li>Use appropriate fusion strategy for match characteristics
 *   <li>Validate fusion results to avoid false positives
 * </ol>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 *   <li>Edge detection is computationally intensive
 *   <li>Cache validation results when possible
 *   <li>Use parallel processing for multiple matches
 *   <li>Consider validation sampling for large match sets
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.model.match
 * @see io.github.jspinak.brobot.action.basic.find
 */
package io.github.jspinak.brobot.analysis.match;
