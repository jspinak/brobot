package io.github.jspinak.brobot.actions.methods.basicactions.capture.findClosestScene;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.capture.SceneAndObjectsForXML;
import io.github.jspinak.brobot.actions.methods.basicactions.capture.SceneObjectCollectionForXML;
import io.github.jspinak.brobot.actions.methods.basicactions.capture.replay.ReadXmlActionsAndReplay;
import io.github.jspinak.brobot.actions.methods.basicactions.capture.replay.ReadXmlScenes;
import io.github.jspinak.brobot.actions.methods.basicactions.capture.replay.ReplayCollection;
import io.github.jspinak.brobot.analysis.Distance;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FindClosest {

    private Action action;
    private Distance distance;
    private ReadXmlScenes readXmlScenes;
    private ReadXmlActionsAndReplay readXmlActionsAndReplay;

    public FindClosest(Action action, Distance distance, ReadXmlScenes readXmlScenes,
                       ReadXmlActionsAndReplay readXmlActionsAndReplay) {
        this.action = action;
        this.distance = distance;
        this.readXmlScenes = readXmlScenes;
        this.readXmlActionsAndReplay = readXmlActionsAndReplay;
    }

    /**
     * Find the closest match of the object in the scene.
     * If found, compare with the saved scenes and found objects.
     * Return the timestamp of the scene with the closest match.
     * This will correspond to a series of actions saved in another xml file.
     * @param stateImageObject object to find
     */
    public boolean findClosestAndPerformActions(double searchTime, StateImageObject stateImageObject) {
        ActionOptions findClosest = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setMaxWait(searchTime)
                .build();
        Matches matches = action.perform(findClosest, stateImageObject);
        if (matches.isEmpty()) return false;
        SceneObjectCollectionForXML scenesObjects = readXmlScenes.getSceneAndObjects();
        if (!scenesContainObject(scenesObjects, stateImageObject)) return false;
        SceneAndObjectsForXML closestMatch = getClosestScene(matches, scenesObjects, stateImageObject);
        if (closestMatch == null) return false;
        int start = Integer.parseInt(closestMatch.getSceneName()) * 1000;
        ReplayCollection replayCollection = readXmlActionsAndReplay.getActionsBetweenTimes(start, start + 5000);
        Report.println(replayCollection.getReplayObjects().size() + " actions found from " + start + " to " + (start + 5));
        readXmlActionsAndReplay.replay(replayCollection);
        return true;
    }

    private boolean scenesContainObject(SceneObjectCollectionForXML scenesObjects, StateImageObject stateImageObject) {
        return scenesObjects.getScenes().stream()
                .anyMatch(scene -> scene.getObjectsNames().stream()
                        .anyMatch(name -> name.equals(stateImageObject.getName())));
    }

    private int getMatchingStateImageObjectIndex(SceneAndObjectsForXML scene, StateImageObject stateImageObject) {
        return scene.getObjectsNames().indexOf(stateImageObject.getName());
    }

    private SceneAndObjectsForXML getClosestScene(Matches matches, SceneObjectCollectionForXML scenesObjects, StateImageObject stateImageObject) {
        if (matches.getBestLocation().isEmpty()) return null; // this should never happen, we've already checked if matches are empty
        Location bestLocation = matches.getBestLocation().get();
        Report.println("Best match: " + bestLocation.getX() + ", " + bestLocation.getY());
        SceneAndObjectsForXML firstScene = scenesObjects.getScenes().get(0);
        int matchingStateImageObjectIndex = getMatchingStateImageObjectIndex(firstScene, stateImageObject);
        double closestScene = 1000000;
        SceneAndObjectsForXML closestSceneObject = null;
        for (SceneAndObjectsForXML scObj : scenesObjects.getScenes()) {
            List<Location> locations = scObj.getObjectsLocations();
            if (locations.isEmpty()) continue;
            double dist = distance.getDistance(bestLocation, locations.get(matchingStateImageObjectIndex));
            if (dist < closestScene) {
                closestScene = dist;
                closestSceneObject = scObj;
            }
        }
        Report.println("Closest scene: " + closestSceneObject.getSceneName());
        return closestSceneObject;
    }
}
