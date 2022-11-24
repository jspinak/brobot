package io.github.jspinak.brobot.actions.methods.basicactions.mouse;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.basicactions.find.SelectRegions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetSceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.illustratedHistory.IllustrateScreenshot;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Match;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Moves the mouse to one or more locations.
 * There can be multiple points per ObjectCollection if Find.EACH or Find.ALL is used.
 * There may be multiple ObjectCollections.
 * Points are visited in the following order:
 *   Within an ObjectCollection, as recorded by the Find operation (Images, Matches, Regions, Locations)
 *   In the order the ObjectCollection appears
 */
@Component
public class MoveMouse implements ActionInterface {

    private final Find find;
    private final MoveMouseWrapper moveMouseWrapper;
    private final Wait wait;
    private IllustrateScreenshot illustrateScreenshot;
    private SelectRegions selectRegions;
    private GetSceneAnalysisCollection getSceneAnalysisCollection;

    public MoveMouse(Find find, MoveMouseWrapper moveMouseWrapper, Wait wait,
                     IllustrateScreenshot illustrateScreenshot, SelectRegions selectRegions,
                     GetSceneAnalysisCollection getSceneAnalysisCollection) {
        this.find = find;
        this.moveMouseWrapper = moveMouseWrapper;
        this.wait = wait;
        this.illustrateScreenshot = illustrateScreenshot;
        this.selectRegions = selectRegions;
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = new Matches();
        List<ObjectCollection> collections = Arrays.asList(objectCollections);
        SceneAnalysisCollection sceneAnalysisCollection = getSceneAnalysisCollection.
                get(Arrays.asList(objectCollections), actionOptions);
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        Report.println("scene analysis collection size: " + sceneAnalysisCollection.getSceneAnalyses().size());
        for (ObjectCollection objColl : collections) {
            Matches newMatches = find.perform(actionOptions, objColl);
            newMatches.getMatchLocations().forEach(moveMouseWrapper::move);
            Report.print("finished move. ");
            matches.addAllResults(newMatches);
            if (newMatches.isSuccess()) matches.setSuccess(true);
            if (collections.indexOf(objColl) < collections.size() - 1)
                wait.wait(actionOptions.getPauseBetweenIndividualActions());
        }
        return matches;
    }



}
