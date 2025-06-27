/**
 * Visual history and execution tracking for GUI automation debugging.
 * 
 * <p>This package provides comprehensive visualization capabilities for Brobot's
 * execution history, creating visual documentation of automated actions, state
 * recognition results, and analysis outcomes. It transforms abstract automation
 * execution into concrete visual representations that aid in debugging,
 * understanding, and validating automation behavior.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>Visualization Control</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.history.VisualizationOrchestrator} - 
 *       Coordinates all visualization components to create comprehensive illustrations</li>
 *   <li>{@link io.github.jspinak.brobot.tools.history.IllustrationController} - 
 *       Controls when and which actions get visualized to prevent redundancy</li>
 *   <li>{@link io.github.jspinak.brobot.tools.history.HistoryFileNamer} - 
 *       Generates unique, informative filenames for history files</li>
 * </ul>
 * 
 * <h3>Action Visualization</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.history.ActionVisualizer} - 
 *       Core drawing utilities for visualizing action results on screenshots</li>
 *   <li>{@link io.github.jspinak.brobot.tools.history.VisualizationLayout} - 
 *       Matrix operations for arranging visual elements in layouts</li>
 * </ul>
 * 
 * <h3>State Visualization</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.history.StateLayoutVisualizer} - 
 *       Shows expected element positions based on state definitions</li>
 *   <li>{@link io.github.jspinak.brobot.tools.history.RuntimeStateVisualizer} - 
 *       Shows actual runtime recognition results with color coding</li>
 * </ul>
 * 
 * <h2>Visualization Process</h2>
 * 
 * <ol>
 *   <li><b>Action Execution</b> - Automation performs an action</li>
 *   <li><b>Result Capture</b> - Screenshot and match data collected</li>
 *   <li><b>Permission Check</b> - Controller determines if visualization needed</li>
 *   <li><b>Orchestration</b> - Components assembled in drawing order</li>
 *   <li><b>File Generation</b> - Visual saved with descriptive filename</li>
 * </ol>
 * 
 * <h2>Visual Elements</h2>
 * 
 * <h3>Action Indicators</h3>
 * <ul>
 *   <li><b>Click Points</b> - Circular markers at click locations</li>
 *   <li><b>Drag Arrows</b> - Arrows showing drag direction</li>
 *   <li><b>Match Rectangles</b> - Boxes around found elements</li>
 *   <li><b>Search Regions</b> - Areas where search occurred</li>
 * </ul>
 * 
 * <h3>Analysis Results</h3>
 * <ul>
 *   <li><b>Color Classifications</b> - Pixel categorization results</li>
 *   <li><b>Motion Detection</b> - Dynamic pixel highlighting</li>
 *   <li><b>Histograms</b> - Color distribution graphs</li>
 *   <li><b>Contours</b> - Object boundary detection</li>
 * </ul>
 * 
 * <h3>State Information</h3>
 * <ul>
 *   <li><b>State Boundaries</b> - Blue rectangles for states</li>
 *   <li><b>Element Positions</b> - Red rectangles for elements</li>
 *   <li><b>Transition Markers</b> - Green indicators (reserved)</li>
 * </ul>
 * 
 * <h2>Subpackages</h2>
 * 
 * <h3>draw</h3>
 * <p>Low-level drawing primitives for specific visual elements</p>
 * 
 * <h3>visual</h3>
 * <p>Data structures for visualization results and layouts</p>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Basic Action Visualization</h3>
 * <pre>{@code
 * // Automatically visualized through action execution
 * ActionOptions options = new ActionOptions.Builder()
 *     .action(Action.CLICK)
 *     .captureScreenshots(true)
 *     .build();
 * 
 * // Visualization created if permitted
 * ActionResult result = action.perform(options);
 * }</pre>
 * 
 * <h3>State Visualization</h3>
 * <pre>{@code
 * // Visualize expected state layout
 * StateLayoutVisualizer layoutViz = new StateLayoutVisualizer();
 * Mat stateLayout = layoutViz.createStateVisualization(state);
 * 
 * // Visualize runtime recognition
 * RuntimeStateVisualizer runtimeViz = new RuntimeStateVisualizer();
 * Mat runtimeView = runtimeViz.illustrate(state, matchSnapshots);
 * }</pre>
 * 
 * <h3>Custom Visualization</h3>
 * <pre>{@code
 * // Direct use of visualization components
 * ActionVisualizer visualizer = new ActionVisualizer();
 * visualizer.drawRectangle(image, match.getRegion(), Color.RED);
 * visualizer.drawPoint(image, clickLocation, Color.BLUE);
 * }</pre>
 * 
 * <h2>Configuration</h2>
 * 
 * <p>Visualization behavior controlled by:</p>
 * <ul>
 *   <li>{@code FrameworkSettings.saveHistory} - Global history enable/disable</li>
 *   <li>{@code FrameworkSettings.screenshot} - Screenshot capture settings</li>
 *   <li>Action-specific capture flags</li>
 *   <li>Illustration directives in ActionOptions</li>
 * </ul>
 * 
 * <h2>File Organization</h2>
 * 
 * <p>History files are organized with descriptive names:</p>
 * <pre>
 * {timestamp}_{action}_{targetImages}_{result}.png
 * 
 * Examples:
 * 2024-01-15_140523_CLICK_loginButton_SUCCESS.png
 * 2024-01-15_140530_FIND_searchBox_menuIcon_MULTIPLE.png
 * </pre>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Enable visualization during development and debugging</li>
 *   <li>Use illustration controller to prevent redundant files</li>
 *   <li>Configure appropriate history retention policies</li>
 *   <li>Review visual history to understand failures</li>
 *   <li>Disable in production for performance</li>
 * </ul>
 * 
 * <h2>Performance Considerations</h2>
 * 
 * <ul>
 *   <li>Visualization adds overhead to action execution</li>
 *   <li>File I/O can impact performance</li>
 *   <li>Consider selective visualization for long runs</li>
 *   <li>Implement cleanup policies for history files</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.action
 * @see io.github.jspinak.brobot.config.FrameworkSettings
 * @see io.github.jspinak.brobot.model.action
 */
package io.github.jspinak.brobot.tools.history;