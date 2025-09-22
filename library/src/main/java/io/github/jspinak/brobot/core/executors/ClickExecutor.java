package io.github.jspinak.brobot.core.executors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.core.location.ElementLocator;
import io.github.jspinak.brobot.core.services.MouseController;
import io.github.jspinak.brobot.model.element.Location;
// Removed old logging import: 
/**
 * Executes click operations on elements WITHOUT depending on Find.
 *
 * <p>This executor handles the actual click mechanics after elements have been located. It depends
 * only on MouseController, completely breaking the circular dependency with Find.
 *
 * <p>Key characteristics:
 *
 * <ul>
 *   <li>NO dependency on Find or any Action classes
 *   <li>Depends only on core services (MouseController)
 *   <li>Handles click execution, not element location
 *   <li>Thread-safe operations
 * </ul>
 *
 * @since 2.0.0
 */
@Component
public class ClickExecutor {

    private final MouseController mouseController;

    public ClickExecutor(@Qualifier("sikuliMouseController") MouseController mouseController) {
        this.mouseController = mouseController;
    }

    /** Configuration for click operations. */
    public static class ClickOptions {
        private int clickCount = 1;
        private MouseController.MouseButton button = MouseController.MouseButton.LEFT;
        private int delayBetweenClicks = 50; // milliseconds
        private boolean moveBeforeClick = true;

        public int getClickCount() {
            return clickCount;
        }

        public void setClickCount(int clickCount) {
            this.clickCount = clickCount;
        }

        public MouseController.MouseButton getButton() {
            return button;
        }

        public void setButton(MouseController.MouseButton button) {
            this.button = button;
        }

        public int getDelayBetweenClicks() {
            return delayBetweenClicks;
        }

        public void setDelayBetweenClicks(int delayBetweenClicks) {
            this.delayBetweenClicks = delayBetweenClicks;
        }

        public boolean isMoveBeforeClick() {
            return moveBeforeClick;
        }

        public void setMoveBeforeClick(boolean moveBeforeClick) {
            this.moveBeforeClick = moveBeforeClick;
        }

        public static class Builder {
            private final ClickOptions options = new ClickOptions();

            public Builder withClickCount(int count) {
                options.clickCount = count;
                return this;
            }

            public Builder withButton(MouseController.MouseButton button) {
                options.button = button;
                return this;
            }

            public Builder withDelayBetweenClicks(int delayMs) {
                options.delayBetweenClicks = delayMs;
                return this;
            }

            public Builder withMoveBeforeClick(boolean move) {
                options.moveBeforeClick = move;
                return this;
            }

            public ClickOptions build() {
                return options;
            }
        }
    }

    /**
     * Executes a click on an element.
     *
     * <p>This method performs the actual click operation on a located element. It handles multiple
     * clicks if configured.
     *
     * @param element The element to click on
     * @param options Click configuration
     * @return true if click was successful, false otherwise
     */
    public boolean executeClick(ElementLocator.Element element, ClickOptions options) {
        if (element == null || options == null) {
            return false;
        }

        Location target = element.getLocation();
        return executeClick(target, options);
    }

    /**
     * Executes a click at a specific location.
     *
     * <p>This method performs the actual click operation at the specified screen coordinates.
     *
     * @param location The location to click at
     * @param options Click configuration
     * @return true if click was successful, false otherwise
     */
    public boolean executeClick(Location location, ClickOptions options) {
        if (location == null || options == null) {
            return false;
        }

        int x = location.getCalculatedX();
        int y = location.getCalculatedY();


        // Move to location first if configured
        if (options.isMoveBeforeClick()) {
            if (!mouseController.moveTo(x, y)) {
                return false;
            }
        }

        // Perform the clicks
        boolean success = true;
        for (int i = 0; i < options.getClickCount(); i++) {
            // Handle double-click specially
            if (options.getClickCount() == 2 && i == 0) {
                // Use double-click method for better reliability
                success = mouseController.doubleClick(x, y, options.getButton());
                break; // Don't need second iteration
            } else {
                // Regular click
                success = mouseController.click(x, y, options.getButton());
                if (!success) {
                    break;
                }

                // Delay between clicks (except after last click)
                if (i < options.getClickCount() - 1) {
                    try {
                        Thread.sleep(options.getDelayBetweenClicks());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }

        if (success) {
        }

        return success;
    }

    /**
     * Executes a simple left-click at a location.
     *
     * <p>Convenience method for the most common click operation.
     *
     * @param location The location to click at
     * @return true if click was successful, false otherwise
     */
    public boolean executeClick(Location location) {
        return executeClick(location, new ClickOptions());
    }

    /**
     * Executes a right-click (context menu) at a location.
     *
     * <p>Convenience method for right-clicking.
     *
     * @param location The location to right-click at
     * @return true if right-click was successful, false otherwise
     */
    public boolean executeRightClick(Location location) {
        ClickOptions options =
                new ClickOptions.Builder().withButton(MouseController.MouseButton.RIGHT).build();
        return executeClick(location, options);
    }

    /**
     * Executes a double-click at a location.
     *
     * <p>Convenience method for double-clicking.
     *
     * @param location The location to double-click at
     * @return true if double-click was successful, false otherwise
     */
    public boolean executeDoubleClick(Location location) {
        ClickOptions options = new ClickOptions.Builder().withClickCount(2).build();
        return executeClick(location, options);
    }
}
