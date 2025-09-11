package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin for java.awt.image.WritableRaster to control JSON serialization.
 *
 * <p>This mixin prevents serialization of WritableRaster's low-level data structures and
 * implementation details. WritableRaster extends Raster and provides write access to pixel data,
 * containing complex memory management structures that are not suitable for JSON serialization.
 * These internal structures would typically be reconstructed from pixel data rather than serialized
 * directly.
 *
 * <p>Properties ignored:
 *
 * <ul>
 *   <li>dataElements - Raw pixel data in primitive array form
 *   <li>dataBuffer - Low-level DataBuffer containing pixel storage
 *   <li>numBands - Number of bands (color channels) in the raster
 *   <li>numDataElements - Number of data elements per pixel
 *   <li>parent - Reference to parent Raster if this is a sub-raster
 *   <li>sampleModel - Describes how pixels are stored in the DataBuffer
 * </ul>
 *
 * @see java.awt.image.WritableRaster
 * @see java.awt.image.Raster
 * @see java.awt.image.DataBuffer
 * @see java.awt.image.SampleModel
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({
    "dataElements",
    "dataBuffer",
    "numBands",
    "numDataElements",
    "parent",
    "sampleModel"
})
public abstract class WritableRasterMixin {}
