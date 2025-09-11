package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin for java.awt.image.DataBuffer to control JSON serialization.
 *
 * <p>This mixin prevents serialization of DataBuffer's low-level memory structures and
 * implementation details. DataBuffer holds raw pixel data arrays that are typically too large and
 * complex for direct JSON serialization.
 *
 * <p>Properties ignored:
 *
 * <ul>
 *   <li>data - Raw pixel data arrays
 *   <li>bankData - Multi-bank pixel data storage
 *   <li>offsets - Bank offset values
 *   <li>size - Buffer size information
 *   <li>dataType - Pixel data type constant
 * </ul>
 *
 * @see java.awt.image.DataBuffer
 * @see java.awt.image.Raster
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({"data", "bankData", "offsets", "size", "dataType"})
public abstract class DataBufferMixin {}
