package io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes;

import io.github.jspinak.brobot.buildStateStructure.buildFromNames.findImages.ImageGroup;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes.Attribute.*;

/**
 * Processes every active Attribute for an Image on a page after a Find operation.
 * Some Attributes, such as GROUP_DEFINE, require input from other Images and are
 *   processed further after UseAttribute has been run for all Images in a State.
 */
@Component
public class UseAttribute {

    private GetAttribute getAttribute;

    private List<AttributeTypes.Attribute> activeAttributes;

    public UseAttribute(GetAttribute getAttribute) {
        this.getAttribute = getAttribute;
    }

    public List<AttributeTypes.Attribute> processAttributes(StateImageObject image, ImageGroup imageGroup,
                                                            List<Match> matches, int page) {
        activeAttributes = new ArrayList<>();
        appears(image, matches, page);
        appearsExclusively(image, matches, page);
        doesntAppear(image, matches, page);
        multipleMatches(image, matches, page);
        variableLocation(image, matches, page);
        define(image, matches, page);
        groupDefine(image, matches, page, imageGroup);
        region(image, matches, page);
        return activeAttributes;
    }

    private void appears(StateImageObject image, List<Match> matches, int page) {
        if (getAttribute.isPresent(image, page, AttributeTypes.Attribute.APPEARS)) {
            activeAttributes.add(APPEARS);
            setPageResult(image, page, APPEARS, !matches.isEmpty());
        }
    }

    private void appearsExclusively(StateImageObject image, List<Match> matches, int page) {
        List<Integer> pages = image.getAttributes().getScreenshots().
                get(APPEARS_EXCLUSIVELY).getPagesActive();
        // no pages are set for the image to appear exclusively
        if (pages.isEmpty()) return;
        // the image appears exclusively on this page
        if (getAttribute.isPresent(image, page, APPEARS_EXCLUSIVELY)) {
            activeAttributes.add(APPEARS_EXCLUSIVELY);
            setPageResult(image, page, APPEARS_EXCLUSIVELY, !matches.isEmpty());
            return;
        }
        // the image appears exclusively but not on this page
        activeAttributes.add(DOESNT_APPEAR);
        setPageResult(image, page, DOESNT_APPEAR, matches.isEmpty());
    }

    private void doesntAppear(StateImageObject image, List<Match> matches, int page) {
        if (getAttribute.isPresent(image, page, DOESNT_APPEAR)) {
            activeAttributes.add(DOESNT_APPEAR);
            setPageResult(image, page, DOESNT_APPEAR, matches.isEmpty());
        }
    }

    private void multipleMatches(StateImageObject image, List<Match> matches, int page) {
        if (!getAttribute.isPresent(image, page, MULTIPLE_MATCHES)) {
            activeAttributes.add(SINGLE_MATCH);
            setPageResult(image, page, SINGLE_MATCH, matches.size() < 2);
        } else {
            activeAttributes.add(MULTIPLE_MATCHES);
            setPageResult(image, page, MULTIPLE_MATCHES, true);
        }
    }

    private void variableLocation(StateImageObject image, List<Match> matches, int page) {
        boolean isVariable = getAttribute.isPresent(image, page, VARIABLE_LOCATION);
        if (isVariable) {
            activeAttributes.add(VARIABLE_LOCATION);
            setPageResult(image, page, VARIABLE_LOCATION, true);
            return;
        }
        else activeAttributes.add(FIXED_LOCATION);
        Optional<Region> definedRegion = image.getDefinedRegion();
        // if the image hasn't been found yet, or it wasn't found on this page, FIXED is not violated.
        if (definedRegion.isEmpty() || matches.isEmpty()) return;
        // else it can't be in a different location than its previous match
        boolean sameLocation = definedRegion.get().toMatch().equals(getBestMatch(matches));
        setPageResult(image, page, FIXED_LOCATION, sameLocation);
    }

    private void define(StateImageObject image, List<Match> matches, int page) {
        defineRegion(image, matches, page, DEFINE);
    }

    /*
    Only defines once. Aborts if the Image SearchRegion has already been defined.
    Used with DEFINE and REGION Attributes.
     */
    private void defineRegion(StateImageObject image, List<Match> matches, int page,
                              AttributeTypes.Attribute attribute) {
        if (!getAttribute.isPresent(image, page, attribute)) return;
        activeAttributes.add(attribute);
        setPageResult(image, page, attribute, !matches.isEmpty());
        if (matches.isEmpty()) return;
        if (image.getSearchRegion().defined()) return;
        image.setSearchRegion(new Region(getBestMatch(matches)));
    }

    /*
     Defining a Group Region is attempted even if the Group Region has already been defined.
     The Group Region will be replaced with a new Region if this region is larger than the
     previous one.
     This Image's match is sent to the ImageGroup for further processing.
     */
    private void groupDefine(StateImageObject image,
                             List<Match> matches, int page, ImageGroup imageGroup) {
        if (!getAttribute.isPresent(image, page, GROUP_DEFINE)) return;
        activeAttributes.add(GROUP_DEFINE);
        setPageResult(image, page, GROUP_DEFINE, !matches.isEmpty());
        imageGroup.addImage(image);
        if (!matches.isEmpty()) imageGroup.addMatch(getBestMatch(matches));
    }

    /*
     The Region Attribute is meant to create a StateRegion with the match from an image.
     The StateImageObject will not be written to the State Structure; instead,
     the StateRegion will be written in the corresponding State.
     This method finds the match and stores the Region as the SearchRegion of the
     StateImageObject. Later, this SearchRegion will be retrieved in order to write
     the StateRegion.
     */
    private void region(StateImageObject image, List<Match> matches, int page) {
        defineRegion(image, matches, page, REGION);
    }

    // sets the Attribute result
    private void setPageResult(StateImageObject image, int page,
                               AttributeTypes.Attribute attribute, boolean result) {
        image.getAttributes().getScreenshots().get(attribute).setPageResult(page, result);
    }

    private Match getBestMatch(List<Match> matches) {
        matches.sort(Comparator.comparing(Match::getScore).reversed());
        return matches.get(0);
    }
}
