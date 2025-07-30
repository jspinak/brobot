package io.github.jspinak.brobot.action.internal.options;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
// ColorFindOptions removed - not used in adapter
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.action.basic.find.MatchFusionOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
// ClickUntilOptions removed - deprecated in favor of action chaining
import io.github.jspinak.brobot.model.element.Location;
import org.springframework.stereotype.Component;

import static io.github.jspinak.brobot.action.internal.options.ActionOptions.Action.*;
import static io.github.jspinak.brobot.action.internal.options.ActionOptions.Find.*;
import static io.github.jspinak.brobot.action.internal.options.ActionOptions.DoOnEach.*;
import static io.github.jspinak.brobot.action.internal.options.ActionOptions.GetTextUntil.*;
import static io.github.jspinak.brobot.action.internal.options.ActionOptions.MatchFusionMethod.*;
import static io.github.jspinak.brobot.action.internal.options.ActionOptions.DefineAs.*;
import static io.github.jspinak.brobot.action.internal.options.ActionOptions.Illustrate.*;

/**
 * Adapter to convert legacy ActionOptions to the new Options class hierarchy.
 * <p>
 * This class provides a safe migration path from the monolithic ActionOptions
 * to the new specialized Options classes. It extracts relevant fields from
 * ActionOptions and creates the appropriate Options instance based on the
 * action type.
 * <p>
 * This is a temporary component that will be removed once all classes have
 * been migrated to use the new Options classes directly.
 *
 * @see ActionOptions
 * @see ActionConfig
 * 
 * @deprecated Since version 2.0, this adapter is only for migration purposes.
 *             New code should use ActionConfig implementations directly rather
 *             than converting from ActionOptions.
 */
@Component
public class ActionOptionsAdapter {

    /**
     * Converts an ActionOptions instance to the appropriate ActionConfig subclass.
     *
     * @param actionOptions The legacy options to convert
     * @return The corresponding ActionConfig instance
     * @throws IllegalArgumentException if the action type is not supported
     */
    public ActionConfig convert(ActionOptions actionOptions) {
        if (actionOptions == null) {
            throw new IllegalArgumentException("ActionOptions cannot be null");
        }

        ActionOptions.Action action = actionOptions.getAction();
        if (action == null) {
            // Default to FIND if no action specified
            action = ActionOptions.Action.FIND;
        }

        switch (action) {
            case FIND:
                return convertToFindOptions(actionOptions);
            case CLICK:
                return convertToClickOptions(actionOptions);
            case TYPE:
                return convertToTypeOptions(actionOptions);
            case DEFINE:
                return convertToDefineOptions(actionOptions);
            case HIGHLIGHT:
                return convertToHighlightOptions(actionOptions);
            case MOVE:
                return convertToMouseMoveOptions(actionOptions);
            case VANISH:
                return convertToVanishOptions(actionOptions);
            case CLICK_UNTIL:
                // CLICK_UNTIL is deprecated - convert to regular click with action chaining
                // For now, return a simple click and log a warning
                System.out.println("WARNING: CLICK_UNTIL is deprecated. Use action chaining instead.");
                return convertToClickOptions(actionOptions);
            default:
                throw new IllegalArgumentException("Unsupported action type: " + action);
        }
    }

    private PatternFindOptions convertToFindOptions(ActionOptions ao) {
        // Note: Color finds should use ColorFindOptions directly, not through this method
        // For now, we'll convert all finds to PatternFindOptions

        PatternFindOptions.Builder builder = new PatternFindOptions.Builder()
            .setPauseBeforeBegin(ao.getPauseBeforeBegin())
            .setPauseAfterEnd(ao.getPauseAfterEnd())
            // Note: Predicate<Matches> to Predicate<ActionResult> conversion not implemented
            // This requires a separate converter utility that wraps the old predicate
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        // Set common find fields
        builder.setSimilarity(ao.getSimilarity())
            .setSearchRegions(ao.getSearchRegions())
            .setCaptureImage(ao.isCaptureImage())
            .setUseDefinedRegion(ao.isUseDefinedRegion())
            .setMaxMatchesToActOn(ao.getMaxMatchesToActOn());

        // Set pattern-specific fields
        builder.setStrategy(mapFindStrategy(ao.getFind()))
            .setDoOnEach(mapDoOnEach(ao.getDoOnEach()));
        
        // Note: keepLargerMatches is now handled through ActionChainOptions with ChainingStrategy

        // Set match adjustment
        if (needsMatchAdjustment(ao)) {
            builder.setMatchAdjustment(createMatchAdjustmentOptions(ao));
        }

        // Set match fusion
        if (needsMatchFusion(ao)) {
            builder.setMatchFusion(createMatchFusionOptions(ao));
        }

        return builder.build();
    }

    // Removed convertToColorFindOptions - not used in current implementation
    // Color finds should use ColorFindOptions directly

    private ClickOptions convertToClickOptions(ActionOptions ao) {
        ClickOptions.Builder builder = new ClickOptions.Builder()
            .setPauseBeforeBegin(ao.getPauseBeforeBegin())
            .setPauseAfterEnd(ao.getPauseAfterEnd())
            // Note: Predicate<Matches> to Predicate<ActionResult> conversion not implemented
            // This requires a separate converter utility that wraps the old predicate
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        // Set mouse press options
        var pressOptionsBuilder = MousePressOptions.builder()
            .pauseBeforeMouseDown(ao.getPauseBeforeMouseDown())
            .pauseAfterMouseDown(ao.getPauseAfterMouseDown())
            .pauseBeforeMouseUp(ao.getPauseBeforeMouseUp())
            .pauseAfterMouseUp(ao.getPauseAfterMouseUp());
        
        // Convert click type to numberOfClicks and mouse button
        convertClickType(builder, pressOptionsBuilder, ao.getClickType());
        
        builder.setPressOptions(pressOptionsBuilder.build());

        // Note: CLICK_UNTIL is deprecated - use action chaining with VerificationOptions

        // Chain mouse move after action if needed
        if (ao.isMoveMouseAfterAction()) {
            builder.then(createMouseMoveAfterAction(ao));
        }

        return builder.build();
    }

    private TypeOptions convertToTypeOptions(ActionOptions ao) {
        TypeOptions.Builder builder = new TypeOptions.Builder()
            .setPauseBeforeBegin(ao.getPauseBeforeBegin())
            .setPauseAfterEnd(ao.getPauseAfterEnd())
            // Note: Predicate<Matches> to Predicate<ActionResult> conversion not implemented
            // This requires a separate converter utility that wraps the old predicate
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        builder.setTypeDelay(ao.getTypeDelay())
            .setModifiers(ao.getModifiers());

        return builder.build();
    }

    private DefineRegionOptions convertToDefineOptions(ActionOptions ao) {
        DefineRegionOptions.Builder builder = new DefineRegionOptions.Builder()
            .setPauseBeforeBegin(ao.getPauseBeforeBegin())
            .setPauseAfterEnd(ao.getPauseAfterEnd())
            // Note: Predicate<Matches> to Predicate<ActionResult> conversion not implemented
            // This requires a separate converter utility that wraps the old predicate
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        builder.setDefineAs(mapDefineAs(ao.getDefineAs()));
        
        // Set match adjustment if needed
        if (needsMatchAdjustment(ao)) {
            builder.setMatchAdjustment(createMatchAdjustmentOptions(ao));
        }

        return builder.build();
    }

    private HighlightOptions convertToHighlightOptions(ActionOptions ao) {
        HighlightOptions.Builder builder = new HighlightOptions.Builder()
            .setPauseBeforeBegin(ao.getPauseBeforeBegin())
            .setPauseAfterEnd(ao.getPauseAfterEnd())
            // Note: Predicate<Matches> to Predicate<ActionResult> conversion not implemented
            // This requires a separate converter utility that wraps the old predicate
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        builder.setHighlightAllAtOnce(ao.isHighlightAllAtOnce())
            .setHighlightSeconds(ao.getHighlightSeconds())
            .setHighlightColor(ao.getHighlightColor());

        return builder.build();
    }

    private MouseMoveOptions convertToMouseMoveOptions(ActionOptions ao) {
        MouseMoveOptions.Builder builder = new MouseMoveOptions.Builder()
            .setPauseBeforeBegin(ao.getPauseBeforeBegin())
            .setPauseAfterEnd(ao.getPauseAfterEnd())
            // Note: Predicate<Matches> to Predicate<ActionResult> conversion not implemented
            // This requires a separate converter utility that wraps the old predicate
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        builder.setMoveMouseDelay(ao.getMoveMouseDelay());

        return builder.build();
    }

    private VanishOptions convertToVanishOptions(ActionOptions ao) {
        VanishOptions.Builder builder = new VanishOptions.Builder()
            .setPauseBeforeBegin(ao.getPauseBeforeBegin())
            .setPauseAfterEnd(ao.getPauseAfterEnd())
            // Note: Predicate<Matches> to Predicate<ActionResult> conversion not implemented
            // This requires a separate converter utility that wraps the old predicate
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        builder.setTimeout(ao.getMaxWait());
        builder.setSimilarity(ao.getMinSimilarity());

        return builder.build();
    }

    // ClickUntilOptions removed - deprecated in favor of action chaining
    // Use ActionChainOptions with RepetitionOptions and VerificationOptions instead

    // Helper methods

    private boolean needsMatchAdjustment(ActionOptions ao) {
        return ao.getTargetPosition() != null || 
               ao.getTargetOffset() != null ||
               ao.getAddW() != 0 || 
               ao.getAddH() != 0 ||
               ao.getAbsoluteW() >= 0 || 
               ao.getAbsoluteH() >= 0 ||
               ao.getAddX() != 0 || 
               ao.getAddY() != 0;
    }

    private MatchAdjustmentOptions createMatchAdjustmentOptions(ActionOptions ao) {
        var builder = MatchAdjustmentOptions.builder();
        
        if (ao.getTargetPosition() != null) {
            builder.targetPosition(ao.getTargetPosition());
        }
        if (ao.getTargetOffset() != null) {
            builder.targetOffset(ao.getTargetOffset());
        }
        
        builder.addW(ao.getAddW())
            .addH(ao.getAddH())
            .absoluteW(ao.getAbsoluteW())
            .absoluteH(ao.getAbsoluteH())
            .addX(ao.getAddX())
            .addY(ao.getAddY());

        return builder.build();
    }

    private boolean needsMatchFusion(ActionOptions ao) {
        return ao.getFusionMethod() != ActionOptions.MatchFusionMethod.NONE;
    }

    private MatchFusionOptions createMatchFusionOptions(ActionOptions ao) {
        return MatchFusionOptions.builder()
            .fusionMethod(mapFusionMethod(ao.getFusionMethod()))
            .maxFusionDistanceX(ao.getMaxFusionDistanceX())
            .maxFusionDistanceY(ao.getMaxFusionDistanceY())
            .sceneToUseForCaptureAfterFusingMatches(ao.getSceneToUseForCaptureAfterFusingMatches())
            .build();
    }

    // Removed createVerificationOptions - ClickUntil is deprecated

    private MouseMoveOptions createMouseMoveAfterAction(ActionOptions ao) {
        MouseMoveOptions.Builder builder = new MouseMoveOptions.Builder();
        
        // Check if we have an offset or absolute location
        Location moveBy = ao.getMoveMouseAfterActionBy();
        Location moveTo = ao.getMoveMouseAfterActionTo();
        
        if (moveBy != null && moveBy.getX() >= 0) {
            // Use offset-based movement (this would need to be added to MouseMoveOptions)
            // For now, we'll just note this limitation
        } else if (moveTo != null && moveTo.getX() >= 0) {
            // Use absolute movement (this would need to be added to MouseMoveOptions)
            // For now, we'll just note this limitation
        }
        
        return builder.build();
    }

    // Mapping methods

    private PatternFindOptions.Strategy mapFindStrategy(ActionOptions.Find find) {
        switch (find) {
            case FIRST: return PatternFindOptions.Strategy.FIRST;
            case ALL: return PatternFindOptions.Strategy.ALL;
            case EACH: return PatternFindOptions.Strategy.EACH;
            case BEST: return PatternFindOptions.Strategy.BEST;
            default: return PatternFindOptions.Strategy.FIRST;
        }
    }

    private PatternFindOptions.DoOnEach mapDoOnEach(ActionOptions.DoOnEach doOnEach) {
        switch (doOnEach) {
            case FIRST: return PatternFindOptions.DoOnEach.FIRST;
            case BEST: return PatternFindOptions.DoOnEach.BEST;
            default: return PatternFindOptions.DoOnEach.FIRST;
        }
    }

    // Removed mapColorStrategy - not used after removing convertToColorFindOptions


    private MatchFusionOptions.FusionMethod mapFusionMethod(ActionOptions.MatchFusionMethod method) {
        switch (method) {
            case NONE: return MatchFusionOptions.FusionMethod.NONE;
            case ABSOLUTE: return MatchFusionOptions.FusionMethod.ABSOLUTE;
            case RELATIVE: return MatchFusionOptions.FusionMethod.RELATIVE;
            default: return MatchFusionOptions.FusionMethod.NONE;
        }
    }

    private DefineRegionOptions.DefineAs mapDefineAs(ActionOptions.DefineAs defineAs) {
        switch (defineAs) {
            case MATCH: return DefineRegionOptions.DefineAs.MATCH;
            case INSIDE_ANCHORS: return DefineRegionOptions.DefineAs.INSIDE_ANCHORS;
            case OUTSIDE_ANCHORS: return DefineRegionOptions.DefineAs.OUTSIDE_ANCHORS;
            // Note: MATCH_COLLECT and UNIVERSAL are not available in DefineRegionOptions
            // These options may need to be handled differently in the new architecture
            default: return DefineRegionOptions.DefineAs.MATCH;
        }
    }

    private ActionConfig.Illustrate mapIllustrate(ActionOptions.Illustrate illustrate) {
        switch (illustrate) {
            case YES: return ActionConfig.Illustrate.YES;
            case NO: return ActionConfig.Illustrate.NO;
            case MAYBE: return ActionConfig.Illustrate.USE_GLOBAL;
            default: return ActionConfig.Illustrate.USE_GLOBAL;
        }
    }
    
    // Removed mapClickUntilCondition - ClickUntil is deprecated
    
    private void convertClickType(ClickOptions.Builder builder, MousePressOptions.MousePressOptionsBuilder pressOptions, ClickOptions.Type clickType) {
        if (clickType == null) {
            return; // Use default (LEFT click)
        }
        
        switch (clickType) {
            case DOUBLE_LEFT:
                builder.setNumberOfClicks(2);
                pressOptions.button(io.github.jspinak.brobot.model.action.MouseButton.LEFT);
                break;
            case DOUBLE_RIGHT:
                builder.setNumberOfClicks(2);
                pressOptions.button(io.github.jspinak.brobot.model.action.MouseButton.RIGHT);
                break;
            case DOUBLE_MIDDLE:
                builder.setNumberOfClicks(2);
                pressOptions.button(io.github.jspinak.brobot.model.action.MouseButton.MIDDLE);
                break;
            case RIGHT:
                builder.setNumberOfClicks(1);
                pressOptions.button(io.github.jspinak.brobot.model.action.MouseButton.RIGHT);
                break;
            case MIDDLE:
                builder.setNumberOfClicks(1);
                pressOptions.button(io.github.jspinak.brobot.model.action.MouseButton.MIDDLE);
                break;
            case LEFT:
            default:
                builder.setNumberOfClicks(1);
                pressOptions.button(io.github.jspinak.brobot.model.action.MouseButton.LEFT);
                break;
        }
    }
}