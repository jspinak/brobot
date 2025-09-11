/**
 * Models for automated state discovery and structure learning.
 *
 * <p>This package contains specialized models that support the automated discovery of application
 * states and their relationships. These models enable Brobot to learn application structure through
 * exploration and observation, building state models dynamically.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.state.discovery.ProvisionalState} -
 *       Candidate state under evaluation before formal acceptance
 *   <li>{@link io.github.jspinak.brobot.model.analysis.state.discovery.ImageSceneMap} - Mapping
 *       between discovered images and their scene contexts
 * </ul>
 *
 * <h2>Discovery Workflow</h2>
 *
 * <h3>1. Provisional State Creation</h3>
 *
 * <pre>{@code
 * ProvisionalState candidate = new ProvisionalState();
 * candidate.setName("PotentialLoginScreen");
 *
 * // Add discovered elements
 * candidate.addImage(usernameFieldImage);
 * candidate.addImage(passwordFieldImage);
 * candidate.addImage(loginButtonImage);
 * candidate.addRegion(formAreaRegion);
 * }</pre>
 *
 * <h3>2. State Validation</h3>
 *
 * <pre>{@code
 * // Test state stability across multiple observations
 * for (int i = 0; i < 5; i++) {
 *     Scene currentScene = captureScreen();
 *     boolean elementsFound = candidate.validateAgainst(currentScene);
 *     candidate.updateConfidence(elementsFound);
 * }
 *
 * if (candidate.getConfidence() > 0.9) {
 *     // High confidence - promote to formal state
 *     State formalState = candidate.toState();
 *     StateStore.add(formalState);
 * }
 * }</pre>
 *
 * <h3>3. Image-Scene Mapping</h3>
 *
 * <pre>{@code
 * ImageSceneMap sceneMap = new ImageSceneMap();
 *
 * // Map discovered images to their contexts
 * sceneMap.addMapping(buttonImage, loginScene);
 * sceneMap.addMapping(logoImage, Arrays.asList(loginScene, homeScene));
 *
 * // Query mappings
 * List<Scene> scenesWithButton = sceneMap.getScenesContaining(buttonImage);
 * Set<Image> loginElements = sceneMap.getImagesInScene(loginScene);
 * }</pre>
 *
 * <h2>Provisional States</h2>
 *
 * <p>ProvisionalState represents a potential state that hasn't been confirmed yet. Key features:
 *
 * <ul>
 *   <li><b>Element Collection</b> - Accumulates discovered elements
 *   <li><b>Confidence Tracking</b> - Measures state stability
 *   <li><b>Validation Logic</b> - Tests against new observations
 *   <li><b>Conversion</b> - Transforms to formal State when ready
 * </ul>
 *
 * <h3>Confidence Calculation</h3>
 *
 * <pre>{@code
 * public class ProvisionalState {
 *     private double confidence = 0.5;
 *     private int observations = 0;
 *
 *     public void updateConfidence(boolean elementsFound) {
 *         observations++;
 *         if (elementsFound) {
 *             // Increase confidence with successful observations
 *             confidence = confidence + (1 - confidence) * 0.1;
 *         } else {
 *             // Decrease confidence with failed observations
 *             confidence = confidence * 0.8;
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h2>Image-Scene Relationships</h2>
 *
 * <p>ImageSceneMap tracks where images appear across different scenes:
 *
 * <ul>
 *   <li>One-to-many: Image can appear in multiple scenes
 *   <li>Many-to-one: Scene contains multiple images
 *   <li>Temporal: Track when images appear/disappear
 *   <li>Contextual: Understand image roles in different contexts
 * </ul>
 *
 * <h3>Usage Patterns</h3>
 *
 * <pre>{@code
 * // Discover shared elements
 * Set<Image> sharedElements = sceneMap.getImagesInMultipleScenes();
 *
 * // Find unique identifiers
 * Set<Image> uniqueToLogin = sceneMap.getUniqueImages(loginScene);
 *
 * // Track element persistence
 * Map<Image, Integer> imageCounts = sceneMap.getImageOccurrenceCounts();
 * }</pre>
 *
 * <h2>State Structure Learning</h2>
 *
 * <p>The discovery process learns application structure:
 *
 * <ol>
 *   <li>Observe application behavior
 *   <li>Identify recurring visual patterns
 *   <li>Group patterns into provisional states
 *   <li>Validate state uniqueness and stability
 *   <li>Discover transitions between states
 *   <li>Build complete state model
 * </ol>
 *
 * <h2>Integration Example</h2>
 *
 * <pre>{@code
 * public class StateDiscoveryService {
 *     private Map<String, ProvisionalState> candidates = new HashMap<>();
 *     private ImageSceneMap sceneMap = new ImageSceneMap();
 *
 *     public void exploreApplication() {
 *         // Systematic exploration
 *         for (Element clickable : findClickableElements()) {
 *             Scene before = captureScreen();
 *             click.perform(clickable);
 *             Scene after = captureScreen();
 *
 *             if (!before.equals(after)) {
 *                 // State change detected
 *                 analyzeStateChange(before, after, clickable);
 *             }
 *         }
 *     }
 *
 *     private void analyzeStateChange(Scene from, Scene to, Element trigger) {
 *         // Extract unique elements in 'to' scene
 *         Set<Image> newElements = findNewElements(from, to);
 *
 *         // Create or update provisional state
 *         String stateId = generateStateId(newElements);
 *         ProvisionalState state = candidates.computeIfAbsent(
 *             stateId,
 *             k -> new ProvisionalState()
 *         );
 *
 *         state.addElements(newElements);
 *         state.addTransitionFrom(from, trigger);
 *
 *         // Update mappings
 *         newElements.forEach(img -> sceneMap.addMapping(img, to));
 *     }
 * }
 * }</pre>
 *
 * <h2>Best Practices</h2>
 *
 * <ol>
 *   <li>Require multiple confirmations before accepting states
 *   <li>Track negative evidence (elements that should NOT appear)
 *   <li>Consider dynamic content when validating states
 *   <li>Use image-scene maps to identify state indicators
 *   <li>Combine automated discovery with manual verification
 *   <li>Version discovered models for change tracking
 * </ol>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state
 * @see io.github.jspinak.brobot.state.stateDiscovery
 * @see io.github.jspinak.brobot.model.analysis.scene
 */
package io.github.jspinak.brobot.model.analysis.state.discovery;
