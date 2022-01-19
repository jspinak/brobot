package actions.actionOptions;

import com.brobot.multimodule.database.primitives.location.Location;

/**
 * This class creates a deep copy of most of the fields in ActionOptions.
 * Fields that are not copied will have the same values as a new ActionOptions variable.
 * Basic enums are immutable, so they can be part of a deep copy.
 */
public class CopyActionOptions {

    // define a deep copy method for the rest of the options and include here
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
                .setMoveMouseAfterClick(orig.isMoveMouseAfterClick())
                .setLocationAfterClick(new Location(
                        orig.getLocationAfterClick().getX(), orig.getLocationAfterClick().getY()))
                .setLocationAfterClickByOffset(
                        orig.getLocationAfterClick().getX(), orig.getLocationAfterClick().getY())
                .setSearchRegions(orig.getSearchRegions().getDeepCopy())
                .setPauseBeforeBegin(orig.getPauseBeforeBegin())
                .setPauseAfterEnd(orig.getPauseAfterEnd())
                .setMaxWait(orig.getMaxWait())
                .setTimesToRepeatIndividualAction(orig.getTimesToRepeatIndividualAction())
                .setMaxTimesToRepeatActionSequence(orig.getMaxTimesToRepeatActionSequence())
                .setPauseBetweenActions(orig.getPauseBetweenIndividualActions())
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
