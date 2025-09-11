package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin for Sikuli's Location class to control JSON serialization.
 *
 * <p>This mixin prevents circular reference issues during JSON serialization by ignoring properties
 * that contain references to Screen, Monitor, and offset objects which can create infinite loops in
 * the object graph.
 *
 * <p>Properties ignored:
 *
 * <ul>
 *   <li>screen - The associated Screen object
 *   <li>monitor - The associated Monitor object
 *   <li>offset - Offset coordinates
 *   <li>targetOffset - Target offset coordinates
 * </ul>
 *
 * @see org.sikuli.script.Location
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({"screen", "monitor", "offset", "targetOffset"})
public abstract class SikuliLocationMixin {}
