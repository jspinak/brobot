package io.github.jspinak.brobot.action.composite.select;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides simplified helper methods for performing Select operations.
 * <p>
 * This class offers convenience methods that simplify the creation and execution of
 * {@link SelectActionObject} instances. It wraps the more complex setup required by
 * the {@link Select} class, making it easier to perform common selection operations
 * that involve swiping through a region until finding and clicking on target images.
 *
 * @see Select
 * @see SelectActionObject
 */
@Component
public class CommonSelect {

    private Select select;

    public CommonSelect(Select select) {
        this.select = select;
    }

    /**
     * Performs a selection operation by swiping through a region until target images are found.
     * <p>
     * This convenience method creates a {@link SelectActionObject} with the provided parameters
     * and executes the selection operation. The operation will swipe in the specified direction
     * up to maxSwipes times, looking for the target images. When found, it will click on them
     * the specified number of times.
     *
     * @param swipeRegion The region where swiping will occur
     * @param findType The type of find operation to use (e.g., FIRST, ALL, EACH)
     * @param clicksPerImage Number of clicks to perform on each found image
     * @param swipeDirection The direction to swipe (e.g., TOP, BOTTOM, LEFT, RIGHT)
     * @param maxSwipes Maximum number of swipes to perform before giving up
     * @param images The target images to search for during swiping
     * @return The {@link SelectActionObject} containing the operation results and state
     */
    public SelectActionObject select(Region swipeRegion, ActionOptions.Find findType, int clicksPerImage,
                                     Position swipeDirection, int maxSwipes, StateImage... images) {
        return select(List.of(images), new ArrayList<>(), swipeRegion, findType, clicksPerImage,
                swipeDirection, maxSwipes);
    }

    /**
     * Performs a selection operation with confirmation verification.
     * <p>
     * This method performs a selection operation similar to the simpler select method, but adds
     * an additional verification step. After clicking on found images, it will search for
     * confirmation images to verify that the action had the desired effect. The operation
     * constructs all necessary {@link ActionOptions} and {@link ObjectCollection} instances
     * internally.
     * <p>
     * The method sets up:
     * <ul>
     * <li>Swipe locations based on the region and direction</li>
     * <li>Drag action with a 0.5 second pause before mouse up</li>
     * <li>Find action with the specified find type</li>
     * <li>Click action with the specified number of clicks and 0.7 second pause between clicks</li>
     * <li>Confirmation find action (if confirmation images are provided)</li>
     * </ul>
     *
     * @param images The target images to search for during swiping
     * @param confirmationImages Images to find after clicking to confirm success (can be empty)
     * @param swipeRegion The region where swiping will occur
     * @param findType The type of find operation to use (e.g., FIRST, ALL, EACH)
     * @param clicksPerImage Number of clicks to perform on each found image
     * @param swipeDirection The direction to swipe (e.g., TOP, BOTTOM, LEFT, RIGHT)
     * @param maxSwipes Maximum number of swipes to perform before giving up
     * @return The {@link SelectActionObject} containing the operation results, including
     *         success status and total swipes performed
     */
    public SelectActionObject select(List<StateImage> images, List<StateImage> confirmationImages,
                                     Region swipeRegion, ActionOptions.Find findType, int clicksPerImage,
                                     Position swipeDirection, int maxSwipes) {
        Location swipeTo = new Location(swipeRegion, swipeDirection);
        Location swipeFrom = swipeTo.getOpposite();
        ObjectCollection swipeFromObjColl = new ObjectCollection.Builder()
                .withLocations(swipeFrom)
                .build();
        ObjectCollection swipeToObjColl = new ObjectCollection.Builder()
                .withLocations(swipeTo)
                .build();
        ActionOptions swipeActionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .setPauseBeforeMouseUp(.5)
                .build();
        ObjectCollection findObjectCollection = new ObjectCollection.Builder()
                .withImages(images)
                .build();
        ActionOptions findActionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(findType)
                .build();
        ActionOptions clickActionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setTimesToRepeatIndividualAction(clicksPerImage)
                .setPauseBetweenIndividualActions(.7)
                .build();
        ObjectCollection confirmationObjectCollection = new ObjectCollection.Builder()
                .withImages(confirmationImages)
                .build();
        ActionOptions confirmActionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        SelectActionObject selectActionObject = new SelectActionObject.Builder()
                .setSwipeFromObjColl(swipeFromObjColl)
                .setSwipeToObjColl(swipeToObjColl)
                .setSwipeActionOptions(swipeActionOptions)
                .setFindObjectCollection(findObjectCollection)
                .setFindActionOptions(findActionOptions)
                .setClickActionOptions(clickActionOptions)
                .setConfirmationObjectCollection(confirmationObjectCollection)
                .setConfirmActionOptions(confirmActionOptions)
                .setMaxSwipes(maxSwipes)
                .build();
        select.select(selectActionObject);
        return selectActionObject;
    }
}
