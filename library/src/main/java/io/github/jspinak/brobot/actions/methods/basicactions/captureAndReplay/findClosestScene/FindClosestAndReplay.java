package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.findClosestScene;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.capture.SceneAndObjectsForXML;
import io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.capture.SceneObjectCollectionForXML;
import io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.replay.GetXmlActions;
import io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.replay.ReadXmlScenes;
import io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.replay.ReplayActionsXml;
import io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.replay.ReplayCollection;
import io.github.jspinak.brobot.analysis.Distance;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FindClosestAndReplay {

    private Action action;
    private Distance distance;
    private ReadXmlScenes readXmlScenes;
    private GetXmlActions getXmlActions;
    private ReplayActionsXml replayActionsXml;

    public FindClosestAndReplay(Action action, Distance distance, ReadXmlScenes readXmlScenes,
                                GetXmlActions getXmlActions, ReplayActionsXml replayActionsXml) {
        this.action = action;
        this.distance = distance;
        this.readXmlScenes = readXmlScenes;
        this.getXmlActions = getXmlActions;
        this.replayActionsXml = replayActionsXml;
    }

    /**
     * Find the closest match of the object in the scene. In most cases you want to use the same
     * ActionOptions that was used to find the locations of objects in the recorded scenes.
     * If found, compare with the saved scenes and found objects.
     * Return the timestamp of the scene with the closest match.
     * This will correspond to a series of actions saved in another xml file.
     * @param stateImageObject object to find
     */
    public boolean findClosestAndPerformActions(ActionOptions actionOptions, StateImageObject stateImageObject) {
        // if the start of playback is specified, we don't need to find objects on the screen
        double start = actionOptions.getStartPlayback();
        double duration = actionOptions.getPlaybackDuration();
        if (start >= 0) {
            ReplayCollection replayCollection = getXmlActions.getActionsBetweenTimes(start, start + duration);
            replayActionsXml.replay(replayCollection);
            return true;
        }
        // otherwise, find the best match and compare it to the recorded matches
        Matches matches = action.perform(actionOptions, stateImageObject);
        if (matches.isEmpty()) return false;
        SceneObjectCollectionForXML scenesObjects = readXmlScenes.getSceneAndObjects();
        if (!scenesContainObject(scenesObjects, stateImageObject)) return false;
        SceneAndObjectsForXML closestMatch = getClosestScene(matches, scenesObjects, stateImageObject);
        if (closestMatch == null) return false;
        start = Integer.parseInt(closestMatch.getSceneName());
        ReplayCollection replayCollection = getXmlActions.getActionsBetweenTimes(start, start + duration);
        replayActionsXml.replay(replayCollection);
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

    private SceneAndObjectsForXML getClosestScene(Matches matches, SceneObjectCollectionForXML scenesObjects,
                                                  StateImageObject stateImageObject) {
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
