package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.ANSI;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Builds a State structure by comparing the images found per screenshot with the existing
 *   State structure at that point.
 *
 * Not currently used. This produces a lot of States with one Image due to some Images being
 *   found in foreign States, some Images not always found in their own State, and images that
 *   are shared among States (such as the 'X' or 'close' button). I developed
 *   the current method for automating the State structure after trying this. The current method
 *   labels image filenames with their State names, transitions, and other Attributes. Complete automation
 *   of the State structure probably requires some form of State recognition using neural nets
 *   and is likely much more complex than this attempt.
 */
@Component
public class PopulateStates {

    // SIMPLIFY THIS CODE BY CREATING CLASSES FOR THESE VARIABLES

    private List<Set<StateImageObject>> states = new ArrayList<>(); // CREATE ImageGroup class
    private Map<StateImageObject, Integer> imageOwnedBy = new HashMap<>();
    private Map<Integer, Integer> oldToNewIndex;  // <old index (or -1 if new), new index>
    private Map<Integer, Set<StateImageObject>> newStates;
    private int freeIndex;
    private Set<Integer> statesAlreadyLookedAt;
    private Map<Integer, StateImageObject> toRemove; // <index in states, image>, avoids ConcurrentModificationException

    /**
     * If the image is seen for the first time, put it in a new State.
     * If the image already has a State, check all other images of the same State:
     *    for images that are found, do nothing
     *    for images that are not found, put all of them in a new State
     * @param stateImageObjects the images found in this screenshot
     */
    public void addImagesFoundInScreenshot(List<StateImageObject> stateImageObjects) {
        newStates = new HashMap<>();
        statesAlreadyLookedAt = new HashSet<>();
        freeIndex = states.size(); // the first free index in the states List
        oldToNewIndex = new HashMap<>();  // <old index (or -1 if new), new index>
        toRemove = new HashMap<>();
        for (StateImageObject image : stateImageObjects) {
            if (!imageOwnedBy.containsKey(image)) {
                imageOps(image, -1);
            } else {
                existingImageOps(imageOwnedBy.get(image), stateImageObjects);
            }
        }
        states.addAll(newStates.values());
    }

    private void existingImageOps(int index, List<StateImageObject> stateImageObjects) {
        if (statesAlreadyLookedAt.contains(index)) return; // we've already gone through all these images
        for (StateImageObject img : states.get(index)) { // for each image in this State
            if (!stateImageObjects.contains(img)) { // if the image was not found in this screenshot
                toRemove.put(index, img);
                imageOps(img, index); // create a new State
            }
        }
        toRemove.forEach((k, v) -> states.get(k).remove(v));
        statesAlreadyLookedAt.add(index);
        statesAlreadyLookedAt.add(oldToNewIndex.get(index));
    }

    private void imageOps(StateImageObject image, int oldIndex) {
        System.out.println("imageOps: "+image.getName()+" original index = "+oldIndex);
        newStateOps(oldIndex);
        int newImageStateIndex = oldToNewIndex.get(oldIndex);
        newStates.get(newImageStateIndex).add(image);
        imageOwnedBy.put(image, newImageStateIndex);
    }

    private void newStateOps(int oldIndex) {
        if (!oldToNewIndex.containsKey(oldIndex)) {
            oldToNewIndex.put(oldIndex, freeIndex);
            newStates.put(freeIndex, new HashSet<>());
            freeIndex++;
        }
    }

    public List<Set<StateImageObject>> getStates() {
        return states;
    }

    public void printStates() {
        int i = 0;
        Report.println("Total number of States = "+states.size());
        for (Set<StateImageObject> state : states) {
            Report.print("State "+i, ANSI.BLUE_BACKGROUND);
            Report.print(" |imgs: ");
            state.forEach(image -> Report.print(image.getName()+" ", ANSI.BLUE));
            Report.println();
            i++;
        }
    }

}
