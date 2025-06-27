/**
 * Fundamental geometric and visual elements for GUI automation.
 * 
 * <p>This package provides the basic building blocks for representing screen
 * elements, locations, and visual patterns. These elements form the foundation
 * for all pattern matching and interaction operations in the Brobot framework.</p>
 * 
 * <h2>Core Element Types</h2>
 * 
 * <h3>Spatial Elements</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.element.Location} - Precise screen coordinates with offset support</li>
 *   <li>{@link io.github.jspinak.brobot.model.element.Region} - Rectangular screen areas with boundaries</li>
 *   <li>{@link io.github.jspinak.brobot.model.element.Position} - Relative positions within regions</li>
 *   <li>{@link io.github.jspinak.brobot.model.element.Positions} - Predefined position constants</li>
 * </ul>
 * 
 * <h3>Visual Elements</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.element.Image} - Image data with metadata and Mat representations</li>
 *   <li>{@link io.github.jspinak.brobot.model.element.Pattern} - Search patterns combining images with matching criteria</li>
 *   <li>{@link io.github.jspinak.brobot.model.element.Scene} - Complete screen captures with analysis data</li>
 *   <li>{@link io.github.jspinak.brobot.model.element.Text} - OCR text content and metadata</li>
 * </ul>
 * 
 * <h3>Composite Elements</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.element.Element} - Unified interface for all element types</li>
 *   <li>{@link io.github.jspinak.brobot.model.element.CompositeElement} - Multiple elements treated as one</li>
 *   <li>{@link io.github.jspinak.brobot.model.element.RegionElement} - Region with associated visual patterns</li>
 * </ul>
 * 
 * <h2>Coordinate System</h2>
 * 
 * <p>The package uses a standard screen coordinate system:</p>
 * <ul>
 *   <li>Origin (0,0) is at the top-left corner of the screen</li>
 *   <li>X-axis increases rightward</li>
 *   <li>Y-axis increases downward</li>
 *   <li>All measurements are in pixels</li>
 * </ul>
 * 
 * <h2>Location Model</h2>
 * 
 * <p>Locations support both absolute and relative positioning:</p>
 * 
 * <pre>{@code
 * // Absolute location
 * Location absolute = new Location(100, 200);
 * 
 * // Location with offset from region
 * Location relative = new Location(region, Positions.Name.CENTER);
 * relative.setOffset(10, -5); // 10 pixels right, 5 pixels up
 * 
 * // Dynamic calculation
 * int actualX = relative.getCalculatedX(); // Region center + offset
 * int actualY = relative.getCalculatedY();
 * }</pre>
 * 
 * <h2>Region Operations</h2>
 * 
 * <p>Regions provide rich geometric operations:</p>
 * 
 * <pre>{@code
 * Region screen = new Region(0, 0, 1920, 1080);
 * Region button = new Region(100, 100, 200, 50);
 * 
 * // Containment checks
 * if (screen.contains(button)) { ... }
 * if (button.contains(clickLocation)) { ... }
 * 
 * // Geometric operations
 * Region intersection = screen.getIntersection(button);
 * Region union = Region.getUnion(region1, region2);
 * 
 * // Boundary adjustments
 * Region expanded = button.getCopy();
 * expanded.adjustByPixels(10); // Expand by 10 pixels on all sides
 * }</pre>
 * 
 * <h2>Pattern Matching</h2>
 * 
 * <p>Patterns combine visual data with search criteria:</p>
 * 
 * <pre>{@code
 * Pattern buttonPattern = new Pattern.Builder()
 *     .withImage("button.png")
 *     .withSimilarity(0.95)        // 95% match threshold
 *     .withSearchRegion(topHalf)   // Limit search area
 *     .withTargetOffset(5, 0)      // Click 5 pixels right of center
 *     .build();
 * }</pre>
 * 
 * <h2>Image Management</h2>
 * 
 * <p>Images support multiple formats and lazy loading:</p>
 * 
 * <pre>{@code
 * Image screenshot = new Image("screenshot.png");
 * 
 * // Access OpenCV Mat representations
 * Mat bgrMat = screenshot.getMatBGR();    // BGR color space
 * Mat hsvMat = screenshot.getMatHSV();    // HSV color space
 * Mat grayMat = screenshot.getMatGRAY();  // Grayscale
 * 
 * // Image metadata
 * int width = screenshot.getWidth();
 * int height = screenshot.getHeight();
 * int channels = screenshot.getChannels();
 * }</pre>
 * 
 * <h2>Text Handling</h2>
 * 
 * <p>Text elements support OCR results and string operations:</p>
 * 
 * <pre>{@code
 * Text ocrResult = new Text();
 * ocrResult.add("Username: ");
 * ocrResult.add("john_doe");
 * 
 * String fullText = ocrResult.asString(); // "Username: john_doe"
 * List<String> lines = ocrResult.getTextLines();
 * }</pre>
 * 
 * <h2>Scene Analysis</h2>
 * 
 * <p>Scenes represent complete screen states for analysis:</p>
 * 
 * <pre>{@code
 * Scene currentScene = new Scene.Builder()
 *     .withScreenshot(captureScreen())
 *     .withTimestamp(Instant.now())
 *     .build();
 * 
 * // Scene can be analyzed for:
 * // - Color distributions
 * // - Motion detection
 * // - Pattern matching
 * // - State classification
 * }</pre>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>Most element classes are immutable and thread-safe. Mutable classes
 * like Region provide copy constructors for safe concurrent use:</p>
 * 
 * <pre>{@code
 * Region shared = new Region(0, 0, 100, 100);
 * Region threadLocal = shared.getCopy(); // Safe copy for modification
 * }</pre>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li>Use builders for complex object construction</li>
 *   <li>Prefer immutable elements when sharing across threads</li>
 *   <li>Cache Image objects to avoid repeated file I/O</li>
 *   <li>Use appropriate Position enums instead of magic offsets</li>
 *   <li>Validate regions stay within screen boundaries</li>
 * </ol>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state
 * @see io.github.jspinak.brobot.model.match
 * @see io.github.jspinak.brobot.action
 */
package io.github.jspinak.brobot.model.element;