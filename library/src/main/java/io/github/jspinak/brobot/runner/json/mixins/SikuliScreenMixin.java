package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin for Sikuli's Screen class to control JSON serialization.
 * <p>
 * This mixin prevents serialization of Screen's complex internal objects and
 * circular references that would cause JSON serialization failures. Screen
 * objects contain references to system resources, robot instances, and cached
 * match results that are not suitable for serialization and could create
 * infinite loops in the object graph.
 * <p>
 * Properties ignored:
 * <ul>
 * <li>screen - Self-reference or parent screen reference</li>
 * <li>monitor - Associated physical monitor object</li>
 * <li>robot - AWT Robot instance for automation</li>
 * <li>mouseRobot - Mouse-specific robot instance</li>
 * <li>lastMatch - Cached result of last match operation</li>
 * <li>lastMatches - Collection of recent match results</li>
 * <li>lastScreenImage - Cached screenshot data</li>
 * <li>offset - Screen offset coordinates</li>
 * </ul>
 *
 * @see org.sikuli.script.Screen
 * @see java.awt.Robot
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({"screen", "monitor", "robot", "mouseRobot", "lastMatch", "lastMatches", "lastScreenImage", "offset"})
public abstract class SikuliScreenMixin {
}
