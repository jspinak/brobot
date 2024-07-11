package io.github.jspinak.brobot.actions.customActions;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.ClickType;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.manageStates.UnknownState;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.*;

/**
 * It is useful to create one or more CommonActions classes for your applications
 * in order to reduce code redundancy and make your code more readable.
 *
 * This class contains examples of potential methods for a CommonActions class.
 */
@Component
public class CommonActions {

    private final Action action;
    private final AllStatesInProjectService allStatesInProjectService;

    public CommonActions(Action action, AllStatesInProjectService allStatesInProjectService) {
        this.action = action;
        this.allStatesInProjectService = allStatesInProjectService;
    }

    public boolean click(double maxWait, StateImage... stateImages) {
        return action.perform(
                new ActionOptions.Builder()
                        .setAction(ActionOptions.Action.CLICK)
                        .setMaxWait(maxWait)
                        .build(),
                stateImages).isSuccess();
    }

    public boolean click(Location location) {
        return action.perform(CLICK, new ObjectCollection.Builder().withLocations(location).build())
                .isSuccess();
    }

    public boolean clickImageUntilItVanishes(int timesToClick, double pauseBetweenClicks, StateImage image) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(CLICK_UNTIL)
                .setClickUntil(ActionOptions.ClickUntil.OBJECTS_VANISH)
                .setPauseBetweenIndividualActions(pauseBetweenClicks)
                .setPauseBetweenActionSequences(pauseBetweenClicks)
                .setMaxTimesToRepeatActionSequence(timesToClick)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(image)
                .build();
        return action.perform(actionOptions, objectCollection).isSuccess();
    }

    public boolean rightClickImageUntilItVanishes(int timesToClick, double pauseBetweenClicks,
                                                  StateImage image, int xMove, int yMove) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(CLICK_UNTIL)
                .setClickType(ClickType.Type.RIGHT)
                .setClickUntil(ActionOptions.ClickUntil.OBJECTS_VANISH)
                .setPauseBetweenIndividualActions(pauseBetweenClicks)
                .setPauseBetweenActionSequences(pauseBetweenClicks)
                .setMaxTimesToRepeatActionSequence(timesToClick)
                .setMoveMouseAfterAction(true)
                .setAddX(xMove)
                .setAddY(yMove)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(image)
                .build();
        return action.perform(actionOptions, objectCollection).isSuccess();
    }

    public boolean doubleClick(double maxWait, StateImage... stateImages) {
        return action.perform(
                new ActionOptions.Builder()
                        .setAction(ActionOptions.Action.CLICK)
                        .setMaxWait(maxWait)
                        .setClickType(ClickType.Type.DOUBLE_LEFT)
                        .build(),
                stateImages).isSuccess();
    }

    public boolean rightClick(double maxWait, StateImage... stateImages) {
        return action.perform(
                new ActionOptions.Builder()
                        .setAction(ActionOptions.Action.CLICK)
                        .setMaxWait(maxWait)
                        .setClickType(ClickType.Type.RIGHT)
                        .build(),
                stateImages).isSuccess();
    }

    public boolean waitVanish(double wait, StateImage... images) {
        ActionOptions vanish = new ActionOptions.Builder()
                .setAction(VANISH)
                .setMaxWait(wait)
                .build();
        return action.perform(vanish, images).isSuccess();
    }

    public boolean find(double maxWait, Pattern image) {
        return find(maxWait, image.inNullState());
    }

    public boolean find(double maxWait, StateImage stateImage) {
        return action.perform(new ActionOptions.Builder().setMaxWait(maxWait).build(), stateImage)
                .isSuccess();
    }

    public boolean waitState(double maxWait, String stateName, ActionOptions.Action actionType) {
        if (Objects.equals(stateName, UnknownState.Enum.UNKNOWN.toString())) return true;
        Optional<State> state = allStatesInProjectService.getState(stateName);
        if (state.isEmpty()) return false;
        return action.perform(
                new ActionOptions.Builder()
                        .setAction(actionType)
                        .setMaxWait(maxWait)
                        .build(),
                new ObjectCollection.Builder()
                        .withAllStateImages(state.get())
                        .build())
                .isSuccess();
    }

    public boolean findState(double maxWait, String stateName) {
        System.out.print("\n__findState:"+stateName+"__ ");
        allStatesInProjectService.getState(stateName).ifPresent(
                state -> System.out.println("prob="+state.getProbabilityExists()));
        return waitState(maxWait, stateName, FIND);
    }

    public boolean waitVanishState(double maxWait, String stateName) {
        System.out.print("\n__waitVanishState:"+stateName+"__ ");
        return waitState(maxWait, stateName, VANISH);
    }

    public void highlightRegion(double seconds, StateRegion region) {
        highlightRegion(seconds, region.getSearchRegion());
    }
    public void highlightRegion(double seconds, Region region) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.HIGHLIGHT)
                .setHighlightSeconds(seconds)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withRegions(region)
                .build();
        action.perform(actionOptions, objectCollection);
    }

    public boolean clickUntilStateAppears(int repeatClickTimes, double pauseBetweenClicks,
                                          StateImage objectToClick, String state) {
        if (allStatesInProjectService.getState(state).isEmpty()) return false;
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setClickUntil(ActionOptions.ClickUntil.OBJECTS_APPEAR)
                .setMaxTimesToRepeatActionSequence(repeatClickTimes)
                .setPauseBetweenIndividualActions(pauseBetweenClicks)
                .build();
        ObjectCollection objectsToClick = new ObjectCollection.Builder()
                .withImages(objectToClick)
                .build();
        ObjectCollection objectsToAppear = new ObjectCollection.Builder()
                .withAllStateImages(allStatesInProjectService.getState(state).get())
                .build();
        return action.perform(actionOptions, objectsToClick, objectsToAppear).isSuccess();
    }

    public boolean clickUntilImageAppears(int repeatClickTimes, double pauseBetweenClicks,
                                          StateImage toClick, StateImage toAppear) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setClickUntil(ActionOptions.ClickUntil.OBJECTS_APPEAR)
                .setMaxTimesToRepeatActionSequence(repeatClickTimes)
                .setPauseBetweenIndividualActions(pauseBetweenClicks)
                .build();
        ObjectCollection objectsToClick = new ObjectCollection.Builder()
                .withImages(toClick)
                .build();
        ObjectCollection objectsToAppear = new ObjectCollection.Builder()
                .withImages(toAppear)
                .build();
        return action.perform(actionOptions, objectsToClick, objectsToAppear).isSuccess();
    }

    public boolean drag(Location from, Location to) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .build();
        ObjectCollection fromOC = new ObjectCollection.Builder()
                .withLocations(from)
                .build();
        ObjectCollection toOC = new ObjectCollection.Builder()
                .withLocations(to)
                .build();
        return action.perform(actionOptions, fromOC, toOC).isSuccess();
    }

    public void dragCenterToOffset(Region region, int plusX, int plusY) {
        dragCenterOfRegion(region, region.sikuli().getCenter().x + plusX, region.sikuli().getCenter().y + plusY);
    }

    public void dragCenterOfRegion(Region region, int toX, int toY) {
        ActionOptions drag = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .build();
        ObjectCollection from = new ObjectCollection.Builder()
                .withRegions(region)
                .build();
        ObjectCollection to = new ObjectCollection.Builder()
                .withLocations(new Location(region.sikuli().getCenter(), toX, toY))
                .build();
        action.perform(drag, from, to);
    }

    public void dragCenterToOffsetStop(Region region, int plusX, int plusY) {
        dragCenterStop(region,region.sikuli().getCenter().x + plusX, region.sikuli().getCenter().y + plusY);
    }

    public void dragCenterStop(Region region, Location location) {
        dragCenterStop(region, location.getX(), location.getY());
    }

    public void dragCenterStop(Region region, int toX, int toY) {
        ActionOptions drag = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .setPauseBeforeMouseUp(.8)
                .build();
        ObjectCollection from = new ObjectCollection.Builder()
                .withRegions(region)
                .build();
        ObjectCollection to = new ObjectCollection.Builder()
                .withLocations(new Location(region.sikuli().getCenter(), toX, toY))
                .build();
        action.perform(drag, from, to);
    }

    public String getText(Region region) {
        return getText(region.inNullState());
    }

    public String getText(StateRegion region) {
        ActionOptions getText = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .getTextUntil(ActionOptions.GetTextUntil.TEXT_APPEARS)
                .setMaxTimesToRepeatActionSequence(3)
                .setPauseBetweenActionSequences(.5)
                .build();
        ObjectCollection textRegion = new ObjectCollection.Builder()
                .withRegions(region)
                .build();
        return action.perform(getText, textRegion).getSelectedText();
    }

    public boolean clickXTimes(int times, double pause, StateImage objectToClick) {
        ActionOptions click = new ActionOptions.Builder()
                .setAction(CLICK)
                .setTimesToRepeatIndividualAction(times)
                .setPauseBetweenIndividualActions(pause)
                .build();
        return action.perform(click, objectToClick).isSuccess();
    }

    public boolean clickXTimes(int times, double pause, Region reg) {
        ActionOptions click = new ActionOptions.Builder()
                .setAction(CLICK)
                .setTimesToRepeatIndividualAction(times)
                .setPauseBetweenIndividualActions(pause)
                .build();
        ObjectCollection regs = new ObjectCollection.Builder()
                .withRegions(reg)
                .build();
        return action.perform(click, regs).isSuccess();
    }

    public boolean moveMouseTo(Location location) {
        ActionOptions move = new ActionOptions.Builder()
                .setAction(MOVE)
                .build();
        ObjectCollection loc = new ObjectCollection.Builder()
                .withLocations(location)
                .build();
        return action.perform(move, loc).isSuccess();
    }

    public boolean swipeToOppositePosition(Region region, Position grabPosition) {
        Location grabLocation = new Location(region, grabPosition);
        ActionOptions drag = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .setPauseAfterEnd(.5)
                .build();
        ObjectCollection from = new ObjectCollection.Builder()
                .withLocations(grabLocation)
                .build();
        ObjectCollection to = new ObjectCollection.Builder()
                .withLocations(grabLocation.getOpposite())
                .build();
        return action.perform(drag, from, to).isSuccess();
    }

    public void type(String str) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(TYPE)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withStrings(str)
                .build();
        action.perform(actionOptions, objectCollection);
    }

    public void type(String str, String modifier) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(TYPE)
                .setModifiers(modifier)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withStrings(str)
                .build();
        action.perform(actionOptions, objectCollection);
    }

    public void holdKey(String str, double seconds) {
        ActionOptions holdKey = new ActionOptions.Builder()
                .setAction(KEY_DOWN)
                .setPauseAfterEnd(seconds)
                .build();
        ObjectCollection key = new ObjectCollection.Builder()
                .withStrings(str)
                .build();
        action.perform(holdKey, key);
        releaseKeys();
    }

    public void releaseKeys() {
        ActionOptions releaseKey = new ActionOptions.Builder()
                .setAction(KEY_UP)
                .build();
        ObjectCollection allKeys = new ObjectCollection.Builder()
                .build();
        action.perform(releaseKey, allKeys);
    }

}
