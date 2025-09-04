package io.github.jspinak.brobot.test.jackson;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.element.*;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.*;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Builder utility for creating properly initialized test data objects.
 * Ensures all required fields are set to avoid serialization issues.
 */
public class JsonTestDataBuilder {

    /**
     * Creates a fully populated ActionRecord for testing.
     */
    public static ActionRecord createValidActionRecord() {
        ActionRecord record = new ActionRecord();
        
        // Set all required fields
        record.setActionConfig(new PatternFindOptions.Builder().build());
        record.setMatchList(new ArrayList<>());
        record.setText("");
        record.setDuration(100.0);
        record.setTimeStamp(LocalDateTime.now());
        record.setActionSuccess(true);
        record.setResultSuccess(false);
        record.setStateName("TestState");
        record.setStateId(1L);
        
        return record;
    }

    /**
     * Creates an ActionRecord with matches.
     */
    public static ActionRecord createActionRecordWithMatches(int matchCount) {
        ActionRecord record = createValidActionRecord();
        
        for (int i = 0; i < matchCount; i++) {
            Match match = createValidMatch("match-" + i, 0.95 - (i * 0.05));
            record.addMatch(match);
        }
        
        return record;
    }

    /**
     * Creates a valid Match object.
     */
    public static Match createValidMatch(String name, double score) {
        Match match = new Match();
        match.setName(name != null ? name : "test-match-" + UUID.randomUUID());
        match.setRegion(new Region(10, 20, 100, 50));
        match.setScore(score);
        match.setText("");
        match.setTimeStamp(LocalDateTime.now());
        if (match.getStateObjectData() == null) {
            match.setStateObjectData(new StateObjectMetadata());
        }
        match.getStateObjectData().setStateObjectId("0");
        return match;
    }

    /**
     * Creates a valid Pattern object.
     */
    public static Pattern createValidPattern(String name) {
        Pattern pattern = new Pattern();
        pattern.setName(name != null ? name : "test-pattern-" + UUID.randomUUID());
        pattern.setImgpath("images/" + pattern.getName() + ".png");
        pattern.setUrl("http://example.com/" + pattern.getName());
        pattern.setFixed(false);
        pattern.setDynamic(false);
        pattern.setIndex(0);
        pattern.setTargetPosition(new Position(0.5, 0.5));
        pattern.setTargetOffset(new Location(0, 0));
        pattern.setSearchRegions(new SearchRegions());
        pattern.setAnchors(new Anchors());
        pattern.setMatchHistory(new ActionHistory());
        
        // Note: Image field should remain null for serialization tests
        return pattern;
    }

    /**
     * Creates a valid StateImage object.
     */
    public static StateImage createValidStateImage(String name) {
        StateImage stateImage = new StateImage();
        stateImage.setName(name != null ? name : "test-state-image-" + UUID.randomUUID());
        stateImage.setId(System.currentTimeMillis());
        stateImage.setOwnerStateName("TestState");
        stateImage.setOwnerStateId(1L);
        stateImage.setTimesActedOn(0);
        // Initialize collections
        stateImage.setPatterns(new ArrayList<>());
        
        // Add a sample pattern
        stateImage.getPatterns().add(createValidPattern(null));
        
        return stateImage;
    }

    /**
     * Creates a valid StateRegion object.
     */
    public static StateRegion createValidStateRegion(String name) {
        StateRegion stateRegion = new StateRegion();
        stateRegion.setName(name != null ? name : "test-state-region-" + UUID.randomUUID());
        stateRegion.setOwnerStateName("TestState");
        stateRegion.setOwnerStateId(0L); // StateRegion uses Long type
        stateRegion.setTimesActedOn(0);
        
        // Set the search region
        stateRegion.setSearchRegion(new Region(0, 0, 800, 600));
        
        return stateRegion;
    }

    /**
     * Creates a valid StateLocation object.
     */
    public static StateLocation createValidStateLocation(String name) {
        StateLocation stateLocation = new StateLocation();
        stateLocation.setName(name != null ? name : "test-state-location-" + UUID.randomUUID());
        stateLocation.setOwnerStateName("TestState");
        stateLocation.setOwnerStateId(null); // StateLocation uses null for ownerStateId
        stateLocation.setTimesActedOn(0);
        
        // Set the location
        stateLocation.setLocation(new Location(400, 300));
        
        // Initialize position and anchors
        stateLocation.setPosition(new Position(0.5, 0.5));
        stateLocation.setAnchors(new Anchors());
        
        return stateLocation;
    }

    /**
     * Creates a valid StateString object.
     */
    public static StateString createValidStateString(String name) {
        StateString stateString = new StateString();
        stateString.setName(name != null ? name : "test-state-string-" + UUID.randomUUID());
        stateString.setOwnerStateName("TestState");
        stateString.setOwnerStateId(null); // StateString uses null for ownerStateId
        stateString.setTimesActedOn(0);
        
        // Set string value
        stateString.setString("Test String Value");
        
        return stateString;
    }

    /**
     * Creates a valid ObjectCollection with various objects.
     */
    public static ObjectCollection createValidObjectCollection() {
        ObjectCollection collection = new ObjectCollection();
        
        // Initialize all lists
        collection.setStateLocations(new ArrayList<>());
        collection.setStateImages(new ArrayList<>());
        collection.setStateRegions(new ArrayList<>());
        collection.setStateStrings(new ArrayList<>());
        collection.setMatches(new ArrayList<>());
        collection.setScenes(new ArrayList<>());
        
        // Add sample objects
        collection.getStateLocations().add(createValidStateLocation(null));
        collection.getStateImages().add(createValidStateImage(null));
        collection.getStateRegions().add(createValidStateRegion(null));
        collection.getStateStrings().add(createValidStateString(null));
        collection.getMatches().add(createValidActionResult());
        
        return collection;
    }

    /**
     * Creates a valid ActionResult object.
     */
    public static ActionResult createValidActionResult() {
        ActionResult result = new ActionResult();
        
        // Set action configuration
        result.setActionConfig(new PatternFindOptions.Builder().build());
        
        // Initialize match list
        result.setMatchList(new ArrayList<>());
        
        // Set success flags
        result.setSuccess(true);
        
        // Add some matches
        result.getMatchList().add(createValidMatch("result-match-1", 0.98));
        result.getMatchList().add(createValidMatch("result-match-2", 0.85));
        
        return result;
    }

    /**
     * Creates a State object for testing.
     */
    public static State createValidState(String name) {
        State state = new State();
        state.setName(name != null ? name : "test-state-" + UUID.randomUUID());
        
        // Initialize state objects
        state.setStateImages(new HashSet<>());
        state.setStateRegions(new HashSet<>());
        state.setStateLocations(new HashSet<>());
        state.setStateStrings(new HashSet<>());
        
        // Add sample state objects
        state.getStateImages().add(createValidStateImage(null));
        state.getStateRegions().add(createValidStateRegion(null));
        state.getStateLocations().add(createValidStateLocation(null));
        state.getStateStrings().add(createValidStateString(null));
        
        // Set state properties
        state.setBlocking(false);
        state.setPathScore(1);
        
        return state;
    }

    /**
     * Creates a Scene object for testing.
     */
    public static Scene createValidScene(String name) {
        Scene scene = new Scene();
        
        // Create a simple pattern for the scene
        Pattern pattern = new Pattern();
        pattern.setName(name != null ? name : "scene-" + UUID.randomUUID());
        pattern.setImgpath("scenes/" + pattern.getName() + ".png");
        
        // Note: Don't set actual image data for serialization tests
        // The Pattern's image field should remain null
        
        scene.setPattern(pattern);
        
        return scene;
    }

    /**
     * Creates PatternFindOptions with common settings.
     */
    public static PatternFindOptions createPatternFindOptions() {
        return new PatternFindOptions.Builder()
            .setSimilarity(0.8)
            .setSearchDuration(10.0) // replaced deprecated methods
            .build();
    }

    /**
     * Creates a SearchRegions object with sample regions.
     */
    public static SearchRegions createSearchRegions() {
        SearchRegions searchRegions = new SearchRegions();
        searchRegions.setRegions(new ArrayList<>());
        searchRegions.getRegions().add(new Region(0, 0, 800, 600));
        searchRegions.getRegions().add(new Region(100, 100, 600, 400));
        searchRegions.setFixedRegion(new Region(200, 200, 400, 300));
        return searchRegions;
    }

    /**
     * Creates an Anchors object with sample anchors.
     */
    public static Anchors createAnchors() {
        Anchors anchors = new Anchors();
        anchors.setAnchorList(new ArrayList<>());
        
        // Add sample anchors
        Anchor topLeft = new Anchor(Positions.Name.TOPLEFT, new Position(0, 0));
        anchors.getAnchorList().add(topLeft);
        
        Anchor center = new Anchor(Positions.Name.MIDDLEMIDDLE, new Position(0.5, 0.5));
        anchors.getAnchorList().add(center);
        
        return anchors;
    }
}