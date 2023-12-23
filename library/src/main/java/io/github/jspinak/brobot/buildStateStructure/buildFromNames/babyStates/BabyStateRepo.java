package io.github.jspinak.brobot.buildStateStructure.buildFromNames.babyStates;

import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.SetAttributes;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.reports.ANSI;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Repository of BabyStates.
 * Adds BabyStates and retrieves BabyStates based on a substring of the State name.
 */
@Component
@Getter
public class BabyStateRepo {

    private SetAttributes setAttributes;

    private Map<String, BabyState> babyStates = new HashMap<>();

    public BabyStateRepo(SetAttributes setAttributes) {
        this.setAttributes = setAttributes;
    }

    public void addImage(StateImage img) {
        String stateName = img.getAttributes().getStateName();
        if (babyStates.containsKey(stateName)) babyStates.get(stateName).addImage(img);
        else babyStates.put(stateName, new BabyState(stateName, img));
    }

    public void printStatesAndImages() {
        Report.println("Total number of States = " + babyStates.size());
        babyStates.forEach((name, state) -> {
            Report.print(name, ANSI.BLACK, ANSI.BLUE_BACKGROUND);
            state.getImages().forEach(img ->
                    Report.print(" "+img.getAttributes().getImageName(), ANSI.BLUE));
            Report.println();
        });
    }

    /**
     * Retrieves a BabyState given a substring of the State's name.
     * If the name matches exactly or there is only one State match, the State is returned.
     * If there are no matches or more than 1 non-exact matches, an error is printed and "" is returned.
     * @param nameSubstring any substring of the State's name.
     * @return the name of the State, or "" if not found.
     */
    public String getTransitionStateName(String nameSubstring) {
        if (nameSubstring.equals("")) return "";
        if (nameSubstring.equals("close")) return "previous";
        List<String> matchingStates = new ArrayList<>();
        for (String state : babyStates.keySet()) {
            if (state.equals(nameSubstring)) return state;
            if (state.contains(nameSubstring)) matchingStates.add(state);
        }
        if (matchingStates.size() == 1) return matchingStates.get(0);
        if (matchingStates.size() > 1)
            Report.println("error: more than 1 State matching this substring: "+nameSubstring, ANSI.RED);
        return "";
    }

    public Optional<BabyState> getState(String state) {
        if (!babyStates.containsKey(state)) return Optional.empty();
        return Optional.of(babyStates.get(state));
    }

    /**
     * Searches for an image in the repository that has the same base name as the parameter.
     * The base name is the text that comes before numbers at the end of the String.
     * For example, 'image' is the base name for 'image2'.
     * If there is a StateImage with the same base name, add this filename to the
     * StateImage.
     * @param newImg the new StateImage
     * @return true if the image was added to an existing StateImage
     */
    public Optional<StateImage> getBaseImage(StateImage newImg) {
        if (!babyStates.containsKey(newImg.getAttributes().getStateName())) return Optional.empty();
        BabyState babyState = babyStates.get(newImg.getAttributes().getStateName());
        for (StateImage img : babyState.getImages()) {
            String existingName = img.getAttributes().getImageName();
            String newName = newImg.getAttributes().getImageName();
            if (existingName.equals(newName)) {
                return Optional.of(img);
            }
        }
        return Optional.empty();
    }

}
