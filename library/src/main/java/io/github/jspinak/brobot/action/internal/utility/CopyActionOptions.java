package io.github.jspinak.brobot.action.internal.utility;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.model.element.Location;

/**
 * Utility class for creating deep copies of ActionOptions configurations.
 * <p>
 * CopyActionOptions provides methods to duplicate ActionOptions instances while
 * ensuring proper copying of nested objects. This is essential for scenarios where
 * action configurations need to be modified without affecting the original, such as:
 * <ul>
 * <li>Creating variations of a base configuration</li>
 * <li>Preserving original settings for rollback</li>
 * <li>Building action templates that can be customized per use</li>
 * </ul>
 * <p>
 * <strong>Implementation notes:</strong>
 * <ul>
 * <li>Primitive types and enums are immutable and safe to copy directly</li>
 * <li>Complex objects like Location and SearchRegions require deep copying</li>
 * <li>Some fields may not be copied if they contain non-serializable elements</li>
 * </ul>
 * <p>
 * The deep copy ensures that modifications to the copy don't affect the original
 * ActionOptions, maintaining isolation between different action executions.
 *
 * @see ActionOptions
 * @see ActionOptions.Builder
 */
public class CopyActionOptions {

    /**
     * Creates a deep copy of an ActionOptions instance.
     * <p>
     * Copies all copyable fields from the original ActionOptions, creating new
     * instances for mutable objects like Location and SearchRegions. Fields that
     * cannot be safely copied (like function references) will retain default values.
     * <p>
     * <strong>Copied fields include:</strong>
     * <ul>
     * <li>All enum values (Action, Find, ScrollDirection, etc.)</li>
     * <li>All primitive values (doubles, ints, booleans)</li>
     * <li>Deep copies of Location objects</li>
     * <li>Deep copy of SearchRegions</li>
     * <li>String values</li>
     * </ul>
     * <p>
     * <strong>Not copied:</strong>
     * <ul>
     * <li>tempFind (function reference)</li>
     * <li>successCriteria (function reference)</li>
     * <li>Custom find actions beyond the primary find</li>
     * </ul>
     *
     * @param orig The ActionOptions instance to copy
     * @return A new ActionOptions instance with copied values
     * @throws NullPointerException if orig is null
     */
    public static ActionOptions copyImmutableOptions(ActionOptions orig) {
        return new ActionOptions.Builder()
                // immutable options
                .setAction(orig.getAction())
                .setFind(orig.getFind())
                .setMinSimilarity(orig.getSimilarity())
                .setScrollDirection(orig.getScrollDirection())
                .setClickUntil(orig.getClickUntil())
                .setGetTextUntil(orig.getGetTextUntil())
                .setPauseBeforeMouseDown(orig.getPauseBeforeMouseDown())
                .setPauseAfterMouseDown(orig.getPauseAfterMouseDown())
                .setMoveMouseDelay(orig.getMoveMouseDelay())
                .setPauseBeforeMouseUp(orig.getPauseBeforeMouseUp())
                .setPauseAfterMouseUp(orig.getPauseAfterMouseUp())
                .setDragToOffsetX(orig.getDragToOffsetX())
                .setDragToOffsetY(orig.getDragToOffsetY())
                .setClickType(orig.getClickType())
                .setMoveMouseAfterAction(orig.isMoveMouseAfterAction())
                .setMoveMouseAfterActionTo(new Location(
                        orig.getMoveMouseAfterActionTo().getCalculatedX(), orig.getMoveMouseAfterActionTo().getCalculatedY()))
                .setMoveMouseAfterActionBy(
                        orig.getMoveMouseAfterActionTo().getCalculatedX(), orig.getMoveMouseAfterActionTo().getCalculatedY())
                .setSearchRegions(orig.getSearchRegions().getDeepCopy())
                .setPauseBeforeBegin(orig.getPauseBeforeBegin())
                .setPauseAfterEnd(orig.getPauseAfterEnd())
                .setMaxWait(orig.getMaxWait())
                .setTimesToRepeatIndividualAction(orig.getTimesToRepeatIndividualAction())
                .setMaxTimesToRepeatActionSequence(orig.getMaxTimesToRepeatActionSequence())
                .setPauseBetweenIndividualActions(orig.getPauseBetweenIndividualActions())
                .setPauseBetweenActionSequences(orig.getPauseBetweenActionSequences())
                .setMaxMatchesToActOn(orig.getMaxMatchesToActOn())
                .setDefineAs(orig.getDefineAs())
                .setAddW(orig.getAddW())
                .setAddH(orig.getAddH())
                .setAbsoluteWidth(orig.getAbsoluteW())
                .setAbsoluteHeight(orig.getAbsoluteH())
                .setAddX(orig.getAddX())
                .setAddY(orig.getAddY())
                .setHighlightAllAtOnce(orig.isHighlightAllAtOnce())
                .setHighlightSeconds(orig.getHighlightSeconds())
                .setHighlightColor(orig.getHighlightColor())
                .setTypeDelay(orig.getTypeDelay())
                .setModifiers(orig.getModifiers())
                .build();
    }
}
