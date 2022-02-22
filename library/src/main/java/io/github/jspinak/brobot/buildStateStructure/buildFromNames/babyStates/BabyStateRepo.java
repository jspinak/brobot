package io.github.jspinak.brobot.buildStateStructure.buildFromNames.babyStates;

import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.ANSI;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository of BabyStates.
 * Adds BabyStates and retrieves BabyStates based on a substring of the State name.
 */
@Component
@Getter
public class BabyStateRepo {

    private Map<String, BabyState> babyStates = new HashMap<>();

    public void addImage(StateImageObject img) {
        String stateName = img.getAttributes().getStateName();
        if (babyStates.containsKey(stateName)) babyStates.get(stateName).addImage(img);
        else babyStates.put(stateName, new BabyState(stateName, img));
    }

    public void printStatesAndImages() {
        Report.println("Total number of States = " + babyStates.size());
        babyStates.forEach((name, state) -> {
            Report.print(name+" ", ANSI.BLACK, ANSI.BLUE_BACKGROUND);
            state.getImages().forEach(img ->
                    Report.print(img.getAttributes().getImageName() +" ", ANSI.BLUE));
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
}
