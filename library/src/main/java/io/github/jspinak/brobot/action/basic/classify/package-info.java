/**
 * Image classification and scene analysis actions.
 *
 * <p>This package provides machine learning-based classification capabilities for identifying and
 * categorizing visual elements in the GUI. It enables semantic understanding of screen content
 * beyond simple pattern matching.
 *
 * <h2>Classification Features</h2>
 *
 * <ul>
 *   <li><b>Image Classification</b> - Identify what objects or UI elements are present
 *   <li><b>Scene Analysis</b> - Understand the overall context of a screen or region
 *   <li><b>Multi-class Detection</b> - Detect multiple categories in a single image
 *   <li><b>Confidence Scoring</b> - Get probability scores for each classification
 * </ul>
 *
 * <h2>Primary Class</h2>
 *
 * <p><b>{@link io.github.jspinak.brobot.action.basic.classify.Classify}</b> - Performs image
 * classification using pre-trained models to identify GUI elements, scenes, or specific visual
 * patterns. Returns classifications with confidence scores.
 *
 * <h2>Classification Process</h2>
 *
 * <ol>
 *   <li>Capture screen region or use provided image
 *   <li>Preprocess image for model input
 *   <li>Run inference using classification model
 *   <li>Post-process results to extract class labels and scores
 *   <li>Filter results based on confidence threshold
 *   <li>Return classifications as matches with metadata
 * </ol>
 *
 * <h2>Use Cases</h2>
 *
 * <ul>
 *   <li>Identifying UI element types (button, textbox, menu, etc.)
 *   <li>Scene recognition (login screen, home page, error dialog)
 *   <li>State detection based on visual content
 *   <li>Semantic search for elements by type rather than appearance
 *   <li>Accessibility testing by identifying UI components
 *   <li>Cross-platform UI detection where appearance varies
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * // Classify elements in a region
 * Classify classify = new Classify(...);
 *
 * ActionOptions classifyOptions = new ActionOptions.Builder()
 *     .setAction(ActionType.CLASSIFY)
 *     .setConfidenceThreshold(0.8)  // Minimum 80% confidence
 *     .build();
 *
 * ObjectCollection targetRegion = new ObjectCollection.Builder()
 *     .withRegions(new Region(100, 100, 400, 300))
 *     .build();
 *
 * ActionResult classifications = classify.perform(new ActionResult(), targetRegion);
 *
 * // Process classification results
 * for (Match classification : classifications.getMatches()) {
 *     String className = classification.getText();  // e.g., "button", "dialog"
 *     double confidence = classification.getScore();
 *     Region location = classification.getRegion();
 *
 *     System.out.println(String.format("Found %s at %s with %.2f confidence",
 *         className, location, confidence));
 * }
 *
 * // Use classification for conditional logic
 * if (classifications.getMatches().stream()
 *     .anyMatch(m -> m.getText().equals("error_dialog"))) {
 *     // Handle error condition
 * }
 * }</pre>
 *
 * <h2>Integration with Other Actions</h2>
 *
 * <p>Classification results can be used with other actions:
 *
 * <ul>
 *   <li>Click on classified elements by type
 *   <li>Use classifications to determine current state
 *   <li>Combine with Find for hybrid detection strategies
 *   <li>Guide navigation based on scene understanding
 * </ul>
 *
 * <h2>Model Management</h2>
 *
 * <ul>
 *   <li>Support for multiple classification models
 *   <li>Model selection based on context or performance needs
 *   <li>Ability to use custom-trained models
 *   <li>Caching for improved performance
 * </ul>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Use appropriate confidence thresholds for your use case
 *   <li>Combine with traditional pattern matching for robustness
 *   <li>Consider performance implications of model inference
 *   <li>Validate classifications with additional checks when critical
 *   <li>Use region constraints to improve speed and accuracy
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.basic.find.Find
 * @see io.github.jspinak.brobot.state.StateObject
 */
package io.github.jspinak.brobot.action.basic.classify;
