package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.action.basic.find.MatchFusionOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.composite.repeat.ClickUntilOptions;
import io.github.jspinak.brobot.model.element.Location;
import org.springframework.stereotype.Component;

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
                return convertToClickUntilOptions(actionOptions);
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
            // TODO: Convert Predicate<Matches> to Predicate<ActionResult>
            // .setSuccessCriteria(ao.getSuccessCriteria())
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
        
        // Note: keepLargerMatches is now handled through ActionChainOptions with ChainingStrategy.CONFIRM

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

    private ColorFindOptions convertToColorFindOptions(ActionOptions ao) {
        ColorFindOptions.Builder builder = new ColorFindOptions.Builder()
            .setPauseBeforeBegin(ao.getPauseBeforeBegin())
            .setPauseAfterEnd(ao.getPauseAfterEnd())
            // TODO: Convert Predicate<Matches> to Predicate<ActionResult>
            // .setSuccessCriteria(ao.getSuccessCriteria())
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        // Set common find fields
        builder.setSimilarity(ao.getMinSimilarity()) // Use minSimilarity for color similarity
            .setSearchRegions(ao.getSearchRegions())
            .setCaptureImage(ao.isCaptureImage())
            .setUseDefinedRegion(ao.isUseDefinedRegion())
            .setMaxMatchesToActOn(ao.getMaxMatchesToActOn());

        // Set color-specific fields
        builder.setColorStrategy(mapColorStrategy(ao.getColor()))
            .setDiameter(ao.getDiameter())
            .setKmeans(ao.getKmeans());

        // Set match adjustment
        if (needsMatchAdjustment(ao)) {
            builder.setMatchAdjustment(createMatchAdjustmentOptions(ao));
        }

        return builder.build();
    }

    private ClickOptions convertToClickOptions(ActionOptions ao) {
        ClickOptions.Builder builder = new ClickOptions.Builder()
            .setPauseBeforeBegin(ao.getPauseBeforeBegin())
            .setPauseAfterEnd(ao.getPauseAfterEnd())
            // TODO: Convert Predicate<Matches> to Predicate<ActionResult>
            // .setSuccessCriteria(ao.getSuccessCriteria())
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        // Set click type - ActionOptions already uses ClickOptions.Type
        builder.setClickType(ao.getClickType());

        // Set mouse press options
        MousePressOptions.Builder pressOptions = new MousePressOptions.Builder()
            .setPauseBeforeMouseDown(ao.getPauseBeforeMouseDown())
            .setPauseAfterMouseDown(ao.getPauseAfterMouseDown())
            .setPauseBeforeMouseUp(ao.getPauseBeforeMouseUp())
            .setPauseAfterMouseUp(ao.getPauseAfterMouseUp());
        builder.setPressOptions(pressOptions);

        // Set verification options if this is a click until action
        if (ao.getAction() == ActionOptions.Action.CLICK_UNTIL) {
            builder.setVerification(createVerificationOptions(ao));
        }

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
            // TODO: Convert Predicate<Matches> to Predicate<ActionResult>
            // .setSuccessCriteria(ao.getSuccessCriteria())
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        builder.setTypeDelay(ao.getTypeDelay())
            .setModifiers(ao.getModifiers());

        return builder.build();
    }

    private DefineRegionOptions convertToDefineOptions(ActionOptions ao) {
        DefineRegionOptions.Builder builder = new DefineRegionOptions.Builder()
            .setPauseBeforeBegin(ao.getPauseBeforeBegin())
            .setPauseAfterEnd(ao.getPauseAfterEnd())
            // TODO: Convert Predicate<Matches> to Predicate<ActionResult>
            // .setSuccessCriteria(ao.getSuccessCriteria())
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
            // TODO: Convert Predicate<Matches> to Predicate<ActionResult>
            // .setSuccessCriteria(ao.getSuccessCriteria())
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
            // TODO: Convert Predicate<Matches> to Predicate<ActionResult>
            // .setSuccessCriteria(ao.getSuccessCriteria())
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        builder.setMoveMouseDelay(ao.getMoveMouseDelay());

        return builder.build();
    }

    private VanishOptions convertToVanishOptions(ActionOptions ao) {
        VanishOptions.Builder builder = new VanishOptions.Builder()
            .setPauseBeforeBegin(ao.getPauseBeforeBegin())
            .setPauseAfterEnd(ao.getPauseAfterEnd())
            // TODO: Convert Predicate<Matches> to Predicate<ActionResult>
            // .setSuccessCriteria(ao.getSuccessCriteria())
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        builder.setTimeout(ao.getMaxWait());
        builder.setSimilarity(ao.getMinSimilarity());

        return builder.build();
    }

    private ClickUntilOptions convertToClickUntilOptions(ActionOptions ao) {
        // TODO: Consider migrating to RepeatUntilConfig which provides more flexibility
        // RepeatUntilConfig allows different action types and separate configuration
        // for the repeated action and termination condition
        ClickUntilOptions.Builder builder = new ClickUntilOptions.Builder()
            .setPauseBeforeBegin(ao.getPauseBeforeBegin())
            .setPauseAfterEnd(ao.getPauseAfterEnd())
            // TODO: Convert Predicate<Matches> to Predicate<ActionResult>
            // .setSuccessCriteria(ao.getSuccessCriteria())
            .setIllustrate(mapIllustrate(ao.getIllustrate()));

        // Set condition
        builder.setCondition(mapClickUntilCondition(ao.getClickUntil()));
        
        // TODO: ClickUntilOptions doesn't include click configuration or repetition details
        // The actual click behavior would need to be handled separately

        return builder.build();
    }

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

    private MatchAdjustmentOptions.Builder createMatchAdjustmentOptions(ActionOptions ao) {
        MatchAdjustmentOptions.Builder builder = new MatchAdjustmentOptions.Builder();
        
        if (ao.getTargetPosition() != null) {
            builder.setTargetPosition(ao.getTargetPosition());
        }
        if (ao.getTargetOffset() != null) {
            builder.setTargetOffset(ao.getTargetOffset());
        }
        
        builder.setAddW(ao.getAddW())
            .setAddH(ao.getAddH())
            .setAbsoluteW(ao.getAbsoluteW())
            .setAbsoluteH(ao.getAbsoluteH())
            .setAddX(ao.getAddX())
            .setAddY(ao.getAddY());

        return builder;
    }

    private boolean needsMatchFusion(ActionOptions ao) {
        return ao.getFusionMethod() != ActionOptions.MatchFusionMethod.NONE;
    }

    private MatchFusionOptions.Builder createMatchFusionOptions(ActionOptions ao) {
        return new MatchFusionOptions.Builder()
            .setFusionMethod(mapFusionMethod(ao.getFusionMethod()))
            .setMaxFusionDistanceX(ao.getMaxFusionDistanceX())
            .setMaxFusionDistanceY(ao.getMaxFusionDistanceY())
            .setSceneToUseForCaptureAfterFusingMatches(ao.getSceneToUseForCaptureAfterFusingMatches());
    }

    private VerificationOptions.Builder createVerificationOptions(ActionOptions ao) {
        VerificationOptions.Builder builder = new VerificationOptions.Builder();
        
        switch (ao.getClickUntil()) {
            case OBJECTS_APPEAR:
                builder.setEvent(VerificationOptions.Event.OBJECTS_APPEAR);
                break;
            case OBJECTS_VANISH:
                builder.setEvent(VerificationOptions.Event.OBJECTS_VANISH);
                break;
            default:
                builder.setEvent(VerificationOptions.Event.NONE);
        }
        
        return builder;
    }

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

    private ColorFindOptions.Color mapColorStrategy(ActionOptions.Color color) {
        switch (color) {
            case KMEANS: return ColorFindOptions.Color.KMEANS;
            case MU: return ColorFindOptions.Color.MU;
            case CLASSIFICATION: return ColorFindOptions.Color.CLASSIFICATION;
            default: return ColorFindOptions.Color.MU;
        }
    }


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
            // TODO: MATCH_COLLECT and UNIVERSAL not available in DefineRegionOptions
            // case MATCH_COLLECT: return DefineRegionOptions.DefineAs.MATCH_COLLECT;
            // case UNIVERSAL: return DefineRegionOptions.DefineAs.UNIVERSAL;
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
    
    private ClickUntilOptions.Condition mapClickUntilCondition(ActionOptions.ClickUntil clickUntil) {
        switch (clickUntil) {
            case OBJECTS_APPEAR: return ClickUntilOptions.Condition.OBJECTS_APPEAR;
            case OBJECTS_VANISH: return ClickUntilOptions.Condition.OBJECTS_VANISH;
            default: return ClickUntilOptions.Condition.OBJECTS_APPEAR;
        }
    }
}