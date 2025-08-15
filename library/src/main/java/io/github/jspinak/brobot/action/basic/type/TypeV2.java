package io.github.jspinak.brobot.action.basic.type;
import io.github.jspinak.brobot.action.ActionType;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import org.sikuli.basics.Settings;
import org.sikuli.script.Key;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Pure Type action that types text without embedded Find.
 * <p>
 * This is a "pure" action that only performs the type operation. It can type
 * text at the current cursor location or click on a provided location first.
 * It does not perform any Find operations. This separation enables better
 * testing, cleaner code, and more flexible action composition.
 * </p>
 * 
 * <p>
 * Usage patterns:
 * <ul>
 * <li>Type at current location: {@code new TypeV2().perform(actionResult)}</li>
 * <li>Click and type: {@code new TypeV2().perform(actionResult, location)}</li>
 * <li>Type after finding: Use ConditionalActionChain</li>
 * </ul>
 * </p>
 * 
 * <p>
 * For Find-then-Type operations, use ConditionalActionChain:
 * 
 * <pre>{@code
 * ConditionalActionChain.find(findOptions)
 *         .ifFound(new TypeOptions.Builder().withText("Hello").build())
 *         .perform(objectCollection);
 * }</pre>
 * </p>
 * 
 * @since 2.0
 * @see Type for the legacy version with embedded Find
 * @see ConditionalActionChain for chaining Find with Type
 */
@Component("typeV2")
public class TypeV2 implements ActionInterface {

    private static final Logger logger = Logger.getLogger(TypeV2.class.getName());
    private Screen screen;
    private boolean headlessMode = false;

    private Screen getScreen() {
        if (screen == null && !headlessMode) {
            try {
                screen = new Screen();
            } catch (Exception e) {
                logger.warning("Failed to initialize Screen - running in headless mode: " + e.getMessage());
                headlessMode = true;
            }
        }
        return screen;
    }

    @Override
    public ActionInterface.Type getActionType() {
        return ActionInterface.Type.TYPE;
    }

    @Override
    public void perform(ActionResult actionResult, ObjectCollection... objectCollections) {
        actionResult.setSuccess(false);

        try {
            // Extract text to type - for now just type "test"
            String textToType = extractTextToType(objectCollections);

            if (textToType == null || textToType.isEmpty()) {
                logger.warning("No text to type provided to TypeV2");
                return;
            }

            // Click on location if provided
            Location clickLocation = extractClickLocation(objectCollections);
            if (clickLocation != null && !headlessMode) {
                try {
                    org.sikuli.script.Location sikuliLoc = clickLocation.sikuli();
                    sikuliLoc.click();
                    Thread.sleep(100);
                } catch (Exception e) {
                    logger.warning("Failed to click location: " + e.getMessage());
                }
            } else if (clickLocation != null && headlessMode) {
                logger.info("Running in headless mode - simulating click at: " + clickLocation);
            }

            // Type the text
            boolean typeSuccess = typeText(textToType);

            actionResult.setSuccess(typeSuccess);
            if (typeSuccess) {
                logger.info("TypeV2: Successfully typed text");

                // Add a match for the typed location if we have one
                if (clickLocation != null) {
                    actionResult.add(createMatchFromLocation(clickLocation));
                }
            }

        } catch (Exception e) {
            logger.severe("Error in TypeV2: " + e.getMessage());
            actionResult.setSuccess(false);
        }
    }

    /**
     * Extracts the text to type from ObjectCollections.
     */
    private String extractTextToType(ObjectCollection... collections) {
        // Check ObjectCollections for strings
        for (ObjectCollection collection : collections) {
            if (!collection.getStateStrings().isEmpty()) {
                return collection.getStateStrings().get(0).getString();
            }
        }

        return null;
    }

    /**
     * Extracts a click location from the provided object collections.
     */
    private Location extractClickLocation(ObjectCollection... collections) {
        for (ObjectCollection collection : collections) {
            // Check for direct locations
            if (!collection.getStateLocations().isEmpty()) {
                return collection.getStateLocations().get(0).getLocation();
            }

            // Check for regions (use center)
            if (!collection.getStateRegions().isEmpty()) {
                Region region = collection.getStateRegions().get(0).getSearchRegion();
                int centerX = region.x() + region.w() / 2;
                int centerY = region.y() + region.h() / 2;
                return new Location(centerX, centerY);
            }
        }

        return null;
    }

    /**
     * Types the specified text.
     */
    private boolean typeText(String text) {
        try {
            if (headlessMode) {
                logger.info("Running in headless mode - simulating type: " + text);
                return true;
            }

            Screen currentScreen = getScreen();
            if (currentScreen == null) {
                logger.warning("Screen not available for typing");
                return false;
            }

            // Type the text
            currentScreen.type(text);

            logger.fine("Successfully typed: " + text);
            return true;

        } catch (Exception e) {
            logger.warning("Failed to type text: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a Match object from a Location for result reporting.
     */
    private Match createMatchFromLocation(Location location) {
        Region region = new Region(location.getX() - 10, location.getY() - 10, 20, 20);
        Match match = new Match(region);
        match.setName("Typed at location");
        return match;
    }
}