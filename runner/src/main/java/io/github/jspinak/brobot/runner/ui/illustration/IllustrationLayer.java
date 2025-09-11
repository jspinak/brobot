package io.github.jspinak.brobot.runner.ui.illustration;

import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single layer in an illustration that can be toggled on/off.
 *
 * <p>Layers enable composition of complex illustrations by separating different visual elements
 * (base image, search regions, matches, actions, etc.) into independently controllable components.
 *
 * @see IllustrationViewer
 */
@Getter
@Setter
public class IllustrationLayer {

    private final String name;
    private final int zOrder;
    private final BooleanProperty visible = new SimpleBooleanProperty(true);

    private Image image;
    private Consumer<GraphicsContext> drawingConsumer;
    private double opacity = 1.0;

    public IllustrationLayer(String name, int zOrder) {
        this.name = name;
        this.zOrder = zOrder;
    }

    /**
     * Renders this layer to the given graphics context.
     *
     * @param gc the graphics context to render to
     */
    public void render(GraphicsContext gc) {
        if (!visible.get()) {
            return;
        }

        gc.save();
        gc.setGlobalAlpha(opacity);

        // Draw image if present
        if (image != null) {
            gc.drawImage(image, 0, 0);
        }

        // Execute custom drawing if present
        if (drawingConsumer != null) {
            drawingConsumer.accept(gc);
        }

        gc.restore();
    }

    /**
     * Checks if this layer is currently visible.
     *
     * @return true if visible
     */
    public boolean isVisible() {
        return visible.get();
    }

    /**
     * Sets the visibility of this layer.
     *
     * @param visible true to show, false to hide
     */
    public void setVisible(boolean visible) {
        this.visible.set(visible);
    }

    /**
     * Gets the visible property for binding.
     *
     * @return the visible property
     */
    public BooleanProperty visibleProperty() {
        return visible;
    }
}
