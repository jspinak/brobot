package io.github.jspinak.brobot.action.composite.chains;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Provides advanced composite action patterns that combine multiple sequential actions.
 *
 * <p>This class demonstrates how to build complex automation workflows by orchestrating multiple
 * basic actions into cohesive sequences. It serves as both a utility class and an example of how to
 * create custom composite actions that encapsulate application-specific interaction patterns.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Sequential execution of multiple action types
 *   <li>Coordinated mouse movements and clicks
 *   <li>Advanced wait conditions with visual feedback
 *   <li>Fine-grained timing control between action steps
 * </ul>
 *
 * <p>This class exemplifies the power of the composite action pattern in Brobot, showing how
 * complex GUI interactions can be abstracted into reusable methods. Teams are encouraged to create
 * similar classes tailored to their specific application workflows.
 *
 * <p>This class has been updated to use the modern ActionConfig API with ActionChainOptions for
 * better type safety and cleaner code. The rightClickAndMoveUntilVanishes method now demonstrates
 * how to build complex action sequences using the fluent ActionChainOptions API.
 *
 * @see ActionChainOptions
 * @see ActionChainExecutor
 * @see ClickOptions
 * @see MouseMoveOptions
 * @see VanishOptions
 * @see HighlightOptions
 */
@Component
public class ActionSequenceBuilder {

    private final ActionChainExecutor chainExecutor;
    private final Action action;

    public ActionSequenceBuilder(ActionChainExecutor chainExecutor, Action action) {
        this.chainExecutor = chainExecutor;
        this.action = action;
    }

    /**
     * Repeatedly right-clicks an image with controlled mouse movements until it vanishes.
     *
     * <p>This method demonstrates a sophisticated composite action pattern that combines:
     *
     * <ol>
     *   <li>Mouse movement to the target position with a pause
     *   <li>Right-click action with configurable timing
     *   <li>Mouse movement away from the clicked position
     *   <li>Visual feedback through region highlighting
     *   <li>Vanish detection with reduced similarity threshold
     * </ol>
     *
     * <p>This pattern is particularly useful for context menu operations where the mouse needs to
     * be moved away after clicking to avoid interfering with menu visibility or to trigger
     * hover-based UI changes. The reduced similarity threshold for vanish detection (original
     * similarity - 0.10) provides more lenient matching to account for visual changes during the
     * interaction.
     *
     * <p><b>Execution flow:</b>
     *
     * <ul>
     *   <li>For each iteration (up to timesToClick):
     *       <ol>
     *         <li>Move mouse to image location and pause
     *         <li>Perform right-click
     *         <li>If click successful, move mouse by (xMove, yMove) and highlight the region
     *         <li>Check if image has vanished within pauseBetweenClicks seconds
     *         <li>Return true immediately if vanished, otherwise continue
     *       </ol>
     * </ul>
     *
     * @param timesToClick Maximum number of right-click attempts before giving up
     * @param pauseBetweenClicks Time in seconds to wait for vanish detection after each click
     * @param pauseBeforeClick Delay in seconds after moving to target before clicking
     * @param pauseAfterMove Delay in seconds after moving away from the clicked position
     * @param image The state image to right-click repeatedly
     * @param xMove Horizontal offset in pixels to move the mouse after clicking
     * @param yMove Vertical offset in pixels to move the mouse after clicking
     * @return true if the image vanished before reaching the click limit, false if the maximum
     *     attempts were exhausted without the image vanishing
     */
    public boolean rightClickAndMoveUntilVanishes(
            int timesToClick,
            double pauseBetweenClicks,
            double pauseBeforeClick,
            double pauseAfterMove,
            StateImage image,
            int xMove,
            int yMove) {
        // Build the action chain for move -> right-click -> move sequence
        ActionChainOptions clickSequence =
                new ActionChainOptions.Builder(
                                // First: Move to the image
                                new MouseMoveOptions.Builder()
                                        .setPauseAfterEnd(pauseBeforeClick)
                                        .build())
                        // Then: Right-click the image
                        .then(
                                new ClickOptions.Builder()
                                        .setPressOptions(
                                                MousePressOptions.builder()
                                                        .setButton(MouseButton.RIGHT)
                                                        .setPauseBeforeMouseDown(pauseBeforeClick)
                                                        .build())
                                        .build())
                        .build();

        // Create the object collection for the target image
        ObjectCollection imageCollection = new ObjectCollection.Builder().withImages(image).build();

        // We'll create a location offset from the match for the move

        // Create vanish options with reduced similarity
        VanishOptions vanishOptions =
                new VanishOptions.Builder()
                        .setTimeout(pauseBetweenClicks)
                        .setSimilarity(
                                Math.max(
                                        0.1, 0.8 - 0.10)) // Use default similarity if not specified
                        .build();

        // Execute the click sequence up to timesToClick times
        for (int i = 0; i < timesToClick; i++) {
            // Execute the move->click sequence
            ActionResult result =
                    chainExecutor.executeChain(
                            clickSequence,
                            new ActionResult(),
                            imageCollection, // For move
                            imageCollection // For click
                            );

            if (result.isSuccess()) {
                // Move mouse away from clicked position
                result.getBestMatch()
                        .ifPresent(
                                match -> {
                                    // Create a region from the match for highlighting
                                    Region matchRegion = new Region(match);

                                    // Create location offset from the match position
                                    // Use match center coordinates and add offset
                                    int centerX = match.x() + match.w() / 2;
                                    int centerY = match.y() + match.h() / 2;
                                    Location offsetLocation =
                                            new Location(centerX + xMove, centerY + yMove);

                                    // Move to the offset location
                                    MouseMoveOptions moveAway =
                                            new MouseMoveOptions.Builder()
                                                    .setPauseAfterEnd(pauseAfterMove)
                                                    .build();
                                    action.perform(
                                            moveAway,
                                            new ObjectCollection.Builder()
                                                    .withLocations(offsetLocation)
                                                    .build());

                                    // Highlight the clicked region for visual feedback
                                    HighlightOptions highlightOptions =
                                            new HighlightOptions.Builder()
                                                    .setHighlightSeconds(1.0)
                                                    .build();
                                    action.perform(
                                            highlightOptions,
                                            new ObjectCollection.Builder()
                                                    .withRegions(matchRegion)
                                                    .build());
                                });
            }

            // Check if the image has vanished
            if (action.perform(vanishOptions, image).isSuccess()) {
                System.out.println(
                        "object vanished, min similarity = " + vanishOptions.getSimilarity());
                return true;
            }
        }

        return false;
    }
}
