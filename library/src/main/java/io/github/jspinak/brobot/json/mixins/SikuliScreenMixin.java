package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mixin for SikuliScreen class to prevent circular references.
 */
@JsonIgnoreProperties({"screen", "monitor", "robot", "mouseRobot", "lastMatch", "lastMatches", "lastScreenImage", "offset"})
public abstract class SikuliScreenMixin {
}
