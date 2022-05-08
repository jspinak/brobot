package io.github.jspinak.brobot.actions.customActions.select;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.Getter;

@Getter
public class SelectActionObject {

    //build
    private int maxSwipes = 10;
    private ObjectCollection swipeFromObjColl;
    private ObjectCollection swipeToObjColl;
    private ActionOptions swipeActionOptions;
    private ObjectCollection findObjectCollection;
    private ActionOptions findActionOptions;
    private ActionOptions clickActionOptions;
    private ObjectCollection clickMatches; // defined after find operation
    private ObjectCollection confirmationObjectCollection; //when null don't confirm
    private ActionOptions confirmActionOptions;

    //results
    private int totalSwipes = 0;
    private Matches foundMatches = new Matches();
    private Matches foundConfirmations = new Matches();
    private boolean success = false;

    //setters
    public void resetTotalSwipes() {
        totalSwipes = 0;
    }
    public void addSwipe() {
        totalSwipes++;
    }
    public void setFoundMatches(Matches matches) {
        foundMatches = matches;
    }
    public void setFoundConfirmations(Matches matches) {
        foundConfirmations = matches;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static class Builder {
        private int maxSwipes = 10;
        private ObjectCollection swipeFromObjColl;
        private ObjectCollection swipeToObjColl;
        private ActionOptions swipeActionOptions;
        private ObjectCollection findObjectCollection;
        private ActionOptions findActionOptions;
        private ActionOptions clickActionOptions;
        private ObjectCollection confirmationObjectCollection; //when null don't confirm
        private ActionOptions confirmActionOptions;

        public Builder() {}

        public Builder setMaxSwipes(int maxSwipes) {
            this.maxSwipes = maxSwipes;
            return this;
        }

        public Builder setSwipeFromObjColl(ObjectCollection swipeFromObjColl) {
            this.swipeFromObjColl = swipeFromObjColl;
            return this;
        }

        public Builder setSwipeToObjColl(ObjectCollection swipeToObjColl) {
            this.swipeToObjColl = swipeToObjColl;
            return this;
        }

        public Builder setSwipeActionOptions(ActionOptions swipeActionOptions) {
            this.swipeActionOptions = swipeActionOptions;
            return this;
        }

        public Builder setFindObjectCollection(ObjectCollection findObjectCollection) {
            this.findObjectCollection = findObjectCollection;
            return this;
        }

        public Builder setFindActionOptions(ActionOptions findActionOptions) {
            this.findActionOptions = findActionOptions;
            return this;
        }

        public Builder setClickActionOptions(ActionOptions clickActionOptions) {
            this.clickActionOptions = clickActionOptions;
            return this;
        }

        public Builder setConfirmationObjectCollection(ObjectCollection confirmationObjectCollection) {
            this.confirmationObjectCollection = confirmationObjectCollection;
            return this;
        }

        public Builder setConfirmActionOptions(ActionOptions confirmActionOptions) {
            this.confirmActionOptions = confirmActionOptions;
            return this;
        }

        public SelectActionObject build() {
            SelectActionObject selectActionObject = new SelectActionObject();
            selectActionObject.maxSwipes = maxSwipes;
            selectActionObject.swipeFromObjColl = swipeFromObjColl;
            selectActionObject.swipeToObjColl = swipeToObjColl;
            selectActionObject.swipeActionOptions = swipeActionOptions;
            selectActionObject.findObjectCollection = findObjectCollection;
            selectActionObject.findActionOptions = findActionOptions;
            selectActionObject.clickActionOptions = clickActionOptions;
            selectActionObject.confirmationObjectCollection = confirmationObjectCollection;
            selectActionObject.confirmActionOptions = confirmActionOptions;
            return selectActionObject;
        }
    }
}
