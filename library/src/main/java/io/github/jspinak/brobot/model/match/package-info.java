/**
 * Pattern matching results and match representation.
 *
 * <p>This package provides data structures for representing pattern matching results in the Brobot
 * framework. Matches are the fundamental output of find operations, containing location data,
 * similarity scores, and metadata about what was found on the screen.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.match.Match} - Primary result object containing
 *       location, score, and metadata
 *   <li>{@link io.github.jspinak.brobot.model.match.EmptyMatch} - Represents unsuccessful match
 *       attempts with failure context
 * </ul>
 *
 * <h2>Match Structure</h2>
 *
 * <p>A Match contains comprehensive information about a successful pattern match:
 *
 * <ul>
 *   <li><b>Location</b> - Screen coordinates where pattern was found
 *   <li><b>Region</b> - Bounding box of the matched area
 *   <li><b>Score</b> - Similarity score (0.0 to 1.0)
 *   <li><b>StateObjectData</b> - Reference to source pattern and state
 *   <li><b>Timestamp</b> - When the match was found
 *   <li><b>Scene</b> - Screenshot context (optional)
 *   <li><b>Text</b> - OCR text if applicable
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Match Usage</h3>
 *
 * <pre>{@code
 * ActionResult result = find.perform(button);
 *
 * // Check if found
 * if (!result.isEmpty()) {
 *     Match match = result.getBestMatch().get();
 *
 *     // Access match properties
 *     Location clickPoint = match.getTarget();
 *     double similarity = match.getScore();
 *     Region matchArea = match.getRegion();
 *     String ownerState = match.getOwnerStateName();
 *
 *     // Use for interaction
 *     click.perform(match);
 * }
 * }</pre>
 *
 * <h3>Multiple Matches</h3>
 *
 * <pre>{@code
 * ActionResult results = find.all(checkbox);
 *
 * // Process all matches
 * for (Match match : results.getMatchList()) {
 *     if (match.getScore() > 0.9) {
 *         // High confidence match
 *         processMatch(match);
 *     }
 * }
 *
 * // Get specific matches
 * Optional<Match> best = results.getBestMatch();
 * Optional<Match> closest = results.getClosestTo(referencePoint);
 * Optional<Region> median = results.getMedian();
 * }</pre>
 *
 * <h3>Match Creation</h3>
 *
 * <pre>{@code
 * // Matches are typically created by find operations
 * // But can be constructed for testing or special cases
 * Match customMatch = new Match.Builder()
 *     .setRegion(new Region(100, 100, 50, 50))
 *     .setScore(0.95)
 *     .setStateObjectData(buttonData)
 *     .setTimestamp(LocalDateTime.now())
 *     .build();
 * }</pre>
 *
 * <h2>Match Properties</h2>
 *
 * <h3>Score Interpretation</h3>
 *
 * <ul>
 *   <li>1.0 = Perfect match (pixel-identical)
 *   <li>0.95+ = Excellent match (imperceptible differences)
 *   <li>0.90+ = Good match (minor variations)
 *   <li>0.80+ = Acceptable match (noticeable differences)
 *   <li>&lt;0.80 = Poor match (significant differences)
 * </ul>
 *
 * <h3>Target vs Region</h3>
 *
 * <p>Matches distinguish between the matched area and interaction point:
 *
 * <ul>
 *   <li><b>Region</b> - The full area that matched the pattern
 *   <li><b>Target</b> - The specific point for interaction (often center, but configurable)
 * </ul>
 *
 * <pre>{@code
 * Match match = findResult.getBestMatch().get();
 * Region fullArea = match.getRegion();      // Entire matched area
 * Location clickPoint = match.getTarget();  // Where to click
 *
 * // Target can be offset from center
 * match.setTargetOffset(10, -5); // 10 pixels right, 5 up from center
 * }</pre>
 *
 * <h2>Empty Matches</h2>
 *
 * <p>EmptyMatch provides context for failed searches:
 *
 * <pre>{@code
 * if (result.isEmpty()) {
 *     Match firstMatch = result.getMatchList().isEmpty() ?
 *         new EmptyMatch() : result.getMatchList().get(0);
 *
 *     if (firstMatch instanceof EmptyMatch) {
 *         EmptyMatch empty = (EmptyMatch) firstMatch;
 *         logger.warn("No match found in region: {}",
 *             empty.getSearchRegion());
 *     }
 * }
 * }</pre>
 *
 * <h2>Match Metadata</h2>
 *
 * <p>Matches carry rich metadata for analysis:
 *
 * <pre>{@code
 * // State association
 * String ownerState = match.getOwnerStateName();
 * StateObjectData stateData = match.getStateObjectData();
 *
 * // Timing information
 * LocalDateTime foundAt = match.getTimestamp();
 * int actedUpon = match.getTimesActedOn();
 *
 * // Source information
 * String imageName = match.getName();
 * StateImage sourceImage = match.getStateObjectData().getStateObject();
 * }</pre>
 *
 * <h2>Advanced Features</h2>
 *
 * <h3>Match as Anchor</h3>
 *
 * <p>Use matches as reference points for relative operations:
 *
 * <pre>{@code
 * Match anchor = find.perform(logo).getBestMatch().get();
 *
 * // Define relative positions
 * Location belowLogo = new Location(anchor.getRegion(), Positions.Name.BELOWCENTER);
 * Region searchArea = anchor.getRegion().below(100);
 *
 * // Find relative to anchor
 * ObjectCollection relativeSearch = new ObjectCollection.Builder()
 *     .withSearchRegions(searchArea)
 *     .build();
 * }</pre>
 *
 * <h3>Match Validation</h3>
 *
 * <pre>{@code
 * public boolean validateMatch(Match match) {
 *     // Check score threshold
 *     if (match.getScore() < minSimilarity) return false;
 *
 *     // Verify location constraints
 *     if (!screenBounds.contains(match.getRegion())) return false;
 *
 *     // Check state association
 *     if (!expectedStates.contains(match.getOwnerStateName())) return false;
 *
 *     // Verify size constraints
 *     if (match.w() < minWidth || match.h() < minHeight) return false;
 *
 *     return true;
 * }
 * }</pre>
 *
 * <h3>Match Conversion</h3>
 *
 * <pre>{@code
 * // Convert match to StateImage for dynamic patterns
 * StateImage dynamicImage = match.toStateImage();
 *
 * // Use matched region as new search area
 * Region matchedArea = match.getRegion();
 * StateRegion dynamicRegion = new StateRegion.Builder()
 *     .withSearchRegion(matchedArea)
 *     .build();
 * }</pre>
 *
 * <h2>Best Practices</h2>
 *
 * <ol>
 *   <li>Always check match scores before using results
 *   <li>Use EmptyMatch instead of null for failed searches
 *   <li>Validate matches are within expected screen regions
 *   <li>Log match scores for debugging pattern quality
 *   <li>Consider match context (state, timestamp) for validation
 *   <li>Use appropriate score thresholds for different use cases
 * </ol>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>Match objects are designed to be immutable after creation, making them thread-safe for
 * concurrent access. The builder pattern should be used by single threads only. EmptyMatch is a
 * singleton and inherently thread-safe.
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.action.ActionResult
 * @see io.github.jspinak.brobot.action.basic.find
 * @see io.github.jspinak.brobot.model.state.StateObject
 */
package io.github.jspinak.brobot.model.match;
