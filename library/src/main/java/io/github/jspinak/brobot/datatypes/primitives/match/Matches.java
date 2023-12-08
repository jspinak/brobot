package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.text.Text;
import io.github.jspinak.brobot.datatypes.state.NullState;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import lombok.Data;
import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Match;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The results object for all actions.
 * <p>
 * - matchObjects contain the MatchObjects found during the operation.
 * - nonoverlappingMatches is a subset of matchObjects where overlapping MatchObjects are removed.
 * - bestMatch is updated every time a MatchObject is added.
 * - activeStates is a list of State names containing objects found during the associated Action.
 * The Find action updates the State Memory with these States.
 * Shared Images are treated the same as non-shared Images in normal operation, since it is assumed that
 * Brobot knows where it is. Shared Images are treated differently when the active State is lost
 * and Images are searched with the StateFinder. With the StateFinder, shared Images are not used
 * to find active States.
 * - duration is the overall time elapsed during the operation.
 * - success is determined differently for different operations and the user can modify the success condition.
 * - definedRegions are saved for Define operations, which define the boundaries of a region or regions.
 * </p>
 */
@Data
public class Matches {

    private String actionDescription = "";
    private List<MatchObject> matchObjects = new ArrayList<>();
    private List<MatchObject> initialMatchObjects = new ArrayList<>(); // the first set of matches in a composite find operation. it may be useful to see these matches in illustrations.
    private List<MatchObject> nonoverlappingMatches = new ArrayList<>();
    private MatchObject bestMatch = null; // query returns an Optional in case there are no matches
    private ActionOptions actionOptions; // the action options used to find the matches
    private List<String> activeStates = new ArrayList<>();
    private Text text = new Text();
    private String selectedText = ""; // the String selected from the Text object as the most accurate representation of the text on-screen
    private Duration duration = Duration.ZERO;
    private LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime endTime;
    private boolean success = false; // for boolean queries (i.e. true for 'find', false for 'vanish' when not empty)
    private List<Region> definedRegions = new ArrayList<>();
    private int maxMatches = -1; // not used when <= 0
    private DanglingSnapshots danglingSnapshots = new DanglingSnapshots();
    private SceneAnalysisCollection sceneAnalysisCollection = new SceneAnalysisCollection();
    private Mat pixelMatches; // for motion detection
    private String outputText = "";

    public Matches() {}

    public Matches(ActionOptions actionOptions) {
        this.actionOptions = actionOptions;
    }

    public void add(MatchObject match) {
        if (maxMatches > 0 && matchObjects.size() >= maxMatches) return;
        matchObjects.add(match);
        addNonoverlappingMatch(match);
        setBestMatch(match);
        addActiveState(match);
    }

    public void addSceneAnalysis(SceneAnalysis sceneAnalysis) {
        sceneAnalysisCollection.add(sceneAnalysis);
    }

    public void addMatchObjects(StateImageObject stateImageObject, List<Match> matchList, double duration) {
        matchList.forEach(match -> {
            try {
                matchObjects.add(new MatchObject(match, stateImageObject, duration));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void addMatchObjects(Matches matches) {
        for (MatchObject match : matches.getMatchObjects()) {
            add(match);
        }
    }

    public void addAllResults(Matches matches) {
        addMatchObjects(matches);
        addNonMatchResults(matches);
    }

    public void addNonMatchResults(Matches matches) {
        text.addAll(matches.text);
        activeStates.addAll(matches.activeStates);
        duration = duration.plus(matches.duration);
        sceneAnalysisCollection.getSceneAnalyses().addAll(matches.sceneAnalysisCollection.getSceneAnalyses()); // = matches.sceneAnalysisCollection;
        danglingSnapshots.addAllSnapshots(matches.getDanglingSnapshots());
        pixelMatches = matches.pixelMatches;
    }

    public void addString(String str) {
        text.add(str);
    }

    public void addDefinedRegion(Region region) {
        definedRegions.add(region);
    }

    public List<Match> getMatches() {
        return matchObjects.stream()
                .map(MatchObject::getMatch)
                .collect(Collectors.toList());
    }

    public void sortMatchObjects() {
        matchObjects.sort(Comparator.comparingDouble(MatchObject::getScore));
    }

    public void sortMatchObjectsDescending() {
        matchObjects.sort(Comparator.comparingDouble(MatchObject::getScore).reversed());
    }

    public List<Region> getMatchRegions() {
        List<Region> regions = new ArrayList<>();
        matchObjects.forEach(mO -> regions.add(new Region(mO.getMatch())));
        return regions;
    }

    public List<Location> getMatchLocations() {
        List<Location> locations = new ArrayList<>();
        matchObjects.forEach(mO -> locations.add(mO.getLocation()));
        return locations;
    }

    public Optional<Location> getBestLocation() {
        if (getBestMatch().isEmpty()) return Optional.empty();
        return Optional.of(getBestMatch().get().getLocation());
    }

    private boolean addNonoverlappingMatch(MatchObject m) {
        Region match = new Region(m.getMatch());
        Region nonoverlap;
        for (MatchObject n : nonoverlappingMatches) {
            nonoverlap = new Region(n.getMatch());
            if (match.overlaps(nonoverlap)) return false;
        }
        nonoverlappingMatches.add(m);
        return true;
    }

    private boolean setBestMatch(MatchObject newMatch) {
        if (bestMatch == null ||
                newMatch.getMatch().getScore() > bestMatch.getMatch().getScore()) {
            bestMatch = newMatch;
            return true;
        }
        return false;
    }

    private boolean addActiveState(MatchObject newMatch) {
        activeStates.add(newMatch.getState());
        return true;
    }

    public Optional<MatchObject> getBestMatch() {
        return Optional.ofNullable(bestMatch);
    }

    public Region getDefinedRegion() {
        if (definedRegions.isEmpty()) return new Region();
        return definedRegions.get(0);
    }

    public int size() {
        return matchObjects.size();
    }

    public boolean isEmpty() {
        return matchObjects.isEmpty();
    }

    public void setTimesActedOn(int timesActedOn) {
        matchObjects.forEach(mO -> mO.getStateObject().setTimesActedOn(timesActedOn));
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
        danglingSnapshots.setDuration((double) duration.getSeconds());
    }

    public void sortByMatchScoreDecending() {
        matchObjects.sort(Comparator.comparingDouble(mO -> mO.getMatch().getScore()));
    }

    public void sortBySizeDecending() {
        matchObjects.sort(Comparator.comparing(MatchObject::getMatchArea).reversed());
    }

    public void saveSnapshots() {
        if (BrobotSettings.saveSnapshots && !BrobotSettings.mock) {
            danglingSnapshots.setSuccess(success);
            danglingSnapshots.save();
        }
    }

    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder()
                .withMatches(this)
                .build();
    }

    public void print() {
        matchObjects.forEach(MatchObject::print);
    }

    /**
     * Returns a new Region with the median x,y,w,h of all Match objects.
     * @return Optional.empty() if no Match objects; otherwise an Optional of the new Region.
     */
    public Optional<Region> getMedian() {
        if (matchObjects.isEmpty()) return Optional.empty();
        int cumX = 0, cumY = 0, cumW = 0, cumH = 0;
        for (MatchObject mO : matchObjects) {
            cumX += mO.getMatch().x;
            cumY += mO.getMatch().y;
            cumW += mO.getMatch().w;
            cumH += mO.getMatch().h;
        }
        int size = matchObjects.size();
        return Optional.of(new Region(cumX/size, cumY/size, cumW/size, cumH/size));
    }

    public Optional<Location> getMedianLocation() {
        Optional<Region> regOpt = getMedian();
        return regOpt.map(region -> new Location(region, Position.Name.MIDDLEMIDDLE));
    }

    public Optional<MatchObject> getClosestTo(Location location) {
        if (matchObjects.isEmpty()) return Optional.empty();
        double closest = getDist(matchObjects.get(0), location);
        MatchObject closestMO = matchObjects.get(0);
        for (MatchObject mO : matchObjects) {
            double dist = getDist(mO, location);
            if (dist <= closest) {
                closest = dist;
                closestMO = mO;
            }
        }
        return Optional.of(closestMO);
    }

    private double getDist(MatchObject matchObject, Location location) {
        int xDist = matchObject.getMatch().x - location.getX();
        int yDist = matchObject.getMatch().y - location.getY();
        return Math.pow(xDist, 2) + Math.pow(yDist, 2);
    }

    /**
     * Returns the matches in this Matches object that are not in the parameter Matches object.
     * @param matches The Matches to subtract.
     * @return a new Matches object with the remaining matches.
     */
    public Matches minus(Matches matches) {
        Matches rest = new Matches();
        matchObjects.forEach(matchObject -> {
            if (!matches.containsMatch(matchObject.getMatch())) rest.add(matchObject);
        });
        return rest;
    }

    public boolean containsMatch(Match match) {
        Match m;
        for (MatchObject matchObject : matchObjects) {
            m = matchObject.getMatch();
            //if (match.equals(matchObject.getMatch())) return true;
            if (match.x == m.x && match.y == m.y && match.w == m.w && match.h == m.h) return true;
        }
        return false;
    }

    /**
     * Matches are confirmed when they contain one of the matches in the
     * parameter insideMatches.
     * @param insideMatches When found inside the region of a MatchObject, keep the MatchObject.
     * @return only MatchObjects that are confirmed.
     */
    public Matches getConfirmedMatches(Matches insideMatches) {
        Matches matches = new Matches();
        //matches.setMaxMatches(this.maxMatches);
        matchObjects.forEach(mO -> {
            if (mO.contains(insideMatches)) {
                matches.add(mO);
                //Report.println("adding inside match: " + mO.getMatch().x + " " + mO.getMatch().y);
            }
        });
        return matches;
    }

    public void removeNonConfirmedMatches(Matches insideMatches) {
        matchObjects.removeIf(mO -> !mO.contains(insideMatches));
    }

    public void keepOnlyConfirmedMatches(Matches insideMatches) {
        Matches confirmedMatches = getConfirmedMatches(insideMatches);
        matchObjects = confirmedMatches.matchObjects;
    }

    public boolean hasImageMatches() {
        for (MatchObject matchObject : matchObjects) {
            if (!Objects.equals(matchObject.getStateObject().getOwnerStateName(), NullState.Name.NULL.toString())) return true;
        }
        return false;
    }

}
