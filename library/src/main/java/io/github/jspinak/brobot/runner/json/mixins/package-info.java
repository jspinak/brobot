/**
 * Jackson mixin classes for third-party type serialization.
 * 
 * <p>This package provides Jackson mixin classes that enable JSON serialization
 * of third-party classes that cannot be directly annotated. Mixins allow us to
 * add Jackson annotations to classes from external libraries like Sikuli, OpenCV,
 * and Java AWT without modifying their source code.</p>
 * 
 * <h2>Mixin Classes</h2>
 * 
 * <h3>Sikuli Types</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.RegionMixin} - 
 *       Sikuli Region serialization</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.MatchMixin} - 
 *       Sikuli Match serialization</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.SikuliLocationMixin} - 
 *       Sikuli Location coordinates</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.SikuliScreenMixin} - 
 *       Sikuli Screen information</li>
 * </ul>
 * 
 * <h3>OpenCV Types</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.MatMixin} - 
 *       OpenCV Mat matrix serialization</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.JavaCVRectMixin} - 
 *       JavaCV Rect serialization</li>
 * </ul>
 * 
 * <h3>Java AWT Types</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.BufferedImageMixin} - 
 *       BufferedImage serialization</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.RectangleMixin} - 
 *       Rectangle geometry</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.Rectangle2DMixin} - 
 *       Rectangle2D geometry</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.ColorModelMixin} - 
 *       Color model information</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.DataBufferMixin} - 
 *       Image data buffer</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.RasterMixin} - 
 *       Raster data</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.WritableRasterMixin} - 
 *       Writable raster data</li>
 * </ul>
 * 
 * <h3>Brobot Types</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.BrobotImageMixin} - 
 *       Brobot Image class</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.mixins.SearchRegionsMixin} - 
 *       Search regions configuration</li>
 * </ul>
 * 
 * <h2>How Mixins Work</h2>
 * 
 * <p>Mixins are abstract classes that define Jackson annotations for target classes:</p>
 * <pre>{@code
 * // Mixin for Sikuli Region
 * @JsonIgnoreProperties({"screen", "device"})
 * public abstract class RegionMixin {
 *     @JsonProperty("x")
 *     public abstract int getX();
 *     
 *     @JsonProperty("y") 
 *     public abstract int getY();
 *     
 *     @JsonProperty("w")
 *     public abstract int getW();
 *     
 *     @JsonProperty("h")
 *     public abstract int getH();
 * }
 * }</pre>
 * 
 * <h2>Registration</h2>
 * 
 * <p>Mixins are registered with the ObjectMapper:</p>
 * <pre>{@code
 * mapper.addMixIn(Region.class, RegionMixin.class);
 * mapper.addMixIn(Match.class, MatchMixin.class);
 * // ... other mixins
 * }</pre>
 * 
 * <h2>Benefits</h2>
 * 
 * <ul>
 *   <li><b>Non-invasive</b> - No modification of third-party code</li>
 *   <li><b>Flexible</b> - Control serialization format</li>
 *   <li><b>Maintainable</b> - Centralized serialization logic</li>
 *   <li><b>Efficient</b> - Avoid circular references</li>
 * </ul>
 * 
 * <h2>Common Patterns</h2>
 * 
 * <h3>Ignoring Properties</h3>
 * <pre>{@code
 * @JsonIgnoreProperties({"internalState", "cache"})
 * }</pre>
 * 
 * <h3>Property Naming</h3>
 * <pre>{@code
 * @JsonProperty("width")
 * public abstract int getW();
 * }</pre>
 * 
 * <h3>Custom Serialization</h3>
 * <pre>{@code
 * @JsonSerialize(using = CustomSerializer.class)
 * public abstract ComplexType getComplexData();
 * }</pre>
 * 
 * @since 1.0
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn
 * @see io.github.jspinak.brobot.runner.json.module.BrobotJsonModule
 */
package io.github.jspinak.brobot.runner.json.mixins;