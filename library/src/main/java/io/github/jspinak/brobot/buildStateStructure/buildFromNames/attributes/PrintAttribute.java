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
        boolean isActive, failedAttribute;
        boolean atLeastOneFailedAttribute = false;
        StringBuilder strB = new StringBuilder();
        for (AttributeTypes.Attribute attribute : AttributeTypes.Attribute.values()) {
            pageResults = image.getAttributes().getScreenshots().get(attribute).getPageResults();
            /* almost every image has an active Attribute on every page.
               printing all images with active Attributes would give a lot of information,
               usually too much information (maybe make this an option for detailed debugging)
             */
            isActive = activeAttributes.contains(attribute);
            failedAttribute = pageResults.containsKey(page) && !pageResults.get(page);
            if (failedAttribute) atLeastOneFailedAttribute = true;
            // there may be failed attributes (such as DOESNT_APPEAR) that are not active
            // in the case of DOESNT_APPEAR, the attribute APPEARS_EXCLUSIVELY is active on another page
            // MULTIPLE_MATCHES will be false in pages where it is not active
            if (isActive || failedAttribute) {
                if (failedAttribute) color = ANSI.RED;
                else color = ANSI.WHITE;
                strB.append(color + attribute.toString() + " " + ANSI.RESET);
            }
        }
        if (!matches.isEmpty() || atLeastOneFailedAttribute) {
            printNames(image);
            Report.print("| Active Attributes: ", ANSI.WHITE);
            Report.println(strB.toString());
            if (matches.size() > 1) Report.println("| "+matches.size() + " matches", ANSI.WHITE);
            else if (matches.size() == 1) Report.println("| 1 match", ANSI.WHITE);
            printDefinedRegion(image);
        }
    }

    private void printNames(StateImageObject image) {
        Report.print(image.getAttributes().getStateName());
        Report.print("."+image.getAttributes().getImageName());
        Report.print(" ");
        Report.println("("+image.getName()+")");
    }

    public void printDefinedRegion(StateImageObject image) {
        printReg(image.getSearchRegion());
    }

    public void printDefinedRegion(Matches matches) {
        printReg(matches.getDefinedRegion());
    }

    private void printReg(Region r) {
        if (!r.defined()) return;
        Report.println("| SearchRegion x." + r.x + " y." + r.y + " w." + r.w + " h." + r.h, ANSI.WHITE);
    }
}
