package io.github.jspinak.brobot.actions.methods.basicactions.find.color.classification;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.MatchOps;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.SelectRegions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.contours.Contours;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysisCollection.Analysis.SCORE_DIST_BELOW_THRESHHOLD;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D_TARGETS;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;

@Component
public class GetClassMatches {

    private SelectRegions selectRegions;
    private MatchOps matchOps;

    public GetClassMatches(SelectRegions selectRegions, MatchOps matchOps) {
        this.selectRegions = selectRegions;
        this.matchOps = matchOps;
    }

    /**
     * The SceneAnalyses contain classifications with a set of StateImages. We specify which of
     * these objects we are interested in and find their matches.
     * @param sceneAnalysisCollection the SceneAnalyses to search
     * @param targetImages the StateImages to search for
     * @param actionOptions the action configuration
     * @return
     */
    public Matches getMatches(SceneAnalysisCollection sceneAnalysisCollection, Set<StateImage> targetImages,
                              ActionOptions actionOptions) {
        Matches matches = new Matches();
        List<SceneAnalysis> sceneAnalyses = sceneAnalysisCollection.getSceneAnalyses();
        if (sceneAnalyses.isEmpty()) return matches;
        for (SceneAnalysis sceneAnalysis : sceneAnalyses) {
            Matches matchesForScene = getMatchesForOneScene(sceneAnalysis, targetImages, actionOptions);
            matches.addAllResults(matchesForScene);
        }
        return matches;
    }

    /**
     * Get all matches for all StateImages for one scene.
     * We use HSV as the default color schema.
     * @param sceneAnalysis contains the scene and StateImages with results matrixes
     * @param actionOptions are needed for creating MatchObjects and may contain the search regions
     */
    private Matches getMatchesForOneScene(SceneAnalysis sceneAnalysis, Set<StateImage> targetImages,
                                          ActionOptions actionOptions) {
        Matches matches = new Matches();
        for (StateImage sio : targetImages) {
            List<Region> searchRegions = selectRegions.getRegions(actionOptions, sio);
            Mat resultsInColor = sceneAnalysis.getAnalysis(BGR, BGR_FROM_INDICES_2D_TARGETS);
            Mat scoresMat = sceneAnalysis.getScoresMat(sio);
            Optional<PixelAnalysisCollection> pixelAnalysisCollection = sceneAnalysis.getPixelAnalysisCollection(sio);
            if (pixelAnalysisCollection.isPresent()) {
                Contours contours = new Contours.Builder()
                        .setScoreThresholdDist(pixelAnalysisCollection.get().getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, BGR))
                        .setScores(scoresMat)
                        .setBgrFromClassification2d(resultsInColor)
                        .setMinArea(actionOptions.getMinArea())
                        .setMaxArea(actionOptions.getMaxArea())
                        .setSearchRegions(searchRegions)
                        .build();
                sceneAnalysis.setContours(contours);
                matches.addSceneAnalysis(sceneAnalysis);
                List<Match> matchList = contours.getMatchList();
                matchOps.addMatchListToMatches(matchList, matches);
            }
        }
        return matches;
    }

}
