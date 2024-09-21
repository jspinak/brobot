package io.github.jspinak.brobot.dsl;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constants and utility methods specific to the DSL (like keywords, etc.)
 *
 * Centralized Constants: It defines all the keywords and delimiters used in the DSL, making it easy to update them if needed.
 * Utility Methods: It includes methods for common operations like formatting DSL lines and validating action types.
 * Validation: It provides a place to implement validation logic for the DSL structure.
 * Parsing Helpers: It includes methods to assist in parsing different parts of the DSL.
 * High-Level Operations: It offers methods to create common action types, abstracting away the details of ActionDefinition creation.
 * DSL Generation and Parsing: It provides centralized methods for converting between ActionDefinition objects and DSL strings.
 *
 * By centralizing these operations and constants in the ActionDSL class, the DSL implementation is more maintainable and easier to use.
 * Other classes like ActionEncoder, ActionDecoder, and DSLParser can use the methods and constants from this class, ensuring consistency
 * across the DSL implementation.
 */
public class ActionDSL {

    // DSL Keywords
    public static final String ACTION_KEYWORD = "ACTION";
    public static final String ON_KEYWORD = "ON";
    public static final String OPTIONS_KEYWORD = "OPTIONS";
    public static final String STEP_KEYWORD = "STEP";

    // DSL Delimiters
    public static final String LINE_SEPARATOR = "\n";
    public static final String KEY_VALUE_SEPARATOR = ":";

    // Action Types
    public static final String CLICK_ACTION = "CLICK";
    public static final String DRAG_ACTION = "DRAG";
    public static final String TYPE_ACTION = "TYPE";
    public static final String FIND_ACTION = "FIND";
    // Add more action types as needed

    // Utility methods for DSL operations
    public static boolean isValidActionType(String actionType) {
        // Check if the action type is valid
        return Arrays.asList(CLICK_ACTION, DRAG_ACTION, TYPE_ACTION, FIND_ACTION).contains(actionType.toUpperCase());
    }

    public static String formatActionLine(String actionType) {
        return ACTION_KEYWORD + KEY_VALUE_SEPARATOR + actionType;
    }

    public static String formatOptionLine(String key, Object value) {
        return key + KEY_VALUE_SEPARATOR + value.toString();
    }

    public static String formatOnLine(String objectDescription) {
        return ON_KEYWORD + KEY_VALUE_SEPARATOR + objectDescription;
    }

    // Validation methods
    public static void validateDSLStructure(List<String> dslLines) throws DSLException {
        // Implement validation logic for the overall DSL structure
        // Throw DSLException if the structure is invalid
    }

    // Helper methods for parsing
    public static Map<String, String> parseOptions(List<String> optionLines) {
        Map<String, String> options = new HashMap<>();
        for (String line : optionLines) {
            String[] parts = line.split(KEY_VALUE_SEPARATOR, 2);
            if (parts.length == 2) {
                options.put(parts[0].trim(), parts[1].trim());
            }
        }
        return options;
    }

    // Higher-level DSL operations
    public static ActionDefinition createClickAction(StateImage targetObject, Map<String, Object> options) {
        ActionDefinition action = new ActionDefinition();
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                // Set other options based on the provided map
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(targetObject)
                .build();
        action.addStep(actionOptions, objectCollection);
        return action;
    }

    public static ActionDefinition createDragAction(StateImage sourceObject, StateImage targetObject, Map<String, Object> options) {
        ActionDefinition action = new ActionDefinition();

        // Mouse down on source
        ActionOptions mouseDownOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.MOUSE_DOWN)
                .build();
        ObjectCollection sourceCollection = new ObjectCollection.Builder()
                .withImages(sourceObject)
                .build();
        action.addStep(mouseDownOptions, sourceCollection);

        // Move to target
        ActionOptions mouseMoveOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.MOVE)
                // Set drag options from the provided map
                .build();
        ObjectCollection targetCollection = new ObjectCollection.Builder()
                .withImages(targetObject)
                .build();
        action.addStep(mouseMoveOptions, targetCollection);

        // Mouse up
        ActionOptions mouseUpOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.MOUSE_UP)
                .build();
        action.addStep(mouseUpOptions, new ObjectCollection.Builder().build());

        return action;
    }

    // DSL generation methods
    public static String generateDSL(ActionDefinition actionDefinition) {
        StringBuilder dsl = new StringBuilder();

        for (int i = 0; i < actionDefinition.getSteps().size(); i++) {
            ActionStep step = actionDefinition.getSteps().get(i);
            dsl.append(STEP_KEYWORD).append(i + 1).append(KEY_VALUE_SEPARATOR).append(LINE_SEPARATOR);

            dsl.append(OPTIONS_KEYWORD).append(KEY_VALUE_SEPARATOR).append(LINE_SEPARATOR);
            dsl.append(formatActionOptions(step.getOptions())).append(LINE_SEPARATOR);

            dsl.append(ON_KEYWORD).append(KEY_VALUE_SEPARATOR);
            dsl.append(formatObjectCollection(step.getObjects())).append(LINE_SEPARATOR);
        }

        return dsl.toString();
    }

    private static String formatActionOptions(ActionOptions options) {
        StringBuilder optionsString = new StringBuilder();

        // Add each non-null field of ActionOptions
        if (options.getAction() != null) {
            optionsString.append(formatOptionLine("action", options.getAction())).append(LINE_SEPARATOR);
        }
        if (options.getClickUntil() != null) {
            optionsString.append(formatOptionLine("clickUntil", options.getClickUntil())).append(LINE_SEPARATOR);
        }
        if (options.getFind() != null) {
            optionsString.append(formatOptionLine("find", options.getFind())).append(LINE_SEPARATOR);
        }
        // Add more fields as necessary...

        optionsString.append(formatOptionLine("keepLargerMatches", options.isKeepLargerMatches())).append(LINE_SEPARATOR);
        optionsString.append(formatOptionLine("captureImage", options.isCaptureImage())).append(LINE_SEPARATOR);
        optionsString.append(formatOptionLine("useDefinedRegion", options.isUseDefinedRegion())).append(LINE_SEPARATOR);
        optionsString.append(formatOptionLine("similarity", options.getSimilarity())).append(LINE_SEPARATOR);

        // Handle fields that might be null

        return optionsString.toString();
    }

    private static String formatObjectCollection(ObjectCollection objectCollection) {
        StringBuilder collectionString = new StringBuilder();

        for (StateImage stateImage : objectCollection.getStateImages()) {
            collectionString.append("STATEIMAGE(").append(stateImage.getName()).append(") ");
        }

        // Add handling for other types in ObjectCollection if necessary

        return collectionString.toString().trim();
    }

    // DSL parsing methods
    public static ActionDefinition parseDSL(String dslString) {
        // This method needs to be implemented to parse the DSL string and create an ActionDefinition
        // It should handle the creation of ActionOptions and ObjectCollection based on the DSL
        return null; // Placeholder
    }
}