/**
 * Image classification and scene analysis actions.
 * 
 * <p>This package provides machine learning-based classification capabilities for
 * identifying and categorizing visual elements in the GUI. It enables semantic
 * understanding of screen content beyond simple pattern matching.</p>
 * 
 * <h2>Classification Features</h2>
 * 
 * <ul>
 *   <li><b>Image Classification</b> - Identify what objects or UI elements are present</li>
 *   <li><b>Scene Analysis</b> - Understand the overall context of a screen or region</li>
 *   <li><b>Multi-class Detection</b> - Detect multiple categories in a single image</li>
 *   <li><b>Confidence Scoring</b> - Get probability scores for each classification</li>
 * </ul>
 * 
 * <h2>Primary Class</h2>
 * 
 * <p><b>{@link io.github.jspinak.brobot.action.basic.classify.Classify}</b> - 
 * Performs image classification using pre-trained models to identify GUI elements,
 * scenes, or specific visual patterns. Returns classifications with confidence scores.</p>
 * 
 * <h2>Classification Process</h2>
 * 
 * <ol>
 *   <li>Capture screen region or use provided image</li>
 *   <li>Preprocess image for model input</li>
 *   <li>Run inference using classification model</li>
 *   <li>Post-process results to extract class labels and scores</li>
 *   <li>Filter results based on confidence threshold</li>
 *   <li>Return classifications as matches with metadata</li>
 * </ol>
 * 
 * <h2>Use Cases</h2>
 * 
 * <ul>
 *   <li>Identifying UI element types (button, textbox, menu, etc.)</li>
 *   <li>Scene recognition (login screen, home page, error dialog)</li>
 *   <li>State detection based on visual content</li>
 *   <li>Semantic search for elements by type rather than appearance</li>
 *   <li>Accessibility testing by identifying UI components</li>
 *   <li>Cross-platform UI detection where appearance varies</li>
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
 * <p>Classification results can be used with other actions:</p>
 * <ul>
 *   <li>Click on classified elements by type</li>
 *   <li>Use classifications to determine current state</li>
 *   <li>Combine with Find for hybrid detection strategies</li>
 *   <li>Guide navigation based on scene understanding</li>
 * </ul>
 * 
 * <h2>Model Management</h2>
 * 
 * <ul>
 *   <li>Support for multiple classification models</li>
 *   <li>Model selection based on context or performance needs</li>
 *   <li>Ability to use custom-trained models</li>
 *   <li>Caching for improved performance</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Use appropriate confidence thresholds for your use case</li>
 *   <li>Combine with traditional pattern matching for robustness</li>
 *   <li>Consider performance implications of model inference</li>
 *   <li>Validate classifications with additional checks when critical</li>
 *   <li>Use region constraints to improve speed and accuracy</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.basic.find.Find
 * @see io.github.jspinak.brobot.state.StateObject
 */
package io.github.jspinak.brobot.action.basic.classify;