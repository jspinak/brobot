package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin for Brobot's SearchRegions class to control JSON serialization.
 *
 * <p>This mixin prevents circular reference issues that can occur when serializing SearchRegions
 * objects. The deepCopy property or method can create infinite recursion during serialization if it
 * returns a reference to complex object graphs or self-referential structures. This is particularly
 * important for search region collections that may contain nested or interconnected regions.
 *
 * <p>Properties ignored:
 *
 * <ul>
 *   <li>deepCopy - Deep copy method/property that could cause circular references
 * </ul>
 *
 * @see io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.SearchRegions
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({"deepCopy"})
public abstract class SearchRegionsMixin {}
