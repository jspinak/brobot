package io.github.jspinak.brobot.actions.customActions.select;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CommonSelect {

    private Select select;

    public CommonSelect(Select select) {
        this.select = select;
    }

    public SelectActionObject select(Region swipeRegion, ActionOptions.Find findType, int clicksPerImage,
                                     Position swipeDirection, int maxSwipes, StateImage... images) {
        return select(List.of(images), new ArrayList<>(), swipeRegion, findType, clicksPerImage,
                swipeDirection, maxSwipes);
    }

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
