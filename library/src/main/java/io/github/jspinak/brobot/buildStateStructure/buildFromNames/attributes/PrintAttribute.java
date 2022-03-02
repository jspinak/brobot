package io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes;

import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.ANSI;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Prints Attribute stats during the creation of the State structure.
 */
@Component
public class PrintAttribute {

    public void byImageAndPage(StateImageObject image, int page) {
        List<Match> matches = image.getAttributes().getMatches().get(page);
        List<AttributeTypes.Attribute> activeAttributes = image.getAttributes().getActiveAttributes(page);
        String color;
        Map<Integer, Boolean> pageResults;
        boolean isActive;
        boolean atLeastOneFailedAttribute = false;
        StringBuilder activeAttributesStr = new StringBuilder();
        for (Map.Entry<AttributeTypes.Attribute, AttributeData> att :
                image.getAttributes().getScreenshots().entrySet()) {
            pageResults = att.getValue().getPageResults();
            AttributeTypes.Attribute attribute = att.getKey();
            /* almost every image has an active Attribute on every page.
               printing all images with active Attributes would give a lot of information,
               usually too much information (maybe make this an option for detailed debugging)
             */
            isActive = activeAttributes.contains(attribute);
            if (isActive) {
                if (!pageResults.get(page)) {
                    color = ANSI.RED;
                    atLeastOneFailedAttribute = true;
                } else color = ANSI.WHITE;
                activeAttributesStr.append(color + attribute.toString() + " " + ANSI.RESET);
            }
        }
        if (!matches.isEmpty() || atLeastOneFailedAttribute) {
            printNames(image);
            Report.print("| Active Attributes: ", ANSI.WHITE);
            Report.println(activeAttributesStr.toString());
            printMatches(matches);
            printDefinedRegion(image);
        }
    }

    private void printMatches(List<Match> matches) {
        if (matches.isEmpty()) return;
        if (matches.size() > 1) Report.print("| " + matches.size() + " matches", ANSI.WHITE);
        else Report.print("| 1 match", ANSI.WHITE);
        matches.forEach(m -> printRegion(new Region(m)));
        Report.println();
    }

    private void printRegion(Region r) {
        Report.print(" | " + r.x + "." + r.y + "_" + r.w + "." + r.h, ANSI.WHITE);
    }

    private void printNames(StateImageObject image) {
        Report.print(image.getAttributes().getStateName());
        Report.print("."+image.getAttributes().getImageName());
        Report.print(" | ", ANSI.WHITE);
        image.getAttributes().getFilenames().forEach(f -> Report.print(f+" ", ANSI.WHITE));
        Report.println("");
    }

    public void printDefinedRegion(StateImageObject image) {
        printSearchRegion(image.getSearchRegion());
    }

    public void printDefinedRegion(Matches matches) {
        printSearchRegion(matches.getDefinedRegion());
    }

    private void printSearchRegion(Region r) {
        if (!r.defined()) return;
        Report.print("| SearchRegion", ANSI.WHITE);
        printRegion(r);
        Report.println();
    }
}
