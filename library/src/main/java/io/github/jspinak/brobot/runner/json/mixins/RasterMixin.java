package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin for java.awt.image.Raster to control JSON serialization.
 *
 * <p>This mixin prevents serialization of Raster's complex internal data structures and
 * implementation details. Raster objects contain low-level pixel storage mechanisms, memory
 * layouts, and data buffers that are not suitable for JSON serialization. These properties would
 * typically be reconstructed from pixel data rather than serialized directly.
 *
 * <p>Properties ignored:
 *
 * <ul>
 *   <li>raster - Self-reference or sub-raster reference
 *   <li>colorModel - Associated ColorModel for pixel interpretation
 *   <li>data - Raw pixel data
 *   <li>properties - Property map for custom attributes
 *   <li>propertyNames - Array of property names
 *   <li>graphics - Graphics context (from BufferedImage inheritance)
 *   <li>accelerationPriority - Hardware acceleration hint
 *   <li>dataElements - Pixel data in primitive form
 *   <li>dataOffsets - Offsets for multi-band data
 *   <li>dataBuffer - Low-level data storage buffer
 *   <li>dataOffset - Starting offset in data buffer
 *   <li>scanlineStride - Number of data elements between scanlines
 *   <li>pixelStride - Number of data elements between pixels
 * </ul>
 *
 * @see java.awt.image.Raster
 * @see java.awt.image.DataBuffer
 * @see java.awt.image.SampleModel
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({
    "raster",
    "colorModel",
    "data",
    "properties",
    "propertyNames",
    "graphics",
    "accelerationPriority",
    "dataElements",
    "dataOffsets",
    "dataBuffer",
    "dataOffset",
    "scanlineStride",
    "pixelStride"
})
public abstract class RasterMixin {}
