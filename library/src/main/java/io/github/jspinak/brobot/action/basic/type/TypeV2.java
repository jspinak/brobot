package io.github.jspinak.brobot.action.basic.type;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import org.sikuli.basics.Settings;
import org.sikuli.script.Key;
import org.sikuli.script.Mouse;
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
 * <p>Usage patterns:
 * <ul>
 *   <li>Type at current location: {@code new TypeV2().perform(typeOptions)}</li>
 *   <li>Click and type: {@code new TypeV2().perform(typeOptions, location)}</li>
 *   <li>Type after finding: Use ConditionalActionChain</li>
 * </ul>
 * </p>
 * 
 * <p>For Find-then-Type operations, use ConditionalActionChain:
 * <pre>{@code
 * ConditionalActionChain.find(findOptions)
 *     .ifFound(new TypeOptions.Builder().setText("Hello").build())
 *     .perform(objectCollection);
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
    private final Screen screen = new Screen();
    
    @Override
    public ActionResult perform(ActionConfig actionConfig, ObjectCollection... objectCollections) {
        if (!(actionConfig instanceof TypeOptions)) {
            throw new IllegalArgumentException("TypeV2 requires TypeOptions configuration");
        }
        
        TypeOptions typeOptions = (TypeOptions) actionConfig;
        ActionResult result = new ActionResult();
        result.setActionType("TYPE_V2");
        
        try {
            // Extract text to type
            String textToType = extractTextToType(typeOptions, objectCollections);
            
            if (textToType == null || textToType.isEmpty()) {
                result.setSuccess(false);
                result.setText("No text to type provided");
                return result;
            }
            
            // Click on location if provided
            Location clickLocation = extractClickLocation(objectCollections);
            if (clickLocation != null && typeOptions.isClickLocationFirst()) {
                Mouse.click(clickLocation.getSikuliLocation());
                Thread.sleep((long)(typeOptions.getPauseAfterClick() * 1000));
            }
            
            // Clear field if requested
            if (typeOptions.isClearField()) {
                clearField(typeOptions);
            }
            
            // Type the text
            boolean typeSuccess = typeText(textToType, typeOptions);
            
            result.setSuccess(typeSuccess);
            result.setText(typeSuccess ? "Typed: " + textToType : "Failed to type text");
            
            // Add a match for the typed location if we have one
            if (typeSuccess && clickLocation != null) {
                result.addMatch(createMatchFromLocation(clickLocation));
            }
            
        } catch (Exception e) {
            logger.severe("Error in TypeV2: " + e.getMessage());
            result.setSuccess(false);
            result.setText("Type failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Extracts the text to type from TypeOptions or ObjectCollections.
     */
    private String extractTextToType(TypeOptions options, ObjectCollection... collections) {
        // First check TypeOptions for text
        if (options.getText() != null && !options.getText().isEmpty()) {
            return options.getText();
        }
        
        // Then check ObjectCollections for strings
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
            if (!collection.getLocations().isEmpty()) {
                return collection.getLocations().get(0);
            }
            
            // Check for regions (use center)
            if (!collection.getStateRegions().isEmpty()) {
                return collection.getStateRegions().get(0).getSearchRegion().getCenter();
            }
            
            // Check for matches
            if (!collection.getMatches().getMatchList().isEmpty()) {
                return collection.getMatches().getMatchList().get(0).getRegion().getCenter();
            }
        }
        
        return null;
    }
    
    /**
     * Clears the current field based on the operating system.
     */
    private void clearField(TypeOptions options) throws Exception {
        String clearKeys;
        
        // Determine OS-specific select-all shortcut
        if (Settings.isMac()) {
            clearKeys = Key.CMD + "a";
        } else {
            clearKeys = Key.CTRL + "a";
        }
        
        // Select all and delete
        screen.type(clearKeys);
        Thread.sleep(100);
        screen.type(Key.DELETE);
        
        // Apply pause after clearing
        if (options.getPauseBeforeType() > 0) {
            Thread.sleep((long)(options.getPauseBeforeType() * 1000));
        }
    }
    
    /**
     * Types the specified text with the given options.
     */
    private boolean typeText(String text, TypeOptions options) {
        try {
            // Apply typing delay if specified
            double originalDelay = Settings.TypeDelay;
            if (options.getTypingDelay() > 0) {
                Settings.TypeDelay = options.getTypingDelay();
            }
            
            // Pause before typing if specified
            if (options.getPauseBeforeType() > 0) {
                Thread.sleep((long)(options.getPauseBeforeType() * 1000));
            }
            
            // Type the text
            screen.type(text);
            
            // Press Enter if requested
            if (options.isPressEnterAfterTyping()) {
                Thread.sleep(100);
                screen.type(Key.ENTER);
            }
            
            // Restore original typing delay
            Settings.TypeDelay = originalDelay;
            
            logger.info("Successfully typed: " + text);
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
        Match match = new Match.Builder()
            .setRegion(location.asRegion())
            .setScore(1.0)
            .setText("Typed at location")
            .build();
        return match;
    }
}