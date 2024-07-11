package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycle;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.text.Text;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.reports.Output;
import lombok.Data;
import org.bytedeco.opencv.opencv_core.Mat;

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
    private List<Match> matchList = new ArrayList<>();
    private List<Match> initialMatchList = new ArrayList<>(); // the first set of matches in a composite find operation. it may be useful to see these matches in illustrations.
    private ActionOptions actionOptions; // the action options used to find the matches
    private Set<String> activeStates = new HashSet<>();
    private Text text = new Text();
    private String selectedText = ""; // the String selected from the Text object as the most accurate representation of the text on-screen
    private Duration duration = Duration.ZERO;
    private LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime endTime;
    private boolean success = false; // for boolean queries (i.e. true for 'find', false for 'vanish' when not empty)
    private List<Region> definedRegions = new ArrayList<>();
    private int maxMatches = -1; // not used when <= 0
    private SceneAnalysisCollection sceneAnalysisCollection = new SceneAnalysisCollection();
    private Mat mask; // for motion detection and other pixel-based analysis
    private String outputText = "";
    private ActionLifecycle actionLifecycle;

    public Matches() {}

    public Matches(ActionOptions actionOptions) {
        this.actionOptions = actionOptions;
    }

    public void add(Match... matches) {
        for (Match m : matches) {
            matchList.add(m);
            addActiveState(m);
        }
    }

    public void addSceneAnalysis(SceneAnalysis sceneAnalysis) {
        sceneAnalysisCollection.add(sceneAnalysis);
    }

    public void addMatchObjects(Matches matches) {
        for (Match match : matches.getMatchList()) {
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
        sceneAnalysisCollection.merge(matches.sceneAnalysisCollection);
        mask = matches.mask;
    }

    public void addString(String str) {
        text.add(str);
    }

    public void addDefinedRegion(Region region) {
        definedRegions.add(region);
    }

    public void sortMatchObjects() {
        matchList.sort(Comparator.comparingDouble(Match::getScore));
    }

    public void sortMatchObjectsDescending() {
        matchList.sort(Comparator.comparingDouble(Match::getScore).reversed());
    }

    /**
     * Gets the matches as regions
     * @return a list of regions
     */
    public List<Region> getMatchRegions() {
        List<Region> regions = new ArrayList<>();
        matchList.forEach(mO -> regions.add(mO.getRegion()));
        return regions;
    }

    /**
     * Gets the matches as locations
     * @return a list of locations
     */
    public List<Location> getMatchLocations() {
        List<Location> locations = new ArrayList<>();
        matchList.forEach(mO -> locations.add(mO.getTarget()));
        return locations;
    }

    public Optional<Location> getBestLocation() {
        if (getBestMatch().isEmpty()) return Optional.empty();
        return Optional.of(getBestMatch().get().getTarget());
    }

    public Optional<Match> getBestMatch() {
        return matchList.stream()
                .max(Comparator.comparingDouble(Match::getScore));
    }

    private void addActiveState(Match newMatch) {
        if (newMatch.getStateObjectData() != null)
            activeStates.add(newMatch.getStateObjectData().getOwnerStateName());
    }

    public Region getDefinedRegion() {
        if (definedRegions.isEmpty()) return new Region();
        return definedRegions.get(0);
    }

    public int size() {
        return matchList.size();
    }

    public boolean isEmpty() {
        return matchList.isEmpty();
    }

    public void setTimesActedOn(int timesActedOn) {
        matchList.forEach(m -> m.setTimesActedOn(timesActedOn));
    }

    public void sortByMatchScoreDecending() {
        matchList.sort(Comparator.comparingDouble(Match::getScore));
    }

    public void sortBySizeDecending() {
        matchList.sort(Comparator.comparing(Match::size).reversed());
    }

    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder()
                .withMatches(this)
                .build();
    }

    public void print() {
        matchList.forEach(System.out::println);
    }

    /**
     * Returns a new Region with the median x,y,w,h of all Match objects.
     * @return Optional.empty() if no Match objects; otherwise an Optional of the new Region.
     */
    public Optional<Region> getMedian() {
        if (matchList.isEmpty()) return Optional.empty();
        int cumX = 0, cumY = 0, cumW = 0, cumH = 0;
        for (Match m : matchList) {
            cumX += m.x();
            cumY += m.y();
            cumW += m.w();
            cumH += m.h();
        }
        int size = matchList.size();
        return Optional.of(new Region(cumX/size, cumY/size, cumW/size, cumH/size));
    }

    public Optional<Location> getMedianLocation() {
        Optional<Region> regOpt = getMedian();
        return regOpt.map(region -> new Location(region, Positions.Name.MIDDLEMIDDLE));
    }

    public Optional<Match> getClosestTo(Location location) {
        if (matchList.isEmpty()) return Optional.empty();
        double closest = getDist(matchList.get(0), location);
        Match closestMO = matchList.get(0);
        for (Match mO : matchList) {
            double dist = getDist(mO, location);
            if (dist <= closest) {
                closest = dist;
                closestMO = mO;
            }
        }
        return Optional.of(closestMO);
    }

    private double getDist(Match match, Location location) {
        int xDist = match.x() - location.getX();
        int yDist = match.y() - location.getY();
        return Math.pow(xDist, 2) + Math.pow(yDist, 2);
    }

    /**
     * Returns the matches in this Matches object that are not in the parameter Matches object.
     * @param matches The Matches to subtract.
     * @return a new Matches object with the remaining matches.
     */
    public Matches minus(Matches matches) {
        Matches rest = new Matches();
        matchList.forEach(match -> {
            if (!matches.containsMatch(match)) rest.add(match);
        });
        return rest;
    }

    public boolean containsMatch(Match match) {
        for (Match m : matchList) {
            if (match.equals(m)) return true;
        }
        return false;
    }

    /**
     * Matches are confirmed when they contain one of the matches in the parameter insideMatches.
     * @param insideMatches When found inside the region of a MatchObject, keep the MatchObject.
     * @return only MatchObjects that are confirmed.
     */
    public Matches getConfirmedMatches(Matches insideMatches) {
        Matches matches = new Matches();
        matchList.forEach(m -> {
            if (insideMatches.getMatchList().contains(m)) matches.add(m);
        });
        return matches;
    }

    public void removeNonConfirmedMatches(Matches insideMatches) {
        matchList.removeIf(m -> !insideMatches.getMatchList().contains(m));
    }

    public void keepOnlyConfirmedMatches(Matches insideMatches) {
        Matches confirmedMatches = getConfirmedMatches(insideMatches);
        matchList = confirmedMatches.getMatchList();
    }

    public void addAll(List<Match> newMatches) {
        matchList.addAll(newMatches);
    }

    public Set<String> getUniqueImageIds() {
        return matchList.stream()
                .map(match -> match.getStateObjectData().getStateObjectId())
                .collect(Collectors.toSet());
    }

    public List<Match> getMatchObjectsWithTargetStateObject(String id) {
        return matchList.stream()
                .filter(match -> Objects.equals(match.getStateObjectData().getStateObjectId(), id))
                .collect(Collectors.toList());
    }

    public List<StateImage> getMatchListAsStateImages() {
        List<StateImage> stateImages = new ArrayList<>();
        matchList.forEach(match -> stateImages.add(match.toStateImage()));
        return stateImages;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Matches: ").append("size=").append(size()).append(" ");
        for (Match match : matchList) {
            stringBuilder.append(match).append(" ");
        }
        return stringBuilder.toString();
    }

    public Set<String> getOwnerStateNames() {
        Set<String> uniqueOwnerStateNames = new HashSet<>();
        matchList.forEach(match -> uniqueOwnerStateNames.add(match.getOwnerStateName()));
        return uniqueOwnerStateNames;
    }

    public List<Match> getMatchObjectsWithOwnerState(String ownerStateName) {
        return matchList.stream()
                .filter(match -> ownerStateName.equals(match.getOwnerStateName()))
                .collect(Collectors.toList());
    }

    public String getSuccessSymbol() {
        if (success) return Output.check;
        return Output.fail;
    }

    /**
     * When building the state structure with automation, the match objects represent state images.
     * This prints out the match objects in a format that helps visualize the state structure.
     * @return a string representing a state structure built from match objects
     */
    public String toStringAsTempStates() {
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> uniqueStates = getOwnerStateNames();
        stringBuilder.append("State Structure: #states=").append(uniqueStates.size()).append("\n");
        uniqueStates.forEach(ownerStateName -> {
            List<Match> stateMatchList = getMatchObjectsWithOwnerState(ownerStateName);
            stringBuilder.append(ownerStateName).append(": size=").append(stateMatchList.size()).append(" ");
            stateMatchList.forEach(stringBuilder::append);
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }

}
