package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Iterator;
import java.util.Optional;

/**
 * Contains functions to find one or all Matches given an Image and a Region.
 * Converts Sikuli find operations to {@code Optional<Match>} or Matches.
 * Does not mock.
 */
@Component
public class FindPattern {

    private Time time;

    public FindPattern(Time time) {
        this.time = time;
    }

    /**
     * The Sikuli function 'find' finds the best Match for a Sikuli Pattern.
     * @param region the region in which to search
     * @param pattern the pattern to search for
     * @return a Match if found
     */
    public Optional<Match> findBest(Region region, Pattern pattern, Scene scene) {
        if (scene == null || scene.getFilename().isEmpty()) return findLive(region, pattern);
        return findInSceneFromFile(region, pattern, scene);
    }

    Finder getFinder(Scene scene) {
        //String filename = ImagePath.getBundlePath() + "/" + scene.getFilename();
        //File file = new File(filename);
        //String path = file.getAbsolutePath();
        //Report.println(path);
        return new Finder(scene.getAbsolutePath());
    }

    private Optional<Match> findInSceneFromFile(Region region, Pattern pattern, Scene scene) {
        Optional<Match> matchOptional = Optional.empty();
        Finder f = getFinder(scene);
        f.find(pattern);
        if (f.hasNext()) {
            Match nextMatch = f.next();
            if (region.contains(nextMatch)) matchOptional = Optional.of(nextMatch);
        }
        f.destroy();
        return matchOptional;
    }

    private Optional<Match> findLive(Region region, Pattern pattern) {
        try {
            Match match = region.sikuli().find(pattern);
            return Optional.of(match);
        } catch (FindFailed ignored) {}
        return Optional.empty();
    }

    public Matches findAll(Region region, Pattern pattern, StateImage stateImage,
                           ActionOptions actionOptions, Scene scene) {
        if (BrobotSettings.screenshots.isEmpty())
            return findAllLive(region, pattern, stateImage, actionOptions);
        return findAllInSceneFromFile(region, pattern, stateImage, actionOptions, scene);
    }

    private void addMatch(Matches matches, Match match, StateImage stateImage,
                     ActionOptions actionOptions, String sceneName) {
        try {
            MatchObject matchObject = new MatchObject(match, stateImage,
                    time.getDuration(actionOptions.getAction()).getSeconds());
            matchObject.setSceneName(sceneName);
            matches.add(matchObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Matches findAllInSceneFromFile(Region region, Pattern pattern, StateImage stateImage,
                                          ActionOptions actionOptions, Scene scene) {
        Finder f = getFinder(scene);
        f.findAll(pattern);
        Matches matches = new Matches();
        while (f.hasNext()) {
            Match nextMatch = f.next();
            if (region.contains(nextMatch))
                addMatch(matches, nextMatch, stateImage, actionOptions, scene.getName());
        }
        f.destroy();
        return matches;
    }

    public Matches findAllLive(Region region, Pattern pattern, StateImage stateImage,
                           ActionOptions actionOptions) {
        Region adjustedRegion = getRegion(region, pattern);
        Matches matches = new Matches();
        Iterator<Match> newMatches;
        try {
            newMatches = adjustedRegion.sikuli().findAll(pattern);
            while (newMatches.hasNext()) {
                addMatch(matches, newMatches.next(), stateImage, actionOptions, "screenshot0");
            }
        } catch (FindFailed ignored) {}
        return matches;
    }

    /**
     * Brobot Images can have multiple Patterns. A RegionImagePairs object could have a defined region that
     * is smaller than one of the Patterns. This requires the search region to be adjusted to the size of
     * the Pattern in question.
     * @param region the region to adjust
     * @param pattern the pattern to adjust to
     * @return the adjusted region
     */
    private Region getRegion(Region region, Pattern pattern) {
        int rW = region.w;
        int rH = region.h;
        int pW = pattern.getImage().w;
        int pH = pattern.getImage().h;
        if (rW >= pW && rH >= pH) return region;
        adjustRegion(region, pW-rW, pH-rH);
        return region;
    }

    /**
     * Try to fit the region to the size of the pattern, but keep it within the screen.
     * @param region the region to be adjusted
     * @param patternWidthToAdd the width of the pattern to be added to the region
     * @param patternHeightToAdd the height of the pattern to be added to the region
     */
    private void adjustRegion(Region region, int patternWidthToAdd, int patternHeightToAdd) {
        int roomBelow = region.y;
        int roomAbove = new Screen().h - region.y - region.h;
        int roomLeft = region.x;
        int roomRight = new Screen().w - region.x - region.w;
        if (patternHeightToAdd > 0) {
            if (roomBelow < patternHeightToAdd / 2) region.y = 0;
            else if (roomAbove < patternHeightToAdd / 2) region.y -= patternHeightToAdd - roomAbove;
            else region.y -= patternHeightToAdd / 2;
            region.h += patternHeightToAdd;
        }
        if (patternWidthToAdd > 0) {
            if (roomLeft < patternWidthToAdd / 2) region.x = 0;
            else if (roomRight < patternWidthToAdd / 2) region.x -= patternWidthToAdd - roomRight;
            else region.x -= patternWidthToAdd / 2;
            region.w += patternWidthToAdd;
        }
    }

}
