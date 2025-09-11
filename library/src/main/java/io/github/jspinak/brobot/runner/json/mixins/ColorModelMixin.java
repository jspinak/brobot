package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin for java.awt.image.ColorModel to control JSON serialization.
 *
 * <p>This mixin prevents serialization of ColorModel's complex internal structures that define how
 * pixel values are interpreted as colors. These properties contain native color space
 * implementations and bit-level specifications that are not suitable for JSON representation and
 * would typically be reconstructed from higher-level color space identifiers.
 *
 * <p>Properties ignored:
 *
 * <ul>
 *   <li>colorSpace - The ColorSpace object defining color interpretation
 *   <li>components - Array of color components
 *   <li>bits - Bit depth for each color component
 *   <li>componentSize - Size of each color component
 *   <li>transparency - Transparency mode constant
 * </ul>
 *
 * @see java.awt.image.ColorModel
 * @see java.awt.color.ColorSpace
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({"colorSpace", "components", "bits", "componentSize", "transparency"})
public abstract class ColorModelMixin {}
