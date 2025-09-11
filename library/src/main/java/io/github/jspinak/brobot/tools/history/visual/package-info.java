/**
 * Data structures for visualization results and layouts.
 *
 * <p>This package contains the data models and structures used to represent visualization results
 * in Brobot's history system. These classes organize the visual components, maintain relationships
 * between elements, and provide structured access to visualization data.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.history.visual.Visualization} - Primary container for
 *       visualization data and results
 *   <li>{@link io.github.jspinak.brobot.tools.history.visual.StateVisualization} - Visual
 *       representation of state recognition results
 *   <li>{@link io.github.jspinak.brobot.tools.history.visual.AnalysisSidebar} - Sidebar component
 *       showing analysis details and metrics
 *   <li>{@link io.github.jspinak.brobot.tools.history.visual.ClassificationLegend} - Legend for
 *       pixel classification visualizations
 * </ul>
 *
 * <h2>Visualization Structure</h2>
 *
 * <h3>Main Visualization</h3>
 *
 * <pre>{@code
 * Visualization viz = new Visualization();
 *
 * // Set base components
 * viz.setScene(capturedScreen);
 * viz.setActionResult(result);
 *
 * // Add analysis layers
 * viz.setClassifications(classificationMat);
 * viz.setMotionMask(motionDetection);
 * viz.setContours(extractedContours);
 *
 * // Generate final image
 * Mat output = viz.generateCompleteVisualization();
 * }</pre>
 *
 * <h3>State Visualization</h3>
 *
 * <pre>{@code
 * StateVisualization stateViz = new StateVisualization();
 *
 * // Add state elements
 * stateViz.addStateRegion(stateBounds, Color.BLUE);
 * stateViz.addElement(elementRegion, Color.RED);
 * stateViz.addTransition(transitionRegion, Color.GREEN);
 *
 * // Set match information
 * stateViz.setMatchSnapshots(snapshots);
 * stateViz.setConfidenceScores(scores);
 * }</pre>
 *
 * <h3>Analysis Sidebar</h3>
 *
 * <pre>{@code
 * AnalysisSidebar sidebar = new AnalysisSidebar();
 *
 * // Add metrics
 * sidebar.addMetric("Action", "CLICK");
 * sidebar.addMetric("Target", "loginButton");
 * sidebar.addMetric("Success", "true");
 * sidebar.addMetric("Duration", "250ms");
 * sidebar.addMetric("Confidence", "0.95");
 *
 * // Add visual elements
 * sidebar.addHistogram(colorHistogram);
 * sidebar.addColorProfile(targetProfile);
 * }</pre>
 *
 * <h2>Data Organization</h2>
 *
 * <h3>Layered Approach</h3>
 *
 * <p>Visualizations are built in layers:
 *
 * <ol>
 *   <li><b>Base Layer</b> - Screenshot or scene
 *   <li><b>Analysis Layer</b> - Classifications, motion
 *   <li><b>Match Layer</b> - Found elements, regions
 *   <li><b>Action Layer</b> - Click points, drags
 *   <li><b>Annotation Layer</b> - Labels, arrows
 *   <li><b>UI Layer</b> - Sidebar, legend
 * </ol>
 *
 * <h3>Metadata Storage</h3>
 *
 * <pre>{@code
 * // Visualization maintains metadata
 * Visualization viz = new Visualization();
 * viz.setTimestamp(System.currentTimeMillis());
 * viz.setActionType(Action.FIND);
 * viz.setStateInfo(currentState);
 * viz.setDuration(executionTime);
 *
 * // Access metadata
 * Map<String, Object> metadata = viz.getMetadata();
 * }</pre>
 *
 * <h2>Visualization Types</h2>
 *
 * <h3>Action Visualizations</h3>
 *
 * <ul>
 *   <li><b>Click</b> - Point marker with optional ripple effect
 *   <li><b>Find</b> - Rectangles around all matches
 *   <li><b>Drag</b> - Arrow from start to end point
 *   <li><b>Type</b> - Text overlay at input location
 * </ul>
 *
 * <h3>Analysis Visualizations</h3>
 *
 * <ul>
 *   <li><b>Color</b> - Pixel classifications with legend
 *   <li><b>Motion</b> - Highlighted moving regions
 *   <li><b>Histogram</b> - Distribution graphs
 *   <li><b>Contour</b> - Object boundaries
 * </ul>
 *
 * <h3>State Visualizations</h3>
 *
 * <ul>
 *   <li><b>Layout</b> - Expected element positions
 *   <li><b>Runtime</b> - Actual recognition results
 *   <li><b>Comparison</b> - Expected vs actual overlay
 * </ul>
 *
 * <h2>Usage Patterns</h2>
 *
 * <h3>Building Complete Visualization</h3>
 *
 * <pre>{@code
 * public Visualization createActionVisualization(
 *         ActionResult result,
 *         Mat scene) {
 *
 *     Visualization viz = new Visualization();
 *     viz.setScene(scene);
 *     viz.setActionResult(result);
 *
 *     // Add action-specific elements
 *     switch (result.getAction()) {
 *         case CLICK:
 *             viz.addClickPoint(result.getClickPoint());
 *             break;
 *         case FIND:
 *             viz.addMatches(result.getMatches());
 *             break;
 *         case DRAG:
 *             viz.addDragPath(result.getDragPath());
 *             break;
 *     }
 *
 *     // Add sidebar
 *     AnalysisSidebar sidebar = createSidebar(result);
 *     viz.setSidebar(sidebar);
 *
 *     return viz;
 * }
 * }</pre>
 *
 * <h3>Accessing Results</h3>
 *
 * <pre>{@code
 * // Get final visualization
 * Mat output = visualization.getFinalImage();
 *
 * // Get individual components
 * Mat scene = visualization.getScene();
 * Mat classifications = visualization.getClassifications();
 * List<Match> matches = visualization.getMatches();
 *
 * // Get metadata
 * long timestamp = visualization.getTimestamp();
 * String actionType = visualization.getActionType();
 * }</pre>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Initialize visualization with scene first
 *   <li>Add layers in proper order for visibility
 *   <li>Include relevant metadata for context
 *   <li>Use consistent color schemes
 *   <li>Provide clear legends for complex visualizations
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.tools.history
 * @see io.github.jspinak.brobot.model.action
 */
package io.github.jspinak.brobot.tools.history.visual;
