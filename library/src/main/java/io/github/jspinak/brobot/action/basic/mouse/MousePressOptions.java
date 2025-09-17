package io.github.jspinak.brobot.action.basic.mouse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import io.github.jspinak.brobot.model.action.MouseButton;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration for mouse button press-and-release behaviors.
 *
 * <p>This class encapsulates all settings related to the physical mouse button press and release
 * events, including which button to press and timing parameters. It is designed to be a reusable
 * component, composed within higher-level action configurations like {@code ClickOptions}, {@code
 * DragOptions}, {@code MouseDownOptions}, and {@code MouseUpOptions}.
 *
 * <p>It is an immutable object and must be constructed using its inner {@link Builder}.
 */
@Getter
@Builder(toBuilder = true, setterPrefix = "set", builderClassName = "MousePressOptionsBuilder")
@JsonDeserialize(builder = MousePressOptions.MousePressOptionsBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class MousePressOptions {

    @Builder.Default private final MouseButton button = MouseButton.LEFT;

    @Builder.Default private final double pauseBeforeMouseDown = 0.0;

    @Builder.Default private final double pauseAfterMouseDown = 0.0;

    @Builder.Default private final double pauseBeforeMouseUp = 0.0;

    @Builder.Default private final double pauseAfterMouseUp = 0.0;

    @JsonPOJOBuilder(withPrefix = "set")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MousePressOptionsBuilder {
        // Lombok generates the implementation
    }
}
